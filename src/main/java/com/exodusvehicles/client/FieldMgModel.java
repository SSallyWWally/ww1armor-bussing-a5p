package com.exodusvehicles.client;

import com.exodusvehicles.ArmoredCarMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/** MG 08 on the Schlitten 08 sled - GENERATED from truck/mg08_geom.py + build_mg08.py. Do not edit the geometry by hand. */
public class FieldMgModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ArmoredCarMod.id("mg08"), "main");
    public static final ModelLayerLocation TRIPOD_LAYER = new ModelLayerLocation(ArmoredCarMod.id("mg08_tripod"), "main");
    public static final ModelLayerLocation BUNKER_TALL_LAYER = new ModelLayerLocation(ArmoredCarMod.id("mg08_bunker_tall"), "main");
    public static final ModelLayerLocation BUNKER_LAYER = new ModelLayerLocation(ArmoredCarMod.id("mg08_bunker"), "main");

    private final ModelPart root, mount, shield, gun, scope;
    private float gunZ = Float.NaN;

    public FieldMgModel(ModelPart root) {
        this.root = root;
        this.mount = root.getChild("mount");
        this.shield = this.mount.getChild("shield");
        this.gun = this.mount.getChild("gun");
        this.scope = this.gun.getChild("scope");
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
                .texOffs(68, 0).addBox(3F, -1F, 3F, 1F, 1F, 14F)
                .texOffs(78, 27).addBox(3F, -2F, 16.25F, 1F, 1F, 2F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(68, 0).addBox(-4F, -1F, 3F, 1F, 1F, 14F)
                .texOffs(78, 27).addBox(-4F, -2F, 16.25F, 1F, 1F, 2F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(104, 27).addBox(-4F, -1.1F, 14F, 8F, 1F, 1F, new CubeDeformation(0F, -0.1F, 0F))
                .texOffs(104, 27).addBox(-4F, -1.1F, 5F, 8F, 1F, 1F, new CubeDeformation(0F, -0.1F, 0F))
                .texOffs(58, 18).addBox(-2.5F, -8.4F, -2.5F, 5F, 2F, 5F, new CubeDeformation(-0.3F, -0.4F, -0.3F))
                .texOffs(68, 27).addBox(1.8F, -0.8F, -10.5F, 3F, 1F, 2F, new CubeDeformation(-0.2F, -0.2F, 0F))
                .texOffs(68, 27).addBox(-4.8F, -0.8F, -10.5F, 3F, 1F, 2F, new CubeDeformation(-0.2F, -0.2F, 0F))
                , PartPose.offsetAndRotation(0F, 24F, 0F, 0F, 0F, 0F));
    }

    private static void build_rail_l(PartDefinition root) {
        root.addOrReplaceChild("rail_l", CubeListBuilder.create()
                .texOffs(8, 0).addBox(2.2F, -0.5F, -14.745F, 1F, 1F, 15F, new CubeDeformation(0F, 0F, -0.255F))
                , PartPose.offsetAndRotation(0F, 23F, 14.5F, -0.4575F, 0F, 0F));
    }

    private static void build_leg_l(PartDefinition root) {
        root.addOrReplaceChild("leg_l", CubeListBuilder.create()
                .texOffs(160, 0).addBox(2.4F, -0.5F, -11.5698F, 1F, 1F, 12F, new CubeDeformation(0F, 0F, -0.4302F))
                , PartPose.offsetAndRotation(0F, 16.8F, -1F, 0.7028F, 0F, 0F));
    }

    private static void build_rail_r(PartDefinition root) {
        root.addOrReplaceChild("rail_r", CubeListBuilder.create()
                .texOffs(8, 0).addBox(-3.2F, -0.5F, -14.745F, 1F, 1F, 15F, new CubeDeformation(0F, 0F, -0.255F))
                , PartPose.offsetAndRotation(0F, 23F, 14.5F, -0.4575F, 0F, 0F));
    }

    private static void build_leg_r(PartDefinition root) {
        root.addOrReplaceChild("leg_r", CubeListBuilder.create()
                .texOffs(160, 0).addBox(-3.4F, -0.5F, -11.5698F, 1F, 1F, 12F, new CubeDeformation(0F, 0F, -0.4302F))
                , PartPose.offsetAndRotation(0F, 16.8F, -1F, 0.7028F, 0F, 0F));
    }

    private static void build_mount(PartDefinition root) {
        PartDefinition p6 = root.addOrReplaceChild("mount", CubeListBuilder.create()
                .texOffs(34, 18).addBox(-3F, -1F, -3F, 6F, 1F, 6F, new CubeDeformation(-0.4F, 0F, 0F))
                .texOffs(202, 18).addBox(1.75F, -2.9F, -1.5F, 1F, 2F, 3F, new CubeDeformation(-0.15F, -0.1F, -0.3F))
                .texOffs(202, 18).addBox(-2.75F, -2.9F, -1.5F, 1F, 2F, 3F, new CubeDeformation(-0.15F, -0.1F, -0.3F))
                .texOffs(140, 18).addBox(1.2F, -2.9F, 3F, 1F, 3F, 3F, new CubeDeformation(0F, -0.3F, 0F))
                .texOffs(0, 18).addBox(-7.6F, -1F, -0.6F, 5F, 5F, 4F, new CubeDeformation(-0.4F, -0.4F, 0F))
                .texOffs(108, 18).addBox(-7.6F, -1.3F, -1.1F, 5F, 1F, 5F, new CubeDeformation(-0.2F, -0.3F, -0.3F))
                .texOffs(134, 27).addBox(-5.6F, -1.75F, 0.8F, 1F, 1F, 1F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(84, 27).addBox(-3.1F, -0.4F, 0.3F, 1F, 1F, 2F, new CubeDeformation(-0.1F, 0F, -0.3F))
                , PartPose.offsetAndRotation(0F, 16F, 0F, 0F, 0F, 0F));
        p6.addOrReplaceChild("shield", CubeListBuilder.create()
                .texOffs(148, 18).addBox(-8F, -1.15F, -5F, 16F, 4F, 1F, new CubeDeformation(0F, -0.35F, -0.2F))
                .texOffs(34, 27).addBox(-8F, -12F, -5F, 16F, 2F, 1F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(232, 0).addBox(4.1F, -10.4F, -5F, 4F, 10F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.2F))
                .texOffs(232, 0).addBox(-8.1F, -10.4F, -5F, 4F, 10F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.2F))
                .texOffs(0, 0).addBox(7.4F, -12.25F, -5F, 1F, 15F, 3F, new CubeDeformation(-0.2F, -0.25F, -0.2F))
                .texOffs(0, 0).addBox(-8.4F, -12.25F, -5F, 1F, 15F, 3F, new CubeDeformation(-0.2F, -0.25F, -0.2F))
                .texOffs(18, 18).addBox(-1F, -1.25F, -4.6F, 2F, 2F, 6F, new CubeDeformation(-0.4F, -0.25F, -0.4F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p8 = p6.addOrReplaceChild("gun", CubeListBuilder.create()
                .texOffs(210, 0).addBox(-1.5F, -3F, -0.75F, 3F, 3F, 8F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(78, 18).addBox(-1.5F, -3.7F, 0.5F, 3F, 1F, 6F, new CubeDeformation(0F, -0.3F, 0F))
                .texOffs(230, 18).addBox(-1.5F, -2.4F, 6.6F, 3F, 2F, 2F, new CubeDeformation(-0.3F, 0F, -0.4F))
                .texOffs(218, 18).addBox(0.5F, -3F, 8F, 1F, 3F, 2F, new CubeDeformation(-0.1F, -0.4F, -0.2F))
                .texOffs(218, 18).addBox(-1.5F, -3F, 8F, 1F, 3F, 2F, new CubeDeformation(-0.1F, -0.4F, -0.2F))
                .texOffs(128, 0).addBox(-2F, -2.4F, -12.4F, 4F, 2F, 12F, new CubeDeformation(-0.25F, -0.05F, -0.1F))
                .texOffs(40, 0).addBox(-1F, -3.4F, -12.4F, 2F, 4F, 12F, new CubeDeformation(-0.05F, -0.25F, -0.1F))
                .texOffs(98, 0).addBox(-1.5F, -2.9F, -12.4F, 3F, 3F, 12F, new CubeDeformation(0F, 0F, -0.1F))
                .texOffs(240, 18).addBox(-1.5F, -2.9F, -13.05F, 3F, 3F, 1F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(248, 18).addBox(-0.5F, -1.9F, -15.5F, 1F, 1F, 3F, new CubeDeformation(-0.05F, -0.05F, -0.3F))
                .texOffs(182, 18).addBox(-2F, -3.4F, -14.25F, 4F, 4F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.35F))
                .texOffs(0, 27).addBox(-1F, -2.4F, -16F, 2F, 2F, 2F, new CubeDeformation(-0.25F, -0.25F, -0.4F))
                .texOffs(122, 27).addBox(-1F, -3.8F, -11.75F, 2F, 1F, 1F, new CubeDeformation(-0.45F, -0.35F, -0.15F))
                .texOffs(96, 27).addBox(-0.5F, -5.1F, -11.75F, 1F, 2F, 1F, new CubeDeformation(-0.34F, -0.35F, -0.3F))
                .texOffs(138, 27).addBox(-0.5F, -5.375F, -11.75F, 1F, 1F, 1F, new CubeDeformation(-0.43F, -0.375F, -0.35F))
                .texOffs(96, 27).addBox(-0.06F, -5.325F, -11.75F, 1F, 2F, 1F, new CubeDeformation(-0.42F, -0.125F, -0.15F))
                .texOffs(96, 27).addBox(-0.94F, -5.325F, -11.75F, 1F, 2F, 1F, new CubeDeformation(-0.42F, -0.125F, -0.15F))
                .texOffs(30, 27).addBox(0.11F, -6.2F, 2.525F, 1F, 3F, 1F, new CubeDeformation(-0.39F, -0.2F, -0.375F))
                .texOffs(30, 27).addBox(-1.11F, -6.2F, 2.525F, 1F, 3F, 1F, new CubeDeformation(-0.39F, -0.2F, -0.375F))
                .texOffs(122, 27).addBox(-1F, -6.6F, 2.525F, 2F, 1F, 1F, new CubeDeformation(-0.28F, -0.4F, -0.375F))
                .texOffs(122, 27).addBox(-1F, -5.1F, 2.525F, 2F, 1F, 1F, new CubeDeformation(-0.28F, -0.35F, -0.275F))
                .texOffs(138, 27).addBox(-0.24F, -5.425F, 2.525F, 1F, 1F, 1F, new CubeDeformation(-0.34F, -0.325F, -0.325F))
                .texOffs(138, 27).addBox(-0.76F, -5.425F, 2.525F, 1F, 1F, 1F, new CubeDeformation(-0.34F, -0.325F, -0.325F))
                .texOffs(142, 27).addBox(0.335F, -5.1F, 2.525F, 1F, 1F, 1F, new CubeDeformation(-0.385F, -0.4F, -0.375F))
                .texOffs(122, 27).addBox(-1F, -4F, 2.55F, 2F, 1F, 1F, new CubeDeformation(-0.2F, -0.4F, -0.05F))
                .texOffs(142, 27).addBox(0.6F, -4.15F, 2.4F, 1F, 1F, 1F, new CubeDeformation(-0.3F, -0.25F, -0.2F))
                .texOffs(182, 18).addBox(-2F, -3.4F, -1.1F, 4F, 4F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.2F))
                .texOffs(186, 0).addBox(-0.5F, 0.15F, -12.25F, 1F, 1F, 11F, new CubeDeformation(-0.15F, -0.25F, -0.25F))
                .texOffs(100, 27).addBox(-0.5F, 0.2F, -12.05F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.15F))
                .texOffs(134, 27).addBox(-2.4F, -1.9F, 4.7F, 1F, 1F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(8, 27).addBox(-2.8F, -1.8F, 5.3F, 1F, 1F, 3F, new CubeDeformation(-0.3F, -0.2F, -0.3F))
                .texOffs(128, 27).addBox(-1F, -3.9F, -3F, 2F, 1F, 1F, new CubeDeformation(-0.3F, -0.3F, 0F))
                .texOffs(192, 18).addBox(-3.15F, -2.6F, 0.25F, 2F, 2F, 3F, new CubeDeformation(-0.35F, 0F, -0.25F))
                .texOffs(224, 18).addBox(-3.5F, -2.3F, 0.7F, 1F, 3F, 2F, new CubeDeformation(-0.1F, -0.3F, -0.3F))
                , PartPose.offsetAndRotation(0F, -2F, 0F, 0F, 0F, 0F));
        p8.addOrReplaceChild("scope", CubeListBuilder.create()
                .texOffs(210, 18).addBox(1.4F, -2.7F, 3.5F, 1F, 2F, 3F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(96, 18).addBox(1.6F, -5.3F, 3.2F, 2F, 3F, 4F, new CubeDeformation(-0.1F, -0.1F, -0.2F))
                .texOffs(128, 18).addBox(1.6F, -5.4F, -0.5F, 2F, 2F, 4F, new CubeDeformation(-0.4F, -0.4F, -0.1F))
                .texOffs(90, 27).addBox(1.6F, -5.4F, -1.15F, 2F, 2F, 1F, new CubeDeformation(-0.25F, -0.25F, -0.25F))
                .texOffs(16, 27).addBox(1.6F, -4.7F, 6.6F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.3F, -0.4F))
                .texOffs(24, 27).addBox(3.2F, -4.7F, 3.9F, 1F, 2F, 2F, new CubeDeformation(-0.3F, -0.3F, -0.3F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
    }


    public void setupAnim(boolean shielded, boolean scoped, float relYaw, float pitch, float recoil) {
        this.scope.visible = scoped;
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
        org.joml.Vector3f tip = m.transformPosition(new org.joml.Vector3f(0.0F, -1.4F / 16.0F, -15.6F / 16.0F));
        org.joml.Vector3f ahead = m.transformPosition(new org.joml.Vector3f(0.0F, -1.4F / 16.0F, -16.6F / 16.0F));
        poseStack.popPose();
        return new org.joml.Vector3f[]{tip, ahead.sub(tip).normalize()};
    }

    public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay) {
        this.root.render(poseStack, consumer, light, overlay);
    }
}
