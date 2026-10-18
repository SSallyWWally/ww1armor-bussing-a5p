package com.ww1armor.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

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

        boolean gunnerView = interior && GunSight.inSight() && car.seatOf(mc.player) == ArmoredCarEntity.GUNNER;

        ResourceLocation tex = getTextureLocation(car);
        this.model.setupAnim(car, partialTick, gunnerView ? mc.player : null);
        this.model.render(poseStack, buffers.getBuffer(RenderType.entityCutoutNoCull(tex)),
                packedLight, OverlayTexture.NO_OVERLAY, interior, gunnerView);
        if (car.flashTicks > 0 && !car.isWrecked() && (!interior || gunnerView)) {
            this.model.renderFlash(poseStack, buffers.getBuffer(RenderType.entityCutoutNoCull(tex)), OverlayTexture.NO_OVERLAY);
        }
        poseStack.popPose();
        renderTracers(car, partialTick, poseStack, buffers);
        super.render(car, entityYaw, partialTick, poseStack, buffers, packedLight);
    }

    /**
     * Tracer rounds: a short glowing streak (hot core + orange glow) flying from the barrel to the impact point.
     * Drawn as camera-facing ribbons with additive blending, relative to the car's render origin.
     */
    private void renderTracers(ArmoredCarEntity car, float partialTick, PoseStack poseStack, MultiBufferSource buffers) {
        if (car.tracers.isEmpty()) {
            return;
        }
        Vec3 origin = new Vec3(Mth.lerp(partialTick, car.xo, car.getX()), Mth.lerp(partialTick, car.yo, car.getY()),
                Mth.lerp(partialTick, car.zo, car.getZ()));
        Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        VertexConsumer vc = buffers.getBuffer(RenderType.lightning());
        Matrix4f m = poseStack.last().pose();
        for (ArmoredCarEntity.Tracer t : car.tracers) {
            Vec3 path = t.end.subtract(t.start);
            double len = path.length();
            if (len < 0.5) {
                continue;
            }
            Vec3 dir = path.scale(1.0 / len);
            double head = (t.age + partialTick) * ArmoredCarEntity.TRACER_SPEED;
            double tail = head - 3.5;
            if (tail >= len) {
                continue;
            }
            Vec3 a = t.start.add(dir.scale(Math.max(0.0, tail)));
            Vec3 b = t.start.add(dir.scale(Math.min(len, head)));
            ribbon(vc, m, a.subtract(origin), b.subtract(origin), cam.subtract(origin), 0.11F, 255, 120, 30, 70);
            ribbon(vc, m, a.subtract(origin), b.subtract(origin), cam.subtract(origin), 0.035F, 255, 235, 190, 230);
        }
    }

    private static void ribbon(VertexConsumer vc, Matrix4f m, Vec3 a, Vec3 b, Vec3 cam, float width,
                               int r, int g, int bl, int alpha) {
        Vec3 axis = b.subtract(a);
        Vec3 view = a.add(b).scale(0.5).subtract(cam);
        Vec3 side = axis.cross(view);
        if (side.lengthSqr() < 1.0E-8) {
            return;
        }
        side = side.normalize().scale(width);
        // tail end fades out, head end is bright
        quad(vc, m, a.subtract(side), a.add(side), b.add(side), b.subtract(side), r, g, bl, 0, alpha);
        quad(vc, m, b.subtract(side), b.add(side), a.add(side), a.subtract(side), r, g, bl, alpha, 0);
    }

    private static void quad(VertexConsumer vc, Matrix4f m, Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3,
                             int r, int g, int b, int a01, int a23) {
        vc.addVertex(m, (float) p0.x, (float) p0.y, (float) p0.z).setColor(r, g, b, a01);
        vc.addVertex(m, (float) p1.x, (float) p1.y, (float) p1.z).setColor(r, g, b, a01);
        vc.addVertex(m, (float) p2.x, (float) p2.y, (float) p2.z).setColor(r, g, b, a23);
        vc.addVertex(m, (float) p3.x, (float) p3.y, (float) p3.z).setColor(r, g, b, a23);
    }

    @Override
    public ResourceLocation getTextureLocation(ArmoredCarEntity car) {
        return car.isWrecked() ? TEXTURE_WRECKED : TEXTURE;
    }
}
