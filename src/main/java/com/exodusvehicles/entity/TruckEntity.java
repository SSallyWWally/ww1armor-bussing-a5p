package com.exodusvehicles.entity;

import com.exodusvehicles.ArmoredCarMod;
import com.exodusvehicles.ModConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Daimler-Marienfelde Series DR, the German army's standard 3-4 t lorry (1915-18): 6.6 m long, 4.4 m wheelbase,
 * solid tyres on wooden spoked wheels, chain drive, canvas tilt over a drop-side bed.
 * Unarmoured and unarmed: every hit counts and the crew sit in the open.
 * Seat 0 = driver (cab, left), 1 = co-driver (cab, right), 2 + 3 = men on the benches in the bed.
 * Two variants (entity types): with the canvas tilt and with an open bed.
 * Sneak + use with a chest loads a supply crate into the bed (up to 6); each crate is one row of 9 slots,
 * opened with sneak + use (any other item / empty hand).
 */
public class TruckEntity extends ArmoredCarEntity {
    /** {right, up, forward} in blocks; feet so the hips rest on the cab bench (1.375 m) / bed benches (1.5 m). */
    private static final double[][] SEATS = {
            {-0.31, 0.63, 0.97},
            {0.31, 0.63, 0.97},
            {0.0, 0.75, -0.75},
            {0.0, 0.75, -2.0},
    };
    private static final float[] SEGMENTS = {2.15F, -2.15F};
    public static final double WHEELBASE = 70.0 / 16.0;
    public static final double WHEEL_RADIUS = 7.75 / 16.0;

    public static final int MAX_CRATES = 6;
    private static final EntityDataAccessor<Integer> DATA_CRATES =
            SynchedEntityData.defineId(TruckEntity.class, EntityDataSerializers.INT);

    /** Cargo in the crates: crate n holds slots 9n..9n+8. */
    private final SimpleContainer cargo = new SimpleContainer(MAX_CRATES * 9) {
        @Override
        public boolean stillValid(Player player) {
            return !TruckEntity.this.isRemoved() && !TruckEntity.this.isWrecked()
                    && player.distanceToSqr(TruckEntity.this) < 8.0 * 8.0;
        }

        @Override
        public void startOpen(Player player) {
            TruckEntity.this.crateSound(SoundEvents.BARREL_OPEN);
        }

        @Override
        public void stopOpen(Player player) {
            TruckEntity.this.crateSound(SoundEvents.BARREL_CLOSE);
        }
    };

    private void crateSound(net.minecraft.sounds.SoundEvent sound) {
        if (!this.level().isClientSide) {
            net.minecraft.world.phys.Vec3 at = localToWorld(0.0, 1.6, -1.5);    // the load bed
            this.level().playSound(null, at.x, at.y, at.z, sound, net.minecraft.sounds.SoundSource.BLOCKS,
                    0.8F, 0.85F + this.random.nextFloat() * 0.1F);
        }
    }

    public TruckEntity(EntityType<? extends TruckEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CRATES, 0);
    }

    /** With the canvas tilt (true) or the open-bed variant. */
    public boolean isCovered() {
        return this.getType() != ArmoredCarMod.DAIMLER_DR_OPEN;
    }

    public int getCrates() {
        return this.entityData.get(DATA_CRATES);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_CRATES, Math.max(0, Math.min(MAX_CRATES, tag.getInt("Crates"))));
        this.cargo.clearContent();
        ContainerHelper.loadAllItems(tag, this.cargo.getItems(), this.registryAccess());
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Crates", getCrates());
        ContainerHelper.saveAllItems(tag, this.cargo.getItems(), this.registryAccess());
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (isWrecked() || !player.isSecondaryUseActive() || player.getVehicle() == this) {
            return super.interact(player, hand);
        }
        ItemStack stack = player.getItemInHand(hand);
        boolean client = this.level().isClientSide;
        if (stack.is(Items.CHEST) && getCrates() < MAX_CRATES) {
            if (!client) {
                // the whole stack in hand goes on at once, as many as the bed takes
                int load = Math.min(MAX_CRATES - getCrates(), player.getAbilities().instabuild ? MAX_CRATES : stack.getCount());
                this.entityData.set(DATA_CRATES, getCrates() + load);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(load);
                }
                this.playSound(SoundEvents.WOOD_PLACE, 1.0F, 0.8F);
                player.displayClientMessage(Component.translatable("msg.exodus_vehicles.crate_loaded",
                        getCrates(), MAX_CRATES), true);
            }
            return InteractionResult.sidedSuccess(client);
        }
        if (getCrates() > 0) {
            if (!client) {
                openCargo(player);     // one inventory across every crate on the bed
            }
            return InteractionResult.sidedSuccess(client);
        }
        return InteractionResult.PASS;
    }

    private void openCargo(Player player) {
        int rows = getCrates();
        MenuType<ChestMenu> type = switch (rows) {
            case 1 -> MenuType.GENERIC_9x1;
            case 2 -> MenuType.GENERIC_9x2;
            case 3 -> MenuType.GENERIC_9x3;
            case 4 -> MenuType.GENERIC_9x4;
            case 5 -> MenuType.GENERIC_9x5;
            default -> MenuType.GENERIC_9x6;
        };
        player.openMenu(new SimpleMenuProvider((id, inventory, p) -> new ChestMenu(type, id, inventory, this.cargo, rows),
                Component.translatable("container.exodus_vehicles.truck_cargo")));
    }

    @Override
    public void tick() {
        super.tick();
        // a destroyed lorry spills whatever was in its crates
        if (!this.level().isClientSide && isWrecked() && !this.cargo.isEmpty()) {
            Containers.dropContents(this.level(), this, this.cargo);
        }
    }

    @Override
    protected double[][] seatOffsets() {
        return SEATS;
    }

    @Override
    public boolean hasGun() {
        return false;
    }

    @Override
    public boolean isArmored() {
        return false;
    }

    @Override
    protected float[] segmentOffsets() {
        return SEGMENTS;
    }

    @Override
    protected EntityType<? extends HullSegmentEntity> segmentType() {
        return ArmoredCarMod.TRUCK_SEGMENT;
    }

    @Override
    public double wheelbase() {
        return WHEELBASE;
    }

    @Override
    public double wheelRadius() {
        return WHEEL_RADIUS;
    }

    @Override
    protected float maxSpeed() {
        return ModConfig.get().truckMaxSpeed;
    }

    @Override
    protected float maxReverseSpeed() {
        return ModConfig.get().truckMaxReverseSpeed;
    }

    @Override
    protected float acceleration() {
        return ModConfig.get().truckAcceleration;
    }

    @Override
    public float maxSteerAngle() {
        return ModConfig.get().truckMaxSteerAngle;
    }

    @Override
    public float getMaxHealth() {
        return ModConfig.get().truckMaxHealth;
    }

    @Override
    protected Item vehicleItem() {
        return isCovered() ? ArmoredCarMod.DAIMLER_DR_ITEM : ArmoredCarMod.DAIMLER_DR_OPEN_ITEM;
    }

    @Override
    public String seatKey(int seat) {
        return "seat.exodus_vehicles.truck." + seat;
    }

    @Override
    public String healthHudKey() {
        return "hud.exodus_vehicles.body";
    }

    @Override
    protected double[] exhaustOffset() {
        return new double[]{0.6, 0.6, -1.35};
    }

    @Override
    protected double[] engineOffset() {
        return new double[]{0.0, 1.8, 2.5};
    }
}
