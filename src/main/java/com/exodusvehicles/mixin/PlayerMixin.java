package com.exodusvehicles.mixin;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {
    /** No tapping sneak to fall out of an aircraft in flight: bailing out is a 2 s hold (handled by the aircraft). */
    @Inject(method = "wantsToStopRiding", at = @At("HEAD"), cancellable = true)
    private void exodus_vehicles$holdToBail(CallbackInfoReturnable<Boolean> cir) {
        Player self = (Player) (Object) this;
        if (self.getVehicle() instanceof com.exodusvehicles.entity.PlaneEntity plane && !plane.onGround() && !plane.isWrecked()) {
            cir.setReturnValue(false);
        }
    }
}
