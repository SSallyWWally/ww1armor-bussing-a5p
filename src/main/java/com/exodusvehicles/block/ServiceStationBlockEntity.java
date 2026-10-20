package com.exodusvehicles.block;

import com.exodusvehicles.ArmoredCarMod;
import com.exodusvehicles.ModConfig;
import com.exodusvehicles.entity.ArmoredCarEntity;
import com.exodusvehicles.entity.MorserEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Services every stopped vehicle within range, a few times a second: hull repairs and fuel, at the same time. */
public class ServiceStationBlockEntity extends BlockEntity {
    public ServiceStationBlockEntity(BlockPos pos, BlockState state) {
        super(ArmoredCarMod.SERVICE_STATION_BE, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ServiceStationBlockEntity be) {
        long t = level.getGameTime();
        if (t % 5 != 0 || !(level instanceof ServerLevel server)) {
            return;
        }
        ModConfig cfg = ModConfig.get();
        double r = cfg.serviceStationRange;
        AABB area = new AABB(pos).inflate(r, 3.0, r);
        Vec3 depot = Vec3.atCenterOf(pos);
        for (Entity e : level.getEntities((Entity) null, area, e -> e instanceof ArmoredCarEntity || e instanceof MorserEntity)) {
            boolean repaired = false, fuelled = false;
            if (e instanceof ArmoredCarEntity car) {
                if (car.isWrecked() || car.recentMovement() > 0.05) {
                    continue;      // pull up and stop
                }
                repaired = car.repairBy(cfg.repairStationPerSecond / 4.0F);
                if (car.getFuel() < car.fuelCapacity()) {
                    car.addFuel(cfg.fuelStationLitresPerSecond / 4.0F);
                    fuelled = true;
                }
            } else if (e instanceof MorserEntity gun && !gun.isTowed()) {
                repaired = gun.repairBy(cfg.repairStationPerSecond / 4.0F);
            }
            if (!repaired && !fuelled) {
                continue;
            }
            // mechanics at work: sparks off the hull, the pump running
            if (repaired) {
                AABB b = e.getBoundingBox();
                double x = b.minX + server.random.nextDouble() * b.getXsize();
                double y = b.minY + 0.3 + server.random.nextDouble() * b.getYsize() * 0.6;
                double z = b.minZ + server.random.nextDouble() * b.getZsize();
                server.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 4, 0.15, 0.15, 0.15, 0.08);
                if (t % 20 == 0) {
                    server.playSound(null, x, y, z, ArmoredCarMod.REPAIR, SoundSource.BLOCKS, 0.8F, 0.95F + server.random.nextFloat() * 0.1F);
                }
            }
            if (fuelled && t % 30 == 0) {
                server.playSound(null, depot.x, depot.y, depot.z, ArmoredCarMod.REFUEL, SoundSource.BLOCKS, 0.9F, 1.0F);
            }
            if (t % 20 == 0) {
                Component msg = e instanceof ArmoredCarEntity car
                        ? Component.translatable("msg.exodus_vehicles.station", (int) Math.floor(car.getHealth()), (int) car.getMaxHealth(),
                        Math.round(car.getFuel()), Math.round(car.fuelCapacity()))
                        : Component.translatable("msg.exodus_vehicles.station_gun", (int) Math.floor(((MorserEntity) e).getHealth()),
                        (int) ((MorserEntity) e).getMaxHealth());
                for (Entity p : e.getPassengers()) {
                    if (p instanceof Player pl) {
                        pl.displayClientMessage(msg, true);
                    }
                }
            }
        }
    }
}
