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
 * 21 cm Mörser 16 heavy howitzer. Indirect fire only: the crew enter target coordinates (from an observer) in the
 * fire-control screen, the gun lays itself on them by cranking traverse and elevation, then fires a shell that
 * scatters around the aim point; corrections walk the fall of shot onto the target.
 * Must be dug in to fire. Moved by hand (slowly) or towed by a Daimler-Marienfelde lorry.
 * Seat 0 = gun layer (fires, lays), 1 = loader (two crew load much faster).
 */
public class MorserEntity extends Entity {
    public static final int SEATS = 2;
    public static final int LAYER = 0;
    /** Trunnion position {up, forward} in blocks; barrel length from the trunnions. */
    public static final double TRUNNION_UP = 27.0 / 16.0, TRUNNION_FWD = 2.0 / 16.0, BARREL = 40.0 / 16.0;
    public static final double WHEEL_RADIUS = 12.0 / 16.0;
    /** Distance from the axle to the trail eye (hitch). */
    public static final double TRAIL = 3.8;
    public static final float MIN_ELEV = 45.0F, MAX_ELEV = 75.0F;

    public static final int STATUS_NONE = 0, STATUS_OK = 1, STATUS_RANGE_LONG = 2, STATUS_RANGE_SHORT = 3, STATUS_TRAVERSE = 4;

    private static final double[][] SEAT_OFFSETS = {{-0.61, 0.2, -0.19}, {0.61, 0.2, -0.19}};

