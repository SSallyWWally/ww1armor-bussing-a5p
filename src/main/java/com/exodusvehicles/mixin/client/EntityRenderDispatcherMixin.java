package com.exodusvehicles.mixin.client;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    /** The pilot is drawn as part of the aircraft (so he banks with it); his own upright model is hidden. */
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private <E extends Entity> void exodus_vehicles$hidePilot(E entity, double x, double y, double z, float yaw, float partialTick,
                                                              PoseStack poseStack, MultiBufferSource buffers, int light, CallbackInfo ci) {
        if (entity.getVehicle() instanceof com.exodusvehicles.entity.PlaneEntity && !com.exodusvehicles.client.PlaneRenderer.DRAWING_PILOT) {
            ci.cancel();
        }
    }
}
