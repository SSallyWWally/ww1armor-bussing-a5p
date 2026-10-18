package com.exodusvehicles.entity;

import com.exodusvehicles.ArmoredCarMod;
import com.exodusvehicles.ModConfig;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * Daimler-Marienfelde Series DR, the German army's standard 3-4 t lorry (1915-18): 6.6 m long, 4.4 m wheelbase,
 * solid tyres on wooden spoked wheels, chain drive, canvas tilt over a drop-side bed.
 * Unarmoured and unarmed: every hit counts and the crew sit in the open.
 * Seat 0 = driver (cab, left), 1 = co-driver (cab, right), 2 + 3 = men on the benches under the canvas.
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

    public TruckEntity(EntityType<? extends TruckEntity> type, Level level) {
        super(type, level);
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
        return ArmoredCarMod.DAIMLER_DR_ITEM;
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
