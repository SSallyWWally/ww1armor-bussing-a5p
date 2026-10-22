package com.exodusvehicles.entity;

import com.exodusvehicles.ArmoredCarMod;
import com.exodusvehicles.ModConfig;
import com.exodusvehicles.artillery.ShellManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * 7.7 cm FK 96 n.A. field gun. Same handling as the Mörser (deploy, get on, tow behind a lorry) but it is laid
 * directly: the barrel follows the gunner's view (left/right traverse, up/down elevation) and the round flies a
 * shallow arc at a fixed muzzle speed. Quick to reload. Everything that matters is decided on the server.
 * <p>
 * The synced aim settings are reused: min/max range = depression/elevation limits, dispersion min = angular spread,
 * dispersion max = muzzle speed.
 */
public class FieldGunEntity extends MorserEntity {
    public static final double TRUNNION_UP = 16.0 / 16.0, TRUNNION_FWD = 1.0 / 16.0, BARREL = 35.0 / 16.0;
    /** Bore axis sits a little above the trunnions. */
    public static final double BORE_UP = 0.6 / 16.0;

    /** Where it was deployed: it can be shuffled about this far from here before it has to be packed up again. */
    private Vec3 anchor;
    private float anchorYaw;
    private int limitMsg;

    public FieldGunEntity(EntityType<? extends FieldGunEntity> type, Level level) {
        super(type, level);
        this.elevVis = this.elevVisO = 0.0F;
        // the shared synced fields start with the Mörser's defaults (a range of 95 m read as 95 degrees of
        // elevation pointed the barrel straight up after a rejoin); give them the field gun's own values at once
        refreshSettings(ModConfig.get());
        setAim(0.0F);
    }

    @Override public float getMaxHealth() { return ModConfig.get().fieldGunMaxHealth; }
    @Override public double trunnionUp() { return TRUNNION_UP; }
    @Override public double trunnionFwd() { return TRUNNION_FWD; }
    @Override public double barrelLength() { return BARREL; }
    @Override public double gunnerRight() { return -8.0 / 16.0; }
    @Override public double gunnerFwd() { return -20.0 / 16.0; }
    @Override public double trailLength() { return 2.95; }
    @Override public double wheelRadius() { return 10.0 / 16.0; }
    @Override public double[] breechOffset() { return new double[]{10.0 / 16.0, 1.0 / 16.0, 2.2 / 16.0}; }
    @Override public boolean directFire() { return true; }
    @Override protected Item gunItem() { return ArmoredCarMod.FIELD_GUN_ITEM; }
    @Override protected Item shellItem() { return ArmoredCarMod.FIELD_SHELL_ITEM; }
    @Override protected int reloadTicksCfg() { return ModConfig.get().fieldGunReloadTicks; }
    @Override public int deployTicks() { return Math.max(1, ModConfig.get().fieldGunDeployTicks); }
    @Override protected float travelElevation() { return 0.0F; }

    /** Aim value is the barrel elevation itself. */
    @Override
    public float shownElevation(float aim) {
        ModConfig c = ModConfig.get();
        return Mth.clamp(aim, -Math.abs(c.fieldGunMaxDepression), c.fieldGunMaxElevation);
    }

    public float minElevation() { return minRange(); }
    public float maxElevation() { return maxRange(); }
    public float spread() { return dispMinRaw(); }
    public double muzzleSpeed() { return dispMaxRaw(); }

    /** Elevation laid for a view pitch (looking up = elevating). */
    public float elevationForPitch(float pitch) {
        return Mth.clamp(-pitch, minElevation(), maxElevation());
    }

    @Override
    protected void refreshSettings(ModConfig cfg) {
        setAimSettings(-Math.abs(cfg.fieldGunMaxDepression), cfg.fieldGunMaxElevation, cfg.fieldGunTraverse,
                Math.max(0.0F, cfg.fieldGunSpread), (float) Math.max(1.0, cfg.fieldGunMuzzleSpeed), 0.0F);
    }

