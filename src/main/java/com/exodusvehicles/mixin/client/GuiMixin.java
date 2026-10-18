package com.exodusvehicles.mixin.client;

import com.exodusvehicles.client.GunSight;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
    /** The iron sight replaces the vanilla crosshair. */
    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void exodus$hideCrosshair(CallbackInfo ci) {
        if (GunSight.inSight()) {
            ci.cancel();
        }
    }

    /** Hotbar (and the bars around it) hidden while manning the gun sight. */
    @Inject(method = "renderHotbarAndDecorations", at = @At("HEAD"), cancellable = true)
    private void exodus$hideHotbar(CallbackInfo ci) {
        if (GunSight.inSight()) {
            ci.cancel();
        }
    }
}
