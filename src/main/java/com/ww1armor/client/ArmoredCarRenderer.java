package com.ww1armor.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.ww1armor.ArmoredCarMod;
import com.ww1armor.entity.ArmoredCarEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ArmoredCarRenderer extends EntityRenderer<ArmoredCarEntity> {
    private static final ResourceLocation TEXTURE = ArmoredCarMod.id("textures/entity/bussing_a5p.png");
    private static final ResourceLocation TEXTURE_WRECKED = ArmoredCarMod.id("textures/entity/bussing_a5p_wrecked.png");

    private final ArmoredCarModel model;

    public ArmoredCarRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 2.6F;
        this.model = new ArmoredCarModel(context.bakeLayer(ArmoredCarModel.LAYER));
    }

    @Override
    public void render(ArmoredCarEntity car, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight) {
        poseStack.pushPose();
        float yaw = Mth.rotLerp(partialTick, car.yRotO, car.getYRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        if (car.isWrecked()) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(2.5F));      // sags onto the missing front wheel
            poseStack.mulPose(Axis.XP.rotationDegrees(-1.5F));
        }
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.501F, 0.0F);

        Minecraft mc = Minecraft.getInstance();
        boolean interior = mc.player != null && car.hasPassenger(mc.player)
                && mc.options.getCameraType().isFirstPerson();

        ResourceLocation tex = getTextureLocation(car);
        this.model.setupAnim(car, partialTick);
        this.model.render(poseStack, buffers.getBuffer(RenderType.entityCutoutNoCull(tex)),
                packedLight, OverlayTexture.NO_OVERLAY, interior);
        if (car.flashTicks > 0 && !car.isWrecked()) {
            this.model.renderFlash(poseStack, buffers.getBuffer(RenderType.entityCutoutNoCull(tex)), OverlayTexture.NO_OVERLAY);
        }
        poseStack.popPose();
        super.render(car, entityYaw, partialTick, poseStack, buffers, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ArmoredCarEntity car) {
        return car.isWrecked() ? TEXTURE_WRECKED : TEXTURE;
    }
}
