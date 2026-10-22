package com.exodusvehicles.entity;

import com.exodusvehicles.ArmoredCarMod;
import com.exodusvehicles.ModConfig;
import com.exodusvehicles.artillery.Suppression;
import com.exodusvehicles.compat.TaczCompat;
import com.exodusvehicles.network.TracerPayload;
import net.minecraft.core.particles.BlockParticleOption;
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
import net.minecraft.world.entity.Pose;
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
 * MG 08 on its sled mount (Schlitten 08), with or without the armoured shield. Get on (right-click) and the gunner
 * sits behind the spade grips; look to aim (limited traverse either side of how it was set down), hold left-click to
 * fire, R for a new belt. Same belt, heat and ammunition as the Eisenwolf's MG. Rounds passing close to someone
 * suppress them. Sneak + right-click (nobody on it) picks it up again.
 */
public class FieldMgEntity extends Entity implements Repairable {
    /** The gunner's eye lined up with the rear notch and the front blade (gun frame, blocks from the elevation pivot). */
    public static final double EYE_UP = 5.0 / 16.0, EYE_BACK = 12.0 / 16.0;
    public static final double BORE_UP = 1.4 / 16.0, MUZZLE_FWD = 15.6 / 16.0;

    private static final EntityDataAccessor<Float> DATA_HEALTH = def(EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_BELT = def(EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_HEAT = def(EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_RELOAD = def(EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_OVERHEATED = def(EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_AIM_YAW = def(EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_AIM_PITCH = def(EntityDataSerializers.FLOAT);
    /** Where the gunner's feet are relative to the gun's base when standing (NaN = sitting behind the sled). */
    private static final EntityDataAccessor<Float> DATA_STAND_DY = def(EntityDataSerializers.FLOAT);

    private static <T> EntityDataAccessor<T> def(net.minecraft.network.syncher.EntityDataSerializer<T> s) {
        return SynchedEntityData.defineId(FieldMgEntity.class, s);
    }

    private float gunCooldown;
    private int dryFireCooldown, lastShotTick = -100, shotCounter, pendingRounds;
    private boolean burstActive;
    private Vec3 lastMuzzle = Vec3.ZERO;
    private int lastExplosionTick = -1;
    private float lastExplosionAmount;
    private final Map<UUID, Integer> lastFlyby = new HashMap<>();
    private int lerpSteps;
    private double lerpX, lerpY, lerpZ;
    private float lerpYRot;

    // client visuals
    public final List<ArmoredCarEntity.Tracer> tracers = new ArrayList<>();
    public int flashTicks, flashStyle;
    public float flashScale = 1.0F, flashSpin, recoil, recoilO;

    public FieldMgEntity(EntityType<? extends FieldMgEntity> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
    }

    public boolean hasShield() {
        EntityType<?> t = this.getType();
        return t == ArmoredCarMod.FIELD_MG_SHIELD || t == ArmoredCarMod.FIELD_MG_BUNKER_SHIELD;
    }

    /** Pedestal mount for bunkers and trenches (no sled; the gunner stands). */
    public boolean isBunker() {
        EntityType<?> t = this.getType();
        return t == ArmoredCarMod.FIELD_MG_BUNKER || t == ArmoredCarMod.FIELD_MG_BUNKER_SHIELD || t == ArmoredCarMod.FIELD_MG_BUNKER_SCOPED;
    }

    /** Light tripod: higher than the sled, the gunner kneels / sits low behind it. */
    public boolean isTripod() {
        EntityType<?> t = this.getType();
        return t == ArmoredCarMod.FIELD_MG_TRIPOD || t == ArmoredCarMod.FIELD_MG_TRIPOD_SCOPED;
    }

    /** Zielfernrohr 12 (2.5x) fitted at the left rear of the receiver. */
    public boolean hasScope() {
        EntityType<?> t = this.getType();
        return t == ArmoredCarMod.FIELD_MG_SCOPED || t == ArmoredCarMod.FIELD_MG_TRIPOD_SCOPED || t == ArmoredCarMod.FIELD_MG_BUNKER_SCOPED;
    }

    /** The eye at the scope's eyepiece (gun frame, blocks): {right, up, back}. */
    public static final double[] SCOPE_EYE = {-2.6 / 16.0, 4.4 / 16.0, 9.0 / 16.0};

    /** Height of the elevation pivot above the base. */
    public double pivotUp() {
        return (isBunker() ? 18.0 : isTripod() ? 16.0 : 10.0) / 16.0;
    }

    /** The gunner stands (bunker mount, or a sled set up on a parapet above where they are). */
    public boolean gunnerStands() {
        return !Float.isNaN(this.entityData.get(DATA_STAND_DY));
    }

    /** Ground under where a standing gunner would be, relative to the gun's base, or NaN if they should sit. */
    private float standHeight() {
        if (isBunker()) {
            return 0.0F;
        }
        Vec3 spot = localToWorld(0.0, 0.4, -1.1);
        BlockHitResult down = this.level().clip(new ClipContext(spot, spot.add(0, -3.0, 0), ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, this));
        if (down.getType() == HitResult.Type.MISS) {
            return Float.NaN;
        }
        double dy = down.getLocation().y - this.getY();
        return dy < -0.45 ? (float) Math.max(-1.6, dy) : Float.NaN;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder b) {
        b.define(DATA_HEALTH, getMaxHealth());
        b.define(DATA_BELT, ModConfig.get().mgBeltSize);
        b.define(DATA_HEAT, 0.0F);
        b.define(DATA_RELOAD, 0);
        b.define(DATA_OVERHEATED, false);
        b.define(DATA_AIM_YAW, 0.0F);
        b.define(DATA_AIM_PITCH, 0.0F);
        b.define(DATA_STAND_DY, Float.NaN);
    }

    // ------------------------------------------------------------------ state

    @Override public float getHealth() { return this.entityData.get(DATA_HEALTH); }
    @Override public float getMaxHealth() { return ModConfig.get().fieldMgMaxHealth; }
    public int getBelt() { return this.entityData.get(DATA_BELT); }
    public float getHeat() { return this.entityData.get(DATA_HEAT); }
    public int getReloadTicks() { return this.entityData.get(DATA_RELOAD); }
    public boolean isOverheated() { return this.entityData.get(DATA_OVERHEATED); }
    /** Synced aim (relative yaw, pitch) for everyone but the gunner, who uses their own view. */
    public float aimYaw() { return this.entityData.get(DATA_AIM_YAW); }
    public float aimPitch() { return this.entityData.get(DATA_AIM_PITCH); }

    @Override
    public boolean repairBy(float amount) {
        if (getHealth() >= getMaxHealth()) {
            return false;
        }
        this.entityData.set(DATA_HEALTH, Math.min(getMaxHealth(), getHealth() + amount));
        return true;
    }

    @Nullable
    public Player getGunner() {
        return this.getFirstPassenger() instanceof Player p ? p : null;
    }

    // ------------------------------------------------------------------ aiming geometry (client and server)

    public static float traverseLimit() { return ModConfig.get().fieldMgTraverse; }

    /** Yaw relative to the sled, clamped to the traverse. */
    public float relYaw(float viewYaw) {
        float lim = traverseLimit();
        return Mth.clamp(Mth.wrapDegrees(viewYaw - this.getYRot()), -lim, lim);
    }

    public static float clampPitch(float pitch) {
        ModConfig c = ModConfig.get();
        return Mth.clamp(pitch, -c.fieldMgMaxElevation, c.fieldMgMaxDepression);
    }

    /** A point in the gun's own frame (right, up, forward from the elevation pivot) in the world. */
    public Vec3 gunPoint(Vec3 base, float sledYaw, float relYaw, float pitch, double right, double up, double fwd) {
        double y = Math.toRadians(sledYaw + relYaw), p = Math.toRadians(pitch);
        Vec3 f = new Vec3(-Math.sin(y) * Math.cos(p), -Math.sin(p), Math.cos(y) * Math.cos(p));
        Vec3 r = new Vec3(-Math.cos(y), 0, -Math.sin(y));
        Vec3 u = f.cross(r).normalize();
        if (u.y < 0) {
            u = u.scale(-1);
        }
        return base.add(0, pivotUp(), 0).add(f.scale(fwd)).add(r.scale(right)).add(u.scale(up));
    }

    public Vec3 localToWorld(double right, double up, double forward) {
        double yaw = Math.toRadians(this.getYRot());
        double fx = -Math.sin(yaw), fz = Math.cos(yaw);
        double rx = -Math.cos(yaw), rz = -Math.sin(yaw);
        return new Vec3(this.getX() + fx * forward + rx * right, this.getY() + up, this.getZ() + fz * forward + rz * right);
    }

    // ------------------------------------------------------------------ save

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Health")) {
            this.entityData.set(DATA_HEALTH, tag.getFloat("Health"));
        }
        if (tag.contains("Belt")) {
            this.entityData.set(DATA_BELT, tag.getInt("Belt"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Health", getHealth());
        tag.putInt("Belt", getBelt());
    }

    // ------------------------------------------------------------------ basics

    @Override public boolean canBeCollidedWith() { return false; }
    @Override public boolean isPushable() { return false; }
    @Override public boolean isPickable() { return !this.isRemoved(); }
    @Override protected Entity.MovementEmission getMovementEmission() { return Entity.MovementEmission.NONE; }

    @Override
    public ItemStack getPickResult() {
        net.minecraft.world.item.Item item = ArmoredCarMod.FIELD_MG_ITEMS.get(this.getType());
        return new ItemStack(item != null ? item : ArmoredCarMod.FIELD_MG_ITEM);
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return this.getBoundingBox().inflate(2.0, 1.0, 2.0);
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().isEmpty();
    }

    /**
     * The gunner stays behind the spade grips, swinging round the pivot as the gun traverses: sitting on the ground
     * behind the sled, or standing (bunker mount, or a sled set up on a parapet above them).
     */
    @Override
    public void positionRider(Entity passenger, Entity.MoveFunction move) {
        if (!this.hasPassenger(passenger)) {
            return;
        }
        float rel = relYaw(passenger.getYHeadRot());
        double yaw = Math.toRadians(this.getYRot() + rel);
        double back = gunnerStands() ? (isBunker() ? 1.0 : 1.1) : 1.18;
        double dy = gunnerStands() ? this.entityData.get(DATA_STAND_DY) : isTripod() ? -0.38 : -0.62;
        move.accept(passenger, this.getX() + Math.sin(yaw) * back, this.getY() + dy, this.getZ() - Math.cos(yaw) * back);
        if (passenger instanceof LivingEntity living) {
            living.setYBodyRot(this.getYRot() + rel);
            living.yBodyRotO = living.yBodyRot;
        }
    }

    /** The gunner can only look as far round as the gun traverses. */
    @Override
    public void onPassengerTurned(Entity passenger) {
        float lim = traverseLimit();
        float rel = Mth.wrapDegrees(passenger.getYRot() - this.getYRot());
        float c = Mth.clamp(rel, -lim, lim);
        passenger.yRotO += c - rel;
        passenger.setYRot(passenger.getYRot() + c - rel);
        passenger.setYHeadRot(passenger.getYRot());
        passenger.setXRot(clampPitch(passenger.getXRot()));
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        for (double side : new double[]{-1.2, 1.2}) {
            for (double fwd : new double[]{-1.2, -0.4, -2.0}) {
                Vec3 base = localToWorld(side, 0.0, fwd);
                for (double dy : new double[]{0.0, 1.0, -1.0}) {
                    Vec3 at = base.add(0, dy, 0);
                    if (this.level().noCollision(passenger, passenger.getDimensions(Pose.STANDING).makeBoundingBox(at))) {
                        return at;
                    }
                }
            }
        }
        return super.getDismountLocationForPassenger(passenger);
    }

    /** Right-click: get on. Sneak + right-click with nobody on it: pick it up. */
    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean client = this.level().isClientSide;
        if (stack.getItem() instanceof com.exodusvehicles.item.VehicleToolItem) {
            return InteractionResult.PASS;
        }
        if (player.getVehicle() == this) {
            return InteractionResult.PASS;
        }
        if (player.isSecondaryUseActive()) {
            if (!client && this.getPassengers().isEmpty()) {
                ItemStack item = getPickResult();
                if (!player.getAbilities().instabuild && !player.getInventory().add(item)) {
                    player.drop(item, false);
                }
                this.playSound(SoundEvents.ARMOR_EQUIP_IRON.value(), 1.0F, 0.8F);
                this.discard();
            }
            return InteractionResult.sidedSuccess(client);
        }
        if (!client) {
            return player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
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
        ModConfig cfg = ModConfig.get();
        float dealt;
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
            dealt = amount * cfg.explosionDamageMultiplier;
        } else {
            dealt = amount >= cfg.penetrationThreshold ? amount : 0.0F;
            this.level().playSound(null, this.getX(), this.getY() + 0.7, this.getZ(), ArmoredCarMod.ARMOR_HIT, SoundSource.PLAYERS,
                    0.6F, 1.1F + this.random.nextFloat() * 0.2F);
        }
        if (dealt <= 0.0F) {
            return false;
        }
        shellHit(dealt);
        return true;
    }

    /** Straight off the health (shell hits). True if it broke. */
    public boolean shellHit(float amount) {
        if (this.isRemoved() || amount <= 0.0F) {
            return false;
        }
        this.entityData.set(DATA_HEALTH, getHealth() - amount);
        this.markHurt();
        if (getHealth() <= 0.0F) {
            this.ejectPassengers();
            if (this.level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 0.6, this.getZ(), 15, 0.5, 0.4, 0.5, 0.03);
                server.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ANVIL_DESTROY, SoundSource.PLAYERS, 0.8F, 0.8F);
            }
            this.discard();
            return true;
        }
        return false;
    }

    /**
     * Shielded gun: true when the plate is between the gunner and where a hit came from (in front, not from high above).
     */
    public boolean shieldCovers(Vec3 from) {
        if (!hasShield()) {
            return false;
        }
        Vec3 eye = gunPoint(this.position(), this.getYRot(), aimYaw(), 0.0F, 0.0, EYE_UP, -EYE_BACK);
        Vec3 d = from.subtract(eye);
        double h = Math.sqrt(d.x * d.x + d.z * d.z);
        if (h < 0.5 || Math.toDegrees(Math.atan2(d.y, h)) > 35.0) {
            return false;
        }
        float yawTo = (float) Math.toDegrees(Math.atan2(-d.x, d.z));
        return Math.abs(Mth.wrapDegrees(yawTo - (this.getYRot() + aimYaw()))) < 55.0F;
    }

    // ------------------------------------------------------------------ firing

    public void fire(ServerPlayer shooter) {
        if (getGunner() != shooter || this.gunCooldown >= 1.0F || getReloadTicks() > 0) {
            return;
        }
        ModConfig cfg = ModConfig.get();
        if (isOverheated()) {
            if (this.dryFireCooldown <= 0) {
                this.dryFireCooldown = 20;
                this.playSound(SoundEvents.FIRE_EXTINGUISH, 0.5F, 1.4F);
                shooter.displayClientMessage(Component.translatable("msg.exodus_vehicles.overheated"), true);
            }
            return;
        }
        if (getBelt() <= 0) {
            if (!startReload(shooter) && this.dryFireCooldown <= 0) {
                this.dryFireCooldown = 10;
                this.playSound(SoundEvents.DISPENSER_FAIL, 0.6F, 1.6F);
            }
            return;
        }
        this.entityData.set(DATA_BELT, getBelt() - 1);
        float heat = getHeat() + cfg.mgHeatPerShot;
        if (heat >= 1.0F) {
            heat = 1.0F;
            this.entityData.set(DATA_OVERHEATED, true);
            this.playSound(SoundEvents.FIRE_EXTINGUISH, 1.0F, 0.8F);
            shooter.displayClientMessage(Component.translatable("msg.exodus_vehicles.overheated"), true);
        }
        this.entityData.set(DATA_HEAT, heat);
        this.gunCooldown += 1200.0F / Math.max(60.0F, cfg.mgRoundsPerMinute);
        ServerLevel server = (ServerLevel) this.level();

        float rel = relYaw(shooter.getYHeadRot());
        float pitch = clampPitch(shooter.getXRot());
        Vec3 dir = Vec3.directionFromRotation(pitch, this.getYRot() + rel);
        // a gunner who is being suppressed himself can't hold it steady
        double spread = cfg.mgSpread * (1.0 + 2.5 * Suppression.level(shooter));
        dir = dir.add(this.random.nextGaussian() * spread, this.random.nextGaussian() * spread,
                this.random.nextGaussian() * spread).normalize();

        // rounds fly along the sight line (so they land on the sights); flash and tracers start at the muzzle
        Vec3 eye = gunPoint(this.position(), this.getYRot(), rel, pitch, 0.0, EYE_UP, -EYE_BACK);
        Vec3 rayStart = eye.add(dir.scale(1.2));
        Vec3 muzzle = gunPoint(this.position(), this.getYRot(), rel, pitch, 0.0, BORE_UP, MUZZLE_FWD);
        Vec3 end = eye.add(dir.scale(cfg.fieldMgRange));
        BlockHitResult blockHit = server.clip(new ClipContext(rayStart, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (blockHit.getType() != HitResult.Type.MISS) {
            end = blockHit.getLocation();
        }
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(server, this, rayStart, end, new AABB(rayStart, end).inflate(1.0),
                e -> e != this && !this.hasPassenger(e) && e.isPickable() && e.isAlive() && !e.isSpectator(), 0.3F);
        if (com.exodusvehicles.compat.AotCompat.active()) {
            double firstD = entityHit != null ? entityHit.getLocation().distanceTo(rayStart) : Double.MAX_VALUE;
            java.util.Optional<Entity> eyeT = com.exodusvehicles.compat.AotCompat.eyeAlong(server, rayStart, end,
                    entityHit != null ? entityHit.getEntity() : null, firstD);
            if (eyeT.isPresent()) {
                Entity e = eyeT.get();
                entityHit = new EntityHitResult(e, e.getBoundingBox().inflate(0.35).clip(rayStart, end).orElse(e.getBoundingBox().getCenter()));
            }
        }
        DamageSource src = this.damageSources().mobProjectile(this, shooter);
        if (entityHit != null) {
            Entity target = entityHit.getEntity();
            end = entityHit.getLocation();
            boolean blocked = target.getVehicle() instanceof FieldMgEntity other && other.shieldCovers(eye);
            if (blocked) {
                server.playSound(null, end.x, end.y, end.z, ArmoredCarMod.RICOCHET, SoundSource.PLAYERS, 0.8F,
                        0.9F + this.random.nextFloat() * 0.2F);
                server.sendParticles(ParticleTypes.CRIT, end.x, end.y, end.z, 4, 0.05, 0.05, 0.05, 0.2);
            } else if (com.exodusvehicles.compat.AotCompat.isEye(target)) {
                target.hurt(src, Math.max(cfg.mgDamage, 6.0F));
            } else {
                if (target instanceof LivingEntity living) {
                    living.invulnerableTime = 0;
                }
                target.hurt(src, cfg.mgDamage);
            }
        } else if (blockHit.getType() == HitResult.Type.BLOCK) {
            BlockParticleOption chunk = new BlockParticleOption(ParticleTypes.BLOCK, server.getBlockState(blockHit.getBlockPos()));
            Vec3 n = Vec3.atLowerCornerOf(blockHit.getDirection().getNormal());
            for (int i = 0; i < 7; i++) {
                server.sendParticles(chunk, end.x, end.y + 0.02, end.z, 0, n.x * 0.12 + (this.random.nextDouble() - 0.5) * 0.12,
                        0.12 + this.random.nextDouble() * 0.16, n.z * 0.12 + (this.random.nextDouble() - 0.5) * 0.12, 1.0);
            }
            server.sendParticles(ParticleTypes.POOF, end.x, end.y + 0.05, end.z, 0, 0.0, 0.02, 0.0, 1.0);
            server.playSound(null, end.x, end.y, end.z, ArmoredCarMod.IMPACT_GROUND, SoundSource.PLAYERS, 1.0F,
                    0.9F + this.random.nextFloat() * 0.2F);
        }

        int shotNo = this.shotCounter++;
        if (shotNo % 3 == 0) {
            server.sendParticles(ParticleTypes.WHITE_SMOKE, muzzle.x, muzzle.y + 0.05, muzzle.z, 0, dir.x * 0.02, 0.02, dir.z * 0.02, 1.0);
        }
        if (shotNo % 4 == 0) {
            TracerPayload tracer = new TracerPayload(this.getId(), muzzle.x, muzzle.y, muzzle.z, end.x, end.y, end.z);
            for (ServerPlayer viewer : net.fabricmc.fabric.api.networking.v1.PlayerLookup.tracking(this)) {
                net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(viewer, tracer);
            }
        }
        flybyAndSuppress(server, shooter, muzzle, end);
        playGunSound(server, muzzle, ArmoredCarMod.MGF_FIRE, ArmoredCarMod.MGF_FIRE_FAR, 0.97F + this.random.nextFloat() * 0.06F);
        this.lastShotTick = this.tickCount;
        this.burstActive = true;
        this.lastMuzzle = muzzle;
        server.broadcastEntityEvent(this, (byte) 61);
    }

    /** Open air: the gunner hears the plain close report (no armoured box around him), others close / far by distance. */
    private void playGunSound(ServerLevel server, Vec3 at, SoundEvent close, SoundEvent far, float pitch) {
        long seed = this.random.nextLong();
        var closeH = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(close);
        var farH = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(far);
        for (ServerPlayer p : server.players()) {
            double d = Math.sqrt(p.distanceToSqr(at));
            if (d > 200.0) {
                continue;
            }
            float farMix = this.hasPassenger(p) ? 0.0F : (float) Mth.clamp((d - 12.0) / 6.0, 0.0, 1.0);
            if (farMix < 1.0F) {
                p.connection.send(new ClientboundSoundPacket(closeH, SoundSource.PLAYERS, at.x, at.y, at.z, 1.0F - farMix, pitch, seed));
            }
            if (farMix > 0.0F) {
                p.connection.send(new ClientboundSoundPacket(farH, SoundSource.PLAYERS, at.x, at.y, at.z, 4.0F * farMix, pitch, seed));
            }
        }
    }

    /** Fly-by crack for anyone a round passes close to, and suppression for anyone it passes or lands near. */
    private void flybyAndSuppress(ServerLevel server, ServerPlayer shooter, Vec3 from, Vec3 to) {
        ModConfig cfg = ModConfig.get();
        Vec3 seg = to.subtract(from);
        double len2 = seg.lengthSqr();
        if (len2 < 1.0) {
            return;
        }
        double radius = cfg.suppressionRadius;
        for (ServerPlayer p : server.players()) {
            if (p == shooter || this.hasPassenger(p) || p.isSpectator()) {
                continue;
            }
            Vec3 eye = p.getEyePosition();
            double t = Mth.clamp(eye.subtract(from).dot(seg) / len2, 0.0, 1.0);
            if (t < 0.03) {
                continue;
            }
            Vec3 closest = from.add(seg.scale(t));
            double d = closest.distanceTo(eye);
            if (d > radius) {
                continue;
            }
            if (cfg.suppressionEnabled) {
                Suppression.add(p, (float) (cfg.suppressionPerRound * (0.35 + 0.65 * (1.0 - d / radius))));
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

    /** New belt from the gunner's ammunition (same as the Eisenwolf's MG). */
    public boolean startReload(ServerPlayer gunner) {
        ModConfig cfg = ModConfig.get();
        if (getGunner() != gunner || getReloadTicks() > 0 || getBelt() >= cfg.mgBeltSize) {
            return false;
        }
        int taken = TaczCompat.takeMgAmmo(gunner, cfg.mgBeltSize - getBelt());
        if (taken <= 0) {
            gunner.displayClientMessage(Component.translatable("msg.exodus_vehicles.no_ammo"), true);
            return false;
        }
        this.pendingRounds = taken;
        this.entityData.set(DATA_RELOAD, Math.max(1, cfg.mgReloadTicks));
        gunSound(ArmoredCarMod.RELOAD_OUT);
        return true;
    }

    private void gunSound(SoundEvent sound) {
        this.level().playSound(null, this.getX(), this.getY() + pivotUp(), this.getZ(), sound, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 61) {
            this.flashTicks = 2;
            this.flashStyle = this.random.nextInt(4);
            this.flashScale = 0.7F + this.random.nextFloat() * 0.4F;
            this.flashSpin = this.random.nextFloat() * Mth.TWO_PI;
            this.recoil = 1.0F;
        } else {
            super.handleEntityEvent(id);
        }
    }

    // ------------------------------------------------------------------ tick

    @Override
    public void tick() {
        super.tick();
        if (this.lerpSteps > 0) {
            double x = this.getX() + (this.lerpX - this.getX()) / this.lerpSteps;
            double y = this.getY() + (this.lerpY - this.getY()) / this.lerpSteps;
            double z = this.getZ() + (this.lerpZ - this.getZ()) / this.lerpSteps;
            float yr = this.getYRot() + Mth.wrapDegrees(this.lerpYRot - this.getYRot()) / this.lerpSteps;
            this.lerpSteps--;
            this.setPos(x, y, z);
            this.setYRot(yr);
        }
        if (this.level().isClientSide) {
            this.recoilO = this.recoil;
            this.recoil *= 0.6F;
            if (this.flashTicks > 0) {
                this.flashTicks--;
            }
            this.tracers.removeIf(t -> ++t.age > ArmoredCarEntity.TRACER_LIFE);
            return;
        }
        ModConfig cfg = ModConfig.get();
        Player op = getGunner();
        if (op != null) {
            // pushing the aim against the traverse stop drags the whole gun round after you
            float lim = traverseLimit();
            float want = Mth.wrapDegrees(op.getYHeadRot() - this.getYRot());
            if (Math.abs(want) > lim - 1.0F) {
                float step = Math.signum(want) * Math.min(cfg.fieldMgSwingSpeed, Math.abs(want) - (lim - 1.0F) + 1.0F);
                this.setYRot(this.getYRot() + step);
            }
            this.entityData.set(DATA_AIM_YAW, relYaw(op.getYHeadRot()));
            this.entityData.set(DATA_AIM_PITCH, clampPitch(op.getXRot()));
        }
        if (this.tickCount % 10 == 1) {
            this.entityData.set(DATA_STAND_DY, standHeight());
        }
        if (this.burstActive && this.tickCount - this.lastShotTick > 3) {
            this.burstActive = false;
            playGunSound((ServerLevel) this.level(), this.lastMuzzle, ArmoredCarMod.MG_TAIL, ArmoredCarMod.MG_TAIL_FAR, 1.0F);
        }
        if (this.gunCooldown > 0.0F) {
            this.gunCooldown = Math.max(0.0F, this.gunCooldown - 1.0F);
        }
        if (this.dryFireCooldown > 0) {
            this.dryFireCooldown--;
        }
        float heat = getHeat();
        if (heat > 0.0F) {
            heat = Math.max(0.0F, heat - cfg.mgCoolingPerTick);
            this.entityData.set(DATA_HEAT, heat);
        }
        if (isOverheated() && heat <= cfg.mgOverheatResume) {
            this.entityData.set(DATA_OVERHEATED, false);
        }
        int reload = getReloadTicks();
        if (reload > 0) {
            reload--;
            this.entityData.set(DATA_RELOAD, reload);
            if (reload == Math.min(28, cfg.mgReloadTicks / 2)) {
                gunSound(ArmoredCarMod.RELOAD_IN);
            }
            if (reload == 0) {
                this.entityData.set(DATA_BELT, Math.min(cfg.mgBeltSize, getBelt() + this.pendingRounds));
                this.pendingRounds = 0;
            }
        }
        // settle onto the ground
        Vec3 dm = this.getDeltaMovement();
        double vy = this.onGround() ? -0.04 : (dm.y - 0.08) * 0.98;
        this.setDeltaMovement(0, vy, 0);
        this.move(MoverType.SELF, this.getDeltaMovement());
    }

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
        this.lerpX = x;
        this.lerpY = y;
        this.lerpZ = z;
        this.lerpYRot = yRot;
        this.lerpSteps = 3;
    }
}
