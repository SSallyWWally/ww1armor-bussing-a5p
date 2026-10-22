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

/** Fokker Dr.I - GENERATED from plane/fokker_geom.py + build_fokker.py. Do not edit the geometry by hand. */
public class FokkerModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ArmoredCarMod.id("fokker_dr1"), "main");

    private final ModelPart root, prop, pilot;

    public FokkerModel(ModelPart root) {
        this.root = root;
        this.prop = root.getChild("prop");
        this.pilot = root.getChild("pilot");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        build_fuselage(root);
        build_prop(root);
        build_legf_l(root);
        build_legr_l(root);
        build_legf_r(root);
        build_legr_r(root);
        build_wheell(root);
        build_wheelr(root);
        build_skid(root);
        build_gunl(root);
        build_gunr(root);
        build_pilot(root);
        return LayerDefinition.create(mesh, 512, 256);
    }

    private static void build_fuselage(PartDefinition root) {
        root.addOrReplaceChild("fuselage", CubeListBuilder.create()
                .texOffs(16, 0).addBox(-6F, -29F, -20F, 12F, 14F, 16F)
                .texOffs(328, 0).addBox(-6F, -18F, -4F, 12F, 3F, 18F)
                .texOffs(128, 0).addBox(5F, -29F, -4F, 1F, 11F, 18F)
                .texOffs(128, 0).addBox(-6F, -29F, -4F, 1F, 11F, 18F)
                .texOffs(290, 74).addBox(-6F, -29F, -4F, 12F, 3F, 6F)
                .texOffs(366, 74).addBox(-6F, -29.5F, 14F, 12F, 2F, 4F)
                .texOffs(72, 0).addBox(-6F, -29F, 14F, 12F, 13F, 16F, new CubeDeformation(-0.4F, 0F, 0F))
                .texOffs(166, 0).addBox(-4.5F, -28.45F, 30F, 9F, 11F, 14F, new CubeDeformation(-0.1F, -0.05F, 0F))
                .texOffs(388, 0).addBox(-3.5F, -28.05F, 44F, 7F, 9F, 12F, new CubeDeformation(-0.3F, -0.45F, 0F))
                .texOffs(390, 56).addBox(-2F, -27.05F, 56F, 4F, 6F, 10F, new CubeDeformation(0F, -0.45F, 0F))
                .texOffs(352, 74).addBox(-1.5F, -26F, 66F, 3F, 3F, 4F, new CubeDeformation(-0.3F, 0F, 0F))
                .texOffs(6, 86).addBox(-6.5F, -29.9F, 1.9F, 13F, 1F, 1F, new CubeDeformation(-0.3F, -0.1F, -0.1F))
                .texOffs(6, 86).addBox(-6.5F, -29.9F, 13.1F, 13F, 1F, 1F, new CubeDeformation(-0.3F, -0.1F, -0.1F))
                .texOffs(0, 74).addBox(5.2F, -29.9F, 2.5F, 1F, 1F, 11F, new CubeDeformation(0F, -0.1F, -0.3F))
                .texOffs(0, 74).addBox(-6.2F, -29.9F, 2.5F, 1F, 1F, 11F, new CubeDeformation(0F, -0.1F, -0.3F))
                .texOffs(250, 0).addBox(-7.5F, -29.5F, -27F, 15F, 15F, 7F)
                .texOffs(336, 56).addBox(-8.5F, -27.5F, -27F, 17F, 11F, 7F)
                .texOffs(212, 0).addBox(-6F, -30.5F, -27F, 12F, 17F, 7F)
                .texOffs(24, 74).addBox(-5.5F, -27.5F, -27.4F, 11F, 11F, 1F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(398, 74).addBox(-1.5F, -23.5F, -29.75F, 3F, 3F, 3F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(50, 37).addBox(-58F, -45.7F, -17F, 116F, 2F, 16F, new CubeDeformation(0F, -0.3F, 0F))
                .texOffs(216, 56).addBox(6F, -27.6F, -14F, 44F, 2F, 16F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(452, 0).addBox(5.5F, -28.2F, -14F, 2F, 3F, 16F, new CubeDeformation(-0.1F, 0F, 0F))
                .texOffs(216, 56).addBox(-50F, -27.6F, -14F, 44F, 2F, 16F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(452, 0).addBox(-7.5F, -28.2F, -14F, 2F, 3F, 16F, new CubeDeformation(-0.1F, 0F, 0F))
                .texOffs(0, 56).addBox(-46F, -15.2F, -15F, 92F, 2F, 16F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(0, 0).addBox(37.5F, -44.4F, -11F, 1F, 30F, 7F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(0, 0).addBox(-38.5F, -44.4F, -11F, 1F, 30F, 7F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(384, 56).addBox(4.4F, -44F, -13.6F, 1F, 15F, 2F, new CubeDeformation(-0.1F, 0F, -0.4F))
                .texOffs(384, 56).addBox(4.4F, -44F, -4.1F, 1F, 15F, 2F, new CubeDeformation(-0.1F, 0F, -0.4F))
                .texOffs(384, 56).addBox(-5.4F, -44F, -13.6F, 1F, 15F, 2F, new CubeDeformation(-0.1F, 0F, -0.4F))
                .texOffs(384, 56).addBox(-5.4F, -44F, -4.1F, 1F, 15F, 2F, new CubeDeformation(-0.1F, 0F, -0.4F))
                .texOffs(114, 74).addBox(-16F, -7F, -13F, 32F, 3F, 7F)
                .texOffs(210, 74).addBox(-16F, -25.6F, 58F, 32F, 1F, 8F)
                .texOffs(410, 74).addBox(-11F, -25.6F, 66F, 22F, 1F, 4F)
                .texOffs(294, 0).addBox(-1F, -38F, 62F, 2F, 12F, 10F, new CubeDeformation(-0.4F, 0F, 0F))
                .texOffs(96, 74).addBox(-1F, -39.75F, 63.75F, 2F, 4F, 7F, new CubeDeformation(-0.4F, -0.25F, -0.25F))
                .texOffs(326, 74).addBox(-1F, -26F, 70F, 2F, 5F, 3F, new CubeDeformation(-0.4F, 0F, 0F))
                .texOffs(472, 74).addBox(-1F, -29.95F, -1.5F, 2F, 1F, 3F, new CubeDeformation(-0.1F, -0.05F, 0F))
                , PartPose.offsetAndRotation(0F, 24F, 0F, 0F, 0F, 0F));
    }

    private static void build_prop(PartDefinition root) {
        root.addOrReplaceChild("prop", CubeListBuilder.create()
                .texOffs(462, 74).addBox(-1.5F, -1.5F, -1F, 3F, 3F, 2F)
                .texOffs(318, 0).addBox(-1.5F, -21.25F, -1F, 3F, 20F, 2F, new CubeDeformation(-0.2F, -0.25F, -0.4F))
                .texOffs(318, 0).addBox(-1.5F, 1.25F, -1F, 3F, 20F, 2F, new CubeDeformation(-0.2F, -0.25F, -0.4F))
                , PartPose.offsetAndRotation(0F, 2F, -30F, 0F, 0F, 0F));
    }

    private static void build_legf_l(PartDefinition root) {
        root.addOrReplaceChild("legf_l", CubeListBuilder.create()
                .texOffs(436, 56).addBox(7F, -0.5F, -12.5519F, 1F, 1F, 13F, new CubeDeformation(0F, 0F, -0.4481F))
                , PartPose.offsetAndRotation(0F, 9F, -17F, 2.2391F, 0F, 0F));
    }

    private static void build_legr_l(PartDefinition root) {
        root.addOrReplaceChild("legr_l", CubeListBuilder.create()
                .texOffs(436, 56).addBox(7F, -0.5F, -12.8738F, 1F, 1F, 13F, new CubeDeformation(0F, 0F, -0.1262F))
                , PartPose.offsetAndRotation(0F, 9F, -1F, 0.8409F, 0F, 0F));
    }

    private static void build_legf_r(PartDefinition root) {
        root.addOrReplaceChild("legf_r", CubeListBuilder.create()
                .texOffs(436, 56).addBox(-8F, -0.5F, -12.5519F, 1F, 1F, 13F, new CubeDeformation(0F, 0F, -0.4481F))
                , PartPose.offsetAndRotation(0F, 9F, -17F, 2.2391F, 0F, 0F));
    }

    private static void build_legr_r(PartDefinition root) {
        root.addOrReplaceChild("legr_r", CubeListBuilder.create()
                .texOffs(436, 56).addBox(-8F, -0.5F, -12.8738F, 1F, 1F, 13F, new CubeDeformation(0F, 0F, -0.1262F))
                , PartPose.offsetAndRotation(0F, 9F, -1F, 0.8409F, 0F, 0F));
    }

    private static void build_wheell(PartDefinition root) {
        root.addOrReplaceChild("wheell", CubeListBuilder.create()
                .texOffs(30, 37).addBox(-1.5F, -6F, -3.5F, 3F, 12F, 7F, new CubeDeformation(-0.3F, -0.4F, -0.1F))
                .texOffs(0, 37).addBox(-1.5F, -3.5F, -6F, 3F, 7F, 12F, new CubeDeformation(-0.3F, -0.1F, -0.4F))
                .texOffs(426, 0).addBox(-1.5F, -5F, -5F, 3F, 10F, 10F, new CubeDeformation(-0.3F, -0.2F, -0.2F))
                .texOffs(418, 56).addBox(0.85F, -4F, -4F, 1F, 8F, 8F, new CubeDeformation(-0.4F, -0.4F, -0.4F))
                , PartPose.offsetAndRotation(14F, 18.4F, -9.5F, 0F, 0F, 0F));
    }

    private static void build_wheelr(PartDefinition root) {
        root.addOrReplaceChild("wheelr", CubeListBuilder.create()
                .texOffs(30, 37).addBox(-1.5F, -6F, -3.5F, 3F, 12F, 7F, new CubeDeformation(-0.3F, -0.4F, -0.1F))
                .texOffs(0, 37).addBox(-1.5F, -3.5F, -6F, 3F, 7F, 12F, new CubeDeformation(-0.3F, -0.1F, -0.4F))
                .texOffs(426, 0).addBox(-1.5F, -5F, -5F, 3F, 10F, 10F, new CubeDeformation(-0.3F, -0.2F, -0.2F))
                .texOffs(418, 56).addBox(-1.85F, -4F, -4F, 1F, 8F, 8F, new CubeDeformation(-0.4F, -0.4F, -0.4F))
                , PartPose.offsetAndRotation(-14F, 18.4F, -9.5F, 0F, 0F, 0F));
    }

    private static void build_skid(PartDefinition root) {
        root.addOrReplaceChild("skid", CubeListBuilder.create()
                .texOffs(336, 74).addBox(-1F, -0.5F, -5.8284F, 2F, 1F, 6F, new CubeDeformation(-0.4F, 0F, -0.1716F))
                , PartPose.offsetAndRotation(0F, 3F, 60F, 2.3562F, 0F, 0F));
    }

    private static void build_gunl(PartDefinition root) {
        root.addOrReplaceChild("gunl", CubeListBuilder.create()
                .texOffs(192, 74).addBox(1F, -8.6F, -4F, 2F, 3F, 7F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(336, 74).addBox(1F, -8.85F, -3.25F, 2F, 1F, 6F, new CubeDeformation(-0.15F, -0.35F, -0.25F))
                .texOffs(34, 86).addBox(1.5F, -9.35F, -3.05F, 1F, 1F, 1F, new CubeDeformation(-0.35F, -0.15F, -0.35F))
                .texOffs(74, 74).addBox(1F, -8.1F, -13F, 2F, 2F, 9F, new CubeDeformation(-0.1F, -0.45F, 0F))
                .texOffs(74, 74).addBox(1F, -8.1F, -13F, 2F, 2F, 9F, new CubeDeformation(-0.45F, -0.1F, 0F))
                .texOffs(74, 74).addBox(1F, -8.1F, -13F, 2F, 2F, 9F, new CubeDeformation(-0.25F, -0.25F, 0F))
                .texOffs(498, 74).addBox(1F, -8.1F, -4.8F, 2F, 2F, 1F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(498, 74).addBox(1F, -8.1F, -13.7F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.3F))
                .texOffs(504, 74).addBox(1.5F, -7.6F, -15.1F, 1F, 1F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.3F))
                .texOffs(34, 86).addBox(1.5F, -6F, -11.4F, 1F, 1F, 1F, new CubeDeformation(-0.2F, 0F, -0.1F))
                .texOffs(0, 86).addBox(1.5F, -6F, -1F, 1F, 1F, 2F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
    }

    private static void build_gunr(PartDefinition root) {
        root.addOrReplaceChild("gunr", CubeListBuilder.create()
                .texOffs(192, 74).addBox(-3F, -8.6F, -4F, 2F, 3F, 7F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(336, 74).addBox(-3F, -8.85F, -3.25F, 2F, 1F, 6F, new CubeDeformation(-0.15F, -0.35F, -0.25F))
                .texOffs(34, 86).addBox(-2.5F, -9.35F, -3.05F, 1F, 1F, 1F, new CubeDeformation(-0.35F, -0.15F, -0.35F))
                .texOffs(74, 74).addBox(-3F, -8.1F, -13F, 2F, 2F, 9F, new CubeDeformation(-0.1F, -0.45F, 0F))
                .texOffs(74, 74).addBox(-3F, -8.1F, -13F, 2F, 2F, 9F, new CubeDeformation(-0.45F, -0.1F, 0F))
                .texOffs(74, 74).addBox(-3F, -8.1F, -13F, 2F, 2F, 9F, new CubeDeformation(-0.25F, -0.25F, 0F))
                .texOffs(498, 74).addBox(-3F, -8.1F, -4.8F, 2F, 2F, 1F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(498, 74).addBox(-3F, -8.1F, -13.7F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.3F))
                .texOffs(504, 74).addBox(-2.5F, -7.6F, -15.1F, 1F, 1F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.3F))
                .texOffs(34, 86).addBox(-2.5F, -6F, -11.4F, 1F, 1F, 1F, new CubeDeformation(-0.2F, 0F, -0.1F))
                .texOffs(0, 86).addBox(-2.5F, -6F, -1F, 1F, 1F, 2F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
    }

    private static void build_pilot(PartDefinition root) {
        root.addOrReplaceChild("pilot", CubeListBuilder.create()
                .texOffs(48, 74).addBox(-4.5F, -7F, 8F, 9F, 7F, 4F)
                .texOffs(464, 56).addBox(-3.5F, -13.9F, 4.7F, 7F, 7F, 7F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(482, 74).addBox(-3.5F, -10.9F, 4.4F, 7F, 2F, 1F, new CubeDeformation(0F, -0.3F, -0.2F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
    }

    public void setupAnim(float propAngle, boolean showPilot) {
        this.prop.zRot = propAngle;
        this.pilot.visible = showPilot;
    }

    public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay) {
        this.root.render(poseStack, consumer, light, overlay);
    }
}
