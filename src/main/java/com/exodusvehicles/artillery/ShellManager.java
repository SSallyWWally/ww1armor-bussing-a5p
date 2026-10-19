package com.exodusvehicles.artillery;

import com.exodusvehicles.ArmoredCarMod;
import com.exodusvehicles.ModConfig;
import com.exodusvehicles.entity.ArmoredCarEntity;
import com.exodusvehicles.entity.MorserEntity;
import com.exodusvehicles.network.ShellFiredPayload;
import com.exodusvehicles.network.ShellImpactPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Artillery shells in flight, simulated on the server only. A shell is a point under gravity (no entity, no per-tick
 * packets): one block ray-cast and one small entity query per tick while it is over loaded ground. Clients get one
 * packet when it is fired (to fly a visual copy for the trail / shell camera) and one when it bursts.
 * The landing area is kept loaded by a short-lived chunk ticket. Also schedules sounds that arrive late (sound is slow).
 */
public final class ShellManager {
    /** Gravity on shells, blocks / tick^2. Client copies use the same value. */
    public static final double GRAVITY = 0.08;
    private static final TicketType<ChunkPos> TICKET =
            TicketType.create("exodus_vehicles_shell", Comparator.comparingLong(ChunkPos::toLong), 400);
    /** Speed of sound, blocks per tick (343 m/s). */
    public static final double SOUND_SPEED = 17.15;
    /** Players this close to the gun or the landing point get the fired packet (trail); impact packet radius. */
    private static final double SHARE_RADIUS = 320.0, IMPACT_RADIUS = 160.0;

    private static final List<Shell> SHELLS = new ArrayList<>();
    private static final List<Delayed> DELAYED = new ArrayList<>();
    private static int nextId = 1;

    private ShellManager() {}

    private static final class Shell {
        final int id;
        final ServerLevel level;
        Vec3 pos, vel;
        int age;
        final int whistleAt;
        final Vec3 predicted;
        final UUID owner;
        final Entity gun;

        Shell(int id, ServerLevel level, Vec3 pos, Vec3 vel, UUID owner, Vec3 predicted, int flightTicks, Entity gun) {
            this.id = id;
            this.level = level;
            this.pos = pos;
            this.vel = vel;
            this.owner = owner;
            this.predicted = predicted;
            this.whistleAt = Math.max(1, flightTicks - 50);
            this.gun = gun;
        }
    }

    private record Delayed(ServerLevel level, UUID player, Holder<SoundEvent> sound, Vec3 from, float volume, float pitch,
                           long seed, long at, boolean nearListener) {}

    /** One tick of flight; the half-step of gravity makes it the exact parabola the gun's aiming assumes. */
    public static Vec3 step(Vec3 pos, Vec3 vel) {
        return pos.add(vel.x, vel.y - GRAVITY * 0.5, vel.z);
    }

    /** Fires a shell from 'gun'; groundY = expected landing height (whistle timing, chunk ticket). */
    public static void fire(ServerLevel level, Vec3 pos, Vec3 vel, UUID owner, double groundY, Entity gun) {
        Vec3 p = pos, v = vel;
        int t = 0;
        while (t < 1200 && !(v.y < 0 && p.y <= groundY)) {
            p = step(p, v);
            v = v.add(0, -GRAVITY, 0);
            t++;
        }
        ChunkPos cp = new ChunkPos(BlockPos.containing(p));
        level.getChunkSource().addRegionTicket(TICKET, cp, 2, cp);
        int id = nextId++;
        SHELLS.add(new Shell(id, level, pos, vel, owner, p, t, gun));

        ShellFiredPayload msg = new ShellFiredPayload(id, gun.getId(), pos.x, pos.y, pos.z, vel.x, vel.y, vel.z);
        for (ServerPlayer pl : level.players()) {
            if (pl.distanceToSqr(pos) < SHARE_RADIUS * SHARE_RADIUS || pl.distanceToSqr(p) < SHARE_RADIUS * SHARE_RADIUS
                    || pl.getUUID().equals(owner)) {
                ServerPlayNetworking.send(pl, msg);
            }
        }
    }

