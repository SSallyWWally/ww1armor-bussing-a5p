package com.ww1armor.mixin.client;

import com.ww1armor.client.GunSight;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
    /** The iron sight replaces the vanilla crosshair. */
    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void ww1armor$hideCrosshair(CallbackInfo ci) {
        if (GunSight.inSight()) {
            ci.cancel();
        }
    }

    /** Hotbar (and the bars around it) hidden while manning the gun sight. */
    @Inject(method = "renderHotbarAndDecorations", at = @At("HEAD"), cancellable = true)
    private void ww1armor$hideHotbar(CallbackInfo ci) {
        if (GunSight.inSight()) {
            ci.cancel();
        }
    }
}
