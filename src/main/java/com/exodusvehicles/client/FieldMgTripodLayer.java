package com.exodusvehicles.client;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** MG 08 on the light tripod - GENERATED from truck/mg08_geom.py (MOUNT=tripod). Same parts as FieldMgModel. */
public final class FieldMgTripodLayer {
    private FieldMgTripodLayer() {}

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        build_sled(root);
        build_leg_l(root);
        build_leg_r(root);
        build_leg_b(root);
        build_mount(root);
        return LayerDefinition.create(mesh, 256, 256);
    }

    private static void build_sled(PartDefinition root) {
        root.addOrReplaceChild("sled", CubeListBuilder.create()
                .texOffs(24, 22).addBox(-2.2F, -14F, -2.2F, 4.4F, 1.4F, 4.4F)
                .texOffs(46, 29).addBox(3F, -0.6F, -11F, 2.2F, 0.6F, 1.8F)
                .texOffs(56, 29).addBox(1.8F, -13.4F, -1.4F, 2F, 1F, 2F)
                .texOffs(46, 29).addBox(-5.2F, -0.6F, -11F, 2.2F, 0.6F, 1.8F)
                .texOffs(56, 29).addBox(-3.8F, -13.4F, -1.4F, 2F, 1F, 2F)
                .texOffs(34, 29).addBox(-1.6F, -0.6F, 14.2F, 3.2F, 0.6F, 2F)
                .texOffs(196, 22).addBox(-0.4F, -5.5F, 8.7F, 0.8F, 3.5F, 0.8F)
                , PartPose.offsetAndRotation(0F, 24F, 0F, 0F, 0F, 0F));
    }

    private static void build_leg_l(PartDefinition root) {
        root.addOrReplaceChild("leg_l", CubeListBuilder.create()
                .texOffs(52, 0).addBox(3.6F, -0.5F, -15.8114F, 1F, 1F, 15.8114F)
                , PartPose.offsetAndRotation(0F, 11F, -1F, 0.9653F, 0F, 0F));
    }

    private static void build_leg_r(PartDefinition root) {
        root.addOrReplaceChild("leg_r", CubeListBuilder.create()
                .texOffs(52, 0).addBox(-4.6F, -0.5F, -15.8114F, 1F, 1F, 15.8114F)
                , PartPose.offsetAndRotation(0F, 11F, -1F, 0.9653F, 0F, 0F));
    }

    private static void build_leg_b(PartDefinition root) {
        root.addOrReplaceChild("leg_b", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-0.6F, -0.6F, -19.105F, 1.2F, 1.2F, 19.105F)
                , PartPose.offsetAndRotation(0F, 11F, 1F, 2.3932F, 0F, 0F));
    }

    private static void build_mount(PartDefinition root) {
        PartDefinition p5 = root.addOrReplaceChild("mount", CubeListBuilder.create()
                .texOffs(0, 22).addBox(-2.6F, -1F, -3F, 5.2F, 1F, 6F)
                .texOffs(168, 22).addBox(1.9F, -2.8F, -1.2F, 0.7F, 1.8F, 2.4F)
                .texOffs(168, 22).addBox(-2.6F, -2.8F, -1.2F, 0.7F, 1.8F, 2.4F)
                .texOffs(106, 22).addBox(1.2F, -2.6F, 3F, 1F, 2.4F, 3F)
                .texOffs(204, 0).addBox(-7.2F, -0.6F, -0.6F, 4.2F, 4.2F, 4F)
                .texOffs(74, 22).addBox(-7.4F, -1F, -0.8F, 4.6F, 0.4F, 4.4F)
                .texOffs(110, 29).addBox(-5.6F, -1.5F, 0.8F, 1F, 0.5F, 1F)
                .texOffs(72, 29).addBox(-3F, -0.4F, 0.6F, 0.8F, 1F, 1.4F)
                , PartPose.offsetAndRotation(0F, 10F, 0F, 0F, 0F, 0F));
        p5.addOrReplaceChild("shield", CubeListBuilder.create()
                .texOffs(114, 22).addBox(-8F, -0.8F, -4.8F, 16F, 3.3F, 0.6F)
                .texOffs(0, 29).addBox(-8F, -12F, -4.8F, 16F, 2F, 0.6F)
                .texOffs(194, 0).addBox(4.2F, -10F, -4.8F, 3.8F, 9.2F, 0.6F)
                .texOffs(194, 0).addBox(-8F, -10F, -4.8F, 3.8F, 9.2F, 0.6F)
                .texOffs(44, 0).addBox(7.6F, -12F, -4.8F, 0.6F, 14.5F, 2.6F)
                .texOffs(44, 0).addBox(-8.2F, -12F, -4.8F, 0.6F, 14.5F, 2.6F)
                .texOffs(222, 0).addBox(-0.6F, -1F, -4.2F, 1.2F, 1.5F, 5.2F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p7 = p5.addOrReplaceChild("gun", CubeListBuilder.create()
                .texOffs(172, 0).addBox(-1.5F, -3F, -0.5F, 3F, 3F, 7.5F)
                .texOffs(44, 22).addBox(-1.5F, -3.4F, 0.5F, 3F, 0.4F, 6F)
                .texOffs(200, 22).addBox(-1.2F, -2.4F, 7F, 2.4F, 2F, 1.2F)
                .texOffs(184, 22).addBox(0.6F, -2.6F, 8.2F, 0.8F, 2.2F, 1.6F)
                .texOffs(184, 22).addBox(-1.4F, -2.6F, 8.2F, 0.8F, 2.2F, 1.6F)
                .texOffs(116, 0).addBox(-1.8F, -2.65F, -12.3F, 3.6F, 2.5F, 11.8F)
                .texOffs(86, 0).addBox(-1.25F, -3.2F, -12.3F, 2.5F, 3.6F, 11.8F)
                .texOffs(210, 22).addBox(-1.5F, -2.9F, -12.8F, 3F, 3F, 0.5F)
                .texOffs(218, 22).addBox(-0.45F, -1.85F, -15.2F, 0.9F, 0.9F, 2.4F)
                .texOffs(148, 22).addBox(-1.7F, -3.1F, -13.9F, 3.4F, 3.4F, 0.3F)
                .texOffs(226, 22).addBox(-0.75F, -2.15F, -15.6F, 1.5F, 1.5F, 1.2F)
                .texOffs(98, 29).addBox(-0.8F, -3.6F, -11.8F, 1.6F, 0.4F, 1F)
                .texOffs(90, 29).addBox(-0.25F, -4.7F, -11.5F, 0.5F, 1.1F, 0.5F)
                .texOffs(114, 29).addBox(-0.12F, -5F, -11.45F, 0.24F, 0.3F, 0.4F)
                .texOffs(90, 29).addBox(0.55F, -5.4F, -11.8F, 0.25F, 1.8F, 1F)
                .texOffs(90, 29).addBox(-0.8F, -5.4F, -11.8F, 0.25F, 1.8F, 1F)
                .texOffs(78, 29).addBox(-0.9F, -4.75F, 2.8F, 1.8F, 1.35F, 0.4F)
                .texOffs(114, 29).addBox(0.25F, -5.7F, 2.8F, 0.7F, 0.95F, 0.4F)
                .texOffs(114, 29).addBox(-0.95F, -5.7F, 2.8F, 0.7F, 0.95F, 0.4F)
                .texOffs(64, 29).addBox(-1F, -3.7F, 2.4F, 2F, 0.3F, 1.2F)
                .texOffs(118, 29).addBox(0.9F, -3.9F, 2.6F, 0.4F, 0.5F, 0.6F)
                .texOffs(148, 22).addBox(-1.6F, -3F, -0.9F, 3.2F, 3.2F, 0.6F)
                .texOffs(148, 0).addBox(-0.35F, 0.4F, -12F, 0.7F, 0.5F, 10.5F)
                .texOffs(94, 29).addBox(-0.4F, 0.4F, -11.9F, 0.8F, 1.6F, 0.7F)
                .texOffs(110, 29).addBox(-2.3F, -1.8F, 4.8F, 0.8F, 0.8F, 0.8F)
                .texOffs(234, 22).addBox(-2.5F, -1.6F, 5.6F, 0.4F, 0.6F, 2.4F)
                .texOffs(104, 29).addBox(-0.7F, -3.6F, -3F, 1.4F, 0.4F, 1F)
                .texOffs(158, 22).addBox(-2.8F, -2.6F, 0.5F, 1.3F, 2F, 2.5F)
                .texOffs(190, 22).addBox(-3.4F, -2F, 1F, 0.8F, 2.4F, 1.4F)
                , PartPose.offsetAndRotation(0F, -2F, 0F, 0F, 0F, 0F));
        p7.addOrReplaceChild("scope", CubeListBuilder.create()
                .texOffs(176, 22).addBox(1.5F, -2.4F, 3.6F, 0.8F, 1.4F, 2.8F)
                .texOffs(62, 22).addBox(1.7F, -5.2F, 3.4F, 1.8F, 2.8F, 3.6F)
                .texOffs(94, 22).addBox(2F, -5F, -0.4F, 1.2F, 1.2F, 3.8F)
                .texOffs(84, 29).addBox(1.85F, -5.15F, -0.9F, 1.5F, 1.5F, 0.5F)
                .texOffs(242, 22).addBox(2F, -4.4F, 7F, 1.2F, 1.4F, 1.2F)
                .texOffs(250, 22).addBox(3.5F, -4.4F, 4.2F, 0.4F, 1.4F, 1.4F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
    }


}
