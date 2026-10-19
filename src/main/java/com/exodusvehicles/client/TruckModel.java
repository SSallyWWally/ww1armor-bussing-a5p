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
    private final ModelPart[] crates = new ModelPart[TruckEntity.MAX_CRATES];
    private final ModelPart steerL, steerR;
    private final ModelPart[] spin;

    public TruckModel(ModelPart root) {
        this.root = root;
        this.tarp = root.getChild("tarp");
        for (int i = 0; i < this.crates.length; i++) {
            this.crates[i] = root.getChild("crate" + i);
        }
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
        build_crate0(root);
        build_crate1(root);
        build_crate2(root);
        build_crate3(root);
        build_crate4(root);
        build_crate5(root);
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
                .texOffs(468, 298).addBox(-6F, -15.5F, 44F, 12F, 2.5F, 2F)
                .texOffs(468, 298).addBox(-6F, -15.5F, 20F, 12F, 2.5F, 2F)
                .texOffs(468, 298).addBox(-6F, -15.5F, -6F, 12F, 2.5F, 2F)
                .texOffs(468, 298).addBox(-6F, -15.5F, -28F, 12F, 2.5F, 2F)
                .texOffs(468, 298).addBox(-6F, -15.5F, -48F, 12F, 2.5F, 2F)
                .texOffs(188, 228).addBox(-7F, -27F, -50F, 14F, 10.5F, 19.5F)
                .texOffs(434, 228).addBox(-5.5F, -28.2F, -50F, 11F, 1.2F, 19.5F)
                .texOffs(144, 278).addBox(-8F, -29.5F, -52.5F, 16F, 16F, 2.5F)
                .texOffs(104, 309).addBox(-1F, -31F, -51.9F, 2F, 1.5F, 1.3F)
                .texOffs(252, 298).addBox(-8.5F, -12.5F, -53.5F, 17F, 1.5F, 4.5F)
                .texOffs(18, 309).addBox(-0.6F, -16.2F, -55F, 1.2F, 1.2F, 2.5F)
                .texOffs(434, 298).addBox(9F, -25F, -51.5F, 2.6F, 3F, 3F)
                .texOffs(230, 309).addBox(8F, -24F, -50.5F, 1.2F, 1F, 1F)
                .texOffs(434, 298).addBox(-11.6F, -25F, -51.5F, 2.6F, 3F, 3F)
                .texOffs(230, 309).addBox(-9.2F, -24F, -50.5F, 1.2F, 1F, 1F)
                .texOffs(0, 278).addBox(-15F, -16F, -30F, 30F, 1F, 19F)
                .texOffs(182, 278).addBox(-15F, -30F, -30F, 30F, 14F, 1.5F)
                .texOffs(370, 298).addBox(-15F, -30.2F, -31.5F, 30F, 3.2F, 1.5F)
                .texOffs(112, 309).addBox(-13.6F, -31F, -31.5F, 27.2F, 0.8F, 1.5F)
                .texOffs(148, 228).addBox(13.8F, -30F, -28.5F, 1.2F, 14F, 17.5F)
                .texOffs(388, 278).addBox(13.6F, -42F, -31.4F, 1.6F, 12F, 1.8F)
                .texOffs(388, 278).addBox(13.6F, -42F, -20.7F, 1.6F, 12F, 1.4F)
                .texOffs(388, 278).addBox(13.6F, -42F, -11.4F, 1.6F, 12F, 1.6F)
                .texOffs(28, 309).addBox(15.2F, -33.2F, -31.6F, 1.6F, 2.2F, 2F)
                .texOffs(148, 228).addBox(-15F, -30F, -28.5F, 1.2F, 14F, 17.5F)
                .texOffs(388, 278).addBox(-15.2F, -42F, -31.4F, 1.6F, 12F, 1.8F)
                .texOffs(388, 278).addBox(-15.2F, -42F, -20.7F, 1.6F, 12F, 1.4F)
                .texOffs(388, 278).addBox(-15.2F, -42F, -11.4F, 1.6F, 12F, 1.6F)
                .texOffs(28, 309).addBox(-16.8F, -33.2F, -31.6F, 1.6F, 2.2F, 2F)
                .texOffs(256, 228).addBox(-15F, -42F, -11F, 30F, 27F, 1.2F)
                .texOffs(44, 309).addBox(-13.6F, -42F, -31.4F, 27.2F, 1.4F, 1.8F)
                .texOffs(172, 309).addBox(-13.6F, -36.3F, -30.9F, 27.2F, 0.7F, 0.8F)
                .texOffs(320, 228).addBox(-16F, -43.6F, -33.5F, 32F, 1.6F, 24.9F)
                .texOffs(54, 298).addBox(-13.5F, -22F, -19F, 27F, 2F, 7F)
                .texOffs(122, 298).addBox(-13.5F, -29F, -13F, 27F, 7F, 2F)
                .texOffs(18, 298).addBox(4.5F, -30.5F, -25.5F, 1F, 9.5F, 1F)
                .texOffs(296, 298).addBox(2F, -30.8F, -28F, 6F, 0.6F, 6F)
                .texOffs(220, 298).addBox(-2F, -24F, -23F, 1F, 8F, 1F)
                .texOffs(358, 166).addBox(8F, -11F, -28F, 8.5F, 1F, 49F)
                .texOffs(358, 166).addBox(-16.5F, -11F, -28F, 8.5F, 1F, 49F)
                .texOffs(246, 278).addBox(8F, -15.5F, 8F, 7.5F, 5F, 11F)
                .texOffs(284, 278).addBox(-15.5F, -15.5F, 8F, 7.5F, 4.5F, 11F)
                .texOffs(54, 228).addBox(-10.2F, -10.5F, -30F, 1F, 1F, 46F)
                .texOffs(338, 298).addBox(-10.8F, -11F, 16F, 2.2F, 2F, 5F)
                .texOffs(0, 298).addBox(7.5F, -11F, 21F, 2.5F, 4.5F, 6F)
                .texOffs(0, 298).addBox(-10F, -11F, 21F, 2.5F, 4.5F, 6F)
                .texOffs(192, 102).addBox(-16F, -19F, -8.6F, 32F, 2.5F, 60.6F)
                .texOffs(328, 0).addBox(15F, -28F, -8.6F, 1.2F, 9F, 60.6F)
                .texOffs(328, 0).addBox(-16.2F, -28F, -8.6F, 1.2F, 9F, 60.6F)
                .texOffs(434, 278).addBox(-15F, -28F, -8.6F, 30F, 9F, 1.2F)
                .texOffs(434, 278).addBox(-15F, -28F, 50.8F, 30F, 9F, 1.2F)
                .texOffs(178, 166).addBox(11.6F, -23F, -6.5F, 3.2F, 1.2F, 56.5F)
                .texOffs(36, 309).addBox(12.4F, -21.8F, 46.4F, 1.6F, 2.8F, 1.6F)
                .texOffs(36, 309).addBox(12.4F, -21.8F, 28.4F, 1.6F, 2.8F, 1.6F)
                .texOffs(36, 309).addBox(12.4F, -21.8F, 10.4F, 1.6F, 2.8F, 1.6F)
                .texOffs(36, 309).addBox(12.4F, -21.8F, -5.6F, 1.6F, 2.8F, 1.6F)
                .texOffs(178, 166).addBox(-14.8F, -23F, -6.5F, 3.2F, 1.2F, 56.5F)
                .texOffs(36, 309).addBox(-14F, -21.8F, 46.4F, 1.6F, 2.8F, 1.6F)
                .texOffs(36, 309).addBox(-14F, -21.8F, 28.4F, 1.6F, 2.8F, 1.6F)
                .texOffs(36, 309).addBox(-14F, -21.8F, 10.4F, 1.6F, 2.8F, 1.6F)
                .texOffs(36, 309).addBox(-14F, -21.8F, -5.6F, 1.6F, 2.8F, 1.6F)
                .texOffs(396, 278).addBox(9.2F, -17.4F, -45F, 6.4F, 0.8F, 11.5F)
                .texOffs(396, 278).addBox(-15.6F, -17.4F, -45F, 6.4F, 0.8F, 11.5F)
                , PartPose.offsetAndRotation(0F, 24F, 0F, 0F, 0F, 0F));
    }

    private static void build_tarp(PartDefinition root) {
        root.addOrReplaceChild("tarp", CubeListBuilder.create()
                .texOffs(200, 0).addBox(15.2F, -40.8F, -8.8F, 1.4F, 12.8F, 61.4F)
                .texOffs(200, 0).addBox(-16.6F, -40.8F, -8.8F, 1.4F, 12.8F, 61.4F)
                .texOffs(322, 278).addBox(-15.2F, -40.8F, -8.8F, 30.4F, 12.8F, 1.2F)
                .texOffs(0, 102).addBox(-16.6F, -42.4F, -8.8F, 33.2F, 1.6F, 61.4F)
                .texOffs(0, 166).addBox(-14F, -43.4F, -8.6F, 28F, 1F, 61F)
                .texOffs(0, 309).addBox(9F, -40.8F, 51F, 6.2F, 2.2F, 1.6F)
                .texOffs(0, 309).addBox(-15.2F, -40.8F, 51F, 6.2F, 2.2F, 1.6F)
                , PartPose.offsetAndRotation(0F, 24F, 0F, 0F, 0F, 0F));
    }

    private static void build_crate0(PartDefinition root) {
        root.addOrReplaceChild("crate0", CubeListBuilder.create()
                .texOffs(98, 278).addBox(0.6F, -26F, -3.5F, 10.7F, 7F, 12F)
                .texOffs(180, 298).addBox(0.3F, -22.4F, -1.5F, 11.3F, 0.8F, 8F)
                , PartPose.offsetAndRotation(0F, 24F, 0F, 0F, 0F, 0F));
    }

    private static void build_crate1(PartDefinition root) {
        root.addOrReplaceChild("crate1", CubeListBuilder.create()
                .texOffs(98, 278).addBox(-11.3F, -26F, -3.5F, 10.7F, 7F, 12F)
                .texOffs(180, 298).addBox(-11.6F, -22.4F, -1.5F, 11.3F, 0.8F, 8F)
                , PartPose.offsetAndRotation(0F, 24F, 0F, 0F, 0F, 0F));
    }

    private static void build_crate2(PartDefinition root) {
        root.addOrReplaceChild("crate2", CubeListBuilder.create()
                .texOffs(98, 278).addBox(0.6F, -26F, 38F, 10.7F, 7F, 12F)
                .texOffs(180, 298).addBox(0.3F, -22.4F, 40F, 11.3F, 0.8F, 8F)
                , PartPose.offsetAndRotation(0F, 24F, 0F, 0F, 0F, 0F));
    }

    private static void build_crate3(PartDefinition root) {
        root.addOrReplaceChild("crate3", CubeListBuilder.create()
                .texOffs(98, 278).addBox(-11.3F, -26F, 38F, 10.7F, 7F, 12F)
                .texOffs(180, 298).addBox(-11.6F, -22.4F, 40F, 11.3F, 0.8F, 8F)
                , PartPose.offsetAndRotation(0F, 24F, 0F, 0F, 0F, 0F));
    }

    private static void build_crate4(PartDefinition root) {
        root.addOrReplaceChild("crate4", CubeListBuilder.create()
                .texOffs(98, 278).addBox(0.6F, -33.2F, 38F, 10.7F, 7F, 12F)
                .texOffs(180, 298).addBox(0.3F, -29.6F, 40F, 11.3F, 0.8F, 8F)
                , PartPose.offsetAndRotation(0F, 24F, 0F, 0F, 0F, 0F));
    }

    private static void build_crate5(PartDefinition root) {
        root.addOrReplaceChild("crate5", CubeListBuilder.create()
                .texOffs(98, 278).addBox(-11.3F, -33.2F, 38F, 10.7F, 7F, 12F)
                .texOffs(180, 298).addBox(-11.6F, -29.6F, 40F, 11.3F, 0.8F, 8F)
                , PartPose.offsetAndRotation(0F, 24F, 0F, 0F, 0F, 0F));
    }

    private static void build_fender_al(PartDefinition root) {
        root.addOrReplaceChild("fender_al", CubeListBuilder.create()
                .texOffs(224, 298).addBox(9.2F, -0.4F, -6.1033F, 6.4F, 0.8F, 6.1033F)
                , PartPose.offsetAndRotation(0F, 12F, -48.5F, -2.1815F, 0F, 0F));
    }

    private static void build_fender_bl(PartDefinition root) {
        root.addOrReplaceChild("fender_bl", CubeListBuilder.create()
                .texOffs(22, 298).addBox(9.2F, -0.4F, -8.7727F, 6.4F, 0.8F, 8.7727F)
                , PartPose.offsetAndRotation(0F, 7F, -33.5F, 2.3239F, 0F, 0F));
    }

    private static void build_fender_ar(PartDefinition root) {
        root.addOrReplaceChild("fender_ar", CubeListBuilder.create()
                .texOffs(224, 298).addBox(-15.6F, -0.4F, -6.1033F, 6.4F, 0.8F, 6.1033F)
                , PartPose.offsetAndRotation(0F, 12F, -48.5F, -2.1815F, 0F, 0F));
    }

    private static void build_fender_br(PartDefinition root) {
        root.addOrReplaceChild("fender_br", CubeListBuilder.create()
                .texOffs(22, 298).addBox(-15.6F, -0.4F, -8.7727F, 6.4F, 0.8F, 8.7727F)
                , PartPose.offsetAndRotation(0F, 7F, -33.5F, 2.3239F, 0F, 0F));
    }

    private static void build_wheel0l_steer(PartDefinition root) {
        PartDefinition p13 = root.addOrReplaceChild("wheel0l_steer", CubeListBuilder.create()
                , PartPose.offsetAndRotation(12F, 16.5F, -40F, 0F, 0F, 0F));
        PartDefinition p14 = p13.addOrReplaceChild("wheel0l", CubeListBuilder.create()
                .texOffs(458, 298).addBox(0.3F, -1.5F, -1.5F, 1.95F, 3F, 3F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p14.addOrReplaceChild("wheel0l_t0", CubeListBuilder.create()
                .texOffs(354, 298).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p14.addOrReplaceChild("wheel0l_t1", CubeListBuilder.create()
                .texOffs(354, 298).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0.5236F, 0F, 0F));
        p14.addOrReplaceChild("wheel0l_t2", CubeListBuilder.create()
                .texOffs(354, 298).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 1.0472F, 0F, 0F));
        p14.addOrReplaceChild("wheel0l_t3", CubeListBuilder.create()
                .texOffs(354, 298).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 1.5708F, 0F, 0F));
        p14.addOrReplaceChild("wheel0l_t4", CubeListBuilder.create()
                .texOffs(354, 298).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 2.0944F, 0F, 0F));
        p14.addOrReplaceChild("wheel0l_t5", CubeListBuilder.create()
                .texOffs(354, 298).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 2.618F, 0F, 0F));
        p14.addOrReplaceChild("wheel0l_t6", CubeListBuilder.create()
                .texOffs(354, 298).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 3.1416F, 0F, 0F));
        p14.addOrReplaceChild("wheel0l_t7", CubeListBuilder.create()
                .texOffs(354, 298).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 3.6652F, 0F, 0F));
        p14.addOrReplaceChild("wheel0l_t8", CubeListBuilder.create()
                .texOffs(354, 298).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 4.1888F, 0F, 0F));
        p14.addOrReplaceChild("wheel0l_t9", CubeListBuilder.create()
                .texOffs(354, 298).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 4.7124F, 0F, 0F));
        p14.addOrReplaceChild("wheel0l_t10", CubeListBuilder.create()
                .texOffs(354, 298).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 5.236F, 0F, 0F));
        p14.addOrReplaceChild("wheel0l_t11", CubeListBuilder.create()
                .texOffs(354, 298).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 5.7596F, 0F, 0F));
        p14.addOrReplaceChild("wheel0l_spk", CubeListBuilder.create()
                .texOffs(0, 228).addBox(-0.6F, -12.2F, -12.2F, 1.2F, 24.4F, 24.4F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
    }

    private static void build_wheel1l(PartDefinition root) {
        PartDefinition p28 = root.addOrReplaceChild("wheel1l", CubeListBuilder.create()
                .texOffs(446, 298).addBox(0.3F, -1.5F, -1.5F, 2.5F, 3F, 3F)
                , PartPose.offsetAndRotation(12.5F, 16F, 30F, 0F, 0F, 0F));
        p28.addOrReplaceChild("wheel1l_t0", CubeListBuilder.create()
                .texOffs(320, 298).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p28.addOrReplaceChild("wheel1l_t1", CubeListBuilder.create()
                .texOffs(320, 298).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0.5236F, 0F, 0F));
        p28.addOrReplaceChild("wheel1l_t2", CubeListBuilder.create()
                .texOffs(320, 298).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 1.0472F, 0F, 0F));
        p28.addOrReplaceChild("wheel1l_t3", CubeListBuilder.create()
                .texOffs(320, 298).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 1.5708F, 0F, 0F));
        p28.addOrReplaceChild("wheel1l_t4", CubeListBuilder.create()
                .texOffs(320, 298).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 2.0944F, 0F, 0F));
        p28.addOrReplaceChild("wheel1l_t5", CubeListBuilder.create()
                .texOffs(320, 298).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 2.618F, 0F, 0F));
        p28.addOrReplaceChild("wheel1l_t6", CubeListBuilder.create()
                .texOffs(320, 298).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 3.1416F, 0F, 0F));
        p28.addOrReplaceChild("wheel1l_t7", CubeListBuilder.create()
                .texOffs(320, 298).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 3.6652F, 0F, 0F));
        p28.addOrReplaceChild("wheel1l_t8", CubeListBuilder.create()
                .texOffs(320, 298).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 4.1888F, 0F, 0F));
        p28.addOrReplaceChild("wheel1l_t9", CubeListBuilder.create()
                .texOffs(320, 298).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 4.7124F, 0F, 0F));
        p28.addOrReplaceChild("wheel1l_t10", CubeListBuilder.create()
                .texOffs(320, 298).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 5.236F, 0F, 0F));
        p28.addOrReplaceChild("wheel1l_t11", CubeListBuilder.create()
                .texOffs(320, 298).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 5.7596F, 0F, 0F));
        p28.addOrReplaceChild("wheel1l_spk", CubeListBuilder.create()
                .texOffs(300, 166).addBox(-0.6F, -13.2F, -13.2F, 1.2F, 26.4F, 26.4F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p28.addOrReplaceChild("wheel1l_spk2", CubeListBuilder.create()
                .texOffs(300, 166).addBox(-0.6F, -13.2F, -13.2F, 1.2F, 26.4F, 26.4F)
                , PartPose.offsetAndRotation(-1.2F, 0F, 0F, 0F, 0F, 0F));
    }

    private static void build_wheel0r_steer(PartDefinition root) {
        PartDefinition p43 = root.addOrReplaceChild("wheel0r_steer", CubeListBuilder.create()
                , PartPose.offsetAndRotation(-12F, 16.5F, -40F, 0F, 0F, 0F));
        PartDefinition p44 = p43.addOrReplaceChild("wheel0r", CubeListBuilder.create()
                .texOffs(458, 298).addBox(-2.25F, -1.5F, -1.5F, 1.95F, 3F, 3F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p44.addOrReplaceChild("wheel0r_t0", CubeListBuilder.create()
                .texOffs(354, 298).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p44.addOrReplaceChild("wheel0r_t1", CubeListBuilder.create()
                .texOffs(354, 298).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0.5236F, 0F, 0F));
        p44.addOrReplaceChild("wheel0r_t2", CubeListBuilder.create()
                .texOffs(354, 298).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 1.0472F, 0F, 0F));
        p44.addOrReplaceChild("wheel0r_t3", CubeListBuilder.create()
                .texOffs(354, 298).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 1.5708F, 0F, 0F));
        p44.addOrReplaceChild("wheel0r_t4", CubeListBuilder.create()
                .texOffs(354, 298).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 2.0944F, 0F, 0F));
        p44.addOrReplaceChild("wheel0r_t5", CubeListBuilder.create()
                .texOffs(354, 298).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 2.618F, 0F, 0F));
        p44.addOrReplaceChild("wheel0r_t6", CubeListBuilder.create()
                .texOffs(354, 298).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 3.1416F, 0F, 0F));
        p44.addOrReplaceChild("wheel0r_t7", CubeListBuilder.create()
                .texOffs(354, 298).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 3.6652F, 0F, 0F));
        p44.addOrReplaceChild("wheel0r_t8", CubeListBuilder.create()
                .texOffs(354, 298).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 4.1888F, 0F, 0F));
        p44.addOrReplaceChild("wheel0r_t9", CubeListBuilder.create()
                .texOffs(354, 298).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 4.7124F, 0F, 0F));
        p44.addOrReplaceChild("wheel0r_t10", CubeListBuilder.create()
                .texOffs(354, 298).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 5.236F, 0F, 0F));
        p44.addOrReplaceChild("wheel0r_t11", CubeListBuilder.create()
                .texOffs(354, 298).addBox(-1.25F, -7.5F, -2.1096F, 2.5F, 1.7F, 4.2192F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 5.7596F, 0F, 0F));
        p44.addOrReplaceChild("wheel0r_spk", CubeListBuilder.create()
                .texOffs(0, 228).addBox(-0.6F, -12.2F, -12.2F, 1.2F, 24.4F, 24.4F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
    }

    private static void build_wheel1r(PartDefinition root) {
        PartDefinition p58 = root.addOrReplaceChild("wheel1r", CubeListBuilder.create()
                .texOffs(446, 298).addBox(-2.8F, -1.5F, -1.5F, 2.5F, 3F, 3F)
                , PartPose.offsetAndRotation(-12.5F, 16F, 30F, 0F, 0F, 0F));
        p58.addOrReplaceChild("wheel1r_t0", CubeListBuilder.create()
                .texOffs(320, 298).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p58.addOrReplaceChild("wheel1r_t1", CubeListBuilder.create()
                .texOffs(320, 298).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0.5236F, 0F, 0F));
        p58.addOrReplaceChild("wheel1r_t2", CubeListBuilder.create()
                .texOffs(320, 298).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 1.0472F, 0F, 0F));
        p58.addOrReplaceChild("wheel1r_t3", CubeListBuilder.create()
                .texOffs(320, 298).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 1.5708F, 0F, 0F));
        p58.addOrReplaceChild("wheel1r_t4", CubeListBuilder.create()
                .texOffs(320, 298).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 2.0944F, 0F, 0F));
        p58.addOrReplaceChild("wheel1r_t5", CubeListBuilder.create()
                .texOffs(320, 298).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 2.618F, 0F, 0F));
        p58.addOrReplaceChild("wheel1r_t6", CubeListBuilder.create()
                .texOffs(320, 298).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 3.1416F, 0F, 0F));
        p58.addOrReplaceChild("wheel1r_t7", CubeListBuilder.create()
                .texOffs(320, 298).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 3.6652F, 0F, 0F));
        p58.addOrReplaceChild("wheel1r_t8", CubeListBuilder.create()
                .texOffs(320, 298).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 4.1888F, 0F, 0F));
        p58.addOrReplaceChild("wheel1r_t9", CubeListBuilder.create()
                .texOffs(320, 298).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 4.7124F, 0F, 0F));
        p58.addOrReplaceChild("wheel1r_t10", CubeListBuilder.create()
                .texOffs(320, 298).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 5.236F, 0F, 0F));
        p58.addOrReplaceChild("wheel1r_t11", CubeListBuilder.create()
                .texOffs(320, 298).addBox(-1.8F, -8F, -2.2436F, 3.6F, 1.7F, 4.4872F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 5.7596F, 0F, 0F));
        p58.addOrReplaceChild("wheel1r_spk", CubeListBuilder.create()
                .texOffs(300, 166).addBox(-0.6F, -13.2F, -13.2F, 1.2F, 26.4F, 26.4F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p58.addOrReplaceChild("wheel1r_spk2", CubeListBuilder.create()
                .texOffs(300, 166).addBox(-0.6F, -13.2F, -13.2F, 1.2F, 26.4F, 26.4F)
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
    public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay, boolean hideTarp, int crates) {
        this.tarp.visible = !hideTarp;
        for (int i = 0; i < this.crates.length; i++) {
            this.crates[i].visible = i < crates;
        }
        this.root.render(poseStack, consumer, light, overlay);
        this.tarp.visible = true;
    }
}
