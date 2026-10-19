package com.exodusvehicles.client;

import com.exodusvehicles.entity.MorserEntity;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/** Gears and handwheels: plays while the howitzer's barrel is being traversed or elevated, fades out when it stops. */
public class GunCrankSound extends AbstractTickableSoundInstance {
    private final MorserEntity gun;
    private float lastElev, lastTrav, level;
    private int moving;

    public GunCrankSound(MorserEntity gun, SoundEvent sound) {
        super(sound, SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.gun = gun;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.0F;
        this.lastElev = gun.elevVis;
        this.lastTrav = gun.getTraverse();
        this.x = gun.getX();
        this.y = gun.getY() + 1.2;
        this.z = gun.getZ();
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public void tick() {
        if (this.gun.isRemoved()) {
            this.stop();
            return;
        }
        Vec3 at = this.gun.localToWorld(0.0, 1.3, 0.0);
        this.x = at.x;
        this.y = at.y;
        this.z = at.z;
        float e = this.gun.elevVis, t = this.gun.getTraverse();
        boolean turning = Math.abs(e - this.lastElev) > 1.0E-3F || Math.abs(t - this.lastTrav) > 1.0E-3F;
        this.lastElev = e;
        this.lastTrav = t;
        // synced values arrive in steps: keep running for a few ticks between updates
        this.moving = turning ? 4 : Math.max(0, this.moving - 1);
        float target = this.moving > 0 ? 1.0F : 0.0F;
        this.level += (target - this.level) * (target > this.level ? 0.5F : 0.25F);
        this.volume = this.level;
    }
}
