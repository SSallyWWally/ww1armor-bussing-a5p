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
                .texOffs(138, 0).addBox(-4.5F, -0.8F, -4.5F, 9F, 0.8F, 9F)
                .texOffs(249, 21).addBox(-4F, -1.2F, 3.2F, 0.8F, 0.4F, 0.8F)
                .texOffs(249, 21).addBox(-4F, -1.2F, -4F, 0.8F, 0.4F, 0.8F)
                .texOffs(249, 21).addBox(3.2F, -1.2F, 3.2F, 0.8F, 0.4F, 0.8F)
                .texOffs(249, 21).addBox(3.2F, -1.2F, -4F, 0.8F, 0.4F, 0.8F)
                .texOffs(0, 0).addBox(-1.3F, -18.8F, -1.3F, 2.6F, 18F, 2.6F)
                .texOffs(52, 21).addBox(1.3F, -5F, -0.5F, 1.3F, 4.2F, 1F)
                .texOffs(18, 21).addBox(-0.5F, -5F, -2.6F, 1F, 4.2F, 1.3F)
                .texOffs(52, 21).addBox(-2.6F, -5F, -0.5F, 1.3F, 4.2F, 1F)
                .texOffs(174, 21).addBox(-0.5F, -5F, 2.6F, 1F, 4.2F, -1.3F)
                .texOffs(232, 0).addBox(-2.2F, -20F, -2.2F, 4.4F, 1.2F, 4.4F)
                , PartPose.offsetAndRotation(0F, 24F, 0F, 0F, 0F, 0F));
    }

    private static void build_mount(PartDefinition root) {
        PartDefinition p2 = root.addOrReplaceChild("mount", CubeListBuilder.create()
                .texOffs(208, 0).addBox(-2.6F, -1F, -3F, 5.2F, 1F, 6F)
                .texOffs(112, 21).addBox(1.9F, -2.8F, -1.2F, 0.7F, 1.8F, 2.4F)
                .texOffs(112, 21).addBox(-2.6F, -2.8F, -1.2F, 0.7F, 1.8F, 2.4F)
                .texOffs(44, 21).addBox(1.2F, -2.6F, 3F, 1F, 2.4F, 3F)
                .texOffs(174, 0).addBox(-7.2F, -0.6F, -0.6F, 4.2F, 4.2F, 4F)
                .texOffs(24, 21).addBox(-7.4F, -1F, -0.8F, 4.6F, 0.4F, 4.4F)
                .texOffs(0, 28).addBox(-5.6F, -1.5F, 0.8F, 1F, 0.5F, 1F)
                .texOffs(217, 21).addBox(-3F, -0.4F, 0.6F, 0.8F, 1F, 1.4F)
                , PartPose.offsetAndRotation(0F, 4F, 0F, 0F, 0F, 0F));
        p2.addOrReplaceChild("shield", CubeListBuilder.create()
                .texOffs(58, 21).addBox(-8F, -0.8F, -4.8F, 16F, 3.3F, 0.6F)
                .texOffs(175, 21).addBox(-8F, -12F, -4.8F, 16F, 2F, 0.6F)
                .texOffs(128, 0).addBox(4.2F, -10F, -4.8F, 3.8F, 9.2F, 0.6F)
                .texOffs(128, 0).addBox(-8F, -10F, -4.8F, 3.8F, 9.2F, 0.6F)
                .texOffs(12, 0).addBox(7.6F, -12F, -4.8F, 0.6F, 14.5F, 2.6F)
                .texOffs(12, 0).addBox(-8.2F, -12F, -4.8F, 0.6F, 14.5F, 2.6F)
                .texOffs(192, 0).addBox(-0.6F, -1F, -4.2F, 1.2F, 1.5F, 5.2F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p2.addOrReplaceChild("gun", CubeListBuilder.create()
                .texOffs(106, 0).addBox(-1.5F, -3F, -0.5F, 3F, 3F, 7.5F)
                .texOffs(0, 21).addBox(-1.5F, -3.4F, 0.5F, 3F, 0.4F, 6F)
                .texOffs(132, 21).addBox(-1.2F, -2.4F, 7F, 2.4F, 2F, 1.2F)
                .texOffs(120, 21).addBox(0.6F, -2.6F, 8.2F, 0.8F, 2.2F, 1.6F)
                .texOffs(120, 21).addBox(-1.4F, -2.6F, 8.2F, 0.8F, 2.2F, 1.6F)
                .texOffs(50, 0).addBox(-1.8F, -2.65F, -12.3F, 3.6F, 2.5F, 11.8F)
                .texOffs(20, 0).addBox(-1.25F, -3.2F, -12.3F, 2.5F, 3.6F, 11.8F)
                .texOffs(142, 21).addBox(-1.5F, -2.9F, -12.8F, 3F, 3F, 0.5F)
                .texOffs(150, 21).addBox(-0.45F, -1.85F, -15.2F, 0.9F, 0.9F, 2.4F)
                .texOffs(92, 21).addBox(-1.7F, -3.1F, -13.9F, 3.4F, 3.4F, 0.3F)
                .texOffs(158, 21).addBox(-0.75F, -2.15F, -15.6F, 1.5F, 1.5F, 1.2F)
                .texOffs(237, 21).addBox(-0.8F, -3.6F, -11.8F, 1.6F, 0.4F, 1F)
                .texOffs(229, 21).addBox(-0.25F, -4.7F, -11.5F, 0.5F, 1.1F, 0.5F)
                .texOffs(4, 28).addBox(-0.12F, -5F, -11.45F, 0.24F, 0.3F, 0.4F)
                .texOffs(229, 21).addBox(0.55F, -5.4F, -11.8F, 0.25F, 1.8F, 1F)
                .texOffs(229, 21).addBox(-0.8F, -5.4F, -11.8F, 0.25F, 1.8F, 1F)
                .texOffs(223, 21).addBox(-0.9F, -4.75F, 2.8F, 1.8F, 1.35F, 0.4F)
                .texOffs(4, 28).addBox(0.25F, -5.7F, 2.8F, 0.7F, 0.95F, 0.4F)
                .texOffs(4, 28).addBox(-0.95F, -5.7F, 2.8F, 0.7F, 0.95F, 0.4F)
                .texOffs(209, 21).addBox(-1F, -3.7F, 2.4F, 2F, 0.3F, 1.2F)
                .texOffs(8, 28).addBox(0.9F, -3.9F, 2.6F, 0.4F, 0.5F, 0.6F)
                .texOffs(92, 21).addBox(-1.6F, -3F, -0.9F, 3.2F, 3.2F, 0.6F)
                .texOffs(82, 0).addBox(-0.35F, 0.4F, -12F, 0.7F, 0.5F, 10.5F)
                .texOffs(233, 21).addBox(-0.4F, 0.4F, -11.9F, 0.8F, 1.6F, 0.7F)
                .texOffs(0, 28).addBox(-2.3F, -1.8F, 4.8F, 0.8F, 0.8F, 0.8F)
                .texOffs(166, 21).addBox(-2.5F, -1.6F, 5.6F, 0.4F, 0.6F, 2.4F)
                .texOffs(243, 21).addBox(-0.7F, -3.6F, -3F, 1.4F, 0.4F, 1F)
                .texOffs(102, 21).addBox(-2.8F, -2.6F, 0.5F, 1.3F, 2F, 2.5F)
                .texOffs(126, 21).addBox(-3.4F, -2F, 1F, 0.8F, 2.4F, 1.4F)
                , PartPose.offsetAndRotation(0F, -2F, 0F, 0F, 0F, 0F));
    }


}
