package com.exodusvehicles.client;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** MG 08 on the bunker pedestal - GENERATED from truck/mg08_geom.py (BUNKER=1). Same parts as FieldMgModel. */
public final class FieldMgBunkerLayer {
    private FieldMgBunkerLayer() {}

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        build_sled(root);
        build_mount(root);
        return LayerDefinition.create(mesh, 256, 256);
    }

    private static void build_sled(PartDefinition root) {
        root.addOrReplaceChild("sled", CubeListBuilder.create()
                .texOffs(112, 0).addBox(-4.5F, -0.8F, -4.5F, 9F, 0.8F, 9F)
                .texOffs(179, 21).addBox(-4F, -1.2F, 3.2F, 0.8F, 0.4F, 0.8F)
                .texOffs(179, 21).addBox(-4F, -1.2F, -4F, 0.8F, 0.4F, 0.8F)
                .texOffs(179, 21).addBox(3.2F, -1.2F, 3.2F, 0.8F, 0.4F, 0.8F)
                .texOffs(179, 21).addBox(3.2F, -1.2F, -4F, 0.8F, 0.4F, 0.8F)
                .texOffs(0, 0).addBox(-1.3F, -18.8F, -1.3F, 2.6F, 18F, 2.6F)
                .texOffs(0, 21).addBox(1.3F, -5F, -0.5F, 1.3F, 4.2F, 1F)
                .texOffs(242, 0).addBox(-0.5F, -5F, -2.6F, 1F, 4.2F, 1.3F)
                .texOffs(0, 21).addBox(-2.6F, -5F, -0.5F, 1.3F, 4.2F, 1F)
                .texOffs(108, 21).addBox(-0.5F, -5F, 2.6F, 1F, 4.2F, -1.3F)
                .texOffs(188, 0).addBox(-2.2F, -20F, -2.2F, 4.4F, 1.2F, 4.4F)
                .texOffs(226, 0).addBox(2.2F, -17F, -1F, 4F, 3F, 4F)
                .texOffs(183, 21).addBox(1.3F, -16F, 0.5F, 0.9F, 1F, 1F)
                , PartPose.offsetAndRotation(0F, 24F, 0F, 0F, 0F, 0F));
    }

    private static void build_mount(PartDefinition root) {
        PartDefinition p2 = root.addOrReplaceChild("mount", CubeListBuilder.create()
                .texOffs(164, 0).addBox(-2.6F, -1F, -3F, 5.2F, 1F, 6F)
                .texOffs(60, 21).addBox(1.9F, -2.8F, -1.2F, 0.7F, 1.8F, 2.4F)
                .texOffs(60, 21).addBox(-2.6F, -2.8F, -1.2F, 0.7F, 1.8F, 2.4F)
                .texOffs(248, 0).addBox(1.2F, -2.6F, 3F, 1F, 2.4F, 3F)
                , PartPose.offsetAndRotation(0F, 4F, 0F, 0F, 0F, 0F));
        p2.addOrReplaceChild("shield", CubeListBuilder.create()
                .texOffs(6, 21).addBox(-8F, -0.8F, -4.8F, 16F, 3.3F, 0.6F)
                .texOffs(109, 21).addBox(-8F, -12F, -4.8F, 16F, 2F, 0.6F)
                .texOffs(96, 0).addBox(4.2F, -10F, -4.8F, 3.8F, 9.2F, 0.6F)
                .texOffs(96, 0).addBox(-8F, -10F, -4.8F, 3.8F, 9.2F, 0.6F)
                .texOffs(12, 0).addBox(7.6F, -12F, -4.8F, 0.6F, 14.5F, 2.6F)
                .texOffs(12, 0).addBox(-8.2F, -12F, -4.8F, 0.6F, 14.5F, 2.6F)
                .texOffs(148, 0).addBox(-0.6F, -1F, -4.2F, 1.2F, 1.5F, 5.2F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p2.addOrReplaceChild("gun", CubeListBuilder.create()
                .texOffs(52, 0).addBox(-1.5F, -3F, -0.5F, 3F, 3F, 7.5F)
                .texOffs(208, 0).addBox(-1.5F, -3.4F, 0.5F, 3F, 0.4F, 6F)
                .texOffs(68, 21).addBox(-1.2F, -2.4F, 7F, 2.4F, 2F, 1.2F)
                .texOffs(102, 21).addBox(0.7F, -2.2F, 8.2F, 0.8F, 1.6F, 1.4F)
                .texOffs(102, 21).addBox(-1.5F, -2.2F, 8.2F, 0.8F, 1.6F, 1.4F)
                .texOffs(20, 0).addBox(-1.8F, -3.2F, -12.5F, 3.6F, 3.6F, 12F)
                .texOffs(78, 21).addBox(-1F, -2.4F, -14F, 2F, 2F, 1.5F)
                .texOffs(86, 21).addBox(-1.3F, -2.7F, -15F, 2.6F, 2.6F, 1F)
                .texOffs(161, 21).addBox(-0.8F, -3.6F, -11.8F, 1.6F, 0.4F, 1F)
                .texOffs(157, 21).addBox(-0.25F, -4.7F, -11.5F, 0.5F, 1.1F, 0.5F)
                .texOffs(187, 21).addBox(-0.12F, -5F, -11.45F, 0.24F, 0.3F, 0.4F)
                .texOffs(157, 21).addBox(0.55F, -5.4F, -11.8F, 0.25F, 1.8F, 1F)
                .texOffs(157, 21).addBox(-0.8F, -5.4F, -11.8F, 0.25F, 1.8F, 1F)
                .texOffs(151, 21).addBox(-0.9F, -4.75F, 2.8F, 1.8F, 1.35F, 0.4F)
                .texOffs(187, 21).addBox(0.25F, -5.7F, 2.8F, 0.7F, 0.95F, 0.4F)
                .texOffs(187, 21).addBox(-0.95F, -5.7F, 2.8F, 0.7F, 0.95F, 0.4F)
                .texOffs(143, 21).addBox(-1F, -3.7F, 2.4F, 2F, 0.3F, 1.2F)
                .texOffs(191, 21).addBox(0.9F, -3.9F, 2.6F, 0.4F, 0.5F, 0.6F)
                .texOffs(40, 21).addBox(-2F, -3.4F, -1.3F, 4F, 4F, 0.8F)
                .texOffs(40, 21).addBox(-2F, -3.4F, -12.5F, 4F, 4F, 0.8F)
                .texOffs(74, 0).addBox(1.8F, -1.4F, -11.7F, 0.6F, 0.6F, 9.7F)
                .texOffs(183, 21).addBox(-2.3F, -1.8F, 4.8F, 0.8F, 0.8F, 0.8F)
                .texOffs(94, 21).addBox(-2.5F, -1.6F, 5.6F, 0.4F, 0.6F, 2.4F)
                .texOffs(167, 21).addBox(-0.7F, -3.6F, -3F, 1.4F, 0.4F, 1F)
                .texOffs(50, 21).addBox(-2.8F, -2.6F, 0.5F, 1.3F, 2F, 2.5F)
                .texOffs(106, 0).addBox(-3.4F, -2F, 1F, 0.8F, 8.5F, 1.4F)
                .texOffs(173, 21).addBox(1.5F, -1.2F, 1.6F, 1.9F, 1F, 1F)
                , PartPose.offsetAndRotation(0F, -2F, 0F, 0F, 0F, 0F));
    }

}
