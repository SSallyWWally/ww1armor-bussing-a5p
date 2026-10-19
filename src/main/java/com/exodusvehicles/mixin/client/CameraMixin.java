package com.exodusvehicles.mixin.client;

import com.exodusvehicles.client.ArtilleryClient;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    private boolean detached;

    @Shadow
    protected abstract void setPosition(Vec3 pos);

    @Shadow
    protected abstract void setRotation(float yRot, float xRot);

    @Shadow
    public abstract float getYRot();

    @Shadow
    public abstract float getXRot();

    /** Artillery aiming / shell cameras (pure client view; the player entity never moves) and screen shake. */
    @Inject(method = "setup", at = @At("TAIL"))
    private void exodus_vehicles$artilleryCamera(BlockGetter level, Entity entity, boolean detached, boolean mirror,
                                                 float partialTick, CallbackInfo ci) {
        double[] view = ArtilleryClient.cameraView(partialTick);
        if (view != null) {
            this.detached = true;          // draw the operator at the gun
            this.setPosition(new Vec3(view[0], view[1], view[2]));
            this.setRotation((float) view[3], (float) view[4]);
        }
        float[] shake = ArtilleryClient.shake(partialTick);
        if (shake != null) {
            this.setRotation(this.getYRot() + shake[0], this.getXRot() + shake[1]);
        }
    }
}
