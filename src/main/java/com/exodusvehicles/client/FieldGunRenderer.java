package com.exodusvehicles.client;

import com.exodusvehicles.ArmoredCarMod;
import com.exodusvehicles.entity.FieldGunEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class FieldGunRenderer extends EntityRenderer<FieldGunEntity> {
    private static final ResourceLocation TEXTURE = ArmoredCarMod.id("textures/entity/fk96.png");
    private final FieldGunModel model;

    public FieldGunRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 1.1F;
        this.model = new FieldGunModel(context.bakeLayer(FieldGunModel.LAYER));
    }

    @Override
    public void render(FieldGunEntity gun, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight) {
        poseStack.pushPose();
        float yaw = Mth.rotLerp(partialTick, gun.yRotO, gun.getYRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        // the local gunner's own gun follows their mouse directly (smooth); everyone else sees the synced aim
        float trav = gun.getTraverse();
        float elev = Mth.lerp(partialTick, gun.elevVisO, gun.elevVis);
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player != null && gun.isOperatedBy(mc.player)) {
            trav = ArtilleryClient.localTraverse(gun, mc.player, partialTick);
            elev = gun.shownElevation(ArtilleryClient.localRange(gun, mc.player, partialTick));
        }
        this.model.setupAnim(gun, partialTick, trav, elev);
        this.model.render(poseStack, buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(gun, entityYaw, partialTick, poseStack, buffers, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(FieldGunEntity gun) {
        return TEXTURE;
    }
}
