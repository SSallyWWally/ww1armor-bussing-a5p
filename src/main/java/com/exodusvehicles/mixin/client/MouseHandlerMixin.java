package com.exodusvehicles.mixin.client;

import com.exodusvehicles.client.GunSight;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @Shadow private double accumulatedDX;
    @Shadow private double accumulatedDY;

    /** Slower aim while zoomed, like a spyglass. */
    @Inject(method = "turnPlayer", at = @At("HEAD"))
    private void exodus$zoomSensitivity(CallbackInfo ci) {
        double s = GunSight.sensitivityScale();
        if (s != 1.0) {
            this.accumulatedDX *= s;
            this.accumulatedDY *= s;
        }
    }
}
