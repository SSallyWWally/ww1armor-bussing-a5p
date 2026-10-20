package com.exodusvehicles.entity;

import com.exodusvehicles.ArmoredCarMod;
import com.exodusvehicles.ModConfig;
import com.exodusvehicles.compat.TaczCompat;
import com.exodusvehicles.network.TracerPayload;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * M.15 Eisenwolf armoured car (1 block = 1 m; 8 m long, 3.8 m tall, two driven axles, four wheels).
 * Seat 0 = driver, 1 = cupola gunner (MG 08), 2 + 3 = passengers in the casemate.
 *
 * Driving is simulated on the driver's client (like vanilla boats) and sent to the server with the
 * vanilla vehicle-move packet; everything else (damage, seats, MG, wreck) is server-authoritative.
 */
public class ArmoredCarEntity extends Entity implements Repairable {
    /** Number of seats (Eisenwolf: 4). */
    public int seatCount() {
        return 4;
    }

    /** A seat that can't be used right now (e.g. cargo stacked on it). */
    public boolean isSeatBlocked(int seat) {
        return false;
    }
    public static final int DRIVER = 0;
    public static final int GUNNER = 1;

    /** Seat feet positions in blocks: {right, up, forward}. */
    private static final double[][] SEAT_OFFSETS = {
            {0.00, 1.19, 1.10},    // driver in the front step, eyes at the front vision slit
            {0.00, 1.82, -0.72},   // gunner, head in the turret at the port level
            {0.45, 1.19, -1.85},   // passenger, fighting compartment (side vision ports)
            {-0.45, 1.19, -1.85},  // passenger
    };
    /** Turret centre / gun pivot (blocks). Matches the model: turret at y 50 px, z +11.5 px; gun +5 px. */
    public static final double GUN_PIVOT_UP = 55.0 / 16.0;
    public static final double GUN_PIVOT_FWD = -11.5 / 16.0;
    public static final double MUZZLE_DIST = 29.3 / 16.0;
    /** How far the barrel sits below the gunner's sight line (blocks). */
    public static final double BARREL_BELOW_SIGHT = 5.5 / 16.0;
    /** In the gunner's own view the whole gun sits just under the sight; its muzzle is this far ahead of the eye. */
    public static final double GUNNER_MUZZLE_DIST = 20.0 / 16.0;    // -4 + 16 * 1.5 px
    /** ...and this far to the gunner's right. */
    public static final double GUNNER_MUZZLE_RIGHT = 1.5 / 16.0;
    /** Tracer flight speed (blocks per tick) and lifetime (ticks). */
    public static final double TRACER_SPEED = 12.0;
    public static final int TRACER_LIFE = 12;
    public static final double WHEEL_RADIUS = 12.5 / 16.0;
    /** Front to rear axle (blocks). */
    public static final double WHEELBASE = 70.0 / 16.0;
    /** Forward offsets of the two extra hitboxes (blocks): nose and tail of the 8 m hull. */
    public static final float[] SEGMENT_OFFSETS = {2.6F, -2.75F};

