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
                .texOffs(120, 0).addBox(-4.5F, -0.8F, -4.5F, 9F, 0.8F, 9F)
                .texOffs(12, 24).addBox(-4F, -1.2F, 3.2F, 0.8F, 0.4F, 0.8F)
                .texOffs(12, 24).addBox(-4F, -1.2F, -4F, 0.8F, 0.4F, 0.8F)
                .texOffs(12, 24).addBox(3.2F, -1.2F, 3.2F, 0.8F, 0.4F, 0.8F)
                .texOffs(12, 24).addBox(3.2F, -1.2F, -4F, 0.8F, 0.4F, 0.8F)
                .texOffs(0, 0).addBox(-1.3F, -14.8F, -1.3F, 2.6F, 14F, 2.6F)
                .texOffs(62, 17).addBox(1.3F, -5F, -0.5F, 1.3F, 4.2F, 1F)
                .texOffs(0, 17).addBox(-0.5F, -5F, -2.6F, 1F, 4.2F, 1.3F)
                .texOffs(62, 17).addBox(-2.6F, -5F, -0.5F, 1.3F, 4.2F, 1F)
                .texOffs(194, 17).addBox(-0.5F, -5F, 2.6F, 1F, 4.2F, -1.3F)
                .texOffs(206, 0).addBox(-2.2F, -16F, -2.2F, 4.4F, 1.2F, 4.4F)
                , PartPose.offsetAndRotation(0F, 24F, 0F, 0F, 0F, 0F));
    }

    private static void build_mount(PartDefinition root) {
        PartDefinition p2 = root.addOrReplaceChild("mount", CubeListBuilder.create()
                .texOffs(182, 0).addBox(-2.6F, -1F, -3F, 5.2F, 1F, 6F)
                .texOffs(88, 17).addBox(1.9F, -2.8F, -1.2F, 0.7F, 1.8F, 2.4F)
                .texOffs(88, 17).addBox(-2.6F, -2.8F, -1.2F, 0.7F, 1.8F, 2.4F)
                .texOffs(54, 17).addBox(1.2F, -2.6F, 3F, 1F, 2.4F, 3F)
                .texOffs(156, 0).addBox(-7.2F, -0.6F, -0.6F, 4.2F, 4.2F, 4F)
                .texOffs(6, 17).addBox(-7.4F, -1F, -0.8F, 4.6F, 0.4F, 4.4F)
                .texOffs(16, 24).addBox(-5.6F, -1.5F, 0.8F, 1F, 0.5F, 1F)
                .texOffs(203, 17).addBox(-3F, -0.4F, 0.6F, 0.8F, 1F, 1.4F)
                , PartPose.offsetAndRotation(0F, 8F, 0F, 0F, 0F, 0F));
        p2.addOrReplaceChild("shield", CubeListBuilder.create()
                .texOffs(116, 17).addBox(-5F, -1.4F, -4.7F, 10F, 2.4F, 0.5F)
                .texOffs(174, 0).addBox(2.2F, -8.2F, -4.7F, 2.8F, 6.8F, 0.5F)
                .texOffs(174, 0).addBox(-5F, -8.2F, -4.7F, 2.8F, 6.8F, 0.5F)
                .texOffs(229, 17).addBox(-5F, -8.9F, -4.7F, 10F, 0.7F, 0.5F)
                .texOffs(26, 17).addBox(-0.6F, 0.8F, -4.2F, 1.2F, -0.3F, 5.2F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p4 = p2.addOrReplaceChild("gun", CubeListBuilder.create()
                .texOffs(98, 0).addBox(-1.5F, -3F, -0.5F, 3F, 3F, 7.5F)
                .texOffs(226, 0).addBox(-1.5F, -3.4F, 0.5F, 3F, 0.4F, 6F)
                .texOffs(138, 17).addBox(-1.2F, -2.4F, 7F, 2.4F, 2F, 1.2F)
                .texOffs(104, 17).addBox(0.6F, -2.6F, 8.2F, 0.8F, 2.2F, 1.6F)
                .texOffs(104, 17).addBox(-1.4F, -2.6F, 8.2F, 0.8F, 2.2F, 1.6F)
                .texOffs(42, 0).addBox(-1.8F, -2.65F, -12.3F, 3.6F, 2.5F, 11.8F)
                .texOffs(12, 0).addBox(-1.25F, -3.2F, -12.3F, 2.5F, 3.6F, 11.8F)
                .texOffs(148, 17).addBox(-1.5F, -2.9F, -12.8F, 3F, 3F, 0.5F)
                .texOffs(156, 17).addBox(-0.45F, -1.85F, -15.2F, 0.9F, 0.9F, 2.4F)
                .texOffs(68, 17).addBox(-1.7F, -3.1F, -13.9F, 3.4F, 3.4F, 0.3F)
                .texOffs(164, 17).addBox(-0.75F, -2.15F, -15.6F, 1.5F, 1.5F, 1.2F)
                .texOffs(0, 24).addBox(-0.8F, -3.6F, -11.8F, 1.6F, 0.4F, 1F)
                .texOffs(221, 17).addBox(-0.25F, -4.7F, -11.5F, 0.5F, 1.1F, 0.5F)
                .texOffs(20, 24).addBox(-0.12F, -5F, -11.45F, 0.24F, 0.3F, 0.4F)
                .texOffs(221, 17).addBox(0.55F, -5.4F, -11.8F, 0.25F, 1.8F, 1F)
                .texOffs(221, 17).addBox(-0.8F, -5.4F, -11.8F, 0.25F, 1.8F, 1F)
                .texOffs(209, 17).addBox(-0.9F, -4.75F, 2.8F, 1.8F, 1.35F, 0.4F)
                .texOffs(20, 24).addBox(0.25F, -5.7F, 2.8F, 0.7F, 0.95F, 0.4F)
                .texOffs(20, 24).addBox(-0.95F, -5.7F, 2.8F, 0.7F, 0.95F, 0.4F)
                .texOffs(195, 17).addBox(-1F, -3.7F, 2.4F, 2F, 0.3F, 1.2F)
                .texOffs(24, 24).addBox(0.9F, -3.9F, 2.6F, 0.4F, 0.5F, 0.6F)
                .texOffs(68, 17).addBox(-1.6F, -3F, -0.9F, 3.2F, 3.2F, 0.6F)
                .texOffs(74, 0).addBox(-0.35F, 0.4F, -12F, 0.7F, 0.5F, 10.5F)
                .texOffs(225, 17).addBox(-0.4F, 0.4F, -11.9F, 0.8F, 1.6F, 0.7F)
                .texOffs(16, 24).addBox(-2.3F, -1.8F, 4.8F, 0.8F, 0.8F, 0.8F)
                .texOffs(172, 17).addBox(-2.5F, -1.6F, 5.6F, 0.4F, 0.6F, 2.4F)
                .texOffs(6, 24).addBox(-0.7F, -3.6F, -3F, 1.4F, 0.4F, 1F)
                .texOffs(78, 17).addBox(-2.8F, -2.6F, 0.5F, 1.3F, 2F, 2.5F)
                .texOffs(110, 17).addBox(-3.4F, -2F, 1F, 0.8F, 2.4F, 1.4F)
                , PartPose.offsetAndRotation(0F, -2F, 0F, 0F, 0F, 0F));
        p4.addOrReplaceChild("scope", CubeListBuilder.create()
                .texOffs(96, 17).addBox(1.5F, -2.4F, 3.6F, 0.8F, 1.4F, 2.8F)
                .texOffs(244, 0).addBox(1.7F, -5.2F, 3.4F, 1.8F, 2.8F, 3.6F)
                .texOffs(42, 17).addBox(2F, -5F, -0.4F, 1.2F, 1.2F, 3.8F)
                .texOffs(215, 17).addBox(1.85F, -5.15F, -0.9F, 1.5F, 1.5F, 0.5F)
                .texOffs(180, 17).addBox(2F, -4.4F, 7F, 1.2F, 1.4F, 1.2F)
                .texOffs(188, 17).addBox(3.5F, -4.4F, 4.2F, 0.4F, 1.4F, 1.4F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
    }


}