    private static final EntityDataAccessor<Float> DATA_HEALTH = def(EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_SEAT_0 = def(EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_SEAT_1 = def(EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_EMPLACED = def(EntityDataSerializers.BOOLEAN);
    /** >0 digging in, <0 packing up (ticks done). */
    private static final EntityDataAccessor<Integer> DATA_WORK = def(EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_LOADED = def(EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_LOADING = def(EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TOWED_BY = def(EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_HAS_TARGET = def(EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_TX = def(EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TZ = def(EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ADD = def(EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_RIGHT = def(EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_STATUS = def(EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_CHARGE = def(EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_RANGE = def(EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_BEARING = def(EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_WANT_ELEV = def(EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_WANT_TRAV = def(EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_ELEV = def(EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_TRAV = def(EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_FLIGHT = def(EntityDataSerializers.INT);

    private static <T> EntityDataAccessor<T> def(net.minecraft.network.syncher.EntityDataSerializer<T> s) {
        return SynchedEntityData.defineId(MorserEntity.class, s);
    }

    private int recoilLock;
    private int lerpSteps;
    private double lerpX, lerpY, lerpZ;
    private float lerpYRot;
    private int lastExplosionTick = -1;
    private float lastExplosionAmount;

    // client visuals
    public float wheelRot, wheelRotO;
    public float recoil, recoilO;

    public MorserEntity(EntityType<? extends MorserEntity> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder b) {
        b.define(DATA_HEALTH, ModConfig.get().morserMaxHealth);
        b.define(DATA_SEAT_0, -1);
        b.define(DATA_SEAT_1, -1);
        b.define(DATA_EMPLACED, false);
        b.define(DATA_WORK, 0);
        b.define(DATA_LOADED, false);
        b.define(DATA_LOADING, 0);
        b.define(DATA_TOWED_BY, -1);
        b.define(DATA_HAS_TARGET, false);
        b.define(DATA_TX, 0);
        b.define(DATA_TZ, 0);
        b.define(DATA_ADD, 0);
        b.define(DATA_RIGHT, 0);
        b.define(DATA_STATUS, STATUS_NONE);
        b.define(DATA_CHARGE, 0);
        b.define(DATA_RANGE, 0.0F);
        b.define(DATA_BEARING, 0.0F);
        b.define(DATA_WANT_ELEV, MIN_ELEV);
        b.define(DATA_WANT_TRAV, 0.0F);
        b.define(DATA_ELEV, 10.0F);
        b.define(DATA_TRAV, 0.0F);
        b.define(DATA_FLIGHT, 0);
    }

    // ------------------------------------------------------------------ getters for HUD / renderer

    public float getHealth() { return this.entityData.get(DATA_HEALTH); }
    public float getMaxHealth() { return ModConfig.get().morserMaxHealth; }
    public boolean isEmplaced() { return this.entityData.get(DATA_EMPLACED); }
    public int getWork() { return this.entityData.get(DATA_WORK); }
    public boolean isLoaded() { return this.entityData.get(DATA_LOADED); }
    public int getLoading() { return this.entityData.get(DATA_LOADING); }
    public boolean isTowed() { return this.entityData.get(DATA_TOWED_BY) >= 0; }
    public boolean hasTarget() { return this.entityData.get(DATA_HAS_TARGET); }
    public int targetX() { return this.entityData.get(DATA_TX); }
    public int targetZ() { return this.entityData.get(DATA_TZ); }
    public int corrAdd() { return this.entityData.get(DATA_ADD); }
    public int corrRight() { return this.entityData.get(DATA_RIGHT); }
    public int getStatus() { return this.entityData.get(DATA_STATUS); }
    public int getCharge() { return this.entityData.get(DATA_CHARGE); }
    public float getRange() { return this.entityData.get(DATA_RANGE); }
    public float getBearing() { return this.entityData.get(DATA_BEARING); }
    public float getElevation() { return this.entityData.get(DATA_ELEV); }
    public float getTraverse() { return this.entityData.get(DATA_TRAV); }
    public float getWantedElevation() { return this.entityData.get(DATA_WANT_ELEV); }
    public int getFlightTicks() { return this.entityData.get(DATA_FLIGHT); }

    /** Barrel on the computed settings (within a hair). */
    public boolean isLaid() {
        return hasTarget() && getStatus() == STATUS_OK
                && Math.abs(getElevation() - getWantedElevation()) < 0.05F
                && Math.abs(getTraverse() - this.entityData.get(DATA_WANT_TRAV)) < 0.05F;
    }

    // ------------------------------------------------------------------ save

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Health")) {
            this.entityData.set(DATA_HEALTH, tag.getFloat("Health"));
        }
        this.entityData.set(DATA_EMPLACED, tag.getBoolean("Emplaced"));
        this.entityData.set(DATA_LOADED, tag.getBoolean("Loaded"));
        this.entityData.set(DATA_HAS_TARGET, tag.getBoolean("HasTarget"));
        this.entityData.set(DATA_TX, tag.getInt("TargetX"));
        this.entityData.set(DATA_TZ, tag.getInt("TargetZ"));
        this.entityData.set(DATA_ADD, tag.getInt("CorrAdd"));
        this.entityData.set(DATA_RIGHT, tag.getInt("CorrRight"));
        this.entityData.set(DATA_ELEV, tag.contains("Elev") ? tag.getFloat("Elev") : 10.0F);
        this.entityData.set(DATA_TRAV, tag.getFloat("Trav"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Health", getHealth());
        tag.putBoolean("Emplaced", isEmplaced());
        tag.putBoolean("Loaded", isLoaded());
        tag.putBoolean("HasTarget", hasTarget());
        tag.putInt("TargetX", targetX());
        tag.putInt("TargetZ", targetZ());
        tag.putInt("CorrAdd", corrAdd());
        tag.putInt("CorrRight", corrRight());
        tag.putFloat("Elev", getElevation());
        tag.putFloat("Trav", getTraverse());
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

    public Vec3 localToWorld(double right, double up, double forward) {
        double yaw = Math.toRadians(this.getYRot());
        double fx = -Math.sin(yaw), fz = Math.cos(yaw);
        double rx = -Math.cos(yaw), rz = -Math.sin(yaw);
        return new Vec3(this.getX() + fx * forward + rx * right, this.getY() + up, this.getZ() + fz * forward + rz * right);
    }

    /** Unit vector along the bore. */
    public Vec3 boreDirection() {
        double yaw = Math.toRadians(this.getYRot() + getTraverse());
        double e = Math.toRadians(getElevation());
        return new Vec3(-Math.sin(yaw) * Math.cos(e), Math.sin(e), Math.cos(yaw) * Math.cos(e));
    }

    public Vec3 trunnions() {
        return localToWorld(0.0, TRUNNION_UP, TRUNNION_FWD);
    }

    // ------------------------------------------------------------------ seats

    private EntityDataAccessor<Integer> seatAccessor(int seat) {
        return seat == 0 ? DATA_SEAT_0 : DATA_SEAT_1;
    }

    public int seatOf(Entity e) {
        for (int i = 0; i < SEATS; i++) {
            if (this.entityData.get(seatAccessor(i)) == e.getId() && this.hasPassenger(e)) {
                return i;
            }
        }
        return -1;
    }

    @Nullable
    private Entity inSeat(int seat) {
        int id = this.entityData.get(seatAccessor(seat));
        for (Entity p : this.getPassengers()) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }

    private int crewCount() {
        int n = 0;
        for (int i = 0; i < SEATS; i++) {
            if (inSeat(i) != null) {
                n++;
            }
        }
        return n;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().size() < SEATS && !isTowed();
    }

    @Override
    public void addPassenger(Entity passenger) {
        super.addPassenger(passenger);
        if (!this.level().isClientSide && seatOf(passenger) < 0) {
            for (int i = 0; i < SEATS; i++) {
                if (inSeat(i) == null) {
                    this.entityData.set(seatAccessor(i), passenger.getId());
                    announce(passenger, i);
                    break;
                }
            }
        }
    }

    @Override
    public void removePassenger(Entity passenger) {
        int seat = seatOf(passenger);
        super.removePassenger(passenger);
        if (!this.level().isClientSide && seat >= 0) {
            this.entityData.set(seatAccessor(seat), -1);
        }
    }

    public void switchSeat(ServerPlayer player) {
        int cur = seatOf(player);
        int other = 1 - cur;
        if (cur < 0) {
            return;
        }
        if (inSeat(other) != null) {
            player.displayClientMessage(Component.translatable("msg.exodus_vehicles.no_free_seat"), true);
            return;
        }
        this.entityData.set(seatAccessor(cur), -1);
        this.entityData.set(seatAccessor(other), player.getId());
        announce(player, other);
        this.positionRider(player);
    }

    private void announce(Entity e, int seat) {
        if (e instanceof Player p) {
            p.displayClientMessage(Component.translatable("msg.exodus_vehicles.seat",
                    Component.translatable("seat.exodus_vehicles.morser." + seat)), true);
        }
    }

    @Override
    public void positionRider(Entity passenger, Entity.MoveFunction move) {
        if (!this.hasPassenger(passenger)) {
            return;
        }
        int seat = Math.max(0, seatOf(passenger));
        double[] o = SEAT_OFFSETS[Math.min(seat, SEATS - 1)];
        Vec3 p = localToWorld(o[0], o[1], o[2]);
        move.accept(passenger, p.x, p.y, p.z);
    }

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

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean client = this.level().isClientSide;
        if (stack.is(Items.IRON_INGOT) && getHealth() < getMaxHealth()) {
            if (!client) {
                this.entityData.set(DATA_HEALTH, Math.min(getMaxHealth(), getHealth() + ModConfig.get().repairPerIngot));
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                this.playSound(SoundEvents.ANVIL_USE, 0.6F, 1.3F);
            }
            return InteractionResult.sidedSuccess(client);
        }
        if (stack.is(Items.LEAD)) {
            if (!client) {
                toggleTow(player);
            }
            return InteractionResult.sidedSuccess(client);
        }
        if (stack.is(ArmoredCarMod.SHELL_ITEM) && player.getVehicle() != this) {
            if (!client) {
                startLoading(player);
            }
            return InteractionResult.sidedSuccess(client);
        }
        if (player.isSecondaryUseActive() || player.getVehicle() == this) {
            return InteractionResult.PASS;
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
        if (isEmplaced() || getWork() != 0) {
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

    /** Engine key: dig the gun in (spade down, platform levelled) or pack it up for moving. */
    public void toggleEmplace(ServerPlayer player) {
        if (seatOf(player) < 0 || isTowed()) {
            return;
        }
        if (getWork() != 0) {
            this.entityData.set(DATA_WORK, 0);
            player.displayClientMessage(Component.translatable("msg.exodus_vehicles.morser_work_stopped"), true);
            return;
        }
        this.entityData.set(DATA_WORK, isEmplaced() ? -1 : 1);
        this.playSound(SoundEvents.SHOVEL_FLATTEN, 1.0F, 0.8F);
    }

    public void startLoading(Player player) {
        if (isLoaded() || getLoading() > 0 || isTowed()) {
            return;
        }
        if (!player.getAbilities().instabuild) {
            int slot = -1;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                if (player.getInventory().getItem(i).is(ArmoredCarMod.SHELL_ITEM)) {
                    slot = i;
                    break;
                }
            }
            if (slot < 0) {
                player.displayClientMessage(Component.translatable("msg.exodus_vehicles.morser_no_shell"), true);
                return;
            }
            player.getInventory().getItem(slot).shrink(1);
        }
        ModConfig cfg = ModConfig.get();
        this.entityData.set(DATA_LOADING, crewCount() >= 2 ? cfg.morserReloadTicksTwoCrew : cfg.morserReloadTicks);
        Vec3 b = trunnions();
        this.level().playSound(null, b.x, b.y, b.z, ArmoredCarMod.MORSER_RELOAD, SoundSource.PLAYERS, 1.0F, 0.9F);
    }

    // ------------------------------------------------------------------ fire control

    public void setTarget(ServerPlayer player, int x, int z, int add, int right) {
        if (seatOf(player) < 0) {
            return;
        }
        this.entityData.set(DATA_TX, x);
        this.entityData.set(DATA_TZ, z);
        this.entityData.set(DATA_ADD, Mth.clamp(add, -2000, 2000));
        this.entityData.set(DATA_RIGHT, Mth.clamp(right, -2000, 2000));
        this.entityData.set(DATA_HAS_TARGET, true);
        computeSolution();
        this.playSound(SoundEvents.BOOK_PAGE_TURN, 0.8F, 1.0F);
    }

    /** Aim point after corrections, at ground level where that is known. */
    private Vec3 aimPoint() {
        Vec3 g = trunnions();
        double tx = targetX() + 0.5, tz = targetZ() + 0.5;
        Vec3 h = new Vec3(tx - g.x, 0, tz - g.z);
        if (h.lengthSqr() > 1.0E-4) {
            Vec3 along = h.normalize();
            Vec3 right = new Vec3(-along.z, 0, along.x);     // to the right looking along the line of fire
            tx += along.x * corrAdd() + right.x * corrRight();
            tz += along.z * corrAdd() + right.z * corrRight();
        }
        double ty = this.getY();
        int bx = Mth.floor(tx), bz = Mth.floor(tz);
        if (this.level().hasChunk(bx >> 4, bz >> 4)) {
            ty = this.level().getHeight(Heightmap.Types.MOTION_BLOCKING, bx, bz);
        }
        return new Vec3(tx, ty, tz);
    }

    private static double muzzleSpeed(int charge) {
        double maxRange = ModConfig.get().morserMaxRange * (charge == 1 ? 0.5 : 1.0);
        return Math.sqrt(ShellManager.GRAVITY * maxRange);
    }

    /** High-angle elevation (degrees) to reach horizontal distance d with height difference dy, or NaN. */
    private static double highAngle(double v, double d, double dy) {
        double g = ShellManager.GRAVITY;
        double disc = v * v * v * v - g * (g * d * d + 2 * dy * v * v);
        if (disc < 0) {
            return Double.NaN;
        }
        return Math.toDegrees(Math.atan((v * v + Math.sqrt(disc)) / (g * d)));
    }

    private void computeSolution() {
        if (!hasTarget()) {
            this.entityData.set(DATA_STATUS, STATUS_NONE);
            return;
        }
        ModConfig cfg = ModConfig.get();
        Vec3 g = trunnions();
        Vec3 t = aimPoint();
        double dx = t.x - g.x, dz = t.z - g.z;
        double d = Math.sqrt(dx * dx + dz * dz);
        double dy = t.y - g.y;
        float bearing = (float) Math.toDegrees(Math.atan2(-dx, dz));
        this.entityData.set(DATA_RANGE, (float) d);
        this.entityData.set(DATA_BEARING, Mth.wrapDegrees(bearing));
        int status = STATUS_OK;
        int charge = 0;
        double elev = Double.NaN;
        for (int c = 1; c <= 2 && Double.isNaN(elev); c++) {
            double e = d < 1.0 ? Double.NaN : highAngle(muzzleSpeed(c), d, dy);
            if (!Double.isNaN(e) && e >= MIN_ELEV && e <= MAX_ELEV) {
                elev = e;
                charge = c;
            }
        }
        if (Double.isNaN(elev)) {
            status = d > cfg.morserMaxRange * 0.75 ? STATUS_RANGE_LONG : STATUS_RANGE_SHORT;
        }
        float trav = Mth.wrapDegrees(bearing - this.getYRot());
        if (status == STATUS_OK && Math.abs(trav) > cfg.morserTraverse) {
            status = STATUS_TRAVERSE;
        }
        this.entityData.set(DATA_STATUS, status);
        this.entityData.set(DATA_CHARGE, charge);
        if (status == STATUS_OK) {
            this.entityData.set(DATA_WANT_ELEV, (float) elev);
            this.entityData.set(DATA_WANT_TRAV, trav);
            double v = muzzleSpeed(charge);
            double vy = v * Math.sin(Math.toRadians(elev));
            this.entityData.set(DATA_FLIGHT, (int) Math.round((vy + Math.sqrt(Math.max(0, vy * vy - 2 * ShellManager.GRAVITY * dy)))
                    / ShellManager.GRAVITY));
        }
    }

    /** Layer's trigger (attack key). */
    public void fire(ServerPlayer player) {
        if (seatOf(player) != LAYER || this.recoilLock > 0) {
            return;
        }
        if (!isEmplaced()) {
            player.displayClientMessage(Component.translatable("msg.exodus_vehicles.morser_not_emplaced"), true);
            return;
        }
        if (!isLoaded()) {
            player.displayClientMessage(Component.translatable("msg.exodus_vehicles.morser_not_loaded"), true);
            return;
        }
        if (!isLaid()) {
            player.displayClientMessage(Component.translatable("msg.exodus_vehicles.morser_not_laid"), true);
            return;
        }
        ModConfig cfg = ModConfig.get();
        ServerLevel server = (ServerLevel) this.level();
        this.entityData.set(DATA_LOADED, false);
        this.recoilLock = 60;

        // scatter: a bit long or short, a bit left or right, growing with range
        double range = getRange();
        double longErr = this.random.nextGaussian() * (cfg.morserRangeSpread * range + 1.5);
        double latErr = this.random.nextGaussian() * (cfg.morserLateralSpread * range + 1.0);
        double v = muzzleSpeed(getCharge()) * Math.sqrt(Math.max(0.5, 1.0 + longErr / Math.max(1.0, range)));
        float yawErr = (float) Math.toDegrees(latErr / Math.max(1.0, range));
        double yaw = Math.toRadians(this.getYRot() + getTraverse() + yawErr);
        double e = Math.toRadians(getElevation());
        Vec3 dir = new Vec3(-Math.sin(yaw) * Math.cos(e), Math.sin(e), Math.cos(yaw) * Math.cos(e));
        Vec3 muzzle = trunnions().add(dir.scale(BARREL));
        // the ballistic solution is worked out from the trunnions; start the shell there so the numbers hold
        ShellManager.fire(server, trunnions(), dir.scale(v), player.getUUID(), aimPoint().y);

        // blast, flash and a smoke cloud that gives the battery away
        server.sendParticles(ParticleTypes.EXPLOSION, muzzle.x, muzzle.y, muzzle.z, 3, 0.3, 0.3, 0.3, 0.0);
        server.sendParticles(ParticleTypes.FLAME, muzzle.x, muzzle.y, muzzle.z, 30, 0.3, 0.3, 0.3, 0.15);
        for (ServerPlayer p : server.players()) {
            double d = Math.sqrt(p.distanceToSqr(muzzle));
            if (d < 512) {
                server.sendParticles(p, ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, true, muzzle.x, muzzle.y, muzzle.z, 10, 0.8, 0.6, 0.8, 0.02);
                server.sendParticles(p, ParticleTypes.LARGE_SMOKE, true, muzzle.x, muzzle.y, muzzle.z, 30, 1.2, 0.8, 1.2, 0.06);
            }
            if (this.hasPassenger(p) || d < 40) {
                p.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(
                        net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ArmoredCarMod.MORSER_CLOSE),
                        SoundSource.PLAYERS, muzzle.x, muzzle.y, muzzle.z, 1.0F, 0.95F + this.random.nextFloat() * 0.1F,
                        this.random.nextLong()));
            } else if (d < 180) {
                ShellManager.soundTo(p, ArmoredCarMod.MORSER_DISTANT, muzzle, (float) (1.0 - (d - 40) / 140 * 0.4), 1.0F, true);
            } else if (d < 1000) {
                ShellManager.soundTo(p, ArmoredCarMod.MORSER_FAR, muzzle, (float) Math.max(0.2, 1.0 - (d - 180) / 820), 1.0F, true);
            }
        }
        server.broadcastEntityEvent(this, (byte) 62);
        // the whole gun jolts back on its spade
        if (this.random.nextFloat() < 0.5F) {
            server.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.2, this.getZ(), 12, 1.5, 0.1, 1.5, 0.02);
        }
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
            dealt = amount >= cfg.penetrationThreshold ? amount : 0.0F;     // the shield stops rifle fire
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
                server.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.5, this.getZ(), 60, 1.5, 1.0, 1.5, 0.05);
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
        if (this.recoilLock > 0) {
            this.recoilLock--;
        }
        // keep the crew in their seats
        for (Entity p : this.getPassengers()) {
            if (seatOf(p) < 0) {
                for (int i = 0; i < SEATS; i++) {
                    if (inSeat(i) == null) {
                        this.entityData.set(seatAccessor(i), p.getId());
                        break;
                    }
                }
            }
        }
        // digging in / packing up
        int work = getWork();
        if (work > 0) {
            if (++work >= cfg.morserEmplaceTicks) {
                this.entityData.set(DATA_EMPLACED, true);
                work = 0;
                this.playSound(SoundEvents.ROOTED_DIRT_PLACE, 1.0F, 0.7F);
            } else if (work % 20 == 0) {
                this.playSound(SoundEvents.SHOVEL_FLATTEN, 0.8F, 0.8F + this.random.nextFloat() * 0.3F);
            }
            this.entityData.set(DATA_WORK, work);
        } else if (work < 0) {
            if (--work <= -cfg.morserPackTicks) {
                this.entityData.set(DATA_EMPLACED, false);
                work = 0;
                this.playSound(SoundEvents.ROOTED_DIRT_BREAK, 1.0F, 0.8F);
            } else if (work % 20 == 0) {
                this.playSound(SoundEvents.SHOVEL_FLATTEN, 0.8F, 0.8F + this.random.nextFloat() * 0.3F);
            }
            this.entityData.set(DATA_WORK, work);
        }
        // loading
        int loading = getLoading();
        if (loading > 0) {
            this.entityData.set(DATA_LOADING, --loading);
            if (loading == 0) {
                this.entityData.set(DATA_LOADED, true);
                this.playSound(SoundEvents.IRON_DOOR_CLOSE, 0.8F, 0.6F);
            }
        }
        // solution follows the gun (moving it or the target re-entered)
        if (hasTarget() && this.tickCount % 10 == 0) {
            computeSolution();
        }
        // laying: crank traverse and elevation toward the solution (barrel lowers for travel)
        float wantE = isEmplaced() && getStatus() == STATUS_OK ? getWantedElevation() : 10.0F;
        float wantT = isEmplaced() && getStatus() == STATUS_OK ? this.entityData.get(DATA_WANT_TRAV) : 0.0F;
        float e = getElevation(), t = getTraverse();
        float ne = e + Mth.clamp(wantE - e, -0.5F, 0.5F);
        float nt = t + Mth.clamp(wantT - t, -0.3F, 0.3F);
        if (ne != e) {
            this.entityData.set(DATA_ELEV, ne);
        }
        if (nt != t) {
            this.entityData.set(DATA_TRAV, nt);
        }

        // movement: towed, pushed by the crew, or nothing (dug in)
        Vec3 before = this.position();
        if (isTowed()) {
            tickTow();
        } else {
            double fwd = 0, turn = 0;
            Entity layer = inSeat(LAYER);
            if (!isEmplaced() && getWork() == 0 && layer instanceof Player p) {
                fwd = p.zza * 0.04;           // manhandling: walking pace at best
                turn = p.xxa * 0.7;
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
        if (this.position().distanceToSqr(before) > 1.0E-4 && getWork() > 0) {
            this.entityData.set(DATA_WORK, 0);    // moved while digging in: start over
        }
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
            away = new Vec3(-Math.sin(Math.toRadians(truck.getYRot())), 0, Math.cos(Math.toRadians(truck.getYRot()))).scale(-1);
        }
        away = away.normalize();
        Vec3 want = hitch.add(away.scale(TRAIL));
        Vec3 delta = new Vec3(want.x - pos.x, 0, want.z - pos.z);
        Vec3 dm = this.getDeltaMovement();
        double vy = this.onGround() ? -0.04 : (dm.y - 0.08) * 0.98;
        this.move(MoverType.SELF, new Vec3(delta.x, vy, delta.z));
        // barrel points away from the lorry, trail toward the hitch
        this.setYRot((float) Math.toDegrees(Math.atan2(-away.x, away.z)));
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
        if (getWork() != 0 && this.random.nextInt(3) == 0) {
            Vec3 p = localToWorld((this.random.nextDouble() - 0.5) * 2, 0.1, -3.5 + this.random.nextDouble());
            this.level().addParticle(ParticleTypes.POOF, p.x, p.y, p.z, 0, 0.05, 0);
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