    /**
     * Plays a sound to one player the way it would arrive from 'from': delayed by distance, and (when nearListener)
     * placed a few blocks from them in the right direction so it carries much further than vanilla allows.
     */
    public static void soundTo(ServerPlayer player, SoundEvent sound, Vec3 from, float volume, float pitch, boolean nearListener) {
        ServerLevel level = player.serverLevel();
        double d = player.getEyePosition().distanceTo(from);
        long at = level.getGameTime() + (long) (d / SOUND_SPEED);
        DELAYED.add(new Delayed(level, player.getUUID(), BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound), from, volume, pitch,
                level.random.nextLong(), at, nearListener));
    }

    public static void tick(ServerLevel level) {
        long now = level.getGameTime();
        for (Iterator<Delayed> it = DELAYED.iterator(); it.hasNext(); ) {
            Delayed d = it.next();
            if (d.level != level || d.at > now) {
                continue;
            }
            it.remove();
            if (!(level.getPlayerByUUID(d.player) instanceof ServerPlayer p)) {
                continue;
            }
            Vec3 at = d.from;
            if (d.nearListener) {
                Vec3 eye = p.getEyePosition();
                Vec3 dir = d.from.subtract(eye);
                at = dir.lengthSqr() < 1.0E-4 ? eye : eye.add(dir.normalize().scale(Math.min(4.0, dir.length())));
            }
            p.connection.send(new ClientboundSoundPacket(d.sound, SoundSource.PLAYERS, at.x, at.y, at.z, d.volume, d.pitch, d.seed));
        }

        for (Iterator<Shell> it = SHELLS.iterator(); it.hasNext(); ) {
            Shell s = it.next();
            if (s.level != level) {
                continue;
            }
            s.age++;
            if (s.age == s.whistleAt) {
                Vec3 w = s.predicted.add(0, 10, 0);
                for (ServerPlayer p : level.players()) {
                    if (p.distanceToSqr(s.predicted) < 90 * 90) {
                        p.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ArmoredCarMod.SHELL_WHISTLE),
                                SoundSource.PLAYERS, w.x, w.y, w.z, 1.0F, 0.95F + level.random.nextFloat() * 0.1F, level.random.nextLong()));
                    }
                }
            }
            Vec3 next = step(s.pos, s.vel);
            if (level.hasChunkAt(BlockPos.containing(next)) && level.hasChunkAt(BlockPos.containing(s.pos))) {
                Vec3 hit = null;
                BlockHitResult bh = level.clip(new ClipContext(s.pos, next, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY,
                        CollisionContext.empty()));
                if (bh.getType() != HitResult.Type.MISS) {
                    hit = bh.getLocation();
                }
                Vec3 end = hit != null ? hit : next;
                for (Entity e : level.getEntities((Entity) null, new AABB(s.pos, end).inflate(1.0),
                        e -> e.isPickable() && !e.isSpectator() && e != s.gun && !(s.gun != null && s.gun.hasPassenger(e)))) {
                    Optional<Vec3> eh = e.getBoundingBox().inflate(0.3).clip(s.pos, end);
                    if (eh.isPresent() && (hit == null || eh.get().distanceToSqr(s.pos) < hit.distanceToSqr(s.pos))) {
                        hit = eh.get();
                    }
                }
                if (hit != null) {
                    impact(level, hit, s.owner, s.id);
                    it.remove();
                    continue;
                }
            }
            s.pos = next;
            s.vel = s.vel.add(0, -GRAVITY, 0);
            if (s.age > 1200 || s.pos.y < level.getMinBuildHeight() - 16) {
                it.remove();
            }
        }
    }

    /** No entity damage or knockback from the (optional) block-breaking blast: the shell's own damage handles that. */
    private static final ExplosionDamageCalculator BLOCKS_ONLY = new ExplosionDamageCalculator() {
        @Override
        public boolean shouldDamageEntity(Explosion explosion, Entity entity) {
            return false;
        }

        @Override
        public float getKnockbackMultiplier(Entity entity) {
            return 0.0F;
        }
    };

    private static void impact(ServerLevel level, Vec3 at, UUID ownerId, int shellId) {
        ModConfig cfg = ModConfig.get();
        ServerPlayer owner = ownerId == null ? null : (ServerPlayer) level.getPlayerByUUID(ownerId);
        Vec3 c = at.add(0, 0.4, 0);
        RandomSource rnd = level.random;
        DamageSource src = level.damageSources().explosion(null, owner);

        // ---- damage and knockback, falling off with distance and blocked by cover
        float radius = cfg.shellBlastRadius;
        boolean hit = false, kill = false;
        for (Entity e : level.getEntities((Entity) null, new AABB(c, c).inflate(radius + 2.0), e -> !e.isSpectator())) {
            if (e instanceof ArmoredCarEntity || e instanceof MorserEntity) {
                continue;      // vehicles below
            }
            Vec3 mid = e.getBoundingBox().getCenter();
            double d = mid.distanceTo(c);
            if (d > radius) {
                continue;
            }
            double f = 1.0 - d / radius;
            double seen = Explosion.getSeenPercent(c, e);
            float dmg = (float) (cfg.shellDamage * f * (0.25 + 0.75 * seen));
            if (dmg > 0.5F && e.hurt(src, dmg) && e instanceof ServerPlayer && e != owner) {
                hit = true;
                kill |= ((ServerPlayer) e).isDeadOrDying();
            }
            double kb = cfg.shellKnockback * f * (0.3 + 0.7 * seen);
            if (e instanceof LivingEntity living) {
                kb *= 1.0 - living.getAttributeValue(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE);
            }
            if (kb > 0.01) {
                Vec3 dir = mid.subtract(c);
                dir = dir.lengthSqr() < 1.0E-4 ? new Vec3(0, 1, 0) : dir.normalize();
                e.setDeltaMovement(e.getDeltaMovement().add(dir.x * kb, Math.max(0.25, dir.y) * kb * 0.8, dir.z * kb));
                e.hurtMarked = true;
            }
            // shell shock for anyone caught close: staggered and dazed for a moment
            if (e instanceof ServerPlayer p && !p.isCreative() && d < cfg.shellShockRadius) {
                int dur = (int) (40 + 80 * (1.0 - d / cfg.shellShockRadius));
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, dur, d < cfg.shellShockRadius * 0.5 ? 1 : 0, false, false, false));
                if (d < cfg.shellShockRadius * 0.6) {
                    p.addEffect(new MobEffectInstance(MobEffects.CONFUSION, dur + 40, 0, false, false, false));
                }
            }
        }
        // vehicles: a heavy shell wrecks an armoured car on a direct hit and badly hurts it close by
        double vr = cfg.shellVehicleRadius;
        for (Entity e : level.getEntities((Entity) null, new AABB(c, c).inflate(vr + 4.0), e -> e instanceof ArmoredCarEntity || e instanceof MorserEntity)) {
            double d = Math.sqrt(e.getBoundingBox().distanceToSqr(c));
            if (d < vr && e.hurt(src, (float) (cfg.shellVehicleDamage * (1.0 - d / vr)))) {
                hit = true;
                kill |= e.isRemoved() || (e instanceof ArmoredCarEntity car && car.isWrecked());
            }
        }

        if (hit && owner != null) {
            ServerPlayNetworking.send(owner, new com.exodusvehicles.network.HitMarkerPayload(kill));
        }

        // ---- terrain (off by default); never inside spawn protection or where the firing player may not build
        BlockPos bp = BlockPos.containing(at);
        if (cfg.shellBreaksBlocks && owner != null && level.mayInteract(owner, bp)
                && !level.getServer().isUnderSpawnProtection(level, bp, owner)) {
            level.explode(owner, src, BLOCKS_ONLY, c.x, c.y, c.z, cfg.shellBlockPower, false, Level.ExplosionInteraction.TNT,
                    ParticleTypes.EXPLOSION, ParticleTypes.EXPLOSION_EMITTER,
                    BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ArmoredCarMod.SHELL_IMPACT_CLOSE));
        } else {
            // close blast: plays at volume 4 so it carries ~96 blocks
            level.playSound(null, c.x, c.y, c.z, ArmoredCarMod.SHELL_IMPACT_CLOSE, SoundSource.PLAYERS, 4.0F,
                    0.9F + rnd.nextFloat() * 0.15F);
        }

        // ---- effects: players within IMPACT_RADIUS draw the full blast themselves from the impact packet (no particle
        // traffic); further out they only see the smoke column
        ShellImpactPayload burst = new ShellImpactPayload(shellId, c.x, c.y, c.z, radius);
        for (ServerPlayer p : level.players()) {
            double d = Math.sqrt(p.distanceToSqr(c));
            if (d >= IMPACT_RADIUS && d < 384) {
                level.sendParticles(p, ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, true, c.x, c.y + 1.0, c.z, 6, 1.5, 1.0, 1.5, 0.03);
                level.sendParticles(p, ParticleTypes.EXPLOSION_EMITTER, true, c.x, c.y + 0.5, c.z, 1, 0.0, 0.0, 0.0, 0.0);
            }
            if (d < IMPACT_RADIUS || p == owner) {
                ServerPlayNetworking.send(p, burst);
            }
            if (d > 56 && d < 1000) {
                float vol = (float) Math.max(0.2, 1.0 - (d - 56) / 944.0);
                soundTo(p, ArmoredCarMod.SHELL_IMPACT_FAR, c, vol, 0.95F + rnd.nextFloat() * 0.1F, true);
            }
        }
    }

    /** Uniform random point in a disc of the given radius. */
    public static double[] disc(RandomSource rnd, double radius) {
        double r = radius * Math.sqrt(rnd.nextDouble());
        double a = rnd.nextDouble() * Mth.TWO_PI;
        return new double[]{Math.cos(a) * r, Math.sin(a) * r};
    }
}
