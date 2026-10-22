package com.exodusvehicles.client;

import com.exodusvehicles.ArmoredCarMod;
import com.exodusvehicles.entity.PlaneEntity;
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

public class PlaneRenderer extends EntityRenderer<PlaneEntity> {
    private static final ResourceLocation TEXTURE = ArmoredCarMod.id("textures/entity/fokker_dr1.png");
    private static final ResourceLocation TEXTURE_BURNT = ArmoredCarMod.id("textures/entity/fokker_dr1_wrecked.png");
    private final FokkerModel model;
    /** True while the aircraft draws its own pilot (lets that one drawing through the pilot-hiding mixin). */
    public static boolean DRAWING_PILOT;

    public PlaneRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 2.5F;
        this.model = new FokkerModel(context.bakeLayer(FokkerModel.LAYER));
    }

    @Override
    public void render(PlaneEntity plane, float entityYaw, float pt, PoseStack poseStack, MultiBufferSource buffers, int light) {
        poseStack.pushPose();
        float yaw = Mth.rotLerp(pt, plane.yRotO, plane.getYRot());
        float pitch = Mth.lerp(pt, plane.xRotO, plane.getXRot()) - Mth.lerp(pt, plane.groundTiltO, plane.groundTilt);
        float roll = Mth.lerp(pt, plane.rollO, plane.roll);
        poseStack.translate(0.0, PlaneEntity.CG_UP, 0.0);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(-pitch));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-roll));
        Minecraft mc0 = Minecraft.getInstance();
        if (plane.getPilot() != null && !(plane.hasPassenger(mc0.player) && mc0.options.getCameraType().isFirstPerson())) {
            // the pilot, seated in the cockpit and banking with the aircraft (his own upright drawing is suppressed)
            net.minecraft.world.entity.player.Player pilot = plane.getPilot();
            poseStack.pushPose();
            poseStack.translate(0.0, PlaneEntity.PILOT_UP, PlaneEntity.PILOT_BACK);
            poseStack.mulPose(Axis.YP.rotationDegrees(Mth.rotLerp(pt, pilot.yBodyRotO, pilot.yBodyRot) - 180.0F));
            DRAWING_PILOT = true;
            try {
                mc0.getEntityRenderDispatcher().render(pilot, 0.0, 0.0, 0.0, pilot.getYRot(), pt, poseStack, buffers, light);
            } finally {
                DRAWING_PILOT = false;
            }
            poseStack.popPose();
        }
        poseStack.scale(PlaneEntity.SCALE, PlaneEntity.SCALE, PlaneEntity.SCALE);
        poseStack.translate(0.0, -22.0 / 16.0, 0.0);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        Minecraft mc = Minecraft.getInstance();
        boolean mine = mc.player != null && plane.hasPassenger(mc.player);
        this.model.setupAnim(Mth.lerp(pt, plane.propRotO, plane.propRot), false, plane.isWrecked());
        this.model.render(poseStack, buffers.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(plane))), light, OverlayTexture.NO_OVERLAY);
        if (plane.flashTicks > 0) {
            // both guns' muzzles (model units: x left, y down from 24, z backward)
            for (float x : new float[]{2.0F, -2.0F}) {
                org.joml.Matrix4f m = poseStack.last().pose();
                org.joml.Vector3f tip = m.transformPosition(new org.joml.Vector3f(x / 16.0F, (24.0F - 31.1F) / 16.0F, -14.8F / 16.0F));
                org.joml.Vector3f ahead = m.transformPosition(new org.joml.Vector3f(x / 16.0F, (24.0F - 31.1F) / 16.0F, -15.8F / 16.0F));
                ArmoredCarRenderer.renderFlash(plane.flashTicks, plane.flashStyle, plane.flashScale * 0.8F, plane.flashSpin, pt, tip,
                        ahead.sub(tip).normalize(), buffers, mine && mc.options.getCameraType().isFirstPerson());
            }
        }
        poseStack.popPose();
        ArmoredCarRenderer.renderTracers(plane.tracers, plane, pt, poseStack, buffers);
        super.render(plane, entityYaw, pt, poseStack, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(PlaneEntity plane) {
        return plane.isDowned() ? TEXTURE_BURNT : TEXTURE;
    }
}
