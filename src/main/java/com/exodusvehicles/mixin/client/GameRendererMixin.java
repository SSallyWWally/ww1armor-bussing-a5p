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
    private void exodus$sightZoom(CallbackInfoReturnable<Double> cir) {
        double scale = GunSight.fovScale();
        if (scale != 1.0) {
            cir.setReturnValue(cir.getReturnValueD() * scale);
        }
    }

    /** No hand / held item drawn over the sight. */
    @Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
    private void exodus$hideHand(CallbackInfo ci) {
        if (GunSight.inSight()) {
            ci.cancel();
        }
    }
}
