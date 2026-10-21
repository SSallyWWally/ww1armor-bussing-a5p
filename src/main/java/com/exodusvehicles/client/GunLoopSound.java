package com.exodusvehicles.client;

import com.exodusvehicles.entity.MorserEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * A looping howitzer sound that fades in while something is happening and out when it stops:
 * AIM = barrel traversing / elevating, PUSH = the crew manhandling the packed gun along.
 */
public class GunLoopSound extends AbstractTickableSoundInstance {
    public enum Kind { AIM, PUSH }

    private final MorserEntity gun;
    private final Kind kind;
    private float a0 = Float.NaN, a1, level;
    private double px, pz;
    private float pyaw;
    private int hold;

    public GunLoopSound(MorserEntity gun, SoundEvent sound, Kind kind) {
        super(sound, SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.gun = gun;
        this.kind = kind;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.0F;
        this.x = gun.getX();
        this.y = gun.getY() + 1.0;
        this.z = gun.getZ();
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    public boolean isFor(MorserEntity g) {
        return this.gun == g;
    }

    @Override
    public void tick() {
        if (this.gun.isRemoved()) {
            this.stop();
            return;
        }
        Vec3 at = this.gun.localToWorld(0.0, 1.2, 0.0);
        this.x = at.x;
        this.y = at.y;
        this.z = at.z;
        boolean busy;
        if (this.kind == Kind.AIM) {
            float t, r;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && this.gun.isOperatedBy(mc.player)) {
                t = ArtilleryClient.localTraverse(this.gun, mc.player, 1.0F);      // the operator hears their own aim at once
                r = this.gun.shownElevation(ArtilleryClient.localRange(this.gun, mc.player, 1.0F));
            } else {
                t = this.gun.getTraverse();
                r = this.gun.elevVis;
            }
            busy = !Float.isNaN(this.a0) && (Math.abs(t - this.a0) > 0.02F || Math.abs(r - this.a1) > 0.02F);
            this.a0 = t;
            this.a1 = r;
        } else {
            double dx = this.gun.getX() - this.px, dz = this.gun.getZ() - this.pz;
            float dyaw = Math.abs(this.gun.getYRot() - this.pyaw);
            busy = !this.gun.isTowed() && this.gun.tickCount > 2
                    && (dx * dx + dz * dz > 1.0E-4 || dyaw > 0.05F);
            this.px = this.gun.getX();
            this.pz = this.gun.getZ();
            this.pyaw = this.gun.getYRot();
        }
        this.hold = busy ? 4 : Math.max(0, this.hold - 1);
        float target = this.hold > 0 ? 1.0F : 0.0F;
        this.level += (target - this.level) * (target > this.level ? 0.5F : 0.25F);
        this.volume = this.level;
    }
}
