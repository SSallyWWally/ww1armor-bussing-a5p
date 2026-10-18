package com.exodusvehicles.client;

import com.exodusvehicles.entity.ArmoredCarEntity;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

/**
 * Looping vehicle sound that follows the car.
 * ENGINE: six-cylinder petrol engine, runs while someone is in the driver seat, pitch rises with speed.
 * ROLL: wheels/gravel rumble, volume follows speed.
 */
public class CarSoundInstance extends AbstractTickableSoundInstance {
    public enum Kind { ENGINE, ROLL }

    private final ArmoredCarEntity car;
    private final Kind kind;
    private float level;
    private int driverTicks;

    public CarSoundInstance(ArmoredCarEntity car, SoundEvent sound, Kind kind) {
        super(sound, SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.car = car;
        this.kind = kind;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.0F;
        this.x = car.getX();
        this.y = car.getY();
        this.z = car.getZ();
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public boolean canPlaySound() {
        return !this.car.isSilent();
    }

    @Override
    public void tick() {
        if (this.car.isRemoved() || this.car.isWrecked()) {
            this.stop();
            return;
        }
        this.x = this.car.getX();
        this.y = this.car.getY() + 1.0;
        this.z = this.car.getZ();
        float speed = this.car.clientSpeed;
        float target;
        if (this.kind == Kind.ENGINE) {
            this.driverTicks = this.car.isEngineOn() ? this.driverTicks + 1 : 0;
            // idle loop comes in under the tail of the start-up sound; pitch rises with road speed
            target = this.driverTicks > 26 ? 0.6F + Math.min(speed, 0.5F) * 0.7F : 0.0F;
            this.pitch = 1.0F + Math.min(speed, 0.5F) * 1.0F;
        } else {
            target = Mth.clamp(speed * 1.8F, 0.0F, 0.75F);
            this.pitch = 0.55F + Math.min(speed, 0.5F) * 0.8F;
        }
        this.level += (target - this.level) * 0.15F;
        this.volume = this.level;
    }
}
