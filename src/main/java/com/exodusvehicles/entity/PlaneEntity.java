package com.exodusvehicles.entity;

import com.exodusvehicles.ArmoredCarMod;
import com.exodusvehicles.ModConfig;
import com.exodusvehicles.artillery.Suppression;
import com.exodusvehicles.network.TracerPayload;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Fokker Dr.I triplane, Battlefield 1 style. The pilot's client flies it (like a boat or the cars): W / S open and
 * close the throttle, which stays where it was left; the aircraft turns, climbs and banks toward wherever the pilot is
 * looking (mouse aim), A / D add rudder. It stalls when slow, can't climb past the ceiling, and crashes if it hits
 * the ground or a wall hard. Two synchronised LMG 08/15 fire along the nose. Shot down it loses its engine and spins in.
 */
public class PlaneEntity extends Entity implements Repairable {
    /** Centre of gravity above the entity position (wheels on the ground, aircraft level). */
    public static final double CG_UP = 22.0 / 16.0;
    /** Pilot's eye, from the CG: {right, up, forward} (blocks). */
    public static final double[] EYE = {0.0, 12.0 / 16.0, -8.5 / 16.0};
    /** Gun muzzles from the CG: {right (left gun is negative), up, forward}. */
    public static final double GUN_SIDE = 2.5 / 16.0, GUN_UP = 8.3 / 16.0, GUN_FWD = 16.5 / 16.0;
    /** Nose-up angle the aircraft sits at on its tail skid (visual). */
    public static final float GROUND_TILT = 11.0F;

