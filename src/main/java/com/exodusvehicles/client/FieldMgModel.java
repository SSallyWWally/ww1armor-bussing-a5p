package com.exodusvehicles.client;

import com.exodusvehicles.ArmoredCarMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/** MG 08 on the Schlitten 08 sled - GENERATED from truck/mg08_geom.py + build_mg08.py. Do not edit the geometry by hand. */
public class FieldMgModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ArmoredCarMod.id("mg08"), "main");
    public static final ModelLayerLocation BUNKER_LAYER = new ModelLayerLocation(ArmoredCarMod.id("mg08_bunker"), "main");

    private final ModelPart root, mount, shield, gun;
    private float gunZ = Float.NaN;

    public FieldMgModel(ModelPart root) {
        this.root = root;
        this.mount = root.getChild("mount");
        this.shield = this.mount.getChild("shield");
        this.gun = this.mount.getChild("gun");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        build_sled(root);
        build_rail_l(root);
        build_leg_l(root);
        build_rail_r(root);
        build_leg_r(root);
        build_mount(root);
        return LayerDefinition.create(mesh, 256, 256);
    }

    private static void build_sled(PartDefinition root) {
        root.addOrReplaceChild("sled", CubeListBuilder.create()
                .texOffs(72, 0).addBox(3F, -1F, 3F, 1F, 1F, 14F)
                .texOffs(200, 18).addBox(3F, -2F, 16.5F, 1F, 1F, 1.5F)
                .texOffs(72, 0).addBox(-4F, -1F, 3F, 1F, 1F, 14F)
                .texOffs(200, 18).addBox(-4F, -2F, 16.5F, 1F, 1F, 1.5F)
                .texOffs(216, 18).addBox(-4F, -1F, 14F, 8F, 0.8F, 1F)
                .texOffs(216, 18).addBox(-4F, -1F, 5F, 8F, 0.8F, 1F)
                .texOffs(0, 18).addBox(-2.2F, -8F, -2.2F, 4.4F, 1.2F, 4.4F)
                .texOffs(182, 18).addBox(2F, -0.6F, -10.5F, 2.6F, 0.6F, 2F)
                .texOffs(182, 18).addBox(-4.6F, -0.6F, -10.5F, 2.6F, 0.6F, 2F)
                .texOffs(188, 0).addBox(5F, -3.2F, 4F, 4F, 3.2F, 5F)
                .texOffs(234, 18).addBox(6.4F, -3.6F, 6F, 1.2F, 0.4F, 1F)
                , PartPose.offsetAndRotation(0F, 24F, 0F, 0F, 0F, 0F));
    }

    private static void build_rail_l(PartDefinition root) {
        root.addOrReplaceChild("rail_l", CubeListBuilder.create()
                .texOffs(8, 0).addBox(2.2F, -0.5F, -14.49F, 1F, 1F, 14.49F)
                , PartPose.offsetAndRotation(0F, 23F, 14.5F, -0.4575F, 0F, 0F));
    }

    private static void build_leg_l(PartDefinition root) {
        root.addOrReplaceChild("leg_l", CubeListBuilder.create()
                .texOffs(102, 0).addBox(2.4F, -0.5F, -11.1396F, 1F, 1F, 11.1396F)
                , PartPose.offsetAndRotation(0F, 16.8F, -1F, 0.7028F, 0F, 0F));
    }

    private static void build_rail_r(PartDefinition root) {
        root.addOrReplaceChild("rail_r", CubeListBuilder.create()
                .texOffs(8, 0).addBox(-3.2F, -0.5F, -14.49F, 1F, 1F, 14.49F)
                , PartPose.offsetAndRotation(0F, 23F, 14.5F, -0.4575F, 0F, 0F));
    }

    private static void build_leg_r(PartDefinition root) {
        root.addOrReplaceChild("leg_r", CubeListBuilder.create()
                .texOffs(102, 0).addBox(-3.4F, -0.5F, -11.1396F, 1F, 1F, 11.1396F)
                , PartPose.offsetAndRotation(0F, 16.8F, -1F, 0.7028F, 0F, 0F));
    }

    private static void build_mount(PartDefinition root) {
        PartDefinition p6 = root.addOrReplaceChild("mount", CubeListBuilder.create()
                .texOffs(222, 0).addBox(-2.6F, -1F, -3F, 5.2F, 1F, 6F)
                .texOffs(100, 18).addBox(1.9F, -2.8F, -1.2F, 0.7F, 1.8F, 2.4F)
                .texOffs(100, 18).addBox(-2.6F, -2.8F, -1.2F, 0.7F, 1.8F, 2.4F)
                .texOffs(38, 18).addBox(1.2F, -2.6F, 3F, 1F, 2.4F, 3F)
                , PartPose.offsetAndRotation(0F, 16F, 0F, 0F, 0F, 0F));
        p6.addOrReplaceChild("shield", CubeListBuilder.create()
                .texOffs(46, 18).addBox(-8F, -0.8F, -4.8F, 16F, 3.3F, 0.6F)
                .texOffs(148, 18).addBox(-8F, -12F, -4.8F, 16F, 2F, 0.6F)
                .texOffs(172, 0).addBox(4.2F, -10F, -4.8F, 3.8F, 9.2F, 0.6F)
                .texOffs(172, 0).addBox(-8F, -10F, -4.8F, 3.8F, 9.2F, 0.6F)
                .texOffs(0, 0).addBox(7.6F, -12F, -4.8F, 0.6F, 14.5F, 2.6F)
                .texOffs(0, 0).addBox(-8.2F, -12F, -4.8F, 0.6F, 14.5F, 2.6F)
                .texOffs(206, 0).addBox(-0.6F, -1F, -4.2F, 1.2F, 1.5F, 5.2F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p6.addOrReplaceChild("gun", CubeListBuilder.create()
                .texOffs(128, 0).addBox(-1.5F, -3F, -0.5F, 3F, 3F, 7.5F)
                .texOffs(20, 18).addBox(-1.5F, -3.4F, 0.5F, 3F, 0.4F, 6F)
                .texOffs(108, 18).addBox(-1.2F, -2.4F, 7F, 2.4F, 2F, 1.2F)
                .texOffs(142, 18).addBox(0.7F, -2.2F, 8.2F, 0.8F, 1.6F, 1.4F)
                .texOffs(142, 18).addBox(-1.5F, -2.2F, 8.2F, 0.8F, 1.6F, 1.4F)
                .texOffs(40, 0).addBox(-1.8F, -3.2F, -12.5F, 3.6F, 3.6F, 12F)
                .texOffs(118, 18).addBox(-1F, -2.4F, -14F, 2F, 2F, 1.5F)
                .texOffs(126, 18).addBox(-1.3F, -2.7F, -15F, 2.6F, 2.6F, 1F)
                .texOffs(234, 18).addBox(-0.8F, -3.6F, -11.8F, 1.6F, 0.4F, 1F)
                .texOffs(212, 18).addBox(-0.25F, -4.7F, -11.5F, 0.5F, 1.1F, 0.5F)
                .texOffs(252, 18).addBox(-0.12F, -5F, -11.45F, 0.24F, 0.3F, 0.4F)
                .texOffs(212, 18).addBox(0.55F, -5.4F, -11.8F, 0.25F, 1.8F, 1F)
                .texOffs(212, 18).addBox(-0.8F, -5.4F, -11.8F, 0.25F, 1.8F, 1F)
                .texOffs(206, 18).addBox(-0.9F, -4.75F, 2.8F, 1.8F, 1.35F, 0.4F)
                .texOffs(252, 18).addBox(0.25F, -5.7F, 2.8F, 0.7F, 0.95F, 0.4F)
                .texOffs(252, 18).addBox(-0.95F, -5.7F, 2.8F, 0.7F, 0.95F, 0.4F)
                .texOffs(192, 18).addBox(-1F, -3.7F, 2.4F, 2F, 0.3F, 1.2F)
                .texOffs(0, 25).addBox(0.9F, -3.9F, 2.6F, 0.4F, 0.5F, 0.6F)
                .texOffs(80, 18).addBox(-2F, -3.4F, -1.3F, 4F, 4F, 0.8F)
                .texOffs(80, 18).addBox(-2F, -3.4F, -12.5F, 4F, 4F, 0.8F)
                .texOffs(150, 0).addBox(1.8F, -1.4F, -11.7F, 0.6F, 0.6F, 9.7F)
                .texOffs(4, 25).addBox(-2.3F, -1.8F, 4.8F, 0.8F, 0.8F, 0.8F)
                .texOffs(134, 18).addBox(-2.5F, -1.6F, 5.6F, 0.4F, 0.6F, 2.4F)
                .texOffs(240, 18).addBox(-0.7F, -3.6F, -3F, 1.4F, 0.4F, 1F)
                .texOffs(90, 18).addBox(-2.8F, -2.6F, 0.5F, 1.3F, 2F, 2.5F)
                .texOffs(182, 0).addBox(-3.4F, -2F, 1F, 0.8F, 8.5F, 1.4F)
                .texOffs(246, 18).addBox(1.5F, -1.2F, 1.6F, 1.9F, 1F, 1F)
                , PartPose.offsetAndRotation(0F, -2F, 0F, 0F, 0F, 0F));
    }


    public void setupAnim(boolean shielded, float relYaw, float pitch, float recoil) {
        if (Float.isNaN(this.gunZ)) {
            this.gunZ = this.gun.z;
        }
        this.shield.visible = shielded;
        this.mount.yRot = Mth.DEG_TO_RAD * relYaw;
        this.gun.xRot = Mth.DEG_TO_RAD * pitch;
        this.gun.z = this.gunZ + 0.6F * recoil;
    }

    /** Muzzle position and direction in the current pose space. */
    public org.joml.Vector3f[] muzzle(PoseStack poseStack) {
        poseStack.pushPose();
        this.root.translateAndRotate(poseStack);
        this.mount.translateAndRotate(poseStack);
        this.gun.translateAndRotate(poseStack);
        org.joml.Matrix4f m = poseStack.last().pose();
        org.joml.Vector3f tip = m.transformPosition(new org.joml.Vector3f(0.0F, -1.4F / 16.0F, -15.2F / 16.0F));
        org.joml.Vector3f ahead = m.transformPosition(new org.joml.Vector3f(0.0F, -1.4F / 16.0F, -16.2F / 16.0F));
        poseStack.popPose();
        return new org.joml.Vector3f[]{tip, ahead.sub(tip).normalize()};
    }

    public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay) {
        this.root.render(poseStack, consumer, light, overlay);
    }
}
