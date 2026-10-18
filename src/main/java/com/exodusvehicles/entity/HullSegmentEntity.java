package com.exodusvehicles.entity;

import com.exodusvehicles.ArmoredCarMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Invisible extra hitbox for the 9.5 m hull. Minecraft only supports square, axis-aligned boxes, so the car
 * is covered by three boxes: the main entity (middle) plus a front and a rear segment that follow it.
 * Hits, clicks and collisions on a segment are passed to the car. Segments are never saved; the car
 * respawns them when it loads.
 */
public class HullSegmentEntity extends Entity {
    private static final EntityDataAccessor<Integer> DATA_PARENT =
            SynchedEntityData.defineId(HullSegmentEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_OFFSET =
            SynchedEntityData.defineId(HullSegmentEntity.class, EntityDataSerializers.FLOAT);

    public HullSegmentEntity(EntityType<? extends HullSegmentEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.blocksBuilding = true;
    }

    public void attach(ArmoredCarEntity car, float forwardOffset) {
        this.entityData.set(DATA_PARENT, car.getId());
        this.entityData.set(DATA_OFFSET, forwardOffset);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_PARENT, -1);
        builder.define(DATA_OFFSET, 0.0F);
    }

    @Nullable
    public ArmoredCarEntity getCar() {
        Entity e = this.level().getEntity(this.entityData.get(DATA_PARENT));
        return e instanceof ArmoredCarEntity car ? car : null;
    }

    public boolean belongsTo(Entity car) {
        return this.entityData.get(DATA_PARENT) == car.getId();
    }

    public void snapTo(ArmoredCarEntity car) {
        Vec3 p = car.localToWorld(0.0, 0.0, this.entityData.get(DATA_OFFSET));
        this.setPos(p.x, p.y, p.z);
        this.setYRot(car.getYRot());
    }

    @Override
    public void tick() {
        super.tick();
        ArmoredCarEntity car = getCar();
        if (car == null || car.isRemoved()) {
            if (!this.level().isClientSide && this.tickCount > 40) {
                this.discard();
            }
            return;
        }
        snapTo(car);
    }

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
        // positioned locally from the car every tick
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        ArmoredCarEntity car = getCar();
        return car != null && car.hurt(source, amount);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ArmoredCarEntity car = getCar();
        return car != null ? car.interact(player, hand) : InteractionResult.PASS;
    }

    @Override
    public boolean is(Entity entity) {
        return this == entity || (entity != null && belongsTo(entity));
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public boolean canBeCollidedWith() {
        return !this.isRemoved();
    }

    @Override
    public boolean canCollideWith(Entity other) {
        return !(other instanceof ArmoredCarEntity) && !(other instanceof HullSegmentEntity)
                && (other.canBeCollidedWith() || other.isPushable());
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public ItemStack getPickResult() {
        ArmoredCarEntity car = getCar();
        return car != null ? car.getPickResult() : new ItemStack(ArmoredCarMod.EISENWOLF_ITEM);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}
