package com.exodusvehicles.client;

import com.exodusvehicles.ArmoredCarMod;
import com.exodusvehicles.entity.TruckEntity;
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

public class TruckRenderer extends EntityRenderer<TruckEntity> {
    private static final ResourceLocation TEXTURE = ArmoredCarMod.id("textures/entity/daimler_dr.png");
    private static final ResourceLocation TEXTURE_WRECKED = ArmoredCarMod.id("textures/entity/daimler_dr_wrecked.png");

    private final TruckModel model;

    public TruckRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 2.0F;
        this.model = new TruckModel(context.bakeLayer(TruckModel.LAYER));
    }

    @Override
    public void render(TruckEntity truck, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight) {
        poseStack.pushPose();
        float yaw = Mth.rotLerp(partialTick, truck.yRotO, truck.getYRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        if (truck.isWrecked()) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(-2.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(1.0F));
        }
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.501F, 0.0F);

        Minecraft mc = Minecraft.getInstance();
        // the men in the back would otherwise be sitting inside the canvas
        boolean underTarp = mc.player != null && truck.hasPassenger(mc.player) && truck.seatOf(mc.player) >= 2
                && mc.options.getCameraType().isFirstPerson();

        this.model.setupAnim(truck, partialTick);
        this.model.render(poseStack, buffers.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(truck))),
                packedLight, OverlayTexture.NO_OVERLAY, underTarp || truck.isWrecked());
        poseStack.popPose();
        super.render(truck, entityYaw, partialTick, poseStack, buffers, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(TruckEntity truck) {
        return truck.isWrecked() ? TEXTURE_WRECKED : TEXTURE;
    }
}