    private static final EntityDataAccessor<Float> DATA_HEALTH = def(EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_THROTTLE = def(EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_ROLL = def(EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_HEAT = def(EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_OVERHEATED = def(EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_DOWNED = def(EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_SPEED = def(EntityDataSerializers.FLOAT);

    private static <T> EntityDataAccessor<T> def(net.minecraft.network.syncher.EntityDataSerializer<T> s) {
        return SynchedEntityData.defineId(PlaneEntity.class, s);
    }

    // flight state (simulated wherever the aircraft is controlled: the pilot's client, or the server when empty)
    public float speed;
    public float throttle;
    public float roll, rollO;
    public float groundTilt, groundTiltO;
    /** Set by the pilot's client every tick before the aircraft ticks. */
    public float inYaw, inPitch, inThrottle, inRudder;
    public boolean hasInput;
    /** Pilot's client: hit something hard this tick (reported to the server). */
    public boolean crashedLocally;

    // guns (server)
    private float gunCooldown;
    private int shotCounter, lastShotTick = -100, dryCooldown;
    private boolean burstActive;
    private final Map<UUID, Integer> lastFlyby = new HashMap<>();
    private int lastExplosionTick = -1;
    private float lastExplosionAmount;
    private Vec3 lastServerPos;
    private int downedTicks;

    // client visuals
    public float propRot, propRotO;
    public final List<ArmoredCarEntity.Tracer> tracers = new ArrayList<>();
    public int flashTicks, flashStyle;
    public float flashScale = 1.0F, flashSpin;
    private int lerpSteps;
    private double lerpX, lerpY, lerpZ;
    private float lerpYRot, lerpXRot;

    public PlaneEntity(EntityType<? extends PlaneEntity> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder b) {
        b.define(DATA_HEALTH, getMaxHealth());
        b.define(DATA_THROTTLE, 0.0F);
        b.define(DATA_ROLL, 0.0F);
        b.define(DATA_HEAT, 0.0F);
        b.define(DATA_OVERHEATED, false);
        b.define(DATA_DOWNED, false);
        b.define(DATA_SPEED, 0.0F);
    }

    // ------------------------------------------------------------------ state

    @Override public float getHealth() { return this.entityData.get(DATA_HEALTH); }
    @Override public float getMaxHealth() { return ModConfig.get().planeMaxHealth; }
    public float syncedThrottle() { return this.entityData.get(DATA_THROTTLE); }
    public float syncedRoll() { return this.entityData.get(DATA_ROLL); }
    public float syncedSpeed() { return this.entityData.get(DATA_SPEED); }
    public float getHeat() { return this.entityData.get(DATA_HEAT); }
    public boolean isOverheated() { return this.entityData.get(DATA_OVERHEATED); }
    public boolean isDowned() { return this.entityData.get(DATA_DOWNED); }

    @Override
    public boolean repairBy(float amount) {
        if (isDowned() || getHealth() >= getMaxHealth()) {
            return false;
        }
        this.entityData.set(DATA_HEALTH, Math.min(getMaxHealth(), getHealth() + amount));
        return true;
    }

    @Nullable
    public Player getPilot() {
        return this.getFirstPassenger() instanceof Player p ? p : null;
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return this.getFirstPassenger() instanceof Player p ? p : null;
    }

    /** Point on the aircraft from the CG ({right, up, forward} in blocks), using the given attitude. */
    public static Vec3 bodyPoint(Vec3 pos, float yaw, float pitch, float rollDeg, double right, double up, double fwd) {
        Vec3 f = Vec3.directionFromRotation(pitch, yaw);
        Vec3 r0 = new Vec3(-Math.cos(Math.toRadians(yaw)), 0, -Math.sin(Math.toRadians(yaw)));
        Vec3 u0 = r0.cross(f).normalize().scale(-1.0);
        if (u0.y < 0) {
            u0 = u0.scale(-1.0);
        }
        double rr = Math.toRadians(rollDeg);
        // bank right (positive roll): the right wing goes down
        Vec3 r = r0.scale(Math.cos(rr)).add(u0.scale(-Math.sin(rr)));
        Vec3 u = u0.scale(Math.cos(rr)).add(r0.scale(Math.sin(rr)));
        return pos.add(0, CG_UP, 0).add(f.scale(fwd)).add(r.scale(right)).add(u.scale(up));
    }

    public Vec3 bodyPoint(double right, double up, double fwd) {
        return bodyPoint(this.position(), this.getYRot(), this.getXRot(), this.roll, right, up, fwd);
    }

    // ------------------------------------------------------------------ save

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Health")) {
            this.entityData.set(DATA_HEALTH, tag.getFloat("Health"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Health", getHealth());
    }

    // ------------------------------------------------------------------ basics

    @Override public boolean canBeCollidedWith() { return false; }
    @Override public boolean isPushable() { return false; }
    @Override public boolean isPickable() { return !this.isRemoved(); }
    @Override protected Entity.MovementEmission getMovementEmission() { return Entity.MovementEmission.NONE; }
    @Override public ItemStack getPickResult() { return new ItemStack(ArmoredCarMod.FOKKER_ITEM); }
    @Override public AABB getBoundingBoxForCulling() { return this.getBoundingBox().inflate(5.0, 3.0, 5.0); }
    @Override public boolean shouldRenderAtSqrDistance(double d) { return d < 512.0 * 512.0; }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().isEmpty() && !isDowned();
    }

    /** The pilot sits in the cockpit (drawn as part of the aircraft; his own model is hidden). */
    @Override
    public void positionRider(Entity passenger, Entity.MoveFunction move) {
        if (!this.hasPassenger(passenger)) {
            return;
        }
        Vec3 eye = bodyPoint(EYE[0], EYE[1], EYE[2]);
        move.accept(passenger, eye.x, eye.y - passenger.getEyeHeight(), eye.z);
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        Vec3 side = bodyPoint(-1.0, 0.0, -1.0);
        if (this.onGround()) {
            return new Vec3(side.x, this.getY(), side.z);
        }
        return side;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof com.exodusvehicles.item.VehicleToolItem) {
            return InteractionResult.PASS;
        }
        if (player.getVehicle() == this || isDowned()) {
            return InteractionResult.PASS;
        }
        if (!this.level().isClientSide) {
            if (player.startRiding(this)) {
                sound(ArmoredCarMod.PLANE_START, 1.0F, 1.0F);
                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        if (!this.level().isClientSide && !isDowned()) {
            sound(ArmoredCarMod.PLANE_STOP, 1.0F, 1.0F);
        }
    }

    private void sound(SoundEvent s, float vol, float pitch) {
        this.level().playSound(null, this.getX(), this.getY() + CG_UP, this.getZ(), s, SoundSource.PLAYERS, vol, pitch);
    }

    // ------------------------------------------------------------------ flight

    /**
     * One tick of flight toward a target heading (yaw / pitch the pilot is looking at). Throttle input -1..1 moves the
     * throttle, which then stays put; speed follows the throttle, falls off climbing and builds diving.
     */
    private void fly(float tYaw, float tPitch, float throttleIn, float rudder, boolean engine) {
        ModConfig c = ModConfig.get();
        boolean ground = this.onGround();
        this.throttle = Mth.clamp(this.throttle + throttleIn * 0.02F, 0.0F, 1.0F);
        float max = c.planeMaxSpeed, stall = c.planeStallSpeed;
        float target = !engine ? 0.0F : ground ? this.throttle * max : Mth.lerp(this.throttle, c.planeIdleSpeed, max);
        float pitch = this.getXRot(), yaw = this.getYRot();
        double climb = Math.sin(Math.toRadians(-pitch));
        this.speed += (target - this.speed) * (ground ? 0.025F : 0.012F) - (float) climb * 0.024F;
        if (ground && this.throttle < 0.05F) {
            this.speed *= 0.96F;              // rolling to a stop
        }
        this.speed = Mth.clamp(this.speed, 0.0F, max * 1.45F);
        float lift = Mth.clamp(this.speed / stall, 0.0F, 1.0F);
        float auth = Mth.clamp(this.speed / (stall * 1.3F), 0.12F, 1.0F);
        float dy = Mth.wrapDegrees(tYaw - yaw) + rudder * 8.0F;
        if (ground) {
            float taxi = Mth.clamp(this.speed / 0.15F, 0.25F, 1.0F) * 2.2F;
            yaw += Mth.clamp(dy * 0.1F, -taxi, taxi);
            boolean takeOff = this.speed > c.planeTakeoffSpeed && tPitch < -4.0F;
            pitch = takeOff ? -7.0F : pitch + (0.0F - pitch) * 0.3F;
            this.roll += (0.0F - this.roll) * 0.3F;
        } else {
            float rate = c.planeTurnRate * auth;
            yaw += Mth.clamp(dy * 0.09F, -rate, rate);
            pitch += Mth.clamp((tPitch - pitch) * 0.12F, -rate, rate);
            pitch += (1.0F - lift) * 1.8F;     // stalling: the nose drops
            float ceil = c.planeCeiling;
            if (this.getY() > ceil - 20) {
                float k = Mth.clamp((float) (this.getY() - (ceil - 20)) / 20.0F, 0.0F, 1.0F);
                pitch = Math.max(pitch, Mth.lerp(k, -75.0F, 3.0F));      // can't climb past the ceiling
            }
            pitch = Mth.clamp(pitch, -75.0F, 80.0F);
            float rollT = Mth.clamp(dy * 2.2F, -72.0F, 72.0F);
            this.roll += (rollT - this.roll) * 0.12F;
        }
        this.setYRot(yaw);
        this.setXRot(pitch);
        Vec3 dir = Vec3.directionFromRotation(pitch, yaw);
        Vec3 vel = dir.scale(this.speed);
        if (ground) {
            vel = new Vec3(vel.x, pitch < -1.0F ? vel.y : -0.1, vel.z);
        } else {
            vel = vel.add(0, -(1.0F - lift) * 0.5F, 0);     // sinking when too slow to fly
        }
        double before = vel.length();
        Vec3 from = this.position();
        this.move(MoverType.SELF, vel);
        Vec3 moved = this.position().subtract(from);
        if (this.horizontalCollision && Math.sqrt(vel.x * vel.x + vel.z * vel.z) > c.planeCrashSpeed) {
            this.crashedLocally = true;
        }
        if (this.verticalCollision && !ground && vel.y < -c.planeCrashSpeed * 0.9) {
            this.crashedLocally = true;            // flown into the ground
        }
        if (this.horizontalCollision && before > 0.01) {
            this.speed *= (float) Mth.clamp(moved.length() / before, 0.0, 1.0);
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.rollO = this.roll;
        this.groundTiltO = this.groundTilt;
        if (this.lerpSteps > 0 && !this.isControlledByLocalInstance()) {
            double x = this.getX() + (this.lerpX - this.getX()) / this.lerpSteps;
            double y = this.getY() + (this.lerpY - this.getY()) / this.lerpSteps;
            double z = this.getZ() + (this.lerpZ - this.getZ()) / this.lerpSteps;
            float yr = this.getYRot() + Mth.wrapDegrees(this.lerpYRot - this.getYRot()) / this.lerpSteps;
            float xr = this.getXRot() + (this.lerpXRot - this.getXRot()) / this.lerpSteps;
            this.lerpSteps--;
            this.setPos(x, y, z);
            this.setYRot(yr);
            this.setXRot(xr);
        }
        if (this.isControlledByLocalInstance()) {
            if (isDowned()) {
                // shot down: no engine, spinning in
                this.throttle = 0.0F;
                fly(this.getYRot() + 14.0F, Math.min(70.0F, this.getXRot() + 6.0F), -1.0F, 0.0F, false);
                this.roll = Math.min(95.0F, this.roll + 4.0F);
            } else if (this.hasInput && getPilot() != null) {
                fly(this.inYaw, this.inPitch, this.inThrottle, this.inRudder, true);
            } else {
                fly(this.getYRot(), this.onGround() ? 0.0F : Math.min(30.0F, this.getXRot() + 0.5F), -1.0F, 0.0F, true);
            }
            this.hasInput = false;
            if (!this.level().isClientSide && this.crashedLocally) {
                crash();
                return;
            }
        } else if (this.level().isClientSide) {
            this.roll += (syncedRoll() - this.roll) * 0.35F;
            this.throttle = syncedThrottle();
        }
        boolean sitting = this.onGround() && this.speed < 0.9F;
        if (this.level().isClientSide && !this.isControlledByLocalInstance()) {
            sitting = this.onGround() && syncedSpeed() < 0.9F;
        }
        this.groundTilt += ((sitting ? GROUND_TILT : 0.0F) - this.groundTilt) * 0.15F;
        if (this.level().isClientSide) {
            tickClient();
        } else {
            tickServer();
        }
    }

    private void tickClient() {
        this.propRotO = this.propRot;
        float t = isDowned() ? 0.0F : (getPilot() != null ? Math.max(0.25F, this.throttle) : 0.0F);
        this.propRot += t * 1.9F;
        if (this.flashTicks > 0) {
            this.flashTicks--;
        }
        this.tracers.removeIf(tr -> ++tr.age > ArmoredCarEntity.TRACER_LIFE);
        if (getHealth() < getMaxHealth() * 0.4F || isDowned()) {
            Vec3 e = bodyPoint(0.0, 0.2, 1.2);
            this.level().addParticle(isDowned() ? ParticleTypes.LARGE_SMOKE : ParticleTypes.SMOKE, e.x, e.y, e.z, 0, 0.02, 0);
            if (isDowned() && this.random.nextInt(2) == 0) {
                this.level().addParticle(ParticleTypes.FLAME, e.x, e.y, e.z, 0, 0.02, 0);
            }
        }
    }

    private void tickServer() {
        ModConfig cfg = ModConfig.get();
        Vec3 pos = this.position();
        if (this.lastServerPos != null) {
            this.entityData.set(DATA_SPEED, (float) pos.distanceTo(this.lastServerPos));
        }
        this.lastServerPos = pos;
        if (!this.isControlledByLocalInstance()) {
            // flown by the pilot's client: keep others' view of throttle / roll from the pilot's reports
        } else {
            this.entityData.set(DATA_THROTTLE, this.throttle);
            this.entityData.set(DATA_ROLL, this.roll);
        }
        if (isDowned()) {
            this.downedTicks++;
            if (this.onGround() || this.isInWater() || this.downedTicks > 600) {
                crash();
                return;
            }
        }
        if (this.gunCooldown > 0.0F) {
            this.gunCooldown = Math.max(0.0F, this.gunCooldown - 1.0F);
        }
        if (this.dryCooldown > 0) {
            this.dryCooldown--;
        }
        float heat = getHeat();
        if (heat > 0.0F) {
            heat = Math.max(0.0F, heat - cfg.planeGunCooling);
            this.entityData.set(DATA_HEAT, heat);
        }
        if (isOverheated() && heat <= 0.3F) {
            this.entityData.set(DATA_OVERHEATED, false);
        }
        if (this.burstActive && this.tickCount - this.lastShotTick > 3) {
            this.burstActive = false;
            gunSound((ServerLevel) this.level(), ArmoredCarMod.PLANE_MG_TAIL);
        }
        if (this.isInWater() && this.speed > 0.3F) {
            crash();
        }
    }

    /** From the pilot's client: throttle and bank (for everyone else's view), and a crash if it had one. */
    public void pilotReport(ServerPlayer from, float throttle, float roll, boolean crashed) {
        if (getPilot() != from) {
            return;
        }
        this.throttle = Mth.clamp(throttle, 0.0F, 1.0F);
        this.roll = Mth.clamp(roll, -180.0F, 180.0F);
        this.entityData.set(DATA_THROTTLE, this.throttle);
        this.entityData.set(DATA_ROLL, this.roll);
        if (crashed && !this.isRemoved()) {
            crash();
        }
    }

    /** Hit the ground or a wall too hard, or came down shot to pieces: it goes up, and so does the pilot. */
    public void crash() {
        if (this.isRemoved() || !(this.level() instanceof ServerLevel server)) {
            return;
        }
        Vec3 c = this.position().add(0, CG_UP, 0);
        List<Entity> riders = new ArrayList<>(this.getPassengers());
        this.ejectPassengers();
        for (Entity r : riders) {
            r.hurt(this.damageSources().explosion(this, null), ModConfig.get().planeCrashDamage);
        }
        server.explode(this, c.x, c.y, c.z, 2.5F, false, Level.ExplosionInteraction.NONE);
        server.sendParticles(ParticleTypes.LARGE_SMOKE, c.x, c.y, c.z, 40, 2.0, 1.0, 2.0, 0.04);
        server.sendParticles(ParticleTypes.FLAME, c.x, c.y, c.z, 25, 1.5, 0.6, 1.5, 0.02);
        this.discard();
    }

    // ------------------------------------------------------------------ damage

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source) || this.isRemoved()) {
            return false;
        }
        if (this.level().isClientSide) {
            return true;
        }
        if (source.getEntity() instanceof Player p && p.getAbilities().instabuild && source.getDirectEntity() == p) {
            this.ejectPassengers();
            this.discard();
            return true;
        }
        if (source.getEntity() != null && this.hasPassenger(source.getEntity())) {
            return false;
        }
        if (source.is(DamageTypeTags.IS_EXPLOSION)) {
            if (this.tickCount == this.lastExplosionTick) {
                if (amount <= this.lastExplosionAmount) {
                    return false;
                }
                float extra = amount - this.lastExplosionAmount;
                this.lastExplosionAmount = amount;
                amount = extra;
            } else {
                this.lastExplosionTick = this.tickCount;
                this.lastExplosionAmount = amount;
            }
        }
        if (isDowned()) {
            return true;
        }
        this.entityData.set(DATA_HEALTH, getHealth() - amount);
        this.markHurt();
        if (getHealth() <= 0.0F) {
            this.entityData.set(DATA_DOWNED, true);
            this.entityData.set(DATA_HEALTH, 0.0F);
            sound(ArmoredCarMod.PLANE_DEATH, 4.0F, 1.0F);
            if (source.getEntity() instanceof ServerPlayer killer) {
                net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(killer,
                        new com.exodusvehicles.network.HitMarkerPayload(true, -1));
            }
            if (this.onGround()) {
                crash();
            }
        }
        return true;
    }

    // ------------------------------------------------------------------ guns

    public void fire(ServerPlayer pilot) {
        if (getPilot() != pilot || isDowned() || this.gunCooldown >= 1.0F) {
            return;
        }
        ModConfig cfg = ModConfig.get();
        if (isOverheated()) {
            if (this.dryCooldown <= 0) {
                this.dryCooldown = 20;
                pilot.displayClientMessage(Component.translatable("msg.exodus_vehicles.plane_overheated"), true);
            }
            return;
        }
        float heat = getHeat() + cfg.planeGunHeatPerShot;
        if (heat >= 1.0F) {
            heat = 1.0F;
            this.entityData.set(DATA_OVERHEATED, true);
            pilot.displayClientMessage(Component.translatable("msg.exodus_vehicles.plane_overheated"), true);
        }
        this.entityData.set(DATA_HEAT, heat);
        this.gunCooldown += 2.0F;      // the pair fires alternately, a round every other tick between them
        ServerLevel server = (ServerLevel) this.level();
        int shot = this.shotCounter++;
        double side = (shot % 2 == 0 ? -1 : 1) * GUN_SIDE;
        Vec3 muzzle = bodyPoint(side, GUN_UP, GUN_FWD);
        Vec3 dir = Vec3.directionFromRotation(this.getXRot(), this.getYRot());
        double sp = cfg.planeGunSpread;
        dir = dir.add(this.random.nextGaussian() * sp, this.random.nextGaussian() * sp, this.random.nextGaussian() * sp).normalize();
        Vec3 end = muzzle.add(dir.scale(cfg.planeGunRange));
        BlockHitResult bh = server.clip(new ClipContext(muzzle, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (bh.getType() != HitResult.Type.MISS) {
            end = bh.getLocation();
        }
        EntityHitResult eh = ProjectileUtil.getEntityHitResult(server, this, muzzle, end, new AABB(muzzle, end).inflate(1.0),
                e -> e != this && !this.hasPassenger(e) && e.isPickable() && e.isAlive() && !e.isSpectator(), 0.3F);
        if (eh != null) {
            Entity target = eh.getEntity();
            end = eh.getLocation();
            if (target instanceof LivingEntity living) {
                living.invulnerableTime = 0;
            }
            target.hurt(this.damageSources().mobProjectile(this, pilot), cfg.planeGunDamage);
        } else if (bh.getType() == HitResult.Type.BLOCK) {
            server.sendParticles(new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK,
                    server.getBlockState(bh.getBlockPos())), end.x, end.y + 0.05, end.z, 5, 0.1, 0.1, 0.1, 0.1);
            server.sendParticles(ParticleTypes.POOF, end.x, end.y + 0.05, end.z, 1, 0, 0, 0, 0.01);
            if (shot % 3 == 0) {
                server.playSound(null, end.x, end.y, end.z, ArmoredCarMod.IMPACT_GROUND, SoundSource.PLAYERS, 1.0F,
                        0.9F + this.random.nextFloat() * 0.2F);
            }
        }
        if (shot % 3 == 0) {
            TracerPayload tracer = new TracerPayload(this.getId(), muzzle.x, muzzle.y, muzzle.z, end.x, end.y, end.z);
            for (ServerPlayer viewer : net.fabricmc.fabric.api.networking.v1.PlayerLookup.tracking(this)) {
                net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(viewer, tracer);
            }
        }
        suppress(server, pilot, muzzle, end);
        gunSound(server, ArmoredCarMod.PLANE_MG);
        this.lastShotTick = this.tickCount;
        this.burstActive = true;
        server.broadcastEntityEvent(this, (byte) 61);
    }

    private void gunSound(ServerLevel server, SoundEvent s) {
        Vec3 at = bodyPoint(0.0, GUN_UP, GUN_FWD);
        var h = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(s);
        long seed = this.random.nextLong();
        float pitch = 0.97F + this.random.nextFloat() * 0.06F;
        for (ServerPlayer p : server.players()) {
            double d = Math.sqrt(p.distanceToSqr(at));
            if (d > 220) {
                continue;
            }
            float vol = this.hasPassenger(p) ? 0.85F : (float) Math.max(0.15, 1.0 - d / 220.0) * 3.0F;
            p.connection.send(new ClientboundSoundPacket(h, SoundSource.PLAYERS, at.x, at.y, at.z, vol, pitch, seed));
        }
    }

    private void suppress(ServerLevel server, ServerPlayer shooter, Vec3 from, Vec3 to) {
        ModConfig cfg = ModConfig.get();
        Vec3 seg = to.subtract(from);
        double len2 = seg.lengthSqr();
        if (len2 < 1.0) {
            return;
        }
        for (ServerPlayer p : server.players()) {
            if (p == shooter || p.isSpectator()) {
                continue;
            }
            Vec3 eye = p.getEyePosition();
            double t = Mth.clamp(eye.subtract(from).dot(seg) / len2, 0.0, 1.0);
            Vec3 closest = from.add(seg.scale(t));
            double d = closest.distanceTo(eye);
            if (d > cfg.suppressionRadius || t < 0.03) {
                continue;
            }
            if (cfg.suppressionEnabled) {
                Suppression.add(p, (float) (cfg.suppressionPerRound * (0.35 + 0.65 * (1.0 - d / cfg.suppressionRadius))));
            }
            if (t < 0.97 && d < 2.5) {
                Integer last = this.lastFlyby.get(p.getUUID());
                if (last == null || this.tickCount - last >= 4) {
                    this.lastFlyby.put(p.getUUID(), this.tickCount);
                    p.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ArmoredCarMod.BULLET_FLYBY),
                            SoundSource.PLAYERS, closest.x, closest.y, closest.z, 1.0F, 0.9F + this.random.nextFloat() * 0.2F,
                            this.random.nextLong()));
                }
            }
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 61) {
            this.flashTicks = 2;
            this.flashStyle = this.random.nextInt(4);
            this.flashScale = 0.6F + this.random.nextFloat() * 0.3F;
            this.flashSpin = this.random.nextFloat() * Mth.TWO_PI;
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
        this.lerpX = x;
        this.lerpY = y;
        this.lerpZ = z;
        this.lerpYRot = yRot;
        this.lerpXRot = xRot;
        this.lerpSteps = 3;
    }

    @Override
    public void setXRot(float xRot) {
        // no player-style clamp to +-90: the aircraft only ever uses -75..80, but keep it finite
        super.setXRot(Mth.clamp(xRot, -89.0F, 89.0F));
    }
}