    private static final EntityDataAccessor<Float> DATA_HEALTH =
            SynchedEntityData.defineId(ArmoredCarEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_WRECKED =
            SynchedEntityData.defineId(ArmoredCarEntity.class, EntityDataSerializers.BOOLEAN);
    /** Entity id in each seat (-1 = empty), as an int array "s"; one tag so vehicles can have any number of seats. */
    private static final EntityDataAccessor<CompoundTag> DATA_SEATS =
            SynchedEntityData.defineId(ArmoredCarEntity.class, EntityDataSerializers.COMPOUND_TAG);
    public static final int MAX_SEATS = 16;

    // MG 08 state, synced so the gunner's HUD can show it
    private static final EntityDataAccessor<Integer> DATA_BELT =
            SynchedEntityData.defineId(ArmoredCarEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_HEAT =
            SynchedEntityData.defineId(ArmoredCarEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_RELOAD =
            SynchedEntityData.defineId(ArmoredCarEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_OVERHEATED =
            SynchedEntityData.defineId(ArmoredCarEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_ENGINE =
            SynchedEntityData.defineId(ArmoredCarEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_FUEL =
            SynchedEntityData.defineId(ArmoredCarEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_BRAKE =
            SynchedEntityData.defineId(ArmoredCarEntity.class, EntityDataSerializers.BOOLEAN);

    // driving (simulated on the driver's client)
    private float speed;
    private float steer;               // degrees, + = left
    private float yawVel;              // smoothed yaw rate, degrees per tick
    private float pendingRiderYaw;

    // interpolation for non-controlled instances
    private int lerpSteps;
    private double lerpX, lerpY, lerpZ;
    private float lerpYRot;

    // server state
    private float gunCooldown;
    private int pendingRounds;
    private int dryFireCooldown;
    private int lastShotTick = -100;
    private boolean burstActive;
    private Vec3 lastMuzzle = Vec3.ZERO;
    private int stillTicks;
    private double lastMoved;
    private int rampCooldown;
    private Vec3 lastServerPos;
    private int noDriverTicks;
    /** Client: tracers in flight from this car's MG. */
    public final java.util.List<Tracer> tracers = new java.util.ArrayList<>();

    /** A tracer round in flight (client side, visual only). */
    public static final class Tracer {
        public final Vec3 start, end;
        public int age;
        public Tracer(Vec3 start, Vec3 end) {
            this.start = start;
            this.end = end;
        }
    }
    private int wreckTicks;
    private int shotCounter;
    private int lastExplosionTick = -1;
    private float lastExplosionAmount;
    private final HullSegmentEntity[] segments = new HullSegmentEntity[segmentOffsets().length];

    // client visuals
    public float wheelRot, wheelRotO;
    public float steerVis, steerVisO;
    public float turretYaw, turretYawO, gunPitch, gunPitchO;
    public float recoil, recoilO;
    public int flashTicks;
    /** Muzzle flash look for the current round: sprite style (0-3), size and in-plane spin. */
    public int flashStyle;
    public float flashScale = 1.0F, flashSpin;
    public float clientSpeed;
    private int clientWreckAge;

    public ArmoredCarEntity(EntityType<? extends ArmoredCarEntity> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
    }

    // ------------------------------------------------------------------ vehicle spec (overridden by other vehicles)

    /** Seat feet positions in blocks: {right, up, forward}. */
    protected double[][] seatOffsets() {
        return SEAT_OFFSETS;
    }

    /** Whether seat 1 mans an MG. */
    public boolean hasGun() {
        return true;
    }

    /** Armoured: small-arms fire below the penetration threshold bounces off, and the crew are protected. */
    public boolean isArmored() {
        return true;
    }

    protected float[] segmentOffsets() {
        return SEGMENT_OFFSETS;
    }

    protected EntityType<? extends HullSegmentEntity> segmentType() {
        return ArmoredCarMod.HULL_SEGMENT;
    }

    public double wheelbase() {
        return WHEELBASE;
    }

    public double wheelRadius() {
        return WHEEL_RADIUS;
    }

    protected float maxSpeed() {
        return ModConfig.get().maxSpeed;
    }

    protected float maxReverseSpeed() {
        return ModConfig.get().maxReverseSpeed;
    }

    protected float acceleration() {
        return ModConfig.get().acceleration;
    }

    public float maxSteerAngle() {
        return ModConfig.get().maxSteerAngle;
    }

    protected net.minecraft.world.item.Item vehicleItem() {
        return ArmoredCarMod.EISENWOLF_ITEM;
    }

    // ------------------------------------------------------------------ fuel

    /** Tank size in litres. */
    public float fuelCapacity() {
        return ModConfig.get().fuelCapacity;
    }

    /** Litres per minute at full speed (idling uses fuelIdlePerMinute). */
    protected float fuelPerMinuteDriving() {
        return ModConfig.get().fuelPerMinuteDriving;
    }

    /** Blocks moved last server tick (the driver's client moves the car, so this is how the server sees it). */
    public double recentMovement() {
        return this.lastMoved;
    }

    public float getFuel() {
        return this.entityData.get(DATA_FUEL);
    }

    public void addFuel(float litres) {
        this.entityData.set(DATA_FUEL, Mth.clamp(getFuel() + litres, 0.0F, fuelCapacity()));
    }

    @Override
    public boolean repairBy(float amount) {
        if (isWrecked() || getHealth() >= getMaxHealth()) {
            return false;
        }
        setHealth(getHealth() + amount);
        return true;
    }

    /** Name of a seat, for the HUD and seat messages. */
    public Component seatName(int seat) {
        return Component.translatable("seat.exodus_vehicles." + seat);
    }

    /** HUD label for the health bar ("Hull 300 / 300"). */
    public String healthHudKey() {
        return "hud.exodus_vehicles.hull";
    }

    /** Exhaust outlet {right, up, forward} in blocks. */
    protected double[] exhaustOffset() {
        return new double[]{-0.6, 0.8, -3.6};
    }

    /** Where a badly damaged engine smokes {right, up, forward}. */
    protected double[] engineOffset() {
        return new double[]{0.0, 2.6, 2.4};
    }

    // ------------------------------------------------------------------ data

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_HEALTH, getMaxHealth());
        builder.define(DATA_WRECKED, false);
        CompoundTag seats = new CompoundTag();
        int[] empty = new int[MAX_SEATS];
        java.util.Arrays.fill(empty, -1);
        seats.putIntArray("s", empty);
        builder.define(DATA_SEATS, seats);
        builder.define(DATA_BELT, ModConfig.get().mgBeltSize);
        builder.define(DATA_HEAT, 0.0F);
        builder.define(DATA_RELOAD, 0);
        builder.define(DATA_OVERHEATED, false);
        builder.define(DATA_ENGINE, false);
        builder.define(DATA_BRAKE, false);
        builder.define(DATA_FUEL, fuelCapacity());
    }

    public int getBelt() {
        return this.entityData.get(DATA_BELT);
    }

    public float getHeat() {
        return this.entityData.get(DATA_HEAT);
    }

    public int getReloadTicks() {
        return this.entityData.get(DATA_RELOAD);
    }

    public boolean isOverheated() {
        return this.entityData.get(DATA_OVERHEATED);
    }

    public boolean isEngineOn() {
        return this.entityData.get(DATA_ENGINE) && !isWrecked();
    }

    public boolean isParkingBrakeOn() {
        return this.entityData.get(DATA_BRAKE);
    }

    /** Parking brake key: driver only. Holds the car completely still until released. */
    public void toggleParkingBrake(ServerPlayer player) {
        if (isWrecked() || seatOf(player) != DRIVER) {
            return;
        }
        boolean on = !isParkingBrakeOn();
        this.entityData.set(DATA_BRAKE, on);
        this.playSound(on ? SoundEvents.IRON_TRAPDOOR_CLOSE : SoundEvents.IRON_TRAPDOOR_OPEN, 0.7F, on ? 0.7F : 0.9F);
    }

    /** Engine start/stop key: only the driver can work the engine. */
    public void toggleEngine(ServerPlayer player) {
        if (isWrecked() || seatOf(player) != DRIVER) {
            return;
        }
        boolean on = !this.entityData.get(DATA_ENGINE);
        if (on && getFuel() <= 0.0F) {
            player.displayClientMessage(Component.translatable("msg.exodus_vehicles.no_fuel"), true);
            this.playSound(SoundEvents.DISPENSER_FAIL, 0.6F, 0.6F);
            return;
        }
        this.entityData.set(DATA_ENGINE, on);
        this.noDriverTicks = 0;
        engineSound(on ? ArmoredCarMod.ENGINE_START : ArmoredCarMod.ENGINE_STOP);
    }

    public float getHealth() {
        return this.entityData.get(DATA_HEALTH);
    }

    public void setHealth(float health) {
        this.entityData.set(DATA_HEALTH, Mth.clamp(health, 0.0F, getMaxHealth()));
    }

    public float getMaxHealth() {
        return ModConfig.get().maxHealth;
    }

    public boolean isWrecked() {
        return this.entityData.get(DATA_WRECKED);
    }

    public boolean hasDriver() {
        return getControllingPassenger() != null;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Health")) {
            setHealth(tag.getFloat("Health"));
        }
        this.entityData.set(DATA_WRECKED, tag.getBoolean("Wrecked"));
        this.wreckTicks = tag.getInt("WreckTicks");
        if (tag.contains("Belt")) {
            this.entityData.set(DATA_BELT, Mth.clamp(tag.getInt("Belt"), 0, ModConfig.get().mgBeltSize));
        }
        this.entityData.set(DATA_HEAT, tag.getFloat("Heat"));
        this.entityData.set(DATA_OVERHEATED, tag.getBoolean("Overheated"));
        this.entityData.set(DATA_ENGINE, tag.getBoolean("Engine"));
        this.entityData.set(DATA_BRAKE, tag.getBoolean("ParkingBrake"));
        this.entityData.set(DATA_FUEL, tag.contains("Fuel") ? Mth.clamp(tag.getFloat("Fuel"), 0.0F, fuelCapacity()) : fuelCapacity());
        // a reload interrupted by a save just completes on load
        int pending = tag.getInt("PendingRounds");
        if (pending > 0) {
            this.entityData.set(DATA_BELT, Math.min(ModConfig.get().mgBeltSize, getBelt() + pending));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Health", getHealth());
        tag.putBoolean("Wrecked", isWrecked());
        tag.putInt("WreckTicks", this.wreckTicks);
        tag.putInt("Belt", getBelt());
        tag.putFloat("Heat", getHeat());
        tag.putBoolean("Overheated", isOverheated());
        tag.putBoolean("Engine", this.entityData.get(DATA_ENGINE));
        tag.putBoolean("ParkingBrake", isParkingBrakeOn());
        tag.putFloat("Fuel", getFuel());
        tag.putInt("PendingRounds", getReloadTicks() > 0 ? this.pendingRounds : 0);
    }

    // ------------------------------------------------------------------ physics basics

    @Override
    public boolean canBeCollidedWith() {
        return !this.isRemoved();
    }

    @Override
    public boolean canCollideWith(Entity other) {
        if (other instanceof HullSegmentEntity seg && seg.belongsTo(this)) {
            return false;
        }
        return (other.canBeCollidedWith() || other.isPushable()) && !this.isPassengerOfSameVehicle(other);
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
    protected void playStepSound(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
    }

    @Override
    public float maxUpStep() {
        return 1.05F;
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        AABB box = this.getBoundingBox().inflate(4.0, 0.5, 4.0);
        for (Tracer t : this.tracers) {      // keep drawing while tracers fly off-screen from the car
            box = box.minmax(new AABB(t.start, t.end));
        }
        return box;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(vehicleItem());
    }

    /** Converts a car-relative offset (blocks) into a world position. */
    public Vec3 localToWorld(double right, double up, double forward) {
        return localToWorld(this.getX(), this.getY(), this.getZ(), this.getYRot(), right, up, forward);
    }

    private static Vec3 localToWorld(double x, double y, double z, float yawDeg, double right, double up, double forward) {
        double yaw = Math.toRadians(yawDeg);
        double fx = -Math.sin(yaw), fz = Math.cos(yaw);
        double rx = -Math.cos(yaw), rz = -Math.sin(yaw);
        return new Vec3(x + fx * forward + rx * right, y + up, z + fz * forward + rz * right);
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        super.remove(reason);
        for (HullSegmentEntity s : this.segments) {
            if (s != null && !s.isRemoved()) {
                s.discard();
            }
        }
    }

    private void tickSegments() {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        for (int i = 0; i < this.segments.length; i++) {
            HullSegmentEntity s = this.segments[i];
            if (s == null || s.isRemoved()) {
                s = segmentType().create(server);
                if (s == null) {
                    continue;
                }
                s.attach(this, segmentOffsets()[i]);
                s.snapTo(this);
                server.addFreshEntity(s);
                this.segments[i] = s;
            } else {
                s.snapTo(this);
            }
        }
    }

    /**
     * The 9.5 m hull is longer than the main hitbox, so the ends are tested separately against blocks:
     * a box above step height at each end must stay clear for the car to move or turn.
     */
    private boolean endsClear(double x, double z, float yaw) {
        for (float off : segmentOffsets()) {
            Vec3 c = localToWorld(x, this.getY(), z, yaw, 0.0, 0.0, off + Math.signum(off) * 0.6);
            AABB box = new AABB(c.x - 0.95, this.getY() + 1.1, c.z - 0.95, c.x + 0.95, this.getY() + 2.4, c.z + 0.95);
            if (!this.level().noCollision(this, box)) {
                return false;
            }
        }
        return true;
    }

    // ------------------------------------------------------------------ seats

    public int seatEntityId(int seat) {
        int[] ids = this.entityData.get(DATA_SEATS).getIntArray("s");
        return seat >= 0 && seat < ids.length ? ids[seat] : -1;
    }

    protected void setSeat(int seat, int entityId) {
        int[] ids = this.entityData.get(DATA_SEATS).getIntArray("s").clone();
        if (seat < 0 || seat >= ids.length) {
            return;
        }
        ids[seat] = entityId;
        CompoundTag t = new CompoundTag();
        t.putIntArray("s", ids);
        this.entityData.set(DATA_SEATS, t);
    }

    public int seatOf(Entity entity) {
        for (int i = 0; i < seatCount(); i++) {
            if (seatEntityId(i) == entity.getId() && this.hasPassenger(entity)) {
                return i;
            }
        }
        return -1;
    }

    @Nullable
    public Entity entityInSeat(int seat) {
        int id = seatEntityId(seat);
        if (id < 0) {
            return null;
        }
        for (Entity p : this.getPassengers()) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }

    protected int firstFreeSeat() {
        for (int i = 0; i < seatCount(); i++) {
            if (entityInSeat(i) == null && !isSeatBlocked(i)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return !isWrecked() && firstFreeSeat() >= 0;
    }

    @Override
    public void addPassenger(Entity passenger) {
        super.addPassenger(passenger);
        if (!this.level().isClientSide && seatOf(passenger) < 0) {
            int free = firstFreeSeat();
            if (free >= 0) {
                setSeat(free, passenger.getId());
                announceSeat(passenger, free);
            }
        }
    }

    private void engineSound(net.minecraft.sounds.SoundEvent sound) {
        if (!this.level().isClientSide && !isWrecked()) {
            // same sound category as the gun, so one volume slider ("Players") covers the whole vehicle
            this.level().playSound(null, this.getX(), this.getY() + 1.0, this.getZ(), sound, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    @Override
    public void removePassenger(Entity passenger) {
        int seat = seatOf(passenger);
        super.removePassenger(passenger);
        if (!this.level().isClientSide && seat >= 0) {
            setSeat(seat, -1);
        }
    }

    /** Called from the switch-seat keybind packet. Cycles to the next free seat. */
    public void switchSeat(ServerPlayer player) {
        int current = seatOf(player);
        if (current < 0 || isWrecked()) {
            return;
        }
        for (int i = 1; i < seatCount(); i++) {
            int s = (current + i) % seatCount();
            if (entityInSeat(s) == null && !isSeatBlocked(s)) {
                setSeat(current, -1);
                setSeat(s, player.getId());
                announceSeat(player, s);
                this.positionRider(player);
                return;
            }
        }
        player.displayClientMessage(Component.translatable("msg.exodus_vehicles.no_free_seat"), true);
    }

    protected void announceSeat(Entity passenger, int seat) {
        if (passenger instanceof Player p) {
            p.displayClientMessage(Component.translatable("msg.exodus_vehicles.seat",
                    seatName(seat)), true);
        }
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        if (isWrecked()) {
            return null;
        }
        Entity driver = entityInSeat(DRIVER);
        return driver instanceof Player p ? p : null;
    }

    @Override
    public void positionRider(Entity passenger, Entity.MoveFunction moveFunction) {
        if (!this.hasPassenger(passenger)) {
            return;
        }
        int seat = seatOf(passenger);
        if (seat < 0) {
            seat = Math.max(0, Math.min(this.getPassengers().indexOf(passenger), seatCount() - 1));
        }
        double[] o = seatOffsets()[seat];
        Vec3 p = localToWorld(o[0], o[1], o[2]);
        moveFunction.accept(passenger, p.x, p.y, p.z);

        if (this.level().isClientSide && passenger instanceof Player pl && pl.isLocalPlayer() && this.pendingRiderYaw != 0.0F) {
            passenger.setYRot(passenger.getYRot() + this.pendingRiderYaw);
            passenger.setYHeadRot(passenger.getYHeadRot() + this.pendingRiderYaw);
            this.pendingRiderYaw = 0.0F;
        }
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        for (double fwd : new double[]{-1.4, 0.0, 1.2}) {
            for (double side : new double[]{1.0, -1.0}) {
                Vec3 base = localToWorld(side * 2.0, 0.0, fwd);
                for (double dy : new double[]{0.0, 1.0, -1.0, 2.0}) {
                    Vec3 at = base.add(0.0, dy, 0.0);
                    AABB box = passenger.getDimensions(Pose.STANDING).makeBoundingBox(at);
                    if (this.level().noCollision(passenger, box)) {
                        return at;
                    }
                }
            }
        }
        return super.getDismountLocationForPassenger(passenger);
    }

    // ------------------------------------------------------------------ interaction

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (isWrecked()) {
            return InteractionResult.PASS;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof com.exodusvehicles.item.VehicleToolItem) {
            return InteractionResult.PASS;      // wrench / jerry can: held on the vehicle, see the item
        }

        // No picking the car back up as an item: once placed it stays until destroyed
        // (creative players can still remove it by hitting it).
        if (player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }

        if (player.getVehicle() == this) {
            return InteractionResult.PASS;
        }
        if (!this.level().isClientSide) {
            return player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }

    // ------------------------------------------------------------------ armour / damage

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        if (this.level().isClientSide) {
            return !isWrecked();
        }
        if (isWrecked() || this.isRemoved()) {
            return false;
        }
        if (source.getEntity() instanceof Player p && p.getAbilities().instabuild && source.getDirectEntity() == p) {
            this.ejectPassengers();
            this.discard();
            return true;
        }
        // one explosion can hit the body and both end hitboxes in the same tick: count it once
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

        float dealt = armorFilter(source, amount);
        if (dealt <= 0.0F) {
            ricochet(source);
            return false;
        }
        if (!source.is(DamageTypeTags.IS_EXPLOSION) && source.getDirectEntity() != null) {
            // a round that gets through still rings the plate, just duller
            Vec3 at = impactPoint(source);
            this.level().playSound(null, at.x, at.y, at.z, ArmoredCarMod.ARMOR_HIT, SoundSource.PLAYERS, 0.8F,
                    0.9F + this.random.nextFloat() * 0.2F);
        }
        setHealth(getHealth() - dealt);
        this.markHurt();
        if (getHealth() <= 0.0F) {
            destroy();
        }
        return true;
    }

    /**
     * Where a hit actually struck the vehicle. The damage source's own position is no good for this: for a gun it can be
     * the shooter, and fast modded bullets (TACZ) report where they were at the start of the tick, up to ~20 blocks back,
     * which put the spark/impact effects in the shooter's face. Instead the shot is traced onto the hull's hitboxes.
     */
    protected Vec3 impactPoint(DamageSource source) {
        java.util.List<AABB> boxes = new java.util.ArrayList<>();
        boxes.add(this.getBoundingBox());
        for (HullSegmentEntity seg : this.segments) {
            if (seg != null && !seg.isRemoved()) {
                boxes.add(seg.getBoundingBox());
            }
        }
        Entity direct = source.getDirectEntity();
        Entity shooter = direct != null ? direct : source.getEntity();
        Vec3 from = null, dir = null;
        if (shooter instanceof LivingEntity living) {
            from = living.getEyePosition();
            dir = living.getLookAngle();
        } else if (shooter != null) {
            from = shooter.position();
            Vec3 v = shooter.getDeltaMovement();
            dir = v.lengthSqr() > 1.0E-6 ? v.normalize() : this.getBoundingBox().getCenter().subtract(from).normalize();
        }
        if (from != null) {
            Vec3 to = from.add(dir.scale(256.0));
            Vec3 best = null;
            for (AABB b : boxes) {
                if (b.inflate(0.05).contains(from)) {
                    continue;
                }
                java.util.Optional<Vec3> hit = b.inflate(0.05).clip(from, to);
                if (hit.isPresent() && (best == null || hit.get().distanceToSqr(from) < best.distanceToSqr(from))) {
                    best = hit.get();
                }
            }
            if (best != null) {
                return best;
            }
            // missed every box (spread, explosion splash...): the nearest point of the hull to where it came from
            AABB near = boxes.get(0);
            for (AABB b : boxes) {
                if (b.distanceToSqr(from) < near.distanceToSqr(from)) {
                    near = b;
                }
            }
            return new Vec3(Mth.clamp(from.x, near.minX, near.maxX), Mth.clamp(from.y, near.minY, near.maxY),
                    Mth.clamp(from.z, near.minZ, near.maxZ));
        }
        Vec3 at = source.getSourcePosition();
        if (at != null && this.getBoundingBox().inflate(4.5, 1.0, 4.5).contains(at)) {
            return at;
        }
        return this.position().add(0.0, 1.5, 0.0);
    }

    /** Returns the damage that gets through the armour (0 = bounced). */
    protected float armorFilter(DamageSource source, float amount) {
        ModConfig cfg = ModConfig.get();
        if (source.is(DamageTypeTags.IS_EXPLOSION)) {
            return amount * cfg.explosionDamageMultiplier;
        }
        if (!isArmored()) {
            return amount;
        }
        if (TaczCompat.isTaczDamage(source)) {
            Set<String> ids = TaczCompat.weaponIds(source);
            for (String id : ids) {
                if (cfg.antiTankWeapons.contains(id)) {
                    return Math.max(amount, 1.0F) * cfg.antiTankDamageMultiplier;
                }
            }
        }
        if (amount >= cfg.penetrationThreshold) {
            return amount;
        }
        return 0.0F;
    }

    private void ricochet(DamageSource source) {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        Vec3 at = impactPoint(source);
        server.sendParticles(ParticleTypes.CRIT, at.x, at.y, at.z, 6, 0.05, 0.05, 0.05, 0.25);
        server.sendParticles(ParticleTypes.SMOKE, at.x, at.y, at.z, 1, 0.0, 0.0, 0.0, 0.01);
        this.level().playSound(null, at.x, at.y, at.z, ArmoredCarMod.RICOCHET, SoundSource.PLAYERS, 0.7F,
                0.9F + this.random.nextFloat() * 0.3F);
    }

    private void destroy() {
        ModConfig cfg = ModConfig.get();
        this.entityData.set(DATA_WRECKED, true);
        this.entityData.set(DATA_HEALTH, 0.0F);
        this.wreckTicks = Math.max(20, cfg.wreckSeconds * 20);
        this.speed = 0.0F;
        this.ejectPassengers();
        this.level().explode(this, this.getX(), this.getY() + 1.8, this.getZ(),
                cfg.destroyExplosionPower, cfg.destroyExplosionStartsFire,
                cfg.destroyExplosionBreaksBlocks ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE);
        if (this.level() instanceof ServerLevel server) {
            for (double f : new double[]{-3.0, 0.0, 3.0}) {
                Vec3 p = localToWorld(0.0, 2.2, f);
                server.sendParticles(ParticleTypes.LAVA, p.x, p.y, p.z, 10, 1.0, 0.6, 1.0, 0.0);
                server.sendParticles(ParticleTypes.LARGE_SMOKE, p.x, p.y, p.z, 20, 1.2, 0.8, 1.2, 0.05);
            }
        }
    }

    // ------------------------------------------------------------------ MG 08 in the cupola

    public void fireGun(ServerPlayer shooter) {
        // the trigger is polled once per tick; a fractional cooldown lets the gun average ~16 rounds/s
        if (!hasGun() || isWrecked() || seatOf(shooter) != GUNNER || this.gunCooldown >= 1.0F || getReloadTicks() > 0) {
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

        float pitch = Mth.clamp(shooter.getXRot(), -cfg.mgMaxElevation, cfg.mgMaxDepression);
        Vec3 dir = Vec3.directionFromRotation(pitch, shooter.getYHeadRot());
        dir = dir.add(this.random.nextGaussian() * cfg.mgSpread,
                this.random.nextGaussian() * cfg.mgSpread,
                this.random.nextGaussian() * cfg.mgSpread).normalize();

        // Rounds travel along the sight line (so they land on the reticle); the barrel itself sits
        // below the sight, so flash, smoke and tracers start there and stay out of the gunner's view.
        Vec3 pivot = localToWorld(0.0, GUN_PIVOT_UP, GUN_PIVOT_FWD);
        Vec3 rayStart = pivot.add(dir.scale(1.0));
        Vec3 right = new Vec3(-dir.z, 0.0, dir.x).normalize();
        Vec3 viewDown = dir.cross(right).normalize();          // "down" on the gunner's screen
        if (viewDown.y > 0) {
            viewDown = viewDown.scale(-1.0);
        }
        Vec3 muzzle = pivot.add(dir.scale(GUNNER_MUZZLE_DIST)).add(viewDown.scale(BARREL_BELOW_SIGHT))
                .add(right.scale(GUNNER_MUZZLE_RIGHT));
        Vec3 end = pivot.add(dir.scale(cfg.mgRange));

        BlockHitResult blockHit = this.level().clip(new ClipContext(rayStart, end,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (blockHit.getType() != HitResult.Type.MISS) {
            end = blockHit.getLocation();
        }
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(this.level(), this, rayStart, end,
                new AABB(rayStart, end).inflate(1.0),
                e -> e != this && !this.hasPassenger(e) && !(e instanceof HullSegmentEntity s && s.belongsTo(this))
                        && e.isPickable() && e.isAlive() && !e.isSpectator(), 0.3F);

        // Danny's AoT: a titan's eye on the line of fire is hit in preference to the head around it
        if (com.exodusvehicles.compat.AotCompat.active()) {
            double firstD = entityHit != null ? entityHit.getLocation().distanceTo(rayStart) : Double.MAX_VALUE;
            java.util.Optional<Entity> eye = com.exodusvehicles.compat.AotCompat.eyeAlong(server, rayStart, end,
                    entityHit != null ? entityHit.getEntity() : null, firstD);
            if (eye.isPresent()) {
                Entity e = eye.get();
                Vec3 at = e.getBoundingBox().inflate(0.35).clip(rayStart, end).orElse(e.getBoundingBox().getCenter());
                entityHit = new EntityHitResult(e, at);
            }
        }
        if (entityHit != null) {
            Entity target = entityHit.getEntity();
            end = entityHit.getLocation();
            if (com.exodusvehicles.compat.AotCompat.isEye(target)) {
                // a 7.92 mm round is plenty to put out an eye (the AoT mod wants 6+ damage from a projectile)
                target.hurt(this.damageSources().mobProjectile(this, shooter), Math.max(cfg.mgDamage, 6.0F));
            } else {
            if (target instanceof LivingEntity living) {
                living.invulnerableTime = 0;
            }
            target.hurt(this.damageSources().mobProjectile(this, shooter), cfg.mgDamage);
            }
        } else if (blockHit.getType() == HitResult.Type.BLOCK) {
            // a small kick of whatever the round hit (dirt from dirt, chips from stone), thrown upward
            BlockParticleOption chunk = new BlockParticleOption(ParticleTypes.BLOCK, this.level().getBlockState(blockHit.getBlockPos()));
            Vec3 n = Vec3.atLowerCornerOf(blockHit.getDirection().getNormal());
            for (int i = 0; i < 7; i++) {
                double vx = n.x * 0.12 + (this.random.nextDouble() - 0.5) * 0.12;
                double vy = 0.12 + this.random.nextDouble() * 0.16 + Math.max(0.0, n.y) * 0.05;
                double vz = n.z * 0.12 + (this.random.nextDouble() - 0.5) * 0.12;
                server.sendParticles(chunk, end.x, end.y + 0.02, end.z, 0, vx, vy, vz, 1.0);
            }
            server.sendParticles(ParticleTypes.POOF, end.x, end.y + 0.05, end.z, 0, 0.0, 0.02, 0.0, 1.0);
        }

        // the visible barrel (model) is on the sight line for onlookers; the gunner sees it lowered in the sight
        Vec3 barrelTip = pivot.add(dir.scale(MUZZLE_DIST));
        int shotNo = this.shotCounter++;
        if (shotNo % 3 == 0) {
            // a wisp of white smoke from the barrel
            for (ServerPlayer viewer : server.players()) {
                if (viewer.distanceToSqr(barrelTip) > 64.0 * 64.0) {
                    continue;
                }
                Vec3 at = viewer == shooter ? muzzle.add(dir.scale(0.3)) : barrelTip;   // gunner: their view of the barrel
                server.sendParticles(viewer, ParticleTypes.WHITE_SMOKE, false, at.x, at.y + 0.05, at.z, 0,
                        dir.x * 0.02, 0.02, dir.z * 0.02, 1.0);
            }
        }
        if (shotNo % 4 == 0) {
            // every 4th round is a tracer (clients draw it flying out from the barrel)
            TracerPayload tracer = new TracerPayload(this.getId(), barrelTip.x, barrelTip.y, barrelTip.z, end.x, end.y, end.z);
            for (ServerPlayer viewer : net.fabricmc.fabric.api.networking.v1.PlayerLookup.tracking(this)) {
                net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(viewer, tracer);
            }
        }
        groundAndFlyby(server, shooter, muzzle, end, blockHit.getType() == HitResult.Type.BLOCK && entityHit == null);
        playGunSound(server, muzzle, ArmoredCarMod.MG_FIRE, ArmoredCarMod.MG_FIRE_FAR,
                0.97F + this.random.nextFloat() * 0.06F);
        this.lastShotTick = this.tickCount;
        this.burstActive = true;
        this.lastMuzzle = muzzle;
        server.broadcastEntityEvent(this, (byte) 61);    // client: recoil + muzzle flash
    }

    /**
     * Close sound for listeners within ~15 blocks (and the crew), distant sound beyond, crossfaded between 12 and 18.
     * Sent per player so each one hears only the version that fits their distance.
     */
    private void playGunSound(ServerLevel server, Vec3 at, net.minecraft.sounds.SoundEvent close,
                              net.minecraft.sounds.SoundEvent far, float pitch) {
        long seed = this.random.nextLong();
        net.minecraft.core.Holder<net.minecraft.sounds.SoundEvent> closeH = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(close);
        net.minecraft.core.Holder<net.minecraft.sounds.SoundEvent> farH = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(far);
        for (ServerPlayer p : server.players()) {
            double d = Math.sqrt(p.distanceToSqr(at));
            if (d > 200.0) {
                continue;
            }
            if (this.hasPassenger(p)) {
                // the crew are inside the armoured box with the gun: louder, boomier version
                if (close == ArmoredCarMod.MG_FIRE) {
                    p.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(
                            BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ArmoredCarMod.MG_FIRE_INSIDE), SoundSource.PLAYERS,
                            at.x, at.y, at.z, 1.0F, pitch, seed));
                } else {
                    p.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(closeH, SoundSource.PLAYERS,
                            at.x, at.y, at.z, 1.0F, pitch, seed));
                }
                continue;
            }
            float farMix = (float) Mth.clamp((d - 12.0) / 6.0, 0.0, 1.0);
            if (farMix < 1.0F) {
                p.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(closeH, SoundSource.PLAYERS,
                        at.x, at.y, at.z, 1.0F - farMix, pitch, seed));
            }
            if (farMix > 0.0F) {
                p.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(farH, SoundSource.PLAYERS,
                        at.x, at.y, at.z, 4.0F * farMix, pitch, seed));    // volume > 1 widens the hearing range
            }
        }
    }

    /** Last tick each player heard a round fly past (keeps fly-bys from stacking up at 10 rounds/s). */
    private final java.util.Map<java.util.UUID, Integer> lastFlyby = new java.util.HashMap<>();

    /**
     * Ground impact sound where a round lands, and a fly-by for anyone the round passes close to
     * (heard only by that player, from the point where it went past them).
     */
    private void groundAndFlyby(ServerLevel server, ServerPlayer shooter, Vec3 from, Vec3 to, boolean hitGround) {
        if (hitGround) {
            server.playSound(null, to.x, to.y, to.z, ArmoredCarMod.IMPACT_GROUND, SoundSource.PLAYERS,
                    1.0F, 0.9F + this.random.nextFloat() * 0.2F);
        }
        Vec3 seg = to.subtract(from);
        double len2 = seg.lengthSqr();
        if (len2 < 1.0) {
            return;
        }
        for (ServerPlayer p : server.players()) {
            if (p == shooter || this.hasPassenger(p) || p.isSpectator()) {
                continue;
            }
            Vec3 eye = p.getEyePosition();
            double t = eye.subtract(from).dot(seg) / len2;
            if (t < 0.05 || t > 0.97) {
                continue;               // not passed: in front of the gun, or it hit them / landed by them
            }
            Vec3 closest = from.add(seg.scale(t));
            if (closest.distanceToSqr(eye) > 2.5 * 2.5) {
                continue;
            }
            Integer last = this.lastFlyby.get(p.getUUID());
            if (last != null && this.tickCount - last < 4) {
                continue;
            }
            this.lastFlyby.put(p.getUUID(), this.tickCount);
            p.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(
                    BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ArmoredCarMod.BULLET_FLYBY), SoundSource.PLAYERS,
                    closest.x, closest.y, closest.z, 1.0F, 0.9F + this.random.nextFloat() * 0.2F, this.random.nextLong()));
        }
    }

    /** Feeds a new belt from the gunner's ammunition. Returns false if there was nothing to load. */
    public boolean startReload(ServerPlayer gunner) {
        ModConfig cfg = ModConfig.get();
        if (!hasGun() || isWrecked() || seatOf(gunner) != GUNNER || getReloadTicks() > 0 || getBelt() >= cfg.mgBeltSize) {
            return false;
        }
        int needed = cfg.mgBeltSize - getBelt();
        int taken = TaczCompat.takeMgAmmo(gunner, needed);
        if (taken <= 0) {
            gunner.displayClientMessage(Component.translatable("msg.exodus_vehicles.no_ammo"), true);
            return false;
        }
        this.pendingRounds = taken;
        this.entityData.set(DATA_RELOAD, Math.max(1, cfg.mgReloadTicks));
        gunSoundAtTurret(ArmoredCarMod.RELOAD_OUT);            // empty belt out
        return true;
    }

    private void gunSoundAtTurret(net.minecraft.sounds.SoundEvent sound) {
        Vec3 at = localToWorld(0.0, GUN_PIVOT_UP, GUN_PIVOT_FWD);
        this.level().playSound(null, at.x, at.y, at.z, sound, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private void tickGunServer() {
        ModConfig cfg = ModConfig.get();
        // end of a burst: the mechanism's last clatter and the report rolling away
        if (this.burstActive && this.tickCount - this.lastShotTick > 3) {
            this.burstActive = false;
            if (this.level() instanceof ServerLevel server) {
                playGunSound(server, this.lastMuzzle, ArmoredCarMod.MG_TAIL, ArmoredCarMod.MG_TAIL_FAR, 1.0F);
            }
        }
        // engine revs when the car pulls away from a standstill
        Vec3 pos = this.position();
        double moved = this.lastServerPos == null ? 0.0 : pos.subtract(this.lastServerPos).horizontalDistance();
        this.lastServerPos = pos;
        if (this.rampCooldown > 0) {
            this.rampCooldown--;
        }
        this.lastMoved = moved;
        // fuel: a trickle while idling, more the faster it goes
        if (isEngineOn()) {
            ModConfig fc = ModConfig.get();
            float load = (float) Mth.clamp(moved / Math.max(0.05, maxSpeed()), 0.0, 1.0);
            float perMinute = Mth.lerp(load, fc.fuelIdlePerMinute, fuelPerMinuteDriving());
            float fuel = Math.max(0.0F, getFuel() - perMinute / 1200.0F);
            this.entityData.set(DATA_FUEL, fuel);
            if (fuel <= 0.0F) {
                this.entityData.set(DATA_ENGINE, false);
                engineSound(ArmoredCarMod.ENGINE_STOP);
                if (getControllingPassenger() instanceof Player d) {
                    d.displayClientMessage(Component.translatable("msg.exodus_vehicles.out_of_fuel"), true);
                }
            }
        }
        if (hasDriver() && isEngineOn()) {
            if (moved < 0.02) {
                this.stillTicks++;
            } else {
                if (this.stillTicks > 20 && moved > 0.03 && this.rampCooldown == 0) {
                    engineSound(ArmoredCarMod.ENGINE_RAMP);
                    this.rampCooldown = 70;
                }
                this.stillTicks = 0;
            }
        } else {
            this.stillTicks = 0;
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
                gunSoundAtTurret(ArmoredCarMod.RELOAD_IN);         // new belt in, ends as the reload completes
            }
            if (reload == 0) {
                this.entityData.set(DATA_BELT, Math.min(cfg.mgBeltSize, getBelt() + this.pendingRounds));
                this.pendingRounds = 0;
            }
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 61) {
            this.flashTicks = 2;
            this.flashStyle = this.random.nextInt(4);
            this.flashScale = 0.8F + this.random.nextFloat() * 0.5F;
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
        if (!this.level().isClientSide && !isWrecked()) {
            tickGunServer();
        }
        if (isWrecked()) {
            tickWreck();
        }
        tickLerp();

        if (this.isControlledByLocalInstance()) {
            LivingEntity controller = getControllingPassenger();
            if (controller instanceof Player driver) {
                drive(driver);
            } else {
                this.speed *= isParkingBrakeOn() ? 0.5F : 0.9F;
                this.steer *= 0.9F;
                this.yawVel *= 0.6F;
                if (Math.abs(this.speed) < 0.001F) {
                    this.speed = 0.0F;
                }
            }
            applyMotion();
            Vec3 before = this.position();
            Vec3 wanted = this.getDeltaMovement();
            this.move(MoverType.SELF, wanted);
            if (this.horizontalCollision) {
                // Lose speed only in proportion to how much of the move was actually blocked:
                // clipping a corner or a slab barely slows the car, a solid wall stops it.
                double want = Math.hypot(wanted.x, wanted.z);
                double got = Math.hypot(this.getX() - before.x, this.getZ() - before.z);
                float ratio = want > 1.0E-4 ? (float) Mth.clamp(got / want, 0.0, 1.0) : 1.0F;
                if (ratio < 0.15F) {
                    this.speed *= 0.2F;           // driving into a wall
                } else if (ratio < 0.85F) {
                    this.speed *= 0.6F + 0.4F * ratio;
                }
            }
            if (this.getY() - before.y > 0.4) {
                this.speed *= 0.97F;              // climbing a block: a jolt, not a stop
            }
        } else {
            this.setDeltaMovement(Vec3.ZERO);
        }

        if (this.level().isClientSide) {
            tickClientVisuals();
        } else {
            tickSegments();
            if (!isWrecked()) {
                for (Entity p : this.getPassengers()) {
                    if (seatOf(p) < 0) {
                        int free = firstFreeSeat();
                        if (free >= 0) {
                            setSeat(free, p.getId());
                        }
                    }
                }
            }
        }
    }

    /** Heavy truck model: slow throttle response, steering wheel that turns gradually, bicycle-model yaw. */
    private void drive(Player driver) {
        float forward = isEngineOn() && !isParkingBrakeOn() ? driver.zza : 0.0F;   // engine off: the car only coasts
        if (isParkingBrakeOn()) {
            // parking brake: wheels locked, the car stops dead within a moment and stays put
            this.speed *= 0.5F;
            if (Math.abs(this.speed) < 0.01F) {
                this.speed = 0.0F;
            }
        }
        float strafe = driver.xxa;

        if (forward > 0.0F) {
            this.speed += this.speed < 0.0F ? ModConfig.get().braking : acceleration();
        } else if (forward < 0.0F) {
            this.speed -= this.speed > 0.0F ? ModConfig.get().braking : acceleration() * 0.7F;
        } else {
            this.speed *= 0.985F;
            if (Math.abs(this.speed) < 0.002F) {
                this.speed = 0.0F;
            }
        }
        this.speed = Mth.clamp(this.speed, -maxReverseSpeed(), maxSpeed());
        ModConfig cfg = ModConfig.get();

        // Steering: less lock at speed, eased toward the target so the wheel never snaps,
        // and the yaw rate itself is smoothed so the hull swings in and out of turns.
        float speedFrac = Math.min(1.0F, Math.abs(this.speed) / Math.max(0.01F, maxSpeed()));
        float lock = maxSteerAngle() * (1.0F - 0.3F * speedFrac);
        float target = strafe > 0 ? lock : strafe < 0 ? -lock : 0.0F;
        float rate = target == 0.0F ? cfg.steerReturnSpeed : cfg.steerSpeed;
        this.steer += Mth.clamp((target - this.steer) * 0.3F, -rate, rate);
        if (target == 0.0F && Math.abs(this.steer) < 0.05F) {
            this.steer = 0.0F;
        }

        float turn = 0.0F;
        if (Math.abs(this.speed) > 0.001F && this.onGround()) {
            // yaw rate = v / L * tan(steer)   (rad/tick -> deg/tick)
            turn = (float) Math.toDegrees(this.speed / wheelbase() * Math.tan(Math.toRadians(this.steer)));
        }
        this.yawVel += (turn - this.yawVel) * cfg.turnSmoothing;
        if (Math.abs(this.yawVel) < 0.002F) {
            this.yawVel = 0.0F;
        }
        if (this.yawVel != 0.0F) {
            float newYaw = this.getYRot() - this.yawVel;
            boolean wasClear = endsClear(this.getX(), this.getZ(), this.getYRot());
            if (!wasClear || endsClear(this.getX(), this.getZ(), newYaw)) {
                this.setYRot(newYaw);
                if (this.level().isClientSide) {
                    this.pendingRiderYaw -= this.yawVel;
                }
            } else {
                this.speed = 0.0F;
                this.yawVel = 0.0F;
            }
        }

        // stop if the overhanging nose or tail would drive into a wall
        double yaw = Math.toRadians(this.getYRot());
        double nx = this.getX() - Math.sin(yaw) * this.speed;
        double nz = this.getZ() + Math.cos(yaw) * this.speed;
        if (endsClear(this.getX(), this.getZ(), this.getYRot()) && !endsClear(nx, nz, this.getYRot())) {
            this.speed = 0.0F;
        }
    }

    public float getSteer() {
        return this.steer;
    }

    private void applyMotion() {
        double yaw = Math.toRadians(this.getYRot());
        Vec3 dm = this.getDeltaMovement();
        double vy = this.onGround() ? -0.04 : (dm.y - 0.08) * 0.98;
        this.setDeltaMovement(-Math.sin(yaw) * this.speed, vy, Math.cos(yaw) * this.speed);
    }

    private void tickLerp() {
        if (this.isControlledByLocalInstance()) {
            this.lerpSteps = 0;
            this.syncPacketPositionCodec(this.getX(), this.getY(), this.getZ());
        }
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
        this.lerpSteps = 3;     // short and smooth, like vanilla boats
    }

    private void tickWreck() {
        if (!this.level().isClientSide) {
            if (--this.wreckTicks <= 0) {
                if (this.level() instanceof ServerLevel server) {
                    server.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.5, this.getZ(),
                            60, 2.0, 1.0, 2.0, 0.03);
                }
                this.discard();
            }
            return;
        }
        this.clientWreckAge++;
        RandomSource r = this.random;
        Vec3 top = localToWorld((r.nextDouble() - 0.5) * 2.0, 3.0, (r.nextDouble() - 0.5) * 6.0);
        if (r.nextInt(2) == 0) {
            this.level().addParticle(ParticleTypes.LARGE_SMOKE, top.x, top.y, top.z, 0.0, 0.07, 0.0);
        }
        if (this.clientWreckAge % 3 == 0) {
            this.level().addAlwaysVisibleParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, true,
                    top.x, top.y + 0.3, top.z, 0.0, 0.06, 0.0);
        }
        if (this.clientWreckAge < 600) {
            if (r.nextInt(2) == 0) {
                Vec3 f = localToWorld((r.nextDouble() - 0.5) * 2.2, 1.6 + r.nextDouble() * 1.6, (r.nextDouble() - 0.5) * 8.0);
                this.level().addParticle(ParticleTypes.FLAME, f.x, f.y, f.z, 0.0, 0.03, 0.0);
            }
            if (r.nextInt(10) == 0) {
                this.level().addParticle(ParticleTypes.LAVA, top.x, top.y, top.z, 0.0, 0.0, 0.0);
            }
        }
    }

    /** Client only: true when this client's player is the gunner looking through the sight. */
    private boolean isLocalGunnerInFirstPerson() {
        return com.exodusvehicles.client.GunSight.inSight();
    }

    private void tickClientVisuals() {
        this.tracers.removeIf(t -> ++t.age > TRACER_LIFE);
        double dx = this.getX() - this.xo;
        double dz = this.getZ() - this.zo;
        double yaw = Math.toRadians(this.getYRot());
        double forward = dx * -Math.sin(yaw) + dz * Math.cos(yaw);
        this.clientSpeed = (float) Math.abs(forward);

        this.wheelRotO = this.wheelRot;
        if (!isWrecked()) {
            this.wheelRot += (float) (forward / wheelRadius());
        }

        // front wheel steering: exact for the driver, estimated from the yaw rate for everyone else
        this.steerVisO = this.steerVis;
        float targetSteer;
        if (this.isControlledByLocalInstance()) {
            targetSteer = this.steer;
        } else if (Math.abs(forward) > 0.01) {
            float yawRate = Mth.wrapDegrees(this.yRotO - this.getYRot());
            targetSteer = (float) Math.toDegrees(Math.atan(Math.toRadians(yawRate) * wheelbase() / forward));
            targetSteer = Mth.clamp(targetSteer, -maxSteerAngle(), maxSteerAngle());
        } else {
            targetSteer = this.steerVis;
        }
        this.steerVis += (targetSteer - this.steerVis) * 0.5F;

        // cupola follows the gunner's view
        this.turretYawO = this.turretYaw;
        this.gunPitchO = this.gunPitch;
        Entity gunner = entityInSeat(GUNNER);
        if (hasGun() && gunner instanceof LivingEntity g && !isWrecked()) {
            ModConfig cfg = ModConfig.get();
            float rel = Mth.wrapDegrees(g.getYHeadRot() - this.getYRot());
            this.turretYaw = this.turretYawO + Mth.wrapDegrees(rel - this.turretYawO);
            this.gunPitch = Mth.clamp(g.getXRot(), -cfg.mgMaxElevation, cfg.mgMaxDepression);
        }

        this.recoilO = this.recoil;
        this.recoil *= 0.45F;
        if (this.flashTicks > 0) {
            this.flashTicks--;
        }

        if (!isWrecked() && getHealth() < getMaxHealth() * 0.35F && this.random.nextInt(3) == 0) {
            double[] e = engineOffset();
            Vec3 p = localToWorld(e[0] + (this.random.nextDouble() - 0.5) * 1.2, e[1], e[2]);
            this.level().addParticle(ParticleTypes.SMOKE, p.x, p.y, p.z, 0.0, 0.05, 0.0);
        }
        if (hasGun() && !isWrecked() && (isOverheated() || getHeat() > 0.75F) && this.random.nextInt(isOverheated() ? 1 : 4) == 0
                && !isLocalGunnerInFirstPerson()) {
            Vec3 jacket = localToWorld(0.0, GUN_PIVOT_UP, GUN_PIVOT_FWD).add(
                    Vec3.directionFromRotation(this.gunPitch, this.getYRot() + this.turretYaw).scale(MUZZLE_DIST * 0.6));
            this.level().addParticle(ParticleTypes.CLOUD, jacket.x, jacket.y + 0.1, jacket.z, 0.0, 0.04, 0.0);
        }
        if (isEngineOn() && this.random.nextInt(4) == 0) {   // exhaust puffs, left rear
            double[] e = exhaustOffset();
            Vec3 p = localToWorld(e[0], e[1], e[2]);
            this.level().addParticle(ParticleTypes.SMOKE, p.x, p.y, p.z, 0.0, 0.02, 0.0);
        }
    }
}
