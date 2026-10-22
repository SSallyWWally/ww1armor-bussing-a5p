package com.exodusvehicles.client;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
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
                .texOffs(40, 22).addBox(-2.5F, -14.3F, -2.5F, 5F, 2F, 5F, new CubeDeformation(-0.3F, -0.3F, -0.3F))
                .texOffs(72, 30).addBox(2.6F, -0.8F, -11.1F, 3F, 1F, 2F, new CubeDeformation(-0.4F, -0.2F, -0.1F))
                .texOffs(82, 30).addBox(1.8F, -13.4F, -1.4F, 2F, 1F, 2F)
                .texOffs(72, 30).addBox(-5.6F, -0.8F, -11.1F, 3F, 1F, 2F, new CubeDeformation(-0.4F, -0.2F, -0.1F))
                .texOffs(82, 30).addBox(-3.8F, -13.4F, -1.4F, 2F, 1F, 2F)
                .texOffs(60, 30).addBox(-2F, -0.8F, 14.2F, 4F, 1F, 2F, new CubeDeformation(-0.4F, -0.2F, 0F))
                .texOffs(212, 22).addBox(-0.5F, -5.75F, 8.6F, 1F, 4F, 1F, new CubeDeformation(-0.1F, -0.25F, -0.1F))
                , PartPose.offsetAndRotation(0F, 24F, 0F, 0F, 0F, 0F));
    }

    private static void build_leg_l(PartDefinition root) {
        root.addOrReplaceChild("leg_l", CubeListBuilder.create()
                .texOffs(52, 0).addBox(3.6F, -0.5F, -15.9057F, 1F, 1F, 16F, new CubeDeformation(0F, 0F, -0.0943F))
                , PartPose.offsetAndRotation(0F, 11F, -1F, 0.9653F, 0F, 0F));
    }

    private static void build_leg_r(PartDefinition root) {
        root.addOrReplaceChild("leg_r", CubeListBuilder.create()
                .texOffs(52, 0).addBox(-4.6F, -0.5F, -15.9057F, 1F, 1F, 16F, new CubeDeformation(0F, 0F, -0.0943F))
                , PartPose.offsetAndRotation(0F, 11F, -1F, 0.9653F, 0F, 0F));
    }

    private static void build_leg_b(PartDefinition root) {
        root.addOrReplaceChild("leg_b", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1F, -1F, -19.5525F, 2F, 2F, 20F, new CubeDeformation(-0.4F, -0.4F, -0.4475F))
                , PartPose.offsetAndRotation(0F, 11F, 1F, 2.3932F, 0F, 0F));
    }

    private static void build_mount(PartDefinition root) {
        PartDefinition p5 = root.addOrReplaceChild("mount", CubeListBuilder.create()
                .texOffs(16, 22).addBox(-3F, -1F, -3F, 6F, 1F, 6F, new CubeDeformation(-0.4F, 0F, 0F))
                .texOffs(184, 22).addBox(1.75F, -2.9F, -1.5F, 1F, 2F, 3F, new CubeDeformation(-0.15F, -0.1F, -0.3F))
                .texOffs(184, 22).addBox(-2.75F, -2.9F, -1.5F, 1F, 2F, 3F, new CubeDeformation(-0.15F, -0.1F, -0.3F))
                .texOffs(122, 22).addBox(1.2F, -2.9F, 3F, 1F, 3F, 3F, new CubeDeformation(0F, -0.3F, 0F))
                .texOffs(232, 0).addBox(-7.6F, -1F, -0.6F, 5F, 5F, 4F, new CubeDeformation(-0.4F, -0.4F, 0F))
                .texOffs(90, 22).addBox(-7.6F, -1.3F, -1.1F, 5F, 1F, 5F, new CubeDeformation(-0.2F, -0.3F, -0.3F))
                .texOffs(122, 30).addBox(-5.6F, -1.75F, 0.8F, 1F, 1F, 1F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(90, 30).addBox(-3.1F, -0.4F, 0.3F, 1F, 1F, 2F, new CubeDeformation(-0.1F, 0F, -0.3F))
                , PartPose.offsetAndRotation(0F, 10F, 0F, 0F, 0F, 0F));
        p5.addOrReplaceChild("shield", CubeListBuilder.create()
                .texOffs(130, 22).addBox(-8F, -1.15F, -5F, 16F, 4F, 1F, new CubeDeformation(0F, -0.35F, -0.2F))
                .texOffs(26, 30).addBox(-8F, -12F, -5F, 16F, 2F, 1F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(222, 0).addBox(4.1F, -10.4F, -5F, 4F, 10F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.2F))
                .texOffs(222, 0).addBox(-8.1F, -10.4F, -5F, 4F, 10F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.2F))
                .texOffs(44, 0).addBox(7.4F, -12.25F, -5F, 1F, 15F, 3F, new CubeDeformation(-0.2F, -0.25F, -0.2F))
                .texOffs(44, 0).addBox(-8.4F, -12.25F, -5F, 1F, 15F, 3F, new CubeDeformation(-0.2F, -0.25F, -0.2F))
                .texOffs(0, 22).addBox(-1F, -1.25F, -4.6F, 2F, 2F, 6F, new CubeDeformation(-0.4F, -0.25F, -0.4F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p7 = p5.addOrReplaceChild("gun", CubeListBuilder.create()
                .texOffs(200, 0).addBox(-1.5F, -3F, -0.75F, 3F, 3F, 8F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(60, 22).addBox(-1.5F, -3.7F, 0.5F, 3F, 1F, 6F, new CubeDeformation(0F, -0.3F, 0F))
                .texOffs(216, 22).addBox(-1.5F, -2.4F, 6.6F, 3F, 2F, 2F, new CubeDeformation(-0.3F, 0F, -0.4F))
                .texOffs(200, 22).addBox(0.5F, -3F, 8F, 1F, 3F, 2F, new CubeDeformation(-0.1F, -0.4F, -0.2F))
                .texOffs(200, 22).addBox(-1.5F, -3F, 8F, 1F, 3F, 2F, new CubeDeformation(-0.1F, -0.4F, -0.2F))
                .texOffs(144, 0).addBox(-2F, -2.4F, -12.4F, 4F, 2F, 12F, new CubeDeformation(-0.25F, -0.05F, -0.1F))
                .texOffs(86, 0).addBox(-1F, -3.4F, -12.4F, 2F, 4F, 12F, new CubeDeformation(-0.05F, -0.25F, -0.1F))
                .texOffs(114, 0).addBox(-1.5F, -2.9F, -12.4F, 3F, 3F, 12F, new CubeDeformation(0F, 0F, -0.1F))
                .texOffs(226, 22).addBox(-1.5F, -2.9F, -13.05F, 3F, 3F, 1F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(234, 22).addBox(-0.5F, -1.9F, -15.5F, 1F, 1F, 3F, new CubeDeformation(-0.05F, -0.05F, -0.3F))
                .texOffs(164, 22).addBox(-2F, -3.4F, -14.25F, 4F, 4F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.35F))
                .texOffs(242, 22).addBox(-1F, -2.4F, -16F, 2F, 2F, 2F, new CubeDeformation(-0.25F, -0.25F, -0.4F))
                .texOffs(110, 30).addBox(-1F, -3.8F, -11.75F, 2F, 1F, 1F, new CubeDeformation(-0.45F, -0.35F, -0.15F))
                .texOffs(102, 30).addBox(-0.5F, -5.1F, -11.75F, 1F, 2F, 1F, new CubeDeformation(-0.34F, -0.35F, -0.3F))
                .texOffs(126, 30).addBox(-0.5F, -5.375F, -11.75F, 1F, 1F, 1F, new CubeDeformation(-0.43F, -0.375F, -0.35F))
                .texOffs(102, 30).addBox(-0.06F, -5.325F, -11.75F, 1F, 2F, 1F, new CubeDeformation(-0.42F, -0.125F, -0.15F))
                .texOffs(102, 30).addBox(-0.94F, -5.325F, -11.75F, 1F, 2F, 1F, new CubeDeformation(-0.42F, -0.125F, -0.15F))
                .texOffs(22, 30).addBox(0.11F, -6.2F, 2.525F, 1F, 3F, 1F, new CubeDeformation(-0.39F, -0.2F, -0.375F))
                .texOffs(22, 30).addBox(-1.11F, -6.2F, 2.525F, 1F, 3F, 1F, new CubeDeformation(-0.39F, -0.2F, -0.375F))
                .texOffs(110, 30).addBox(-1F, -6.6F, 2.525F, 2F, 1F, 1F, new CubeDeformation(-0.28F, -0.4F, -0.375F))
                .texOffs(110, 30).addBox(-1F, -5.1F, 2.525F, 2F, 1F, 1F, new CubeDeformation(-0.28F, -0.35F, -0.275F))
                .texOffs(126, 30).addBox(-0.24F, -5.425F, 2.525F, 1F, 1F, 1F, new CubeDeformation(-0.34F, -0.325F, -0.325F))
                .texOffs(126, 30).addBox(-0.76F, -5.425F, 2.525F, 1F, 1F, 1F, new CubeDeformation(-0.34F, -0.325F, -0.325F))
                .texOffs(130, 30).addBox(0.335F, -5.1F, 2.525F, 1F, 1F, 1F, new CubeDeformation(-0.385F, -0.4F, -0.375F))
                .texOffs(110, 30).addBox(-1F, -4F, 2.55F, 2F, 1F, 1F, new CubeDeformation(-0.2F, -0.4F, -0.05F))
                .texOffs(130, 30).addBox(0.6F, -4.15F, 2.4F, 1F, 1F, 1F, new CubeDeformation(-0.3F, -0.25F, -0.2F))
                .texOffs(164, 22).addBox(-2F, -3.4F, -1.1F, 4F, 4F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.2F))
                .texOffs(176, 0).addBox(-0.5F, 0.15F, -12.25F, 1F, 1F, 11F, new CubeDeformation(-0.15F, -0.25F, -0.25F))
                .texOffs(106, 30).addBox(-0.5F, 0.2F, -12.05F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.15F))
                .texOffs(122, 30).addBox(-2.4F, -1.9F, 4.7F, 1F, 1F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(0, 30).addBox(-2.8F, -1.8F, 5.3F, 1F, 1F, 3F, new CubeDeformation(-0.3F, -0.2F, -0.3F))
                .texOffs(116, 30).addBox(-1F, -3.9F, -3F, 2F, 1F, 1F, new CubeDeformation(-0.3F, -0.3F, 0F))
                .texOffs(174, 22).addBox(-3.15F, -2.6F, 0.25F, 2F, 2F, 3F, new CubeDeformation(-0.35F, 0F, -0.25F))
                .texOffs(206, 22).addBox(-3.5F, -2.3F, 0.7F, 1F, 3F, 2F, new CubeDeformation(-0.1F, -0.3F, -0.3F))
                , PartPose.offsetAndRotation(0F, -2F, 0F, 0F, 0F, 0F));
        p7.addOrReplaceChild("scope", CubeListBuilder.create()
                .texOffs(192, 22).addBox(1.4F, -2.7F, 3.5F, 1F, 2F, 3F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(78, 22).addBox(1.6F, -5.3F, 3.2F, 2F, 3F, 4F, new CubeDeformation(-0.1F, -0.1F, -0.2F))
                .texOffs(110, 22).addBox(1.6F, -5.4F, -0.5F, 2F, 2F, 4F, new CubeDeformation(-0.4F, -0.4F, -0.1F))
                .texOffs(96, 30).addBox(1.6F, -5.4F, -1.15F, 2F, 2F, 1F, new CubeDeformation(-0.25F, -0.25F, -0.25F))
                .texOffs(8, 30).addBox(1.6F, -4.7F, 6.6F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.3F, -0.4F))
                .texOffs(16, 30).addBox(3.2F, -4.7F, 3.9F, 1F, 2F, 2F, new CubeDeformation(-0.3F, -0.3F, -0.3F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
    }


}
