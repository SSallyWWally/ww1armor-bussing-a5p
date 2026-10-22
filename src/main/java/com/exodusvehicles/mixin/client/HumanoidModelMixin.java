package com.exodusvehicles.mixin.client;

import com.exodusvehicles.client.ArtilleryClient;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin {
    /** Crew at a howitzer stand rather than sit (vanilla poses every passenger seated). */
    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("HEAD"))
    private void exodus_vehicles$standAtGun(LivingEntity entity, float limbSwing, float limbSwingAmount, float age,
                                            float headYaw, float headPitch, CallbackInfo ci) {
        if (entity.getVehicle() instanceof com.exodusvehicles.entity.MorserEntity) {
            ((HumanoidModel<?>) (Object) this).riding = false;
        }
    }

    /** Gunner at a howitzer: standing, right hand on the breech, left arm loose. */
    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void exodus_vehicles$gunnerPose(LivingEntity entity, float limbSwing, float limbSwingAmount, float age,
                                            float headYaw, float headPitch, CallbackInfo ci) {
        float[] mgPose = com.exodusvehicles.client.FieldMgClient.armPose(entity);
        if (mgPose != null) {
            HumanoidModel<?> self = (HumanoidModel<?>) (Object) this;     // seated behind the MG 08, hands on the grips
            self.rightArm.xRot = mgPose[0];
            self.rightArm.yRot = mgPose[1];
            self.rightArm.zRot = 0.0F;
            self.leftArm.xRot = mgPose[2];
            self.leftArm.yRot = mgPose[3];
            self.leftArm.zRot = 0.0F;
            return;
        }
        float[] pose = ArtilleryClient.gunnerArmPose(entity);
        if (pose == null) {
            return;
        }
        HumanoidModel<?> self = (HumanoidModel<?>) (Object) this;
        self.rightArm.xRot = pose[0];
        self.rightArm.yRot = pose[1];
        self.rightArm.zRot = 0.0F;
        self.leftArm.xRot = pose[2];
        self.leftArm.yRot = 0.0F;
        self.leftArm.zRot = pose[3];
        // standing still at the gun
        self.rightLeg.xRot = 0.0F;
        self.leftLeg.xRot = 0.0F;
        self.rightLeg.yRot = 0.0F;
        self.leftLeg.yRot = 0.0F;
    }
}