    @Override
    protected void updateAim(Player op) {
        setAim(elevationForPitch(op.getXRot()));
    }

    // ------------------------------------------------------------------ shuffling it about while deployed

    @Override
    protected void onDeployChanged(boolean deployed) {
        this.anchor = deployed ? this.position() : null;
        this.anchorYaw = this.getYRot();
    }

    private void limitMessage(Player op) {
        if (this.limitMsg-- <= 0) {
            op.displayClientMessage(net.minecraft.network.chat.Component.translatable("msg.exodus_vehicles.field_shift_limit"), true);
            this.limitMsg = 30;
        }
    }

    /**
     * Deployed and manned: A / D swing the whole gun round on its wheels (trail across), W / S roll it back and forth.
     * Both only so far from how it was dug in; past that it has to be packed up and deployed again.
     */
    @Override
    protected double[] deployedShift(Player op) {
        ModConfig cfg = ModConfig.get();
        if (this.anchor == null) {
            this.anchor = this.position();
            this.anchorYaw = this.getYRot();
        }
        // swing (tilt the trail across)
        if (op.xxa != 0) {
            float step = (float) (-Math.signum(op.xxa) * cfg.fieldGunPivotSpeed);
            float off = Mth.wrapDegrees(this.getYRot() + step - this.anchorYaw);
            if (Math.abs(off) <= cfg.fieldGunPivotLimit) {
                this.setYRot(this.getYRot() + step);
            } else {
                limitMessage(op);
            }
        }
        // back and forth
        double fwd = Math.signum(op.zza) * cfg.fieldGunShiftSpeed;
        if (fwd == 0) {
            return new double[]{0, 0};
        }
        double yaw = Math.toRadians(this.getYRot());
        double mx = -Math.sin(yaw) * fwd, mz = Math.cos(yaw) * fwd;
        double nx = this.getX() + mx - this.anchor.x, nz = this.getZ() + mz - this.anchor.z;
        double r = cfg.fieldGunShiftRadius;
        if (nx * nx + nz * nz > r * r) {
            limitMessage(op);
            double cx = this.getX() - this.anchor.x, cz = this.getZ() - this.anchor.z;
            if (cx * mx + cz * mz > 0) {
                return new double[]{0, 0};     // only back toward where it was dug in
            }
        }
        return new double[]{fwd, 0};
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("AnchorX")) {
            this.anchor = new Vec3(tag.getDouble("AnchorX"), tag.getDouble("AnchorY"), tag.getDouble("AnchorZ"));
            this.anchorYaw = tag.getFloat("AnchorYaw");
        }
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.anchor != null && isDeployed()) {
            tag.putDouble("AnchorX", this.anchor.x);
            tag.putDouble("AnchorY", this.anchor.y);
            tag.putDouble("AnchorZ", this.anchor.z);
            tag.putFloat("AnchorYaw", this.anchorYaw);
        }
    }

    // ------------------------------------------------------------------ ballistics (shared by client and server)

    public Vec3 boreDir(float yaw, float traverse, float elevation) {
        double y = Math.toRadians(yaw + traverse), e = Math.toRadians(elevation);
        return new Vec3(-Math.sin(y) * Math.cos(e), Math.sin(e), Math.cos(y) * Math.cos(e));
    }

    /** Muzzle position for the given gun yaw, traverse and elevation, with the gun at 'base'. */
    public Vec3 muzzle(Vec3 base, float yaw, float traverse, float elevation) {
        double gy = Math.toRadians(yaw);
        Vec3 trunnion = base.add(-Math.sin(gy) * TRUNNION_FWD, TRUNNION_UP + BORE_UP, Math.cos(gy) * TRUNNION_FWD);
        return trunnion.add(boreDir(yaw, traverse, elevation).scale(BARREL));
    }

    /**
     * The round's path from pos with velocity vel, one point per tick, until it meets a block (the last point is where)
     * or leaves loaded ground. Same step as the server's shell simulation.
     */
    public static List<Vec3> trajectory(Level level, Vec3 pos, Vec3 vel, int maxTicks) {
        List<Vec3> pts = new ArrayList<>();
        pts.add(pos);
        Vec3 p = pos, v = vel;
        for (int t = 0; t < maxTicks; t++) {
            Vec3 next = ShellManager.step(p, v);
            if (!level.hasChunkAt(BlockPos.containing(next)) || next.y < level.getMinBuildHeight()) {
                pts.add(next);
                break;
            }
            BlockHitResult hit = level.clip(new ClipContext(p, next, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY,
                    CollisionContext.empty()));
            if (hit.getType() != HitResult.Type.MISS) {
                pts.add(hit.getLocation());
                return pts;
            }
            pts.add(next);
            p = next;
            v = v.add(0, -ShellManager.GRAVITY, 0);
        }
        return pts;
    }

    // ------------------------------------------------------------------ firing

    @Override
    protected void shoot(ServerLevel server, ServerPlayer player) {
        float trav = getTraverse(), elev = getAimRange();
        Vec3 muzzle = muzzle(this.position(), this.getYRot(), trav, elev);
        // a little angular error each round
        float s = spread();
        float t2 = trav + (float) (this.random.nextGaussian() * s * 0.5);
        float e2 = elev + (float) (this.random.nextGaussian() * s * 0.5);
        Vec3 vel = boreDir(this.getYRot(), t2, e2).scale(muzzleSpeed());
        List<Vec3> path = trajectory(server, muzzle, vel, 400);
        double groundY = path.get(path.size() - 1).y;
        ShellManager.fire(server, muzzle, vel, player.getUUID(), groundY, this);

        // flash, a puff of smoke, the report
        Vec3 dir = vel.normalize();
        Vec3 m = muzzle.add(dir.scale(0.2));
        server.sendParticles(ParticleTypes.FLASH, m.x, m.y, m.z, 1, 0, 0, 0, 0);
        server.sendParticles(ParticleTypes.EXPLOSION, m.x, m.y, m.z, 1, 0.1, 0.1, 0.1, 0.0);
        server.sendParticles(ParticleTypes.FLAME, m.x, m.y, m.z, 6, 0.12, 0.12, 0.12, 0.06);
        for (ServerPlayer p : server.players()) {
            double dist = Math.sqrt(p.distanceToSqr(m));
            if (dist < 200) {
                server.sendParticles(p, ParticleTypes.LARGE_SMOKE, true, m.x + dir.x, m.y + dir.y, m.z + dir.z, 8, 0.5, 0.3, 0.5, 0.04);
                server.sendParticles(p, ParticleTypes.CAMPFIRE_COSY_SMOKE, true, m.x, m.y, m.z, 2, 0.3, 0.2, 0.3, 0.01);
            }
            float pitch = 0.95F + this.random.nextFloat() * 0.1F;
            if (this.hasPassenger(p)) {
                p.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ArmoredCarMod.FK_CREW),
                        SoundSource.PLAYERS, m.x, m.y, m.z, 1.0F, pitch, this.random.nextLong()));
                continue;
            }
            if (dist < 60) {
                float closeVol = 1.0F - Math.min(1.0F, Math.max(0.0F, (float) ((dist - 30) / 30.0)));
                p.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ArmoredCarMod.FK_CLOSE),
                        SoundSource.PLAYERS, m.x, m.y, m.z, closeVol, pitch, this.random.nextLong()));
            }
            ShellManager.gunReport(p, m, dist, pitch, ArmoredCarMod.FK_MID, ArmoredCarMod.FK_DISTANT, ArmoredCarMod.FK_FAR);
        }
    }

    @Override
    protected void reloadSounds(int reload, int total) {
        // the crew's reload (about 5 s) starts just after the shot and finishes as the gun is ready
        int startAt = Math.max(1, total - 104);
        if (total - reload == startAt) {
            breechSound(ArmoredCarMod.FK_RELOAD);
        }
    }
}
