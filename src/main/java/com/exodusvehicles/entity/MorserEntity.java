package com.exodusvehicles.entity;

import com.exodusvehicles.ArmoredCarMod;
import com.exodusvehicles.ModConfig;
import com.exodusvehicles.artillery.ShellManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * 21 cm Mörser 16, Battlefield-style: deploy it (a few seconds), get on, aim with the mouse (left/right = traverse,
 * up/down = range), left-click to fire. It reloads by itself. Shells land around the aim point with some scatter.
 * Packed up it can be pushed by its rider or towed by a Daimler-Marienfelde lorry; deployed it can't move.
 * Everything that matters (aim used for the shot, ammunition, reload, the shell's flight and damage) is decided here
 * on the server; the client only draws.
 */
public class MorserEntity extends Entity implements Repairable {
    public static final double TRUNNION_UP = 27.0 / 16.0, TRUNNION_FWD = 2.0 / 16.0, BARREL = 40.0 / 16.0;
    public static final double WHEEL_RADIUS = 12.0 / 16.0;
    /** Distance from the axle to the trail eye (hitch). */
    public static final double TRAIL = 3.8;
    /** Mouse pitch that aims at minimum range (looking down) and at maximum range (looking up). */
    public static final float PITCH_NEAR = 20.0F, PITCH_FAR = -30.0F;
    private static final float TRAVERSE_RATE = 3.0F;            // degrees per tick the barrel can swing
    private static final float TRAVEL_ELEV = 10.0F;

    private static final EntityDataAccessor<Float> DATA_HEALTH = def(EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_DEPLOYED = def(EntityDataSerializers.BOOLEAN);
    /** >0 deploying, <0 packing up (ticks done). */
    private static final EntityDataAccessor<Integer> DATA_WORK = def(EntityDataSerializers.INT);
    /** Ticks until the next round is ready (0 = ready). */
    private static final EntityDataAccessor<Integer> DATA_RELOAD = def(EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TOWED_BY = def(EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_TRAV = def(EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_RANGE = def(EntityDataSerializers.FLOAT);
    // server settings the client needs to aim and draw the same way
    private static final EntityDataAccessor<Float> DATA_MIN_RANGE = def(EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_MAX_RANGE = def(EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_TRAV_LIMIT = def(EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_RELOAD_TOTAL = def(EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_DISP_MIN = def(EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_DISP_MAX = def(EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_CEILING = def(EntityDataSerializers.FLOAT);

    private static <T> EntityDataAccessor<T> def(net.minecraft.network.syncher.EntityDataSerializer<T> s) {
        return SynchedEntityData.defineId(MorserEntity.class, s);
    }

    private int lerpSteps;
    private double lerpX, lerpY, lerpZ;
    private float lerpYRot;
    private int lastExplosionTick = -1;
    private float lastExplosionAmount;
    private int fireLock;
    @Nullable
    private java.util.UUID worker;

    // client visuals
    public float wheelRot, wheelRotO;
    public float recoil, recoilO;
    public float elevVis = TRAVEL_ELEV, elevVisO = TRAVEL_ELEV;

    public MorserEntity(EntityType<? extends MorserEntity> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder b) {
        ModConfig cfg = ModConfig.get();
        b.define(DATA_HEALTH, cfg.morserMaxHealth);
        b.define(DATA_DEPLOYED, false);
        b.define(DATA_WORK, 0);
        b.define(DATA_RELOAD, 0);
        b.define(DATA_TOWED_BY, -1);
        b.define(DATA_TRAV, 0.0F);
        b.define(DATA_RANGE, (float) cfg.artilleryMinRange);
        b.define(DATA_MIN_RANGE, (float) cfg.artilleryMinRange);
        b.define(DATA_MAX_RANGE, (float) cfg.artilleryMaxRange);
        b.define(DATA_TRAV_LIMIT, cfg.artilleryTraverse);
        b.define(DATA_RELOAD_TOTAL, cfg.artilleryReloadTicks);
        b.define(DATA_DISP_MIN, (float) cfg.artilleryDispersionMin);
        b.define(DATA_DISP_MAX, (float) cfg.artilleryDispersionMax);
        b.define(DATA_CEILING, (float) cfg.artilleryShellCeiling);
    }

    // ------------------------------------------------------------------ state

    public float getHealth() { return this.entityData.get(DATA_HEALTH); }
    public float getMaxHealth() { return ModConfig.get().morserMaxHealth; }

    @Override
    public boolean repairBy(float amount) {
        if (getHealth() >= getMaxHealth()) {
            return false;
        }
        this.entityData.set(DATA_HEALTH, Math.min(getMaxHealth(), getHealth() + amount));
        return true;
    }
    public boolean isDeployed() { return this.entityData.get(DATA_DEPLOYED); }
    public int getWork() { return this.entityData.get(DATA_WORK); }
    public int getReload() { return this.entityData.get(DATA_RELOAD); }
    public int getReloadTotal() { return Math.max(1, this.entityData.get(DATA_RELOAD_TOTAL)); }
    public boolean isTowed() { return this.entityData.get(DATA_TOWED_BY) >= 0; }
    public float getTraverse() { return this.entityData.get(DATA_TRAV); }
    public float getAimRange() { return this.entityData.get(DATA_RANGE); }
    public float minRange() { return this.entityData.get(DATA_MIN_RANGE); }
    public float maxRange() { return this.entityData.get(DATA_MAX_RANGE); }
    public float traverseLimit() { return this.entityData.get(DATA_TRAV_LIMIT); }
    public int deployTicks() { return Math.max(1, ModConfig.get().artilleryDeployTicks); }

    /** Scatter radius at a given range: grows from the minimum to the maximum across the gun's range band. */
    public float dispersionAt(float range) {
        float t = Mth.clamp((range - minRange()) / Math.max(1.0F, maxRange() - minRange()), 0.0F, 1.0F);
        return Mth.lerp(t, this.entityData.get(DATA_DISP_MIN), this.entityData.get(DATA_DISP_MAX));
    }

    @Nullable
    public Player getOperator() {
        return this.getFirstPassenger() instanceof Player p ? p : null;
    }

    public boolean isOperatedBy(Entity e) {
        return e != null && this.getFirstPassenger() == e && isDeployed();
    }

    // ------------------------------------------------------------------ aiming maths (shared by client and server)

    /** Range the gun is set to for a given mouse pitch (looking further up = further away). */
    public static float rangeForPitch(float pitch, float min, float max) {
        float t = Mth.clamp((pitch - PITCH_NEAR) / (PITCH_FAR - PITCH_NEAR), 0.0F, 1.0F);
        return Mth.lerp(t, min, max);
    }

    public static double muzzleSpeed(double maxRange) {
        return Math.sqrt(ShellManager.GRAVITY * maxRange);
    }

    /** High-angle elevation (degrees) to reach horizontal distance d with height difference dy, or NaN if out of reach. */
    public static double highAngle(double v, double d, double dy) {
        double g = ShellManager.GRAVITY;
        double disc = v * v * v * v - g * (g * d * d + 2 * dy * v * v);
        if (disc < 0 || d < 1.0E-3) {
            return Double.NaN;
        }
        return Math.toDegrees(Math.atan((v * v + Math.sqrt(disc)) / (g * d)));
    }

    /**
     * Barrel elevation for a range: lobbed steeply close in, flatter at full range, and never so steep that
     * the shell climbs above the ceiling (the clouds), where it would be lost from view. The propellant charge (muzzle
     * speed) is then chosen to reach the target at that elevation.
     */
    public static double arcElevation(double range, double min, double max, double gunY, double ceilingY) {
        // steep (70) close in, down to 25 at full range, so the barrel visibly rises and falls as the range changes
        double t = Mth.clamp((range - min) / Math.max(1.0, max - min), 0.0, 1.0);
        double a = 70.0 - 45.0 * Math.pow(t, 0.7);
        double room = Math.max(8.0, ceilingY - gunY);
        double aMax = Math.toDegrees(Math.atan(4.0 * room / Math.max(1.0, range)));    // apex ~ range * tan(a) / 4
        return Math.max(10.0, Math.min(a, aMax));
    }

    /** Muzzle speed that puts a shell fired at elevation a (degrees) onto a point d away and dy higher; NaN if none. */
    public static double speedFor(double a, double d, double dy) {
        double ar = Math.toRadians(a), c = Math.cos(ar);
        double denom = 2.0 * c * c * (d * Math.tan(ar) - dy);
        return denom <= 1.0E-6 ? Double.NaN : Math.sqrt(ShellManager.GRAVITY * d * d / denom);
    }

    public float ceiling() {
        return this.entityData.get(DATA_CEILING);
    }

    /** Barrel elevation shown for a range. */
    public float elevationForRange(float range) {
        return (float) arcElevation(range, minRange(), maxRange(), this.getY() + TRUNNION_UP, ceiling());
    }

    public Vec3 localToWorld(double right, double up, double forward) {
        double yaw = Math.toRadians(this.getYRot());
        double fx = -Math.sin(yaw), fz = Math.cos(yaw);
        double rx = -Math.cos(yaw), rz = -Math.sin(yaw);
        return new Vec3(this.getX() + fx * forward + rx * right, this.getY() + up, this.getZ() + fz * forward + rz * right);
    }

    public Vec3 trunnions() {
        return localToWorld(0.0, TRUNNION_UP, TRUNNION_FWD);
    }

    /** Where a shell set to this traverse and range comes down (ground height where known, otherwise the gun's). */
    public Vec3 aimPoint(float traverse, float range) {
        Vec3 g = trunnions();
        double yaw = Math.toRadians(this.getYRot() + traverse);
        double x = g.x - Math.sin(yaw) * range, z = g.z + Math.cos(yaw) * range;
        double y = this.getY();
        int bx = Mth.floor(x), bz = Mth.floor(z);
        if (this.level().hasChunk(bx >> 4, bz >> 4)) {
            y = this.level().getHeight(Heightmap.Types.MOTION_BLOCKING, bx, bz);
        }
        return new Vec3(x, y, z);
    }

    // ------------------------------------------------------------------ save

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Health")) {
            this.entityData.set(DATA_HEALTH, tag.getFloat("Health"));
        }
        this.entityData.set(DATA_DEPLOYED, tag.getBoolean("Deployed") || tag.getBoolean("Emplaced"));
        this.entityData.set(DATA_TRAV, tag.getFloat("Trav"));
        this.entityData.set(DATA_RELOAD, Math.max(0, tag.getInt("Reload")));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Health", getHealth());
        tag.putBoolean("Deployed", isDeployed());
        tag.putFloat("Trav", getTraverse());
        tag.putInt("Reload", getReload());
    }

    // ------------------------------------------------------------------ basics

    @Override
    public boolean canBeCollidedWith() {
        return !this.isRemoved() && !isTowed();     // the lorry mustn't snag on its own gun in a turn
    }

    @Override
    public boolean canCollideWith(Entity other) {
        return (other.canBeCollidedWith() || other.isPushable()) && !this.isPassengerOfSameVehicle(other)
                && !(other instanceof ArmoredCarEntity) && !(other instanceof HullSegmentEntity);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    public float maxUpStep() {
        return 1.05F;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(ArmoredCarMod.MORSER_ITEM);
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return this.getBoundingBox().inflate(4.5, 2.5, 4.5);
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().isEmpty() && !isTowed();
    }

    @Override
    public void positionRider(Entity passenger, Entity.MoveFunction move) {
        if (!this.hasPassenger(passenger)) {
            return;
        }
        // deployed: standing behind the left wheel with a hand on the breech; packed: behind the trail, pushing
        Vec3 p = isDeployed() ? localToWorld(GUNNER_RIGHT, 0.0, GUNNER_FWD) : localToWorld(0.0, 0.0, -TRAIL - 0.6);
        move.accept(passenger, p.x, p.y, p.z);
        if (passenger instanceof LivingEntity living) {
            living.setYBodyRot(this.getYRot());     // facing the way the gun points
        }
    }

    /** Where the gunner stands (blocks): left of the trail, just behind the wheel. */
    public static final double GUNNER_RIGHT = -11.0 / 16.0, GUNNER_FWD = -17.0 / 16.0;


    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        for (double side : new double[]{1.6, -1.6}) {
            for (double fwd : new double[]{-0.6, 0.6, -1.6}) {
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

    // ------------------------------------------------------------------ interaction

    /**
     * Right-click: get on (deployed: aim and fire; packed: push it around).
     * Sneak + right-click: deploy / pack up. Lead: hitch to / unhitch from a lorry. Repair: the wrench.
     */
    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean client = this.level().isClientSide;
        if (stack.getItem() instanceof com.exodusvehicles.item.VehicleToolItem) {
            return InteractionResult.PASS;      // repair wrench: held on the gun, see the item
        }
        if (stack.is(Items.LEAD)) {
            if (!client) {
                toggleTow(player);
            }
            return InteractionResult.sidedSuccess(client);
        }
        if (player.getVehicle() == this) {
            return InteractionResult.PASS;
        }
        if (player.isSecondaryUseActive()) {
            if (!client) {
                toggleDeploy(player);
            }
            return InteractionResult.sidedSuccess(client);
        }
        if (!client) {
            return player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }

    private void toggleTow(Player player) {
        if (isTowed()) {
            this.entityData.set(DATA_TOWED_BY, -1);
            this.playSound(SoundEvents.CHAIN_BREAK, 0.8F, 1.0F);
            player.displayClientMessage(Component.translatable("msg.exodus_vehicles.morser_unhitched"), true);
            return;
        }
        if (isDeployed() || getWork() != 0) {
            player.displayClientMessage(Component.translatable("msg.exodus_vehicles.morser_pack_first"), true);
            return;
        }
        TruckEntity best = null;
        double bestD = 10.0 * 10.0;
        for (TruckEntity t : this.level().getEntitiesOfClass(TruckEntity.class, this.getBoundingBox().inflate(10.0))) {
            double d = t.localToWorld(0.0, 0.0, -3.4).distanceToSqr(localToWorld(0.0, 0.0, -TRAIL));
            if (!t.isWrecked() && d < bestD) {
                best = t;
                bestD = d;
            }
        }
        if (best == null) {
            player.displayClientMessage(Component.translatable("msg.exodus_vehicles.morser_no_truck"), true);
            return;
        }
        this.ejectPassengers();
        this.entityData.set(DATA_TOWED_BY, best.getId());
        this.playSound(SoundEvents.CHAIN_PLACE, 1.0F, 0.8F);
        player.displayClientMessage(Component.translatable("msg.exodus_vehicles.morser_hitched"), true);
    }

    /** Sneak + right-click, or the engine key while on it: deploy (spade down, brakes on) or pack up. */
    public void toggleDeploy(Player player) {
        if (isTowed()) {
            player.displayClientMessage(Component.translatable("msg.exodus_vehicles.morser_unhitch_first"), true);
            return;
        }
        if (getWork() != 0) {
            return;     // already at it
        }
        this.entityData.set(DATA_WORK, isDeployed() ? -1 : 1);
        this.worker = player.getUUID();
        this.playSound(SoundEvents.CHAIN_PLACE, 1.0F, 0.6F);
    }

    // ------------------------------------------------------------------ firing

    /** Operator's trigger (attack key). */
    public void fire(ServerPlayer player) {
        if (!isOperatedBy(player) || getWork() != 0 || this.fireLock > 0) {
            return;
        }
        ModConfig cfg = ModConfig.get();
        if (getReload() > 0) {
            return;
        }
        if (cfg.artilleryRequiresShells && !player.getAbilities().instabuild) {
            int slot = -1;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                if (player.getInventory().getItem(i).is(ArmoredCarMod.SHELL_ITEM)) {
                    slot = i;
                    break;
                }
            }
            if (slot < 0) {
                player.displayClientMessage(Component.translatable("msg.exodus_vehicles.morser_no_shell"), true);
                this.fireLock = 10;
                return;
            }
            player.getInventory().getItem(slot).shrink(1);
        }
        ServerLevel server = (ServerLevel) this.level();
        this.fireLock = 10;
        this.entityData.set(DATA_RELOAD, Math.max(1, cfg.artilleryReloadTicks));
        this.entityData.set(DATA_RELOAD_TOTAL, Math.max(1, cfg.artilleryReloadTicks));

        // where the barrel is laid (server state), then scatter around it, then the exact shot to that point
        float range = getAimRange();
        Vec3 aim = aimPoint(getTraverse(), range);
        double[] off = ShellManager.disc(this.random, dispersionAt(range));
        Vec3 target = aim.add(off[0], 0, off[1]);
        int bx = Mth.floor(target.x), bz = Mth.floor(target.z);
        if (server.hasChunk(bx >> 4, bz >> 4)) {
            target = new Vec3(target.x, server.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, bx, bz), target.z);
        }
        Vec3 g = trunnions();
        double dx = target.x - g.x, dz = target.z - g.z;
        double d = Math.sqrt(dx * dx + dz * dz);
        double dy = target.y - g.y;
        double elev = arcElevation(d, minRange(), maxRange(), g.y, ceiling());
        double v = speedFor(elev, d, dy);
        while (Double.isNaN(v) && elev < 70.0) {
            elev += 5.0;            // steeply uphill: lob it higher
            v = speedFor(elev, d, dy);
        }
        if (Double.isNaN(v)) {
            v = speedFor(45.0, d, 0.0);
            elev = 45.0;
        }
        double e = Math.toRadians(elev);
        Vec3 vel = new Vec3(dx / d * Math.cos(e) * v, Math.sin(e) * v, dz / d * Math.cos(e) * v);
        ShellManager.fire(server, g, vel, player.getUUID(), target.y, this);

        // muzzle: flash, a modest smoke cloud, the report (close / distant / far, arriving late with distance)
        Vec3 dir = vel.normalize();
        Vec3 muzzle = g.add(dir.scale(BARREL));
        server.sendParticles(ParticleTypes.FLASH, muzzle.x, muzzle.y, muzzle.z, 1, 0, 0, 0, 0);
        server.sendParticles(ParticleTypes.EXPLOSION, muzzle.x, muzzle.y, muzzle.z, 1, 0.2, 0.2, 0.2, 0.0);
        server.sendParticles(ParticleTypes.FLAME, muzzle.x, muzzle.y, muzzle.z, 8, 0.2, 0.2, 0.2, 0.08);
        for (ServerPlayer p : server.players()) {
            double dist = Math.sqrt(p.distanceToSqr(muzzle));
            if (dist < 256) {
                server.sendParticles(p, ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, true, muzzle.x, muzzle.y, muzzle.z, 3, 0.6, 0.4, 0.6, 0.02);
                server.sendParticles(p, ParticleTypes.LARGE_SMOKE, true, muzzle.x, muzzle.y, muzzle.z, 10, 0.8, 0.5, 0.8, 0.05);
            }
            if (this.hasPassenger(p) || dist < 40) {
                p.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(
                        net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ArmoredCarMod.MORSER_CLOSE),
                        SoundSource.PLAYERS, muzzle.x, muzzle.y, muzzle.z, 1.0F, 0.95F + this.random.nextFloat() * 0.1F,
                        this.random.nextLong()));
            } else if (dist < 180) {
                ShellManager.soundTo(p, ArmoredCarMod.MORSER_DISTANT, muzzle, (float) (1.0 - (dist - 40) / 140 * 0.4), 1.0F, true);
            } else if (dist < 1000) {
                ShellManager.soundTo(p, ArmoredCarMod.MORSER_FAR, muzzle, (float) Math.max(0.2, 1.0 - (dist - 180) / 820), 1.0F, true);
            }
        }
        server.broadcastEntityEvent(this, (byte) 62);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 62) {
            this.recoil = 1.0F;
        } else {
            super.handleEntityEvent(id);
        }
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
            dealt = amount >= cfg.penetrationThreshold ? amount : 0.0F;     // rifle fire just rings off the steel
            Vec3 at = this.position().add(0, 1.4, 0);
            this.level().playSound(null, at.x, at.y, at.z, ArmoredCarMod.ARMOR_HIT, SoundSource.PLAYERS, 0.7F,
                    0.9F + this.random.nextFloat() * 0.2F);
        }
        if (dealt <= 0.0F) {
            return false;
        }
        this.entityData.set(DATA_HEALTH, getHealth() - dealt);
        this.markHurt();
        if (getHealth() <= 0.0F) {
            this.ejectPassengers();
            if (this.level() instanceof ServerLevel server) {
                server.explode(this, this.getX(), this.getY() + 1.2, this.getZ(), 2.5F, Level.ExplosionInteraction.NONE);
                server.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.5, this.getZ(), 30, 1.5, 1.0, 1.5, 0.05);
            }
            this.discard();
        }
        return true;
    }

    // ------------------------------------------------------------------ tick

    @Override
    public void tick() {
        super.tick();
        tickLerp();
        if (!this.level().isClientSide) {
            tickServer();
        } else {
            tickClient();
        }
    }

    private void tickServer() {
        ModConfig cfg = ModConfig.get();
        if (this.fireLock > 0) {
            this.fireLock--;
        }
        if (this.tickCount % 20 == 0) {
            // settings the clients aim with (picks up config changes)
            this.entityData.set(DATA_MIN_RANGE, (float) cfg.artilleryMinRange);
            this.entityData.set(DATA_MAX_RANGE, (float) Math.max(cfg.artilleryMinRange + 10, cfg.artilleryMaxRange));
            this.entityData.set(DATA_TRAV_LIMIT, cfg.artilleryTraverse);
            this.entityData.set(DATA_DISP_MIN, (float) cfg.artilleryDispersionMin);
            this.entityData.set(DATA_DISP_MAX, (float) cfg.artilleryDispersionMax);
            this.entityData.set(DATA_CEILING, (float) cfg.artilleryShellCeiling);
        }

        // deploying / packing up
        int work = getWork();
        if (work != 0) {
            int total = deployTicks();
            work += work > 0 ? 1 : -1;
            if (Math.abs(work) >= total) {
                boolean deploy = work > 0;
                this.entityData.set(DATA_DEPLOYED, deploy);
                this.playSound(deploy ? SoundEvents.ANVIL_PLACE : SoundEvents.CHAIN_BREAK, 0.6F, deploy ? 0.5F : 0.8F);
                work = 0;
                if (deploy) {
                    this.entityData.set(DATA_TRAV, 0.0F);
                }
                for (Entity p : this.getPassengers()) {
                    this.positionRider(p);
                }
            } else if (Math.abs(work) % 10 == 0) {
                this.playSound(SoundEvents.CHAIN_STEP, 0.8F, 0.7F + this.random.nextFloat() * 0.3F);
            }
            this.entityData.set(DATA_WORK, work);
            Player w = this.worker == null ? null : this.level().getPlayerByUUID(this.worker);
            if (w != null && w.distanceToSqr(this) < 16 * 16) {
                int pct = Math.min(100, Math.abs(work) * 100 / total);
                w.displayClientMessage(work == 0 ? Component.translatable(isDeployed()
                        ? "msg.exodus_vehicles.morser_deployed" : "msg.exodus_vehicles.morser_packed")
                        : Component.translatable(getWork() > 0 ? "hud.exodus_vehicles.morser_deploying"
                        : "hud.exodus_vehicles.morser_packing", pct), true);
            }
        }

        // automatic reload, with the breech opening and closing around it
        int reload = getReload();
        if (reload > 0) {
            this.entityData.set(DATA_RELOAD, --reload);
            int total = getReloadTotal();
            // the breech-open sound's clank (3.7 s in) lands as the breech swings open (37% through the reload)
            int openAt = Math.max(1, Math.round(total * 0.365F) - 73);
            if (total - reload == openAt) {
                breechSound(ArmoredCarMod.BREECH_OPEN);
            }
            if (reload == 36) {
                breechSound(ArmoredCarMod.BREECH_CLOSE);     // its clank lands as the round is ready
            }
        }

        // aim follows the operator's view: left/right = traverse (limited), up/down = range
        Player op = getOperator();
        if (isDeployed() && getWork() == 0 && op != null) {
            float lim = traverseLimit();
            float want = Mth.clamp(Mth.wrapDegrees(op.getYRot() - this.getYRot()), -lim, lim);
            float t = getTraverse();
            this.entityData.set(DATA_TRAV, t + Mth.clamp(want - t, -TRAVERSE_RATE, TRAVERSE_RATE));
            this.entityData.set(DATA_RANGE, rangeForPitch(op.getXRot(), minRange(), maxRange()));
        } else if (!isDeployed() && getTraverse() != 0.0F) {
            float t = getTraverse();
            this.entityData.set(DATA_TRAV, t + Mth.clamp(-t, -TRAVERSE_RATE, TRAVERSE_RATE));
        }

        // movement: towed, pushed by its rider, or nothing (deployed)
        if (isTowed()) {
            tickTow();
        } else {
            double fwd = 0, turn = 0;
            if (!isDeployed() && getWork() == 0 && op != null) {
                fwd = op.zza * 0.05;            // manhandling: walking pace at best
                turn = op.xxa * 1.2;
            }
            if (turn != 0) {
                this.setYRot(this.getYRot() - (float) turn);
            }
            double yaw = Math.toRadians(this.getYRot());
            Vec3 dm = this.getDeltaMovement();
            double vy = this.onGround() ? -0.04 : (dm.y - 0.08) * 0.98;
            this.setDeltaMovement(-Math.sin(yaw) * fwd, vy, Math.cos(yaw) * fwd);
            this.move(MoverType.SELF, this.getDeltaMovement());
        }
    }

    private void breechSound(net.minecraft.sounds.SoundEvent sound) {
        Vec3 b = localToWorld(0.0, 1.2, -0.8);
        this.level().playSound(null, b.x, b.y, b.z, sound, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private void tickTow() {
        Entity e = this.level().getEntity(this.entityData.get(DATA_TOWED_BY));
        if (!(e instanceof TruckEntity truck) || truck.isRemoved() || truck.isWrecked()) {
            this.entityData.set(DATA_TOWED_BY, -1);
            return;
        }
        Vec3 hitch = truck.localToWorld(0.0, 0.0, -3.4);
        Vec3 pos = this.position();
        Vec3 away = new Vec3(pos.x - hitch.x, 0, pos.z - hitch.z);
        if (away.lengthSqr() < 1.0E-4) {
            double ty = Math.toRadians(truck.getYRot());
            away = new Vec3(Math.sin(ty), 0, -Math.cos(ty));
        }
        away = away.normalize();
        Vec3 want = hitch.add(away.scale(TRAIL));
        Vec3 dm = this.getDeltaMovement();
        double vy = this.onGround() ? -0.04 : (dm.y - 0.08) * 0.98;
        this.move(MoverType.SELF, new Vec3(want.x - pos.x, vy, want.z - pos.z));
        this.setYRot((float) Math.toDegrees(Math.atan2(-away.x, away.z)));      // barrel away from the lorry
        if (this.position().distanceTo(hitch) > TRAIL + 6.0) {
            this.entityData.set(DATA_TOWED_BY, -1);      // snagged: the tow comes loose
            this.playSound(SoundEvents.CHAIN_BREAK, 1.0F, 0.8F);
        }
    }

    private void tickClient() {
        double dx = this.getX() - this.xo, dz = this.getZ() - this.zo;
        double yaw = Math.toRadians(this.getYRot());
        double forward = dx * -Math.sin(yaw) + dz * Math.cos(yaw);
        this.wheelRotO = this.wheelRot;
        this.wheelRot += (float) (forward / WHEEL_RADIUS);
        this.recoilO = this.recoil;
        this.recoil *= 0.85F;
        this.elevVisO = this.elevVis;
        float want = isDeployed() && getWork() == 0 ? elevationForRange(getAimRange()) : TRAVEL_ELEV;
        this.elevVis += Mth.clamp(want - this.elevVis, -2.0F, 2.0F);
        if (getWork() != 0 && this.random.nextInt(4) == 0) {
            Vec3 p = localToWorld((this.random.nextDouble() - 0.5) * 2, 0.1, -3.5 + this.random.nextDouble());
            this.level().addParticle(ParticleTypes.POOF, p.x, p.y, p.z, 0, 0.03, 0);
        }
    }

    private void tickLerp() {
        if (this.lerpSteps > 0) {
            double x = this.getX() + (this.lerpX - this.getX()) / this.lerpSteps;
            double y = this.getY() + (this.lerpY - this.getY()) / this.lerpSteps;
            double z = this.getZ() + (this.lerpZ - this.getZ()) / this.lerpSteps;
            float yr = this.getYRot() + Mth.wrapDegrees(this.lerpYRot - this.getYRot()) / this.lerpSteps;
            this.lerpSteps--;
            this.setPos(x, y, z);
            this.setYRot(yr);
        }
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
