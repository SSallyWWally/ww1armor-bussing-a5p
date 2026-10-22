package com.exodusvehicles.client;

import com.exodusvehicles.entity.PlaneEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * One layer of a plane's engine: low or high revs, outside or in the cockpit, or the distant drone. Volumes are worked
 * out here from revs and distance (no OpenAL roll-off), so the layers crossfade smoothly as it climbs away.
 */
public class PlaneLoopSound extends AbstractTickableSoundInstance {
    public enum Kind { LOW_EXT, HIGH_EXT, LOW_INT, HIGH_INT, DISTANT }

    private final PlaneEntity plane;
    private final Kind kind;
    private float rpm;

    public PlaneLoopSound(PlaneEntity plane, SoundEvent sound, Kind kind) {
        super(sound, SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.plane = plane;
        this.kind = kind;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.0F;
        this.attenuation = SoundInstance.Attenuation.NONE;
        boolean inside = kind == Kind.LOW_INT || kind == Kind.HIGH_INT;
        this.relative = inside;
        if (!inside) {
            this.x = plane.getX();
            this.y = plane.getY();
            this.z = plane.getZ();
        }
    }

    public boolean isFor(PlaneEntity p) {
        return this.plane == p;
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public void tick() {
        if (this.plane.isRemoved()) {
            this.stop();
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            this.stop();
            return;
        }
        boolean running = this.plane.isEngineOn();
        float want = running ? 0.15F + 0.85F * this.plane.throttle : 0.0F;
        this.rpm += (want - this.rpm) * 0.06F;
        boolean mine = this.plane.hasPassenger(mc.player);
        boolean cockpit = mine && mc.options.getCameraType().isFirstPerson();
        double d = mine ? 0.0 : mc.gameRenderer.getMainCamera().getPosition().distanceTo(this.plane.position());
        float hi = Mth.clamp((this.rpm - 0.2F) / 0.6F, 0.0F, 1.0F);
        float level = running ? Mth.clamp(this.rpm * 3.0F, 0.0F, 1.0F) : 0.0F;
        float near = (float) Mth.clamp(1.0 - d / 200.0, 0.0, 1.0);
        near *= near;
        float far = (float) (Mth.clamp((d - 60.0) / 80.0, 0.0, 1.0) * Mth.clamp(1.0 - (d - 300.0) / 300.0, 0.0, 1.0));
        float v;
        switch (this.kind) {
            case LOW_EXT -> v = cockpit ? 0.0F : (1.0F - hi) * near * level;
            case HIGH_EXT -> v = cockpit ? 0.0F : hi * near * level;
            case LOW_INT -> v = cockpit ? (1.0F - hi) * level : 0.0F;
            case HIGH_INT -> v = cockpit ? hi * level : 0.0F;
            default -> v = mine ? 0.0F : far * level * 0.9F;
        }
        this.volume = v * 0.5F;
        this.pitch = 0.85F + 0.3F * this.rpm + Math.min(0.15F, this.plane.syncedSpeed() * 0.05F);
        if (!this.relative) {
            Vec3 p = this.plane.position();
            this.x = p.x;
            this.y = p.y + PlaneEntity.CG_UP;
            this.z = p.z;
        }
    }
}
