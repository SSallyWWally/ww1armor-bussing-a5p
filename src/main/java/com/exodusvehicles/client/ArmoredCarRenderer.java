package com.exodusvehicles.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.exodusvehicles.ArmoredCarMod;
import com.exodusvehicles.entity.ArmoredCarEntity;
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
    private static final ResourceLocation TEXTURE = ArmoredCarMod.id("textures/entity/eisenwolf.png");
    private static final ResourceLocation TEXTURE_WRECKED = ArmoredCarMod.id("textures/entity/eisenwolf_wrecked.png");
    /** 4 styles (columns) x 3 stages (rows): ignition, full burst, dying smoke and embers. 32 px frames. */
    private static final ResourceLocation FLASH = ArmoredCarMod.id("textures/entity/muzzle_flash.png");

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
            renderMuzzleFlash(car, partialTick, this.model.muzzlePoint(poseStack), buffers, gunnerView);
        }
        poseStack.popPose();
        renderTracers(car, partialTick, poseStack, buffers);
        super.render(car, entityYaw, partialTick, poseStack, buffers, packedLight);
    }

    /**
     * Flat, camera-facing muzzle flash. Each round plays three stages over its 2 ticks: a small hot ignition,
     * the full starburst, then embers in a puff of smoke (seen at the end of a burst). Style, size and spin vary per round.
     */
    private void renderMuzzleFlash(ArmoredCarEntity car, float partialTick, org.joml.Vector3f at,
                                   MultiBufferSource buffers, boolean gunnerView) {
        float age = (2 - car.flashTicks) + partialTick;
        int stage = age < 0.45F ? 0 : age < 1.3F ? 1 : 2;
        float size = 0.42F * car.flashScale * (stage == 0 ? 0.6F : stage == 1 ? 1.0F : 1.25F) * (gunnerView ? 0.5F : 1.0F);
        float u0 = car.flashStyle * 0.25F, v0 = stage / 3.0F, u1 = u0 + 0.25F, v1 = v0 + 1.0F / 3.0F;

        org.joml.Quaternionf camRot = Minecraft.getInstance().gameRenderer.getMainCamera().rotation();
        float c = Mth.cos(car.flashSpin), s = Mth.sin(car.flashSpin);
        float[][] corners = {{-1, -1}, {1, -1}, {1, 1}, {-1, 1}};
        float[][] uvs = {{u1, v1}, {u0, v1}, {u0, v0}, {u1, v0}};
        VertexConsumer vc = buffers.getBuffer(RenderType.entityTranslucentEmissive(FLASH));
        for (int pass = 0; pass < 2; pass++) {               // both faces
            for (int k = 0; k < 4; k++) {
                int i = pass == 0 ? k : 3 - k;
                float x = corners[i][0] * size, y = corners[i][1] * size;
                org.joml.Vector3f p = new org.joml.Vector3f(x * c - y * s, x * s + y * c, 0.0F).rotate(camRot).add(at);
                vc.addVertex(p.x(), p.y(), p.z()).setColor(255, 255, 255, 255).setUv(uvs[i][0], uvs[i][1])
                        .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 1.0F, 0.0F);
            }
        }
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
