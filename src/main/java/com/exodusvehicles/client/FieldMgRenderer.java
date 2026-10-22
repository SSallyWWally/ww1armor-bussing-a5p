package com.exodusvehicles.client;

import com.exodusvehicles.ArmoredCarMod;
import com.exodusvehicles.entity.FieldMgEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class FieldMgRenderer extends EntityRenderer<FieldMgEntity> {
    private static final ResourceLocation TEXTURE = ArmoredCarMod.id("textures/entity/mg08.png");
    private final FieldMgModel model;

    public FieldMgRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.7F;
        this.model = new FieldMgModel(context.bakeLayer(FieldMgModel.LAYER));
    }

    @Override
    public void render(FieldMgEntity mg, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        poseStack.pushPose();
        float yaw = Mth.rotLerp(partialTick, mg.yRotO, mg.getYRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        float rel = mg.aimYaw(), pitch = mg.aimPitch();
        Minecraft mc = Minecraft.getInstance();
        boolean gunner = mc.player != null && mg.hasPassenger(mc.player);
        if (gunner) {
            rel = mg.relYaw(mc.player.getViewYRot(partialTick));
            pitch = FieldMgEntity.clampPitch(mc.player.getViewXRot(partialTick));
        }
        this.model.setupAnim(mg.hasShield(), rel, pitch, Mth.lerp(partialTick, mg.recoilO, mg.recoil));
        this.model.render(poseStack, buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), packedLight, OverlayTexture.NO_OVERLAY);
        if (mg.flashTicks > 0) {
            org.joml.Vector3f[] mz = this.model.muzzle(poseStack);
            ArmoredCarRenderer.renderFlash(mg.flashTicks, mg.flashStyle, mg.flashScale, mg.flashSpin, partialTick, mz[0], mz[1],
                    buffers, gunner && mc.options.getCameraType().isFirstPerson());
        }
        poseStack.popPose();
        ArmoredCarRenderer.renderTracers(mg.tracers, mg, partialTick, poseStack, buffers);
        super.render(mg, entityYaw, partialTick, poseStack, buffers, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(FieldMgEntity mg) {
        return TEXTURE;
    }
}
