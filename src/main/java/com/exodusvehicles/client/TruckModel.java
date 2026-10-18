package com.exodusvehicles.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.exodusvehicles.ArmoredCarMod;
import com.exodusvehicles.entity.TruckEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * Daimler-Marienfelde DR lorry - GENERATED from truck/truck_geom.py + build_truck.py. Do not edit the geometry by hand.
 * 1 texel per model pixel, box UV, 512x512 texture. Spoked wheel discs are built at 2x size and drawn at 0.5x.
 */
public class TruckModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ArmoredCarMod.id("daimler_dr"), "main");

    private final ModelPart root;
    private final ModelPart tarp;
    private final ModelPart steerL, steerR;
    private final ModelPart[] spin;

    public TruckModel(ModelPart root) {
        this.root = root;
        this.tarp = root.getChild("tarp");
        this.steerL = root.getChild("wheel0l_steer");
        this.steerR = root.getChild("wheel0r_steer");
        this.spin = new ModelPart[]{
                this.steerL.getChild("wheel0l"), this.steerR.getChild("wheel0r"),
                root.getChild("wheel1l"), root.getChild("wheel1r")};
        String[] names = {"wheel0l", "wheel0r", "wheel1l", "wheel1r"};
        for (int i = 0; i < this.spin.length; i++) {
            for (String suffix : new String[]{"_spk", "_spk2"}) {
                if (this.spin[i].hasChild(names[i] + suffix)) {
                    ModelPart disc = this.spin[i].getChild(names[i] + suffix);
                    disc.xScale = 0.5F;
                    disc.yScale = 0.5F;
                    disc.zScale = 0.5F;
                }
            }
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        build_body(root);
        build_tarp(root);
        build_fender_al(root);
        build_fender_bl(root);
        build_fender_ar(root);
        build_fender_br(root);
        build_wheel0l_steer(root);
        build_wheel1l(root);
        build_wheel0r_steer(root);
        build_wheel1r(root);
        return LayerDefinition.create(mesh, 512, 512);
    }

    private static void build_body(PartDefinition root) {
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(6F, -16.5F, -48F, 2F, 4F, 98F)
                .texOffs(0, 0).addBox(-8F, -16.5F, -48F, 2F, 4F, 98F)
                .texOffs(316, 276).addBox(-6F, -15.5F, 44F, 12F, 2.5F, 2F)
                .texOffs(316, 276).addBox(-6F, -15.5F, 20F, 12F, 2.5F, 2F)
                .texOffs(316, 276).addBox(-6F, -15.5F, -6F, 12F, 2.5F, 2F)
                .texOffs(316, 276).addBox(-6F, -15.5F, -28F, 12F, 2.5F, 2F)
                .texOffs(316, 276).addBox(-6F, -15.5F, -48F, 12F, 2.5F, 2F)
                .texOffs(40, 228).addBox(-7F, -27F, -50F, 14F, 10.5F, 19.5F)
                .texOffs(286, 228).addBox(-5.5F, -28.2F, -50F, 11F, 1.2F, 19.5F)
                .texOffs(446, 228).addBox(-8F, -29.5F, -52.5F, 16F, 16F, 2.5F)
                .texOffs(440, 276).addBox(-1F, -31F, -51.9F, 2F, 1.5F, 1.3F)
                .texOffs(28, 276).addBox(-8.5F, -12.5F, -53.5F, 17F, 1.5F, 4.5F)
                .texOffs(362, 276).addBox(-0.6F, -16.2F, -55F, 1.2F, 1.2F, 2.5F)
                .texOffs(274, 276).addBox(9F, -25F, -51.5F, 2.6F, 3F, 3F)
                .texOffs(58, 284).addBox(8F, -24F, -50.5F, 1.2F, 1F, 1F)
                .texOffs(274, 276).addBox(-11.6F, -25F, -51.5F, 2.6F, 3F, 3F)
                .texOffs(58, 284).addBox(-9.2F, -24F, -50.5F, 1.2F, 1F, 1F)
                .texOffs(348, 228).addBox(-15F, -16F, -30F, 30F, 1F, 19F)
                .texOffs(0, 260).addBox(-15F, -30F, -30F, 30F, 14F, 1.5F)
                .texOffs(146, 276).addBox(-15F, -30.2F, -31.5F, 30F, 3.2F, 1.5F)
                .texOffs(448, 276).addBox(-13.6F, -31F, -31.5F, 27.2F, 0.8F, 1.5F)
                .texOffs(0, 228).addBox(13.8F, -30F, -28.5F, 1.2F, 14F, 17.5F)
                .texOffs(206, 260).addBox(13.6F, -42F, -31.4F, 1.6F, 12F, 1.8F)
                .texOffs(206, 260).addBox(13.6F, -42F, -20.7F, 1.6F, 12F, 1.4F)
                .texOffs(206, 260).addBox(13.6F, -42F, -11.4F, 1.6F, 12F, 1.6F)
                .texOffs(372, 276).addBox(15.2F, -33.2F, -31.6F, 1.6F, 2.2F, 2F)
                .texOffs(0, 228).addBox(-15F, -30F, -28.5F, 1.2F, 14F, 17.5F)
                .texOffs(206, 260).addBox(-15.2F, -42F, -31.4F, 1.6F, 12F, 1.8F)
                .texOffs(206, 260).addBox(-15.2F, -42F, -20.7F, 1.6F, 12F, 1.4F)
                .texOffs(206, 260).addBox(-15.2F, -42F, -11.4F, 1.6F, 12F, 1.6F)
                .texOffs(372, 276).addBox(-16.8F, -33.2F, -31.6F, 1.6F, 2.2F, 2F)
                .texOffs(108, 228).addBox(-15F, -42F, -11F, 30F, 27F, 1.2F)
                .texOffs(380, 276).addBox(-13.6F, -42F, -31.4F, 27.2F, 1.4F, 1.8F)
                .texOffs(0, 284).addBox(-13.6F, -36.3F, -30.9F, 27.2F, 0.7F, 0.8F)
                .texOffs(172, 228).addBox(-16F, -43.6F, -33.5F, 32F, 1.6F, 24.9F)
                .texOffs(370, 260).addBox(-13.5F, -22F, -19F, 27F, 2F, 7F)
                .texOffs(438, 260).addBox(-13.5F, -29F, -13F, 27F, 7F, 2F)
                .texOffs(334, 260).addBox(4.5F, -30.5F, -25.5F, 1F, 9.5F, 1F)
                .texOffs(72, 276).addBox(2F, -30.8F, -28F, 6F, 0.6F, 6F)
                .texOffs(496, 260).addBox(-2F, -24F, -23F, 1F, 8F, 1F)
                .texOffs(236, 166).addBox(8F, -11F, -28F, 8.5F, 1F, 49F)
                .texOffs(236, 166).addBox(-16.5F, -11F, -28F, 8.5F, 1F, 49F)
                .texOffs(64, 260).addBox(8F, -15.5F, 8F, 7.5F, 5F, 11F)
                .texOffs(102, 260).addBox(-15.5F, -15.5F, 8F, 7.5F, 4.5F, 11F)
                .texOffs(406, 166).addBox(-10.2F, -10.5F, -30F, 1F, 1F, 46F)
                .texOffs(114, 276).addBox(-10.8F, -11F, 16F, 2.2F, 2F, 5F)
                .texOffs(316, 260).addBox(7.5F, -11F, 21F, 2.5F, 4.5F, 6F)
                .texOffs(316, 260).addBox(-10F, -11F, 21F, 2.5F, 4.5F, 6F)
                .texOffs(192, 102).addBox(-16F, -19F, -8.6F, 32F, 2.5F, 60.6F)
                .texOffs(328, 0).addBox(15F, -28F, -8.6F, 1.2F, 9F, 60.6F)
                .texOffs(328, 0).addBox(-16.2F, -28F, -8.6F, 1.2F, 9F, 60.6F)
                .texOffs(252, 260).addBox(-15F, -28F, -8.6F, 30F, 9F, 1.2F)
                .texOffs(252, 260).addBox(-15F, -28F, 50.8F, 30F, 9F, 1.2F)
                .texOffs(210, 276).addBox(-14F, -24F, 10F, 28F, 1.5F, 4F)
                .texOffs(308, 276).addBox(11F, -22.5F, 11F, 2F, 3.5F, 2F)
                .texOffs(308, 276).addBox(-13F, -22.5F, 11F, 2F, 3.5F, 2F)
                .texOffs(210, 276).addBox(-14F, -24F, 30F, 28F, 1.5F, 4F)
                .texOffs(308, 276).addBox(11F, -22.5F, 31F, 2F, 3.5F, 2F)
                .texOffs(308, 276).addBox(-13F, -22.5F, 31F, 2F, 3.5F, 2F)
                .texOffs(214, 260).addBox(9.2F, -17.4F, -45F, 6.4F, 0.8F, 11.5F)
                .texOffs(214, 260).addBox(-15.6F, -17.4F, -45F, 6.4F, 0.8F, 11.5F)
                , PartPose.offsetAndRotation(0F, 24F, 0F, 0F, 0F, 0F));
    }

    private static void build_tarp(PartDefinition root) {
        root.addOrReplaceChild("tarp", CubeListBuilder.create()
                .texOffs(200, 0).addBox(15.2F, -40.8F, -8.8F, 1.4F, 12.8F, 61.4F)
                .texOffs(200, 0).addBox(-16.6F, -40.8F, -8.8F, 1.4F, 12.8F, 61.4F)
                .texOffs(140, 260).addBox(-15.2F, -40.8F, -8.8F, 30.4F, 12.8F, 1.2F)
                .texOffs(0, 102).addBox(-16.6F, -42.4F, -8.8F, 33.2F, 1.6F, 61.4F)
                .texOffs(0, 166).addBox(-14F, -43.4F, -8.6F, 28F, 1F, 61F)
                .texOffs(344, 276).addBox(9F, -40.8F, 51F, 6.2F, 2.2F, 1.6F)
                .texOffs(344, 276).addBox(-15.2F, -40.8F, 51F, 6.2F, 2.2F, 1.6F)
                , PartPose.offsetAndRotation(0F, 24F, 0F, 0F, 0F, 0F));
    }

    private static void build_fender_al(PartDefinition root) {
        root.addOrReplaceChild("fender_al", CubeListBuilder.create()
                .texOffs(0, 276).addBox(9.2F, -0.4F, -6.1033F, 6.4F, 0.8F, 6.1033F)
                , PartPose.offsetAndRotation(0F, 12F, -48.5F, -2.1815F, 0F, 0F));
    }

    private static void build_fender_bl(PartDefinition root) {
        root.addOrReplaceChild("fender_bl", CubeListBuilder.create()
                .texOffs(338, 260).addBox(9.2F, -0.4F, -8.7727F, 6.4F, 0.8F, 8.7727F)
                , PartPose.offsetAndRotation(0F, 7F, -33.5F, 2.3239F, 0F, 0F));
    }

    private static void build_fender_ar(PartDefinition root) {
        root.addOrReplaceChild("fender_ar", CubeListBuilder.create()
                .texOffs(0, 276).addBox(-15.6F, -0.4F, -6.1033F, 6.4F, 0.8F, 6.1033F)
                , PartPose.offsetAndRotation(0F, 12F, -48.5F, -2.1815F, 0F, 0F));
    }

    private static void build_fender_br(PartDefinition root) {
        root.addOrReplaceChild("fender_br", CubeListBuilder.create()
                .texOffs(338, 260).addBox(-15.6F, -0.4F, -8.7727F, 6.4F, 0.8F, 8.7727F)
                , PartPose.offsetAndRotation(0F, 7F, -33.5F, 2.3239F, 0F, 0F));
    }

    private static void build_wheel0l_steer(PartDefinition root) {
        PartDefinition p7 = root.addOrReplaceChild("wheel0l_steer", CubeListBuilder.create()
                , PartPose.offsetAndRotation(12F, 16.5F, -40F, 0F, 0F, 0F));
        PartDefinition p8 = p7.addOrReplaceChild("wheel0l", CubeListBuilder.create()
                .texOffs(298, 276).addBox(0.3F, -1.5F, -1.5F, 1.95F, 3F, 3F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p8.addOrReplaceChild("wheel0l_t0", CubeListBuilder.create()
                .texOffs(130, 276).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p8.addOrReplaceChild("wheel0l_t1", CubeListBuilder.create()
                .texOffs(130, 276).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0.5236F, 0F, 0F));
        p8.addOrReplaceChild("wheel0l_t2", CubeListBuilder.create()
                .texOffs(130, 276).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 1.0472F, 0F, 0F));
        p8.addOrReplaceChild("wheel0l_t3", CubeListBuilder.create()
                .texOffs(130, 276).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 1.5708F, 0F, 0F));
        p8.addOrReplaceChild("wheel0l_t4", CubeListBuilder.create()
                .texOffs(130, 276).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 2.0944F, 0F, 0F));
        p8.addOrReplaceChild("wheel0l_t5", CubeListBuilder.create()
                .texOffs(130, 276).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 2.618F, 0F, 0F));
        p8.addOrReplaceChild("wheel0l_t6", CubeListBuilder.create()
                .texOffs(130, 276).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 3.1416F, 0F, 0F));
        p8.addOrReplaceChild("wheel0l_t7", CubeListBuilder.create()
                .texOffs(130, 276).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 3.6652F, 0F, 0F));
        p8.addOrReplaceChild("wheel0l_t8", CubeListBuilder.create()
                .texOffs(130, 276).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 4.1888F, 0F, 0F));
        p8.addOrReplaceChild("wheel0l_t9", CubeListBuilder.create()
                .texOffs(130, 276).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 4.7124F, 0F, 0F));
        p8.addOrReplaceChild("wheel0l_t10", CubeListBuilder.create()
                .texOffs(130, 276).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 5.236F, 0F, 0F));
        p8.addOrReplaceChild("wheel0l_t11", CubeListBuilder.create()
                .texOffs(130, 276).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 5.7596F, 0F, 0F));
        p8.addOrReplaceChild("wheel0l_spk", CubeListBuilder.create()
                .texOffs(352, 166).addBox(-0.6F, -12.2F, -12.2F, 1.2F, 24.4F, 24.4F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
    }

    private static void build_wheel1l(PartDefinition root) {
        PartDefinition p22 = root.addOrReplaceChild("wheel1l", CubeListBuilder.create()
                .texOffs(286, 276).addBox(0.3F, -1.5F, -1.5F, 2.5F, 3F, 3F)
                , PartPose.offsetAndRotation(12.5F, 16F, 30F, 0F, 0F, 0F));
        p22.addOrReplaceChild("wheel1l_t0", CubeListBuilder.create()
                .texOffs(96, 276).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p22.addOrReplaceChild("wheel1l_t1", CubeListBuilder.create()
                .texOffs(96, 276).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0.5236F, 0F, 0F));
        p22.addOrReplaceChild("wheel1l_t2", CubeListBuilder.create()
                .texOffs(96, 276).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 1.0472F, 0F, 0F));
        p22.addOrReplaceChild("wheel1l_t3", CubeListBuilder.create()
                .texOffs(96, 276).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 1.5708F, 0F, 0F));
        p22.addOrReplaceChild("wheel1l_t4", CubeListBuilder.create()
                .texOffs(96, 276).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 2.0944F, 0F, 0F));
        p22.addOrReplaceChild("wheel1l_t5", CubeListBuilder.create()
                .texOffs(96, 276).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 2.618F, 0F, 0F));
        p22.addOrReplaceChild("wheel1l_t6", CubeListBuilder.create()
                .texOffs(96, 276).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 3.1416F, 0F, 0F));
        p22.addOrReplaceChild("wheel1l_t7", CubeListBuilder.create()
                .texOffs(96, 276).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 3.6652F, 0F, 0F));
        p22.addOrReplaceChild("wheel1l_t8", CubeListBuilder.create()
                .texOffs(96, 276).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 4.1888F, 0F, 0F));
        p22.addOrReplaceChild("wheel1l_t9", CubeListBuilder.create()
                .texOffs(96, 276).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 4.7124F, 0F, 0F));
        p22.addOrReplaceChild("wheel1l_t10", CubeListBuilder.create()
                .texOffs(96, 276).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 5.236F, 0F, 0F));
        p22.addOrReplaceChild("wheel1l_t11", CubeListBuilder.create()
                .texOffs(96, 276).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 5.7596F, 0F, 0F));
        p22.addOrReplaceChild("wheel1l_spk", CubeListBuilder.create()
                .texOffs(178, 166).addBox(-0.6F, -13.2F, -13.2F, 1.2F, 26.4F, 26.4F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p22.addOrReplaceChild("wheel1l_spk2", CubeListBuilder.create()
                .texOffs(178, 166).addBox(-0.6F, -13.2F, -13.2F, 1.2F, 26.4F, 26.4F)
                , PartPose.offsetAndRotation(-1.2F, 0F, 0F, 0F, 0F, 0F));
    }

    private static void build_wheel0r_steer(PartDefinition root) {
        PartDefinition p37 = root.addOrReplaceChild("wheel0r_steer", CubeListBuilder.create()
                , PartPose.offsetAndRotation(-12F, 16.5F, -40F, 0F, 0F, 0F));
        PartDefinition p38 = p37.addOrReplaceChild("wheel0r", CubeListBuilder.create()
                .texOffs(298, 276).addBox(-2.25F, -1.5F, -1.5F, 1.95F, 3F, 3F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p38.addOrReplaceChild("wheel0r_t0", CubeListBuilder.create()
                .texOffs(130, 276).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p38.addOrReplaceChild("wheel0r_t1", CubeListBuilder.create()
                .texOffs(130, 276).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0.5236F, 0F, 0F));
        p38.addOrReplaceChild("wheel0r_t2", CubeListBuilder.create()
                .texOffs(130, 276).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 1.0472F, 0F, 0F));
        p38.addOrReplaceChild("wheel0r_t3", CubeListBuilder.create()
                .texOffs(130, 276).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 1.5708F, 0F, 0F));
        p38.addOrReplaceChild("wheel0r_t4", CubeListBuilder.create()
                .texOffs(130, 276).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 2.0944F, 0F, 0F));
        p38.addOrReplaceChild("wheel0r_t5", CubeListBuilder.create()
                .texOffs(130, 276).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 2.618F, 0F, 0F));
        p38.addOrReplaceChild("wheel0r_t6", CubeListBuilder.create()
                .texOffs(130, 276).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 3.1416F, 0F, 0F));
        p38.addOrReplaceChild("wheel0r_t7", CubeListBuilder.create()
                .texOffs(130, 276).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 3.6652F, 0F, 0F));
        p38.addOrReplaceChild("wheel0r_t8", CubeListBuilder.create()
                .texOffs(130, 276).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 4.1888F, 0F, 0F));
        p38.addOrReplaceChild("wheel0r_t9", CubeListBuilder.create()
                .texOffs(130, 276).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 4.7124F, 0F, 0F));
        p38.addOrReplaceChild("wheel0r_t10", CubeListBuilder.create()
                .texOffs(130, 276).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 5.236F, 0F, 0F));
        p38.addOrReplaceChild("wheel0r_t11", CubeListBuilder.create()
                .texOffs(130, 276).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 5.7596F, 0F, 0F));
        p38.addOrReplaceChild("wheel0r_spk", CubeListBuilder.create()
                .texOffs(352, 166).addBox(-0.6F, -12.2F, -12.2F, 1.2F, 24.4F, 24.4F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
    }

    private static void build_wheel1r(PartDefinition root) {
        PartDefinition p52 = root.addOrReplaceChild("wheel1r", CubeListBuilder.create()
                .texOffs(286, 276).addBox(-2.8F, -1.5F, -1.5F, 2.5F, 3F, 3F)
                , PartPose.offsetAndRotation(-12.5F, 16F, 30F, 0F, 0F, 0F));
        p52.addOrReplaceChild("wheel1r_t0", CubeListBuilder.create()
                .texOffs(96, 276).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p52.addOrReplaceChild("wheel1r_t1", CubeListBuilder.create()
                .texOffs(96, 276).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0.5236F, 0F, 0F));
        p52.addOrReplaceChild("wheel1r_t2", CubeListBuilder.create()
                .texOffs(96, 276).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 1.0472F, 0F, 0F));
        p52.addOrReplaceChild("wheel1r_t3", CubeListBuilder.create()
                .texOffs(96, 276).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 1.5708F, 0F, 0F));
        p52.addOrReplaceChild("wheel1r_t4", CubeListBuilder.create()
                .texOffs(96, 276).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 2.0944F, 0F, 0F));
        p52.addOrReplaceChild("wheel1r_t5", CubeListBuilder.create()
                .texOffs(96, 276).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 2.618F, 0F, 0F));
        p52.addOrReplaceChild("wheel1r_t6", CubeListBuilder.create()
                .texOffs(96, 276).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 3.1416F, 0F, 0F));
        p52.addOrReplaceChild("wheel1r_t7", CubeListBuilder.create()
                .texOffs(96, 276).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 3.6652F, 0F, 0F));
        p52.addOrReplaceChild("wheel1r_t8", CubeListBuilder.create()
                .texOffs(96, 276).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 4.1888F, 0F, 0F));
        p52.addOrReplaceChild("wheel1r_t9", CubeListBuilder.create()
                .texOffs(96, 276).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 4.7124F, 0F, 0F));
        p52.addOrReplaceChild("wheel1r_t10", CubeListBuilder.create()
                .texOffs(96, 276).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 5.236F, 0F, 0F));
        p52.addOrReplaceChild("wheel1r_t11", CubeListBuilder.create()
                .texOffs(96, 276).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 5.7596F, 0F, 0F));
        p52.addOrReplaceChild("wheel1r_spk", CubeListBuilder.create()
                .texOffs(178, 166).addBox(-0.6F, -13.2F, -13.2F, 1.2F, 26.4F, 26.4F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p52.addOrReplaceChild("wheel1r_spk2", CubeListBuilder.create()
                .texOffs(178, 166).addBox(-0.6F, -13.2F, -13.2F, 1.2F, 26.4F, 26.4F)
                , PartPose.offsetAndRotation(1.2F, 0F, 0F, 0F, 0F, 0F));
    }


    public void setupAnim(TruckEntity truck, float partialTick) {
        float wheel = Mth.lerp(partialTick, truck.wheelRotO, truck.wheelRot);
        for (ModelPart w : this.spin) {
            w.xRot = wheel;
        }
        float steer = -Mth.DEG_TO_RAD * Mth.lerp(partialTick, truck.steerVisO, truck.steerVis);
        this.steerL.yRot = steer;
        this.steerR.yRot = steer;
    }

    /** hideTarp: for the men riding under the canvas in first person, and once the canvas has burnt away. */
    public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay, boolean hideTarp) {
        this.tarp.visible = !hideTarp;
        this.root.render(poseStack, consumer, light, overlay);
        this.tarp.visible = true;
    }
}
