package com.exodusvehicles.client;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

/** Rushing air that travels with a shell in flight; ends when the shell does. */
public class ShellFlightSound extends AbstractTickableSoundInstance {
    private final Supplier<Vec3> where;
    private int fade = -1;

    public ShellFlightSound(SoundEvent sound, Supplier<Vec3> where) {
        super(sound, SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.where = where;
        this.looping = true;
        this.delay = 0;
        this.volume = 1.0F;
        Vec3 p = where.get();
        if (p != null) {
            this.x = p.x;
            this.y = p.y;
            this.z = p.z;
        }
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public void tick() {
        Vec3 p = this.where.get();
        if (p == null && this.fade < 0) {
            this.fade = 4;
        }
        if (this.fade >= 0) {
            this.volume = this.fade / 4.0F;
            if (--this.fade < 0) {
                this.stop();
            }
            return;
        }
        this.x = p.x;
        this.y = p.y;
        this.z = p.z;
    }
}
