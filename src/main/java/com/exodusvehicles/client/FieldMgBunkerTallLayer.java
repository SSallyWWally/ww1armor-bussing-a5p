package com.exodusvehicles.client;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** MG 08 on the tall bunker pedestal - GENERATED from truck/mg08_geom.py (MOUNT=bunker_tall). Same parts as FieldMgModel. */
public final class FieldMgBunkerTallLayer {
    private FieldMgBunkerTallLayer() {}

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        build_sled(root);
        build_mount(root);
        return LayerDefinition.create(mesh, 256, 256);
    }

    private static void build_sled(PartDefinition root) {
        root.addOrReplaceChild("sled", CubeListBuilder.create()
                .texOffs(148, 0).addBox(-4.5F, -0.9F, -4.5F, 9F, 1F, 9F, new CubeDeformation(0F, -0.1F, 0F))
                .texOffs(34, 28).addBox(-4.1F, -1.5F, 3.1F, 1F, 1F, 1F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(34, 28).addBox(-4.1F, -1.5F, -4.1F, 1F, 1F, 1F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(34, 28).addBox(3.1F, -1.5F, 3.1F, 1F, 1F, 1F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(34, 28).addBox(3.1F, -1.5F, -4.1F, 1F, 1F, 1F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(0, 0).addBox(-1.5F, -18.8F, -1.5F, 3F, 18F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(92, 21).addBox(0.95F, -5.4F, -0.5F, 2F, 5F, 1F, new CubeDeformation(-0.35F, -0.4F, 0F))
                .texOffs(30, 21).addBox(-0.5F, -5.4F, -2.95F, 1F, 5F, 2F, new CubeDeformation(0F, -0.4F, -0.35F))
                .texOffs(92, 21).addBox(-2.95F, -5.4F, -0.5F, 2F, 5F, 1F, new CubeDeformation(-0.35F, -0.4F, 0F))
                .texOffs(228, 21).addBox(-0.5F, -5.4F, 2.45F, 1F, 5F, -1F, new CubeDeformation(0F, -0.4F, -0.15F))
                .texOffs(234, 0).addBox(-2.5F, -20.4F, -2.5F, 5F, 2F, 5F, new CubeDeformation(-0.3F, -0.4F, -0.3F))
                , PartPose.offsetAndRotation(0F, 24F, 0F, 0F, 0F, 0F));
    }

    private static void build_mount(PartDefinition root) {
        PartDefinition p2 = root.addOrReplaceChild("mount", CubeListBuilder.create()
                .texOffs(210, 0).addBox(-3F, -1F, -3F, 6F, 1F, 6F, new CubeDeformation(-0.4F, 0F, 0F))
                .texOffs(118, 21).addBox(1.75F, -2.9F, -1.5F, 1F, 2F, 3F, new CubeDeformation(-0.15F, -0.1F, -0.3F))
                .texOffs(118, 21).addBox(-2.75F, -2.9F, -1.5F, 1F, 2F, 3F, new CubeDeformation(-0.15F, -0.1F, -0.3F))
                .texOffs(84, 21).addBox(1.2F, -2.9F, 3F, 1F, 3F, 3F, new CubeDeformation(0F, -0.3F, 0F))
                .texOffs(184, 0).addBox(-7.6F, -1F, -0.6F, 5F, 5F, 4F, new CubeDeformation(-0.4F, -0.4F, 0F))
                .texOffs(36, 21).addBox(-7.6F, -1.3F, -1.1F, 5F, 1F, 5F, new CubeDeformation(-0.2F, -0.3F, -0.3F))
                .texOffs(38, 28).addBox(-5.6F, -1.75F, 0.8F, 1F, 1F, 1F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(229, 21).addBox(-3.1F, -0.4F, 0.3F, 1F, 1F, 2F, new CubeDeformation(-0.1F, 0F, -0.3F))
                , PartPose.offsetAndRotation(0F, 4F, 0F, 0F, 0F, 0F));
        p2.addOrReplaceChild("shield", CubeListBuilder.create()
                .texOffs(146, 21).addBox(-5F, -1.7F, -4.95F, 10F, 3F, 1F, new CubeDeformation(0F, -0.3F, -0.25F))
                .texOffs(202, 0).addBox(2.1F, -8.3F, -4.95F, 3F, 7F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.25F))
                .texOffs(202, 0).addBox(-5.1F, -8.3F, -4.95F, 3F, 7F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.25F))
                .texOffs(0, 28).addBox(-5F, -9.05F, -4.95F, 10F, 1F, 1F, new CubeDeformation(0F, -0.15F, -0.25F))
                .texOffs(56, 21).addBox(-1F, 0.65F, -4.6F, 2F, 0F, 6F, new CubeDeformation(-0.4F, -0.15F, -0.4F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p4 = p2.addOrReplaceChild("gun", CubeListBuilder.create()
                .texOffs(126, 0).addBox(-1.5F, -3F, -0.75F, 3F, 3F, 8F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(0, 21).addBox(-1.5F, -3.7F, 0.5F, 3F, 1F, 6F, new CubeDeformation(0F, -0.3F, 0F))
                .texOffs(168, 21).addBox(-1.5F, -2.4F, 6.6F, 3F, 2F, 2F, new CubeDeformation(-0.3F, 0F, -0.4F))
                .texOffs(134, 21).addBox(0.5F, -3F, 8F, 1F, 3F, 2F, new CubeDeformation(-0.1F, -0.4F, -0.2F))
                .texOffs(134, 21).addBox(-1.5F, -3F, 8F, 1F, 3F, 2F, new CubeDeformation(-0.1F, -0.4F, -0.2F))
                .texOffs(70, 0).addBox(-2F, -2.4F, -12.4F, 4F, 2F, 12F, new CubeDeformation(-0.25F, -0.05F, -0.1F))
                .texOffs(12, 0).addBox(-1F, -3.4F, -12.4F, 2F, 4F, 12F, new CubeDeformation(-0.05F, -0.25F, -0.1F))
                .texOffs(40, 0).addBox(-1.5F, -2.9F, -12.4F, 3F, 3F, 12F, new CubeDeformation(0F, 0F, -0.1F))
                .texOffs(178, 21).addBox(-1.5F, -2.9F, -13.05F, 3F, 3F, 1F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(186, 21).addBox(-0.5F, -1.9F, -15.5F, 1F, 1F, 3F, new CubeDeformation(-0.05F, -0.05F, -0.3F))
                .texOffs(98, 21).addBox(-2F, -3.4F, -14.25F, 4F, 4F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.35F))
                .texOffs(194, 21).addBox(-1F, -2.4F, -16F, 2F, 2F, 2F, new CubeDeformation(-0.25F, -0.25F, -0.4F))
                .texOffs(22, 28).addBox(-1F, -3.8F, -11.75F, 2F, 1F, 1F, new CubeDeformation(-0.45F, -0.35F, -0.15F))
                .texOffs(241, 21).addBox(-0.5F, -5.1F, -11.75F, 1F, 2F, 1F, new CubeDeformation(-0.34F, -0.35F, -0.3F))
                .texOffs(42, 28).addBox(-0.5F, -5.375F, -11.75F, 1F, 1F, 1F, new CubeDeformation(-0.43F, -0.375F, -0.35F))
                .texOffs(241, 21).addBox(-0.06F, -5.325F, -11.75F, 1F, 2F, 1F, new CubeDeformation(-0.42F, -0.125F, -0.15F))
                .texOffs(241, 21).addBox(-0.94F, -5.325F, -11.75F, 1F, 2F, 1F, new CubeDeformation(-0.42F, -0.125F, -0.15F))
                .texOffs(224, 21).addBox(0.11F, -6.2F, 2.525F, 1F, 3F, 1F, new CubeDeformation(-0.39F, -0.2F, -0.375F))
                .texOffs(224, 21).addBox(-1.11F, -6.2F, 2.525F, 1F, 3F, 1F, new CubeDeformation(-0.39F, -0.2F, -0.375F))
                .texOffs(22, 28).addBox(-1F, -6.6F, 2.525F, 2F, 1F, 1F, new CubeDeformation(-0.28F, -0.4F, -0.375F))
                .texOffs(22, 28).addBox(-1F, -5.1F, 2.525F, 2F, 1F, 1F, new CubeDeformation(-0.28F, -0.35F, -0.275F))
                .texOffs(42, 28).addBox(-0.24F, -5.425F, 2.525F, 1F, 1F, 1F, new CubeDeformation(-0.34F, -0.325F, -0.325F))
                .texOffs(42, 28).addBox(-0.76F, -5.425F, 2.525F, 1F, 1F, 1F, new CubeDeformation(-0.34F, -0.325F, -0.325F))
                .texOffs(46, 28).addBox(0.335F, -5.1F, 2.525F, 1F, 1F, 1F, new CubeDeformation(-0.385F, -0.4F, -0.375F))
                .texOffs(22, 28).addBox(-1F, -4F, 2.55F, 2F, 1F, 1F, new CubeDeformation(-0.2F, -0.4F, -0.05F))
                .texOffs(46, 28).addBox(0.6F, -4.15F, 2.4F, 1F, 1F, 1F, new CubeDeformation(-0.3F, -0.25F, -0.2F))
                .texOffs(98, 21).addBox(-2F, -3.4F, -1.1F, 4F, 4F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.2F))
                .texOffs(102, 0).addBox(-0.5F, 0.15F, -12.25F, 1F, 1F, 11F, new CubeDeformation(-0.15F, -0.25F, -0.25F))
                .texOffs(245, 21).addBox(-0.5F, 0.2F, -12.05F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.15F))
                .texOffs(38, 28).addBox(-2.4F, -1.9F, 4.7F, 1F, 1F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(202, 21).addBox(-2.8F, -1.8F, 5.3F, 1F, 1F, 3F, new CubeDeformation(-0.3F, -0.2F, -0.3F))
                .texOffs(28, 28).addBox(-1F, -3.9F, -3F, 2F, 1F, 1F, new CubeDeformation(-0.3F, -0.3F, 0F))
                .texOffs(108, 21).addBox(-3.15F, -2.6F, 0.25F, 2F, 2F, 3F, new CubeDeformation(-0.35F, 0F, -0.25F))
                .texOffs(140, 21).addBox(-3.5F, -2.3F, 0.7F, 1F, 3F, 2F, new CubeDeformation(-0.1F, -0.3F, -0.3F))
                , PartPose.offsetAndRotation(0F, -2F, 0F, 0F, 0F, 0F));
        p4.addOrReplaceChild("scope", CubeListBuilder.create()
                .texOffs(126, 21).addBox(1.4F, -2.7F, 3.5F, 1F, 2F, 3F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(18, 21).addBox(1.6F, -5.3F, 3.2F, 2F, 3F, 4F, new CubeDeformation(-0.1F, -0.1F, -0.2F))
                .texOffs(72, 21).addBox(1.6F, -5.4F, -0.5F, 2F, 2F, 4F, new CubeDeformation(-0.4F, -0.4F, -0.1F))
                .texOffs(235, 21).addBox(1.6F, -5.4F, -1.15F, 2F, 2F, 1F, new CubeDeformation(-0.25F, -0.25F, -0.25F))
                .texOffs(210, 21).addBox(1.6F, -4.7F, 6.6F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.3F, -0.4F))
                .texOffs(218, 21).addBox(3.2F, -4.7F, 3.9F, 1F, 2F, 2F, new CubeDeformation(-0.3F, -0.3F, -0.3F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
    }


}
