package com.exodusvehicles.mixin.client;

import com.exodusvehicles.client.GunSight;
import net.minecraft.client.renderer.ItemInHandRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    /** No fist / held item over the gun sight. This is also the path shader mods (Iris) use for hands. */
    @Inject(method = "renderHandsWithItems", at = @At("HEAD"), cancellable = true)
    private void exodus_vehicles$hideHands(CallbackInfo ci) {
        if (GunSight.inSight() || com.exodusvehicles.client.ArtilleryClient.cameraActive()) {
            ci.cancel();
        }
    }
}
