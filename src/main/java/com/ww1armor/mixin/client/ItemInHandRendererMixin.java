package com.ww1armor.mixin.client;

import com.ww1armor.client.GunSight;
import net.minecraft.client.renderer.ItemInHandRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    /** No fist / held item over the gun sight. This is also the path shader mods (Iris) use for hands. */
    @Inject(method = "renderHandsWithItems", at = @At("HEAD"), cancellable = true)
    private void ww1armor$hideHands(CallbackInfo ci) {
        if (GunSight.inSight()) {
            ci.cancel();
        }
    }
}
