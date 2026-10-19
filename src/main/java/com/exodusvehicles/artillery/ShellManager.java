package com.exodusvehicles.artillery;

import com.exodusvehicles.ArmoredCarMod;
import com.exodusvehicles.ModConfig;
import com.exodusvehicles.entity.ArmoredCarEntity;
import com.exodusvehicles.entity.HullSegmentEntity;
import com.exodusvehicles.entity.MorserEntity;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
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
 * Heavy shells in flight. They aren't entities (so they keep flying over unloaded ground); each one is a point moving
 * under gravity, checked against blocks and entities wherever the world is loaded. The landing area is kept loaded by a
 * short-lived chunk ticket from the moment of firing. Also schedules sounds that arrive late because sound is slow.
 */
public final class ShellManager {
    public static final double GRAVITY = 0.05;            // blocks / tick^2
    private static final TicketType<ChunkPos> TICKET =
            TicketType.create("exodus_vehicles_shell", Comparator.comparingLong(ChunkPos::toLong), 600);
    /** Speed of sound, blocks per tick (343 m/s). */
    public static final double SOUND_SPEED = 17.15;

    private static final List<Shell> SHELLS = new ArrayList<>();
    private static final List<Delayed> DELAYED = new ArrayList<>();

    private ShellManager() {}

    private static final class Shell {
        final ServerLevel level;
        Vec3 pos, vel;
        int age;
        final int whistleAt;
        final Vec3 predicted;
        final UUID owner;
        final Entity gun;

        Shell(ServerLevel level, Vec3 pos, Vec3 vel, UUID owner, Vec3 predicted, int flightTicks, Entity gun) {
            this.gun = gun;
            this.level = level;
            this.pos = pos;
            this.vel = vel;
            this.owner = owner;
            this.predicted = predicted;
            this.whistleAt = Math.max(1, flightTicks - 60);
        }
    }

    private record Delayed(ServerLevel level, UUID player, Holder<SoundEvent> sound, Vec3 from, float volume, float pitch,
                           long seed, long at, boolean nearListener) {}

    /** Fires a shell; groundY is the height it is expected to come down at (for the whistle and chunk ticket). */
    public static void fire(ServerLevel level, Vec3 pos, Vec3 vel, UUID owner, double groundY, Entity gun) {
        Vec3 p = pos, v = vel;
        int t = 0;
        while (t < 2000 && !(v.y < 0 && p.y <= groundY)) {
            p = step(p, v);
            v = v.add(0, -GRAVITY, 0);
            t++;
        }
        ChunkPos cp = new ChunkPos(BlockPos.containing(p));
        level.getChunkSource().addRegionTicket(TICKET, cp, 2, cp);
        SHELLS.add(new Shell(level, pos, vel, owner, p, t, gun));
    }

    /** One tick of flight; the half-step of gravity makes it the exact parabola the gun's tables assume. */
    private static Vec3 step(Vec3 pos, Vec3 vel) {
        return pos.add(vel.x, vel.y - GRAVITY * 0.5, vel.z);
    }

    /**
     * Plays a sound to one player the way it would arrive from 'from': delayed by distance, and (when nearListener)
     * placed a few blocks from them in the right direction so it can be heard from much further than vanilla allows.
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
                Vec3 w = s.predicted.add(0, 12, 0);
                for (ServerPlayer p : level.players()) {
                    if (p.distanceToSqr(s.predicted) < 110 * 110) {
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
                    impact(level, hit, s.owner);
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

    private static void impact(ServerLevel level, Vec3 at, UUID ownerId) {
        ModConfig cfg = ModConfig.get();
        Entity owner = ownerId == null ? null : level.getPlayerByUUID(ownerId);
        // a little above the surface, so the blast isn't swallowed by the block it struck
        Vec3 c = at.add(0, 0.5, 0);
        level.explode(null, level.damageSources().explosion(null, owner), null, c.x, c.y, c.z,
                cfg.shellPower, false, cfg.shellBreaksBlocks ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE);

        // vehicles: a heavy shell wrecks an armoured car on a direct hit and badly hurts it close by
        double r = cfg.shellVehicleRadius;
        for (Entity e : level.getEntities((Entity) null, new AABB(c, c).inflate(r + 4.0), e -> e instanceof ArmoredCarEntity || e instanceof MorserEntity)) {
            double d = Math.sqrt(e.getBoundingBox().distanceToSqr(c));
            if (d < r) {
                float dmg = (float) (cfg.shellVehicleDamage * (1.0 - d / r));
                e.hurt(level.damageSources().explosion(null, owner), dmg);
            }
        }

        RandomSource rnd = level.random;
        BlockState ground = level.getBlockState(BlockPos.containing(at.x, at.y - 0.5, at.z));
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, c.x, c.y, c.z, 2, 0.8, 0.4, 0.8, 0.0);
        if (!ground.isAir()) {
            BlockParticleOption chunk = new BlockParticleOption(ParticleTypes.BLOCK, ground);
            for (int i = 0; i < 90; i++) {
                level.sendParticles(chunk, c.x, c.y, c.z, 0, (rnd.nextDouble() - 0.5) * 0.9, 0.5 + rnd.nextDouble() * 0.9,
                        (rnd.nextDouble() - 0.5) * 0.9, 1.0);
            }
        }
        for (ServerPlayer p : level.players()) {
            double d = Math.sqrt(p.distanceToSqr(c));
            if (d < 512) {
                // the dirt column and smoke can be seen from far away
                level.sendParticles(p, ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, true, c.x, c.y + 1, c.z, 14, 1.6, 1.2, 1.6, 0.03);
                level.sendParticles(p, ParticleTypes.LARGE_SMOKE, true, c.x, c.y + 1, c.z, 40, 2.0, 1.5, 2.0, 0.08);
            }
            if (d > 48 && d < 900) {
                // the blast rolling in from a distance
                float vol = (float) Math.max(0.15, 1.0 - d / 900.0);
                soundTo(p, ArmoredCarMod.MORSER_FAR, c, vol, 0.7F + rnd.nextFloat() * 0.1F, true);
            }
        }
    }
}
