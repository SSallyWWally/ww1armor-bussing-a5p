package com.exodusvehicles.mixin.client;

import com.exodusvehicles.client.GunSight;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    /** Gun sight zoom. */
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void exodus_vehicles$sightZoom(CallbackInfoReturnable<Double> cir) {
        double scale = GunSight.fovScale();
        if (scale != 1.0) {
            cir.setReturnValue(cir.getReturnValueD() * scale);
        }
    }

    /** Banking in the cockpit tilts the view. */
    @Inject(method = "bobHurt", at = @At("HEAD"))
    private void exodus_vehicles$planeRoll(com.mojang.blaze3d.vertex.PoseStack poseStack, float partialTick, CallbackInfo ci) {
        float roll = com.exodusvehicles.client.PlaneClient.viewRoll();
        if (roll != 0.0F) {
            poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(roll));
        }
    }

    /** No hand / held item drawn over the sight. */
    @Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
    private void exodus_vehicles$hideHand(CallbackInfo ci) {
        if (GunSight.inSight() || com.exodusvehicles.client.ArtilleryClient.cameraActive()) {
            ci.cancel();
        }
    }
}
