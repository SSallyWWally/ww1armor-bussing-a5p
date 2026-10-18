package com.exodusvehicles.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.exodusvehicles.ArmoredCarMod;
import com.exodusvehicles.entity.ArmoredCarEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

/**
 * M.15 Eisenwolf model - GENERATED from gen2/a5p_s7.py + build7.py (same geometry as the web preview).
 * 1 texel per model pixel, box UV, 512x512 texture. Do not edit the geometry by hand.
 */
public class ArmoredCarModel {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ArmoredCarMod.id("eisenwolf"), "main");

    /** Root parts hidden in first person so the crew can see out (upper hull, casemate). */
    private static final String[] INTERIOR_HIDDEN = {"u_hull", "u_bevel_cl", "u_bevel_sl", "u_bevel_cr", "u_bevel_sr", "u_rear_plate", "u_rear_chamfer", "u_fittings", "u_shutter_l0a", "u_shutter_l0b", "u_shutter_l1a", "u_shutter_l1b", "u_flap_l", "u_shutter_r0a", "u_shutter_r0b", "u_shutter_r1a", "u_shutter_r1b", "u_flap_r"};

    private final ModelPart root;
    private final List<ModelPart> hidden = new ArrayList<>();
    private final List<ModelPart> allParts;
    private final ModelPart turret, gun, recoil, flash;
    private final ModelPart steerL, steerR;
    private final float gunBaseY, gunBaseZ;
    /**
     * Gunner's view (like Battlefield vehicle guns): the whole MG sits right under the eye, breech just in front of it,
     * so the water jacket runs up from the bottom of the screen and the front sight post meets the reticle.
     */
    private static final float GUNNER_GUN_Y = -0.5F;     // turret space; eye at -5: gun axis 4.5 px below the eye
    private static final float GUNNER_GUN_Z = -5.0F;
    private final ModelPart[] spin;

    public ArmoredCarModel(ModelPart root) {
        this.root = root;
        for (String n : INTERIOR_HIDDEN) {
            this.hidden.add(root.getChild(n));
        }
        this.turret = root.getChild("turret");
        this.gun = this.turret.getChild("gun");
        this.recoil = this.gun.getChild("recoil");
        this.flash = this.recoil.getChild("flash");
        this.gunBaseY = this.gun.y;
        this.gunBaseZ = this.gun.z;
        this.steerL = root.getChild("wheel0l_steer");
        this.steerR = root.getChild("wheel0r_steer");
        this.spin = new ModelPart[]{
                this.steerL.getChild("wheel0l"), this.steerR.getChild("wheel0r"),
                root.getChild("wheel1l"), root.getChild("wheel1r")};
        this.allParts = root.getAllParts().toList();
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        build_u_hull(root);
        build_e_hull(root);
        build_u_bevel_cl(root);
        build_u_bevel_sl(root);
        build_u_bevel_cr(root);
        build_u_bevel_sr(root);
        build_e_front_plate(root);
        build_u_rear_plate(root);
        build_u_rear_chamfer(root);
        build_u_fittings(root);
        build_u_shutter_l0a(root);
        build_u_shutter_l0b(root);
        build_u_shutter_l1a(root);
        build_u_shutter_l1b(root);
        build_u_flap_l(root);
        build_u_shutter_r0a(root);
        build_u_shutter_r0b(root);
        build_u_shutter_r1a(root);
        build_u_shutter_r1b(root);
        build_u_flap_r(root);
        build_e_lamp_l(root);
        build_e_lamp_r(root);
        build_turret(root);
        build_lower(root);
        build_guard0l(root);
        build_guard0r(root);
        build_guard1l(root);
        build_guard1r(root);
        build_wheel0l_steer(root);
        build_wheel0r_steer(root);
        build_wheel1l(root);
        build_wheel1r(root);
        return LayerDefinition.create(mesh, 512, 512);
    }

    private static void build_u_hull(PartDefinition root) {
        root.addOrReplaceChild("u_hull", CubeListBuilder.create()
                .texOffs(270, 185).addBox(-13F, -47F, -25F, 26F, 28F, 15F)
                .texOffs(213, 0).addBox(-13F, -50F, -10F, 26F, 31F, 57F)
                .texOffs(139, 277).addBox(-13F, -36.5F, 47F, 26F, 17.5F, 13F)
                .texOffs(387, 379).addBox(-13F, -31.5F, 60F, 26F, 13.5F, 1F, new CubeDeformation(0.04F))
                .texOffs(323, 395).addBox(-13F, -29F, 61F, 26F, 11F, 1F, new CubeDeformation(0.04F))
                .texOffs(154, 409).addBox(-13F, -26F, 62F, 26F, 8F, 1F, new CubeDeformation(0.04F))
                .texOffs(243, 420).addBox(-13F, -23F, 63F, 26F, 5F, 1F, new CubeDeformation(0.04F))
                .texOffs(118, 429).addBox(-13F, -20F, 64F, 26F, 2F, 1F, new CubeDeformation(0.04F))
                .texOffs(192, 242).addBox(-17F, -45.5F, -25F, 4F, 19.5F, 14F, new CubeDeformation(0.02F))
                .texOffs(244, 335).addBox(-17F, -45.5F, -11F, 4F, 21F, 1F, new CubeDeformation(0.04F))
                .texOffs(32, 308).addBox(-17F, -48.5F, -10F, 4F, 25.5F, 1F, new CubeDeformation(0.04F))
                .texOffs(257, 277).addBox(-17F, -48.5F, -9F, 4F, 27F, 1F, new CubeDeformation(0.04F))
                .texOffs(217, 277).addBox(-17F, -48.5F, -8F, 4F, 28.5F, 1F, new CubeDeformation(0.04F))
                .texOffs(379, 0).addBox(-17F, -48.5F, -7F, 4F, 29.5F, 45F)
                .texOffs(227, 277).addBox(-17F, -48.5F, 38F, 4F, 28.5F, 1F, new CubeDeformation(0.04F))
                .texOffs(267, 277).addBox(-17F, -48.5F, 39F, 4F, 27F, 1F, new CubeDeformation(0.04F))
                .texOffs(42, 308).addBox(-17F, -48.5F, 40F, 4F, 25.5F, 1F, new CubeDeformation(0.04F))
                .texOffs(72, 308).addBox(-17F, -48.5F, 41F, 4F, 24F, 1F, new CubeDeformation(0.04F))
                .texOffs(0, 308).addBox(-17F, -48.5F, 42F, 4F, 22.5F, 4F, new CubeDeformation(0.04F))
                .texOffs(228, 242).addBox(13F, -45.5F, -25F, 4F, 19.5F, 14F, new CubeDeformation(0.02F))
                .texOffs(254, 335).addBox(13F, -45.5F, -11F, 4F, 21F, 1F, new CubeDeformation(0.04F))
                .texOffs(52, 308).addBox(13F, -48.5F, -10F, 4F, 25.5F, 1F, new CubeDeformation(0.04F))
                .texOffs(277, 277).addBox(13F, -48.5F, -9F, 4F, 27F, 1F, new CubeDeformation(0.04F))
                .texOffs(237, 277).addBox(13F, -48.5F, -8F, 4F, 28.5F, 1F, new CubeDeformation(0.04F))
                .texOffs(0, 110).addBox(13F, -48.5F, -7F, 4F, 29.5F, 45F)
                .texOffs(247, 277).addBox(13F, -48.5F, 38F, 4F, 28.5F, 1F, new CubeDeformation(0.04F))
                .texOffs(287, 277).addBox(13F, -48.5F, 39F, 4F, 27F, 1F, new CubeDeformation(0.04F))
                .texOffs(62, 308).addBox(13F, -48.5F, 40F, 4F, 25.5F, 1F, new CubeDeformation(0.04F))
                .texOffs(82, 308).addBox(13F, -48.5F, 41F, 4F, 24F, 1F, new CubeDeformation(0.04F))
                .texOffs(16, 308).addBox(13F, -48.5F, 42F, 4F, 22.5F, 4F, new CubeDeformation(0.04F))
                .texOffs(92, 308).addBox(-17F, -50F, 46F, 4F, 24F, 1F, new CubeDeformation(0.04F))
                .texOffs(160, 335).addBox(-17F, -36.5F, 47F, 4F, 10.5F, 12F, new CubeDeformation(0.02F))
                .texOffs(303, 395).addBox(-17F, -36.5F, 59F, 4F, 12F, 1F, new CubeDeformation(0.04F))
                .texOffs(134, 409).addBox(-17F, -31.5F, 60F, 4F, 8.5F, 1F, new CubeDeformation(0.06F))
                .texOffs(351, 409).addBox(-17F, -29F, 61F, 4F, 7.5F, 1F, new CubeDeformation(0.06F))
                .texOffs(115, 420).addBox(-17F, -26F, 62F, 4F, 6F, 1F, new CubeDeformation(0.06F))
                .texOffs(308, 420).addBox(-17F, -23F, 63F, 4F, 4.5F, 1F, new CubeDeformation(0.06F))
                .texOffs(190, 429).addBox(-17F, -20F, 64F, 4F, 2F, 1F, new CubeDeformation(0.06F))
                .texOffs(102, 308).addBox(13F, -50F, 46F, 4F, 24F, 1F, new CubeDeformation(0.04F))
                .texOffs(192, 335).addBox(13F, -36.5F, 47F, 4F, 10.5F, 12F, new CubeDeformation(0.02F))
                .texOffs(313, 395).addBox(13F, -36.5F, 59F, 4F, 12F, 1F, new CubeDeformation(0.04F))
                .texOffs(144, 409).addBox(13F, -31.5F, 60F, 4F, 8.5F, 1F, new CubeDeformation(0.06F))
                .texOffs(361, 409).addBox(13F, -29F, 61F, 4F, 7.5F, 1F, new CubeDeformation(0.06F))
                .texOffs(125, 420).addBox(13F, -26F, 62F, 4F, 6F, 1F, new CubeDeformation(0.06F))
                .texOffs(318, 420).addBox(13F, -23F, 63F, 4F, 4.5F, 1F, new CubeDeformation(0.06F))
                .texOffs(200, 429).addBox(13F, -20F, 64F, 4F, 2F, 1F, new CubeDeformation(0.06F))
                .texOffs(202, 379).addBox(-15.5F, -47.5F, -24.75F, 31F, 0.5F, 14.5F, new CubeDeformation(0.04F))
                .texOffs(0, 185).addBox(-15.5F, -50.5F, -9.75F, 31F, 0.5F, 56F, new CubeDeformation(0.02F))
                .texOffs(216, 395).addBox(-15.5F, -37F, 46.75F, 31F, 0.5F, 12.5F, new CubeDeformation(0.04F))
                , PartPose.offsetAndRotation(0F, 24F, 0F, 0F, 0F, 0F));
    }

    private static void build_e_hull(PartDefinition root) {
        root.addOrReplaceChild("e_hull", CubeListBuilder.create()
                .texOffs(210, 429).addBox(-13F, -16.5F, -61F, 26F, 1.5F, 1F, new CubeDeformation(0.06F))
                .texOffs(26, 429).addBox(-13F, -18.5F, -60F, 26F, 3.5F, 1F, new CubeDeformation(0.04F))
                .texOffs(154, 420).addBox(-13F, -20.5F, -59F, 26F, 5.5F, 1F, new CubeDeformation(0.04F))
                .texOffs(208, 409).addBox(-13F, -23F, -58F, 26F, 8F, 1F, new CubeDeformation(0.04F))
                .texOffs(377, 395).addBox(-13F, -25F, -57F, 26F, 10F, 1F, new CubeDeformation(0.04F))
                .texOffs(114, 395).addBox(-13F, -27.5F, -56F, 26F, 12.5F, 1F, new CubeDeformation(0.04F))
                .texOffs(128, 379).addBox(-13F, -29.5F, -55F, 26F, 14.5F, 1F, new CubeDeformation(0.04F))
                .texOffs(203, 358).addBox(-13F, -32F, -54F, 26F, 17F, 1F, new CubeDeformation(0.02F))
                .texOffs(48, 358).addBox(-13F, -34F, -53F, 26F, 19F, 1F, new CubeDeformation(0.02F))
                .texOffs(106, 335).addBox(-13F, -36.5F, -52F, 26F, 21.5F, 1F, new CubeDeformation(0.02F))
                .texOffs(222, 308).addBox(-13F, -39.5F, -51F, 26F, 20.5F, 4F)
                .texOffs(174, 185).addBox(-13F, -40.5F, -47F, 26F, 21.5F, 22F)
                .texOffs(264, 429).addBox(-17F, -16.5F, -61F, 4F, 1.5F, 1F, new CubeDeformation(0.06F))
                .texOffs(80, 429).addBox(-17F, -18.5F, -60F, 4F, 3.5F, 1F, new CubeDeformation(0.06F))
                .texOffs(208, 420).addBox(-17F, -20.5F, -59F, 4F, 5.5F, 1F, new CubeDeformation(0.06F))
                .texOffs(280, 409).addBox(-17F, -23F, -58F, 4F, 8F, 1F, new CubeDeformation(0.06F))
                .texOffs(431, 395).addBox(-17F, -25F, -57F, 4F, 10F, 1F, new CubeDeformation(0.06F))
                .texOffs(196, 395).addBox(-17F, -27.5F, -56F, 4F, 12.5F, 1F, new CubeDeformation(0.04F))
                .texOffs(182, 379).addBox(-17F, -29.5F, -55F, 4F, 14.5F, 1F, new CubeDeformation(0.04F))
                .texOffs(290, 358).addBox(-17F, -32F, -54F, 4F, 17F, 1F, new CubeDeformation(0.04F))
                .texOffs(102, 358).addBox(-17F, -34F, -53F, 4F, 19F, 1F, new CubeDeformation(0.04F))
                .texOffs(224, 335).addBox(-17F, -36.5F, -52F, 4F, 21.5F, 1F, new CubeDeformation(0.04F))
                .texOffs(282, 308).addBox(-17F, -39.5F, -51F, 4F, 20.5F, 4F, new CubeDeformation(0.04F))
                .texOffs(408, 185).addBox(-17F, -40.5F, -47F, 4F, 21.5F, 15F, new CubeDeformation(0.02F))
                .texOffs(314, 335).addBox(-17F, -40.5F, -32F, 4F, 20.5F, 1F, new CubeDeformation(0.04F))
                .texOffs(112, 358).addBox(-17F, -40.5F, -31F, 4F, 19F, 1F, new CubeDeformation(0.04F))
                .texOffs(142, 358).addBox(-17F, -40.5F, -30F, 4F, 17.5F, 1F, new CubeDeformation(0.04F))
                .texOffs(338, 358).addBox(-17F, -40.5F, -29F, 4F, 16F, 1F, new CubeDeformation(0.04F))
                .texOffs(310, 358).addBox(-17F, -40.5F, -28F, 4F, 14.5F, 3F, new CubeDeformation(0.04F))
                .texOffs(274, 429).addBox(13F, -16.5F, -61F, 4F, 1.5F, 1F, new CubeDeformation(0.06F))
                .texOffs(90, 429).addBox(13F, -18.5F, -60F, 4F, 3.5F, 1F, new CubeDeformation(0.06F))
                .texOffs(218, 420).addBox(13F, -20.5F, -59F, 4F, 5.5F, 1F, new CubeDeformation(0.06F))
                .texOffs(290, 409).addBox(13F, -23F, -58F, 4F, 8F, 1F, new CubeDeformation(0.06F))
                .texOffs(441, 395).addBox(13F, -25F, -57F, 4F, 10F, 1F, new CubeDeformation(0.06F))
                .texOffs(206, 395).addBox(13F, -27.5F, -56F, 4F, 12.5F, 1F, new CubeDeformation(0.04F))
                .texOffs(192, 379).addBox(13F, -29.5F, -55F, 4F, 14.5F, 1F, new CubeDeformation(0.04F))
                .texOffs(300, 358).addBox(13F, -32F, -54F, 4F, 17F, 1F, new CubeDeformation(0.04F))
                .texOffs(122, 358).addBox(13F, -34F, -53F, 4F, 19F, 1F, new CubeDeformation(0.04F))
                .texOffs(234, 335).addBox(13F, -36.5F, -52F, 4F, 21.5F, 1F, new CubeDeformation(0.04F))
                .texOffs(298, 308).addBox(13F, -39.5F, -51F, 4F, 20.5F, 4F, new CubeDeformation(0.04F))
                .texOffs(446, 185).addBox(13F, -40.5F, -47F, 4F, 21.5F, 15F, new CubeDeformation(0.02F))
                .texOffs(324, 335).addBox(13F, -40.5F, -32F, 4F, 20.5F, 1F, new CubeDeformation(0.04F))
                .texOffs(132, 358).addBox(13F, -40.5F, -31F, 4F, 19F, 1F, new CubeDeformation(0.04F))
                .texOffs(152, 358).addBox(13F, -40.5F, -30F, 4F, 17.5F, 1F, new CubeDeformation(0.04F))
                .texOffs(348, 358).addBox(13F, -40.5F, -29F, 4F, 16F, 1F, new CubeDeformation(0.04F))
                .texOffs(324, 358).addBox(13F, -40.5F, -28F, 4F, 14.5F, 3F, new CubeDeformation(0.04F))
                .texOffs(0, 335).addBox(-15.5F, -41F, -47.25F, 31F, 0.5F, 22F, new CubeDeformation(0.04F))
                .texOffs(345, 420).addBox(-17F, -40.5F, -51.5F, 34F, 1F, 4F, new CubeDeformation(0.04F))
                .texOffs(264, 335).addBox(17F, -36F, -45.5F, 0.75F, 10F, 11.5F, new CubeDeformation(0.04F))
                .texOffs(289, 335).addBox(-17.75F, -36F, -45.5F, 0.75F, 10F, 11.5F, new CubeDeformation(0.04F))
                .texOffs(334, 335).addBox(-14F, -41.25F, -46.5F, 28F, 0.75F, 20F, new CubeDeformation(0.02F))
                , PartPose.offsetAndRotation(0F, 24F, 0F, 0F, 0F, 0F));
    }

    private static void build_u_bevel_cl(PartDefinition root) {
        root.addOrReplaceChild("u_bevel_cl", CubeListBuilder.create()
                .texOffs(226, 110).addBox(-1.5F, -3F, -10F, 1.5F, 3F, 56.5F, new CubeDeformation(0.04F))
                , PartPose.offsetAndRotation(17F, -24.5F, 0F, 0F, 0F, -0.6435F));
    }

    private static void build_u_bevel_sl(PartDefinition root) {
        root.addOrReplaceChild("u_bevel_sl", CubeListBuilder.create()
                .texOffs(257, 358).addBox(-1.5F, -3F, -25F, 1.5F, 3F, 15F, new CubeDeformation(0.04F))
                , PartPose.offsetAndRotation(17F, -21.5F, 0F, 0F, 0F, -0.6435F));
    }

    private static void build_u_bevel_cr(PartDefinition root) {
        root.addOrReplaceChild("u_bevel_cr", CubeListBuilder.create()
                .texOffs(226, 110).addBox(0F, -3F, -10F, 1.5F, 3F, 56.5F, new CubeDeformation(0.04F))
                , PartPose.offsetAndRotation(-17F, -24.5F, 0F, 0F, 0F, 0.6435F));
    }

    private static void build_u_bevel_sr(PartDefinition root) {
        root.addOrReplaceChild("u_bevel_sr", CubeListBuilder.create()
                .texOffs(257, 358).addBox(0F, -3F, -25F, 1.5F, 3F, 15F, new CubeDeformation(0.04F))
                , PartPose.offsetAndRotation(-17F, -21.5F, 0F, 0F, 0F, 0.6435F));
    }

    private static void build_e_front_plate(PartDefinition root) {
        PartDefinition p7 = root.addOrReplaceChild("e_front_plate", CubeListBuilder.create()
                .texOffs(264, 242).addBox(-17F, 0F, -28F, 34F, 3.5F, 28F)
                , PartPose.offsetAndRotation(0F, -16F, -51F, 1.1563F, 0F, 0F));
        p7.addOrReplaceChild("radflap", CubeListBuilder.create()
                .texOffs(0, 409).addBox(-7.5F, -1F, -9.5F, 15F, 1F, 9.5F, new CubeDeformation(0.04F))
                , PartPose.offsetAndRotation(0F, 0F, -9.5F, 0.2618F, 0F, 0F));
    }

    private static void build_u_rear_plate(PartDefinition root) {
        PartDefinition p9 = root.addOrReplaceChild("u_rear_plate", CubeListBuilder.create()
                .texOffs(112, 308).addBox(-17F, 0F, 0F, 34F, 3.5F, 21F)
                , PartPose.offsetAndRotation(0F, -13F, 59.5F, -1.2412F, 0F, 0F));
        p9.addOrReplaceChild("hatch", CubeListBuilder.create()
                .texOffs(451, 395).addBox(-6F, -0.75F, 3F, 12F, 0.75F, 10F, new CubeDeformation(0.04F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
    }

    private static void build_u_rear_chamfer(PartDefinition root) {
        root.addOrReplaceChild("u_rear_chamfer", CubeListBuilder.create()
                .texOffs(293, 379).addBox(-17F, 0F, 0F, 34F, 1.5F, 13F, new CubeDeformation(0.02F))
                , PartPose.offsetAndRotation(0F, -26.5F, 36F, -0.4266F, 0F, 0F));
    }

    private static void build_u_fittings(PartDefinition root) {
        root.addOrReplaceChild("u_fittings", CubeListBuilder.create()
                .texOffs(352, 185).addBox(17F, -45F, 22F, 0.75F, 25F, 13F, new CubeDeformation(0.04F))
                .texOffs(49, 409).addBox(17F, -43F, 24.75F, 0.75F, 3F, 7.5F, new CubeDeformation(0.06F))
                .texOffs(67, 420).addBox(17.2F, -42.25F, 25.5F, 0.6F, 1.5F, 6F, new CubeDeformation(0.06F))
                .texOffs(81, 409).addBox(17F, -46.25F, 38.25F, 0.75F, 4.5F, 5.5F, new CubeDeformation(0.06F))
                .texOffs(135, 420).addBox(17.2F, -45.5F, 39F, 0.6F, 3F, 4F, new CubeDeformation(0.06F))
                .texOffs(81, 409).addBox(17F, -46.25F, 12.75F, 0.75F, 4.5F, 5.5F, new CubeDeformation(0.06F))
                .texOffs(135, 420).addBox(17.2F, -45.5F, 13.5F, 0.6F, 3F, 4F, new CubeDeformation(0.06F))
                .texOffs(107, 409).addBox(17F, -46.5F, -7.5F, 0.75F, 5F, 5F, new CubeDeformation(0.06F))
                .texOffs(145, 420).addBox(17.2F, -45.75F, -6.75F, 0.6F, 3.5F, 3.5F, new CubeDeformation(0.06F))
                .texOffs(480, 420).addBox(17.25F, -45.25F, -6.25F, 1.5F, 2.5F, 2.5F, new CubeDeformation(0.06F))
                .texOffs(66, 409).addBox(17F, -43.25F, -20.75F, 0.75F, 3.5F, 6.5F, new CubeDeformation(0.06F))
                .texOffs(103, 420).addBox(17.2F, -42.5F, -20F, 0.6F, 2F, 5F, new CubeDeformation(0.06F))
                .texOffs(338, 409).addBox(17F, -33.5F, 50.25F, 0.75F, 3F, 5.5F, new CubeDeformation(0.06F))
                .texOffs(328, 420).addBox(17.2F, -32.75F, 51F, 0.6F, 1.5F, 4F, new CubeDeformation(0.06F))
                .texOffs(380, 185).addBox(-17.75F, -45F, 22F, 0.75F, 25F, 13F, new CubeDeformation(0.04F))
                .texOffs(49, 409).addBox(-17.75F, -43F, 24.75F, 0.75F, 3F, 7.5F, new CubeDeformation(0.06F))
                .texOffs(67, 420).addBox(-17.8F, -42.25F, 25.5F, 0.6F, 1.5F, 6F, new CubeDeformation(0.06F))
                .texOffs(81, 409).addBox(-17.75F, -46.25F, 38.25F, 0.75F, 4.5F, 5.5F, new CubeDeformation(0.06F))
                .texOffs(135, 420).addBox(-17.8F, -45.5F, 39F, 0.6F, 3F, 4F, new CubeDeformation(0.06F))
                .texOffs(81, 409).addBox(-17.75F, -46.25F, 12.75F, 0.75F, 4.5F, 5.5F, new CubeDeformation(0.06F))
                .texOffs(135, 420).addBox(-17.8F, -45.5F, 13.5F, 0.6F, 3F, 4F, new CubeDeformation(0.06F))
                .texOffs(107, 409).addBox(-17.75F, -46.5F, -7.5F, 0.75F, 5F, 5F, new CubeDeformation(0.06F))
                .texOffs(145, 420).addBox(-17.8F, -45.75F, -6.75F, 0.6F, 3.5F, 3.5F, new CubeDeformation(0.06F))
                .texOffs(480, 420).addBox(-18.75F, -45.25F, -6.25F, 1.5F, 2.5F, 2.5F, new CubeDeformation(0.06F))
                .texOffs(66, 409).addBox(-17.75F, -43.25F, -20.75F, 0.75F, 3.5F, 6.5F, new CubeDeformation(0.06F))
                .texOffs(103, 420).addBox(-17.8F, -42.5F, -20F, 0.6F, 2F, 5F, new CubeDeformation(0.06F))
                .texOffs(338, 409).addBox(-17.75F, -33.5F, 50.25F, 0.75F, 3F, 5.5F, new CubeDeformation(0.06F))
                .texOffs(328, 420).addBox(-17.8F, -32.75F, 51F, 0.6F, 1.5F, 4F, new CubeDeformation(0.06F))
                .texOffs(0, 429).addBox(-6F, -45.5F, -25.75F, 12F, 4F, 0.75F, new CubeDeformation(0.06F))
                .texOffs(309, 429).addBox(-5.25F, -44F, -25.9F, 10.5F, 1F, 0.4F, new CubeDeformation(0.06F))
                .texOffs(0, 395).addBox(-8F, -50F, -23F, 16F, 2.75F, 11F, new CubeDeformation(0.02F))
                .texOffs(54, 395).addBox(-8.75F, -51F, -23.75F, 17.5F, 1F, 12.5F, new CubeDeformation(0.04F))
                .texOffs(284, 429).addBox(-6F, -49F, -23.4F, 12F, 1F, 0.5F, new CubeDeformation(0.06F))
                , PartPose.offsetAndRotation(0F, 24F, 0F, 0F, 0F, 0F));
    }

    private static void build_u_shutter_l0a(PartDefinition root) {
        root.addOrReplaceChild("u_shutter_l0a", CubeListBuilder.create()
                .texOffs(81, 420).addBox(0F, -2.25F, 0F, 0.75F, 4.5F, 2.75F, new CubeDeformation(0.06F))
                , PartPose.offsetAndRotation(17.75F, -20F, 38.25F, 0F, -1.1345F, 0F));
    }

    private static void build_u_shutter_l0b(PartDefinition root) {
        root.addOrReplaceChild("u_shutter_l0b", CubeListBuilder.create()
                .texOffs(81, 420).addBox(0F, -2.25F, -2.75F, 0.75F, 4.5F, 2.75F, new CubeDeformation(0.06F))
                , PartPose.offsetAndRotation(17.75F, -20F, 43.75F, 0F, 1.1345F, 0F));
    }

    private static void build_u_shutter_l1a(PartDefinition root) {
        root.addOrReplaceChild("u_shutter_l1a", CubeListBuilder.create()
                .texOffs(81, 420).addBox(0F, -2.25F, 0F, 0.75F, 4.5F, 2.75F, new CubeDeformation(0.06F))
                , PartPose.offsetAndRotation(17.75F, -20F, 12.75F, 0F, -1.1345F, 0F));
    }

    private static void build_u_shutter_l1b(PartDefinition root) {
        root.addOrReplaceChild("u_shutter_l1b", CubeListBuilder.create()
                .texOffs(81, 420).addBox(0F, -2.25F, -2.75F, 0.75F, 4.5F, 2.75F, new CubeDeformation(0.06F))
                , PartPose.offsetAndRotation(17.75F, -20F, 18.25F, 0F, 1.1345F, 0F));
    }

    private static void build_u_flap_l(PartDefinition root) {
        root.addOrReplaceChild("u_flap_l", CubeListBuilder.create()
                .texOffs(119, 409).addBox(0F, 0F, -3.25F, 0.75F, 3F, 6.5F, new CubeDeformation(0.06F))
                , PartPose.offsetAndRotation(17.75F, -19.25F, -17.5F, 0F, 0F, -0.8727F));
    }

    private static void build_u_shutter_r0a(PartDefinition root) {
        root.addOrReplaceChild("u_shutter_r0a", CubeListBuilder.create()
                .texOffs(81, 420).addBox(-0.75F, -2.25F, 0F, 0.75F, 4.5F, 2.75F, new CubeDeformation(0.06F))
                , PartPose.offsetAndRotation(-17.75F, -20F, 38.25F, 0F, 1.1345F, 0F));
    }

    private static void build_u_shutter_r0b(PartDefinition root) {
        root.addOrReplaceChild("u_shutter_r0b", CubeListBuilder.create()
                .texOffs(81, 420).addBox(-0.75F, -2.25F, -2.75F, 0.75F, 4.5F, 2.75F, new CubeDeformation(0.06F))
                , PartPose.offsetAndRotation(-17.75F, -20F, 43.75F, 0F, -1.1345F, 0F));
    }

    private static void build_u_shutter_r1a(PartDefinition root) {
        root.addOrReplaceChild("u_shutter_r1a", CubeListBuilder.create()
                .texOffs(81, 420).addBox(-0.75F, -2.25F, 0F, 0.75F, 4.5F, 2.75F, new CubeDeformation(0.06F))
                , PartPose.offsetAndRotation(-17.75F, -20F, 12.75F, 0F, 1.1345F, 0F));
    }

    private static void build_u_shutter_r1b(PartDefinition root) {
        root.addOrReplaceChild("u_shutter_r1b", CubeListBuilder.create()
                .texOffs(81, 420).addBox(-0.75F, -2.25F, -2.75F, 0.75F, 4.5F, 2.75F, new CubeDeformation(0.06F))
                , PartPose.offsetAndRotation(-17.75F, -20F, 18.25F, 0F, -1.1345F, 0F));
    }

    private static void build_u_flap_r(PartDefinition root) {
        root.addOrReplaceChild("u_flap_r", CubeListBuilder.create()
                .texOffs(119, 409).addBox(-0.75F, 0F, -3.25F, 0.75F, 3F, 6.5F, new CubeDeformation(0.06F))
                , PartPose.offsetAndRotation(-17.75F, -19.25F, -17.5F, 0F, 0F, 0.8727F));
    }

    private static void build_e_lamp_l(PartDefinition root) {
        root.addOrReplaceChild("e_lamp_l", CubeListBuilder.create()
                .texOffs(88, 420).addBox(-2F, -3.5F, -2F, 4F, 3.5F, 3.5F, new CubeDeformation(0.04F))
                .texOffs(111, 429).addBox(-1.5F, -3.25F, -2.5F, 3F, 3F, 0.5F, new CubeDeformation(0.06F))
                , PartPose.offsetAndRotation(13.5F, -16.5F, -52F, 0F, 0F, 0F));
    }

    private static void build_e_lamp_r(PartDefinition root) {
        root.addOrReplaceChild("e_lamp_r", CubeListBuilder.create()
                .texOffs(88, 420).addBox(-2F, -3.5F, -2F, 4F, 3.5F, 3.5F, new CubeDeformation(0.04F))
                .texOffs(111, 429).addBox(-1.5F, -3.25F, -2.5F, 3F, 3F, 0.5F, new CubeDeformation(0.06F))
                , PartPose.offsetAndRotation(-13.5F, -16.5F, -52F, 0F, 0F, 0F));
    }

    private static void build_turret(PartDefinition root) {
        PartDefinition p25 = root.addOrReplaceChild("turret", CubeListBuilder.create()
                .texOffs(306, 409).addBox(-4F, -10.6F, -4F, 8F, 0.5F, 8F, new CubeDeformation(0.06F))
                , PartPose.offsetAndRotation(0F, -26F, 11.5F, 0F, 0F, 0F));
        p25.addOrReplaceChild("drum", CubeListBuilder.create()
                .texOffs(0, 242).addBox(-3.3647F, -9F, -12.557F, 6.7293F, 9F, 25.1141F, new CubeDeformation(0.02F))
                .texOffs(399, 358).addBox(-12.557F, -9F, -3.3647F, 25.1141F, 9F, 6.7293F, new CubeDeformation(0.02F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p25.addOrReplaceChild("lip", CubeListBuilder.create()
                .texOffs(431, 277).addBox(-3.4986F, -10F, -13.057F, 6.9973F, 1F, 26.1141F, new CubeDeformation(0.04F))
                .texOffs(0, 420).addBox(-13.057F, -10.04F, -3.4986F, 26.1141F, 1.04F, 6.9973F, new CubeDeformation(0.04F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p25.addOrReplaceChild("drum_1", CubeListBuilder.create()
                .texOffs(64, 242).addBox(-3.3647F, -8.98F, -12.557F, 6.7293F, 8.98F, 25.1141F, new CubeDeformation(0.02F))
                .texOffs(0, 379).addBox(-12.557F, -9F, -3.3647F, 25.1141F, 8.98F, 6.7293F, new CubeDeformation(0.02F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.5236F, 0F));
        p25.addOrReplaceChild("lip_1", CubeListBuilder.create()
                .texOffs(364, 277).addBox(-3.4986F, -10.08F, -13.057F, 6.9973F, 1.1F, 26.1141F, new CubeDeformation(0.04F))
                .texOffs(438, 409).addBox(-13.057F, -10.12F, -3.4986F, 26.1141F, 1.1F, 6.9973F, new CubeDeformation(0.04F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.5236F, 0F));
        p25.addOrReplaceChild("drum_2", CubeListBuilder.create()
                .texOffs(128, 242).addBox(-3.3647F, -8.96F, -12.557F, 6.7293F, 8.96F, 25.1141F, new CubeDeformation(0.02F))
                .texOffs(64, 379).addBox(-12.557F, -9F, -3.3647F, 25.1141F, 8.96F, 6.7293F, new CubeDeformation(0.02F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 1.0472F, 0F));
        p25.addOrReplaceChild("lip_2", CubeListBuilder.create()
                .texOffs(297, 277).addBox(-3.4986F, -10.16F, -13.057F, 6.9973F, 1.2F, 26.1141F, new CubeDeformation(0.04F))
                .texOffs(371, 409).addBox(-13.057F, -10.2F, -3.4986F, 26.1141F, 1.16F, 6.9973F, new CubeDeformation(0.04F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 1.0472F, 0F));
        PartDefinition p32 = p25.addOrReplaceChild("port0", CubeListBuilder.create()
                .texOffs(228, 420).addBox(-3F, -7.5F, -13.307F, 6F, 5F, 1.25F, new CubeDeformation(0.06F))
                .texOffs(100, 429).addBox(-2.25F, -6.75F, -13.507F, 4.5F, 3.5F, 0.55F, new CubeDeformation(0.06F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p32.addOrReplaceChild("sh0", CubeListBuilder.create()
                .texOffs(338, 420).addBox(0F, -2.25F, -0.75F, 2.75F, 4.5F, 0.75F, new CubeDeformation(0.06F))
                , PartPose.offsetAndRotation(-3F, -5F, -13.307F, 0F, -1.0472F, 0F));
        p32.addOrReplaceChild("sh2", CubeListBuilder.create()
                .texOffs(338, 420).addBox(-2.75F, -2.25F, -0.75F, 2.75F, 4.5F, 0.75F, new CubeDeformation(0.06F))
                , PartPose.offsetAndRotation(3F, -5F, -13.307F, 0F, 1.0472F, 0F));
        PartDefinition p35 = p25.addOrReplaceChild("port1", CubeListBuilder.create()
                .texOffs(228, 420).addBox(-3F, -7.5F, -13.307F, 6F, 5F, 1.25F, new CubeDeformation(0.06F))
                .texOffs(100, 429).addBox(-2.25F, -6.75F, -13.507F, 4.5F, 3.5F, 0.55F, new CubeDeformation(0.06F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 1.5708F, 0F));
        p35.addOrReplaceChild("sh0", CubeListBuilder.create()
                .texOffs(338, 420).addBox(0F, -2.25F, -0.75F, 2.75F, 4.5F, 0.75F, new CubeDeformation(0.06F))
                , PartPose.offsetAndRotation(-3F, -5F, -13.307F, 0F, -1.0472F, 0F));
        p35.addOrReplaceChild("sh2", CubeListBuilder.create()
                .texOffs(338, 420).addBox(-2.75F, -2.25F, -0.75F, 2.75F, 4.5F, 0.75F, new CubeDeformation(0.06F))
                , PartPose.offsetAndRotation(3F, -5F, -13.307F, 0F, 1.0472F, 0F));
        PartDefinition p38 = p25.addOrReplaceChild("port2", CubeListBuilder.create()
                .texOffs(228, 420).addBox(-3F, -7.5F, -13.307F, 6F, 5F, 1.25F, new CubeDeformation(0.06F))
                .texOffs(100, 429).addBox(-2.25F, -6.75F, -13.507F, 4.5F, 3.5F, 0.55F, new CubeDeformation(0.06F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 3.1416F, 0F));
        p38.addOrReplaceChild("sh0", CubeListBuilder.create()
                .texOffs(338, 420).addBox(0F, -2.25F, -0.75F, 2.75F, 4.5F, 0.75F, new CubeDeformation(0.06F))
                , PartPose.offsetAndRotation(-3F, -5F, -13.307F, 0F, -1.0472F, 0F));
        p38.addOrReplaceChild("sh2", CubeListBuilder.create()
                .texOffs(338, 420).addBox(-2.75F, -2.25F, -0.75F, 2.75F, 4.5F, 0.75F, new CubeDeformation(0.06F))
                , PartPose.offsetAndRotation(3F, -5F, -13.307F, 0F, 1.0472F, 0F));
        PartDefinition p41 = p25.addOrReplaceChild("port3", CubeListBuilder.create()
                .texOffs(228, 420).addBox(-3F, -7.5F, -13.307F, 6F, 5F, 1.25F, new CubeDeformation(0.06F))
                .texOffs(100, 429).addBox(-2.25F, -6.75F, -13.507F, 4.5F, 3.5F, 0.55F, new CubeDeformation(0.06F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 4.7124F, 0F));
        p41.addOrReplaceChild("sh0", CubeListBuilder.create()
                .texOffs(338, 420).addBox(0F, -2.25F, -0.75F, 2.75F, 4.5F, 0.75F, new CubeDeformation(0.06F))
                , PartPose.offsetAndRotation(-3F, -5F, -13.307F, 0F, -1.0472F, 0F));
        p41.addOrReplaceChild("sh2", CubeListBuilder.create()
                .texOffs(338, 420).addBox(-2.75F, -2.25F, -0.75F, 2.75F, 4.5F, 0.75F, new CubeDeformation(0.06F))
                , PartPose.offsetAndRotation(3F, -5F, -13.307F, 0F, 1.0472F, 0F));
        PartDefinition p44 = p25.addOrReplaceChild("gun", CubeListBuilder.create()
                .texOffs(297, 420).addBox(-1.75F, -1.75F, -1.5F, 3.5F, 3.5F, 2F, new CubeDeformation(0.06F))
                , PartPose.offsetAndRotation(0F, -5F, -13.307F, 0F, 0F, 0F));
        PartDefinition p45 = p44.addOrReplaceChild("recoil", CubeListBuilder.create()
                .texOffs(441, 379).addBox(-1.5F, -1.5F, -13F, 3F, 3F, 11.5F, new CubeDeformation(0.04F))
                .texOffs(470, 420).addBox(-1F, -1F, -16F, 2F, 2F, 3F, new CubeDeformation(0.06F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        // front sight blade on the muzzle end of the water jacket
        p45.addOrReplaceChild("front_sight", CubeListBuilder.create()
                .texOffs(470, 420).addBox(-0.3F, -2.6F, -12.9F, 0.6F, 1.2F, 0.6F, new CubeDeformation(0.0F))
                , PartPose.ZERO);
        p45.addOrReplaceChild("flash", CubeListBuilder.create()
                .texOffs(262, 409).addBox(-2F, -2F, -5F, 4F, 4F, 5F, new CubeDeformation(0.04F))
                .texOffs(300, 409).addBox(-0.5F, -3.5F, -3F, 1F, 7F, 2F, new CubeDeformation(0.06F))
                .texOffs(172, 429).addBox(-3.5F, -0.5F, -3F, 7F, 1F, 2F, new CubeDeformation(0.06F))
                , PartPose.offsetAndRotation(0F, 0F, -16F, 0F, 0F, 0F));
    }

    private static void build_lower(PartDefinition root) {
        root.addOrReplaceChild("lower", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-8.5F, -19F, -46F, 2.5F, 5.5F, 104F, new CubeDeformation(0.02F))
                .texOffs(0, 0).addBox(6F, -19F, -46F, 2.5F, 5.5F, 104F, new CubeDeformation(0.02F))
                .texOffs(421, 420).addBox(-11F, -13.25F, -20.75F, 22F, 2.5F, 2.5F, new CubeDeformation(0.04F))
                .texOffs(168, 395).addBox(-3.5F, -15F, -23F, 7F, 6.5F, 7F, new CubeDeformation(0.04F))
                .texOffs(162, 358).addBox(-8.5F, -13.5F, -28.5F, 2.5F, 0.25F, 18F, new CubeDeformation(0.06F))
                .texOffs(162, 358).addBox(6F, -13.5F, -28.5F, 2.5F, 0.25F, 18F, new CubeDeformation(0.06F))
                .texOffs(421, 420).addBox(-11F, -13.25F, 49.25F, 22F, 2.5F, 2.5F, new CubeDeformation(0.04F))
                .texOffs(168, 395).addBox(-3.5F, -15F, 47F, 7F, 6.5F, 7F, new CubeDeformation(0.04F))
                .texOffs(162, 358).addBox(-8.5F, -13.5F, 41.5F, 2.5F, 0.25F, 18F, new CubeDeformation(0.06F))
                .texOffs(162, 358).addBox(6F, -13.5F, 41.5F, 2.5F, 0.25F, 18F, new CubeDeformation(0.06F))
                .texOffs(98, 110).addBox(-1F, -12F, -15.5F, 2F, 2F, 62F, new CubeDeformation(0.04F))
                , PartPose.offsetAndRotation(0F, 24F, 0F, 0F, 0F, 0F));
    }

    private static void build_guard0l(PartDefinition root) {
        PartDefinition p48 = root.addOrReplaceChild("guard0l", CubeListBuilder.create()
                , PartPose.offsetAndRotation(0F, 12F, -19.5F, 0F, 0F, 0F));
        p48.addOrReplaceChild("plate", CubeListBuilder.create()
                .texOffs(430, 335).addBox(12.5F, -15.5F, -10.5439F, 5.25F, 1.75F, 18.5879F, new CubeDeformation(0.04F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, -0.9774F, 0F, 0F));
        p48.addOrReplaceChild("plate_1", CubeListBuilder.create()
                .texOffs(358, 358).addBox(12.5F, -15.5F, -7.4439F, 5.25F, 1.7F, 14.8879F, new CubeDeformation(0.04F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p48.addOrReplaceChild("plate_2", CubeListBuilder.create()
                .texOffs(0, 358).addBox(12.5F, -15.5F, -8.0439F, 5.25F, 1.65F, 18.5879F, new CubeDeformation(0.04F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0.9774F, 0F, 0F));
    }

    private static void build_guard0r(PartDefinition root) {
        PartDefinition p52 = root.addOrReplaceChild("guard0r", CubeListBuilder.create()
                , PartPose.offsetAndRotation(0F, 12F, -19.5F, 0F, 0F, 0F));
        p52.addOrReplaceChild("plate", CubeListBuilder.create()
                .texOffs(430, 335).addBox(-17.75F, -15.5F, -10.5439F, 5.25F, 1.75F, 18.5879F, new CubeDeformation(0.04F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, -0.9774F, 0F, 0F));
        p52.addOrReplaceChild("plate_1", CubeListBuilder.create()
                .texOffs(358, 358).addBox(-17.75F, -15.5F, -7.4439F, 5.25F, 1.7F, 14.8879F, new CubeDeformation(0.04F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p52.addOrReplaceChild("plate_2", CubeListBuilder.create()
                .texOffs(0, 358).addBox(-17.75F, -15.5F, -8.0439F, 5.25F, 1.65F, 18.5879F, new CubeDeformation(0.04F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0.9774F, 0F, 0F));
    }

    private static void build_guard1l(PartDefinition root) {
        PartDefinition p56 = root.addOrReplaceChild("guard1l", CubeListBuilder.create()
                , PartPose.offsetAndRotation(0F, 12F, 50.5F, 0F, 0F, 0F));
        p56.addOrReplaceChild("plate", CubeListBuilder.create()
                .texOffs(430, 335).addBox(12.5F, -15.5F, -10.5439F, 5.25F, 1.75F, 18.5879F, new CubeDeformation(0.04F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, -0.9774F, 0F, 0F));
        p56.addOrReplaceChild("plate_1", CubeListBuilder.create()
                .texOffs(358, 358).addBox(12.5F, -15.5F, -7.4439F, 5.25F, 1.7F, 14.8879F, new CubeDeformation(0.04F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p56.addOrReplaceChild("plate_2", CubeListBuilder.create()
                .texOffs(0, 358).addBox(12.5F, -15.5F, -8.0439F, 5.25F, 1.65F, 18.5879F, new CubeDeformation(0.04F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0.9774F, 0F, 0F));
    }

    private static void build_guard1r(PartDefinition root) {
        PartDefinition p60 = root.addOrReplaceChild("guard1r", CubeListBuilder.create()
                , PartPose.offsetAndRotation(0F, 12F, 50.5F, 0F, 0F, 0F));
        p60.addOrReplaceChild("plate", CubeListBuilder.create()
                .texOffs(430, 335).addBox(-17.75F, -15.5F, -10.5439F, 5.25F, 1.75F, 18.5879F, new CubeDeformation(0.04F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, -0.9774F, 0F, 0F));
        p60.addOrReplaceChild("plate_1", CubeListBuilder.create()
                .texOffs(358, 358).addBox(-17.75F, -15.5F, -7.4439F, 5.25F, 1.7F, 14.8879F, new CubeDeformation(0.04F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p60.addOrReplaceChild("plate_2", CubeListBuilder.create()
                .texOffs(0, 358).addBox(-17.75F, -15.5F, -8.0439F, 5.25F, 1.65F, 18.5879F, new CubeDeformation(0.04F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0.9774F, 0F, 0F));
    }

    private static void build_wheel0l_steer(PartDefinition root) {
        PartDefinition p64 = root.addOrReplaceChild("wheel0l_steer", CubeListBuilder.create()
                , PartPose.offsetAndRotation(14F, 12F, -19.5F, 0F, 0F, 0F));
        PartDefinition p65 = p64.addOrReplaceChild("wheel0l", CubeListBuilder.create()
                .texOffs(94, 409).addBox(3.75F, -2.5F, -2.5F, 1.25F, 5F, 5F, new CubeDeformation(0.06F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p65.addOrReplaceChild("g", CubeListBuilder.create()
                .texOffs(61, 277).addBox(-3.25F, -12.0741F, -3.2352F, 6.5F, 24.1481F, 6.4705F)
                .texOffs(388, 242).addBox(-3.25F, -3.2352F, -12.0741F, 6.5F, 6.4705F, 24.1481F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p65.addOrReplaceChild("g_1", CubeListBuilder.create()
                .texOffs(87, 277).addBox(-3.17F, -12.0741F, -3.2352F, 6.34F, 24.1481F, 6.4705F)
                .texOffs(450, 242).addBox(-3.17F, -3.2352F, -12.0741F, 6.34F, 6.4705F, 24.1481F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, -0.5236F, 0F, 0F));
        p65.addOrReplaceChild("g_2", CubeListBuilder.create()
                .texOffs(113, 277).addBox(-3.09F, -12.0741F, -3.2352F, 6.18F, 24.1481F, 6.4705F)
                .texOffs(0, 277).addBox(-3.09F, -3.2352F, -12.0741F, 6.18F, 6.4705F, 24.1481F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, -1.0472F, 0F, 0F));
        p65.addOrReplaceChild("g_3", CubeListBuilder.create()
                .texOffs(431, 308).addBox(2.75F, -9.1763F, -2.4588F, 1F, 18.3526F, 4.9176F, new CubeDeformation(0.04F))
                .texOffs(314, 308).addBox(2.75F, -2.4588F, -9.1763F, 1F, 4.9176F, 18.3526F, new CubeDeformation(0.04F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p65.addOrReplaceChild("g_4", CubeListBuilder.create()
                .texOffs(443, 308).addBox(2.83F, -9.1763F, -2.4588F, 0.84F, 18.3526F, 4.9176F, new CubeDeformation(0.04F))
                .texOffs(353, 308).addBox(2.83F, -2.4588F, -9.1763F, 0.84F, 4.9176F, 18.3526F, new CubeDeformation(0.04F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, -0.5236F, 0F, 0F));
        p65.addOrReplaceChild("g_5", CubeListBuilder.create()
                .texOffs(455, 308).addBox(2.91F, -9.1763F, -2.4588F, 0.68F, 18.3526F, 4.9176F, new CubeDeformation(0.04F))
                .texOffs(392, 308).addBox(2.91F, -2.4588F, -9.1763F, 0.68F, 4.9176F, 18.3526F, new CubeDeformation(0.04F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, -1.0472F, 0F, 0F));
    }

    private static void build_wheel0r_steer(PartDefinition root) {
        PartDefinition p72 = root.addOrReplaceChild("wheel0r_steer", CubeListBuilder.create()
                , PartPose.offsetAndRotation(-14F, 12F, -19.5F, 0F, 0F, 0F));
        PartDefinition p73 = p72.addOrReplaceChild("wheel0r", CubeListBuilder.create()
                .texOffs(94, 409).addBox(-5F, -2.5F, -2.5F, 1.25F, 5F, 5F, new CubeDeformation(0.06F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p73.addOrReplaceChild("g", CubeListBuilder.create()
                .texOffs(61, 277).addBox(-3.25F, -12.0741F, -3.2352F, 6.5F, 24.1481F, 6.4705F)
                .texOffs(388, 242).addBox(-3.25F, -3.2352F, -12.0741F, 6.5F, 6.4705F, 24.1481F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p73.addOrReplaceChild("g_1", CubeListBuilder.create()
                .texOffs(87, 277).addBox(-3.17F, -12.0741F, -3.2352F, 6.34F, 24.1481F, 6.4705F)
                .texOffs(450, 242).addBox(-3.17F, -3.2352F, -12.0741F, 6.34F, 6.4705F, 24.1481F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, -0.5236F, 0F, 0F));
        p73.addOrReplaceChild("g_2", CubeListBuilder.create()
                .texOffs(113, 277).addBox(-3.09F, -12.0741F, -3.2352F, 6.18F, 24.1481F, 6.4705F)
                .texOffs(0, 277).addBox(-3.09F, -3.2352F, -12.0741F, 6.18F, 6.4705F, 24.1481F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, -1.0472F, 0F, 0F));
        p73.addOrReplaceChild("g_3", CubeListBuilder.create()
                .texOffs(431, 308).addBox(-3.75F, -9.1763F, -2.4588F, 1F, 18.3526F, 4.9176F, new CubeDeformation(0.04F))
                .texOffs(314, 308).addBox(-3.75F, -2.4588F, -9.1763F, 1F, 4.9176F, 18.3526F, new CubeDeformation(0.04F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p73.addOrReplaceChild("g_4", CubeListBuilder.create()
                .texOffs(443, 308).addBox(-3.67F, -9.1763F, -2.4588F, 0.84F, 18.3526F, 4.9176F, new CubeDeformation(0.04F))
                .texOffs(353, 308).addBox(-3.67F, -2.4588F, -9.1763F, 0.84F, 4.9176F, 18.3526F, new CubeDeformation(0.04F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, -0.5236F, 0F, 0F));
        p73.addOrReplaceChild("g_5", CubeListBuilder.create()
                .texOffs(455, 308).addBox(-3.59F, -9.1763F, -2.4588F, 0.68F, 18.3526F, 4.9176F, new CubeDeformation(0.04F))
                .texOffs(392, 308).addBox(-3.59F, -2.4588F, -9.1763F, 0.68F, 4.9176F, 18.3526F, new CubeDeformation(0.04F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, -1.0472F, 0F, 0F));
    }

    private static void build_wheel1l(PartDefinition root) {
        PartDefinition p80 = root.addOrReplaceChild("wheel1l", CubeListBuilder.create()
                .texOffs(94, 409).addBox(3.75F, -2.5F, -2.5F, 1.25F, 5F, 5F, new CubeDeformation(0.06F))
                , PartPose.offsetAndRotation(14F, 12F, 50.5F, 0F, 0F, 0F));
        p80.addOrReplaceChild("g", CubeListBuilder.create()
                .texOffs(61, 277).addBox(-3.25F, -12.0741F, -3.2352F, 6.5F, 24.1481F, 6.4705F)
                .texOffs(388, 242).addBox(-3.25F, -3.2352F, -12.0741F, 6.5F, 6.4705F, 24.1481F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p80.addOrReplaceChild("g_1", CubeListBuilder.create()
                .texOffs(87, 277).addBox(-3.17F, -12.0741F, -3.2352F, 6.34F, 24.1481F, 6.4705F)
                .texOffs(450, 242).addBox(-3.17F, -3.2352F, -12.0741F, 6.34F, 6.4705F, 24.1481F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, -0.5236F, 0F, 0F));
        p80.addOrReplaceChild("g_2", CubeListBuilder.create()
                .texOffs(113, 277).addBox(-3.09F, -12.0741F, -3.2352F, 6.18F, 24.1481F, 6.4705F)
                .texOffs(0, 277).addBox(-3.09F, -3.2352F, -12.0741F, 6.18F, 6.4705F, 24.1481F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, -1.0472F, 0F, 0F));
        p80.addOrReplaceChild("g_3", CubeListBuilder.create()
                .texOffs(431, 308).addBox(2.75F, -9.1763F, -2.4588F, 1F, 18.3526F, 4.9176F, new CubeDeformation(0.04F))
                .texOffs(314, 308).addBox(2.75F, -2.4588F, -9.1763F, 1F, 4.9176F, 18.3526F, new CubeDeformation(0.04F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p80.addOrReplaceChild("g_4", CubeListBuilder.create()
                .texOffs(443, 308).addBox(2.83F, -9.1763F, -2.4588F, 0.84F, 18.3526F, 4.9176F, new CubeDeformation(0.04F))
                .texOffs(353, 308).addBox(2.83F, -2.4588F, -9.1763F, 0.84F, 4.9176F, 18.3526F, new CubeDeformation(0.04F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, -0.5236F, 0F, 0F));
        p80.addOrReplaceChild("g_5", CubeListBuilder.create()
                .texOffs(455, 308).addBox(2.91F, -9.1763F, -2.4588F, 0.68F, 18.3526F, 4.9176F, new CubeDeformation(0.04F))
                .texOffs(392, 308).addBox(2.91F, -2.4588F, -9.1763F, 0.68F, 4.9176F, 18.3526F, new CubeDeformation(0.04F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, -1.0472F, 0F, 0F));
    }

    private static void build_wheel1r(PartDefinition root) {
        PartDefinition p87 = root.addOrReplaceChild("wheel1r", CubeListBuilder.create()
                .texOffs(94, 409).addBox(-5F, -2.5F, -2.5F, 1.25F, 5F, 5F, new CubeDeformation(0.06F))
                , PartPose.offsetAndRotation(-14F, 12F, 50.5F, 0F, 0F, 0F));
        p87.addOrReplaceChild("g", CubeListBuilder.create()
                .texOffs(61, 277).addBox(-3.25F, -12.0741F, -3.2352F, 6.5F, 24.1481F, 6.4705F)
                .texOffs(388, 242).addBox(-3.25F, -3.2352F, -12.0741F, 6.5F, 6.4705F, 24.1481F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p87.addOrReplaceChild("g_1", CubeListBuilder.create()
                .texOffs(87, 277).addBox(-3.17F, -12.0741F, -3.2352F, 6.34F, 24.1481F, 6.4705F)
                .texOffs(450, 242).addBox(-3.17F, -3.2352F, -12.0741F, 6.34F, 6.4705F, 24.1481F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, -0.5236F, 0F, 0F));
        p87.addOrReplaceChild("g_2", CubeListBuilder.create()
                .texOffs(113, 277).addBox(-3.09F, -12.0741F, -3.2352F, 6.18F, 24.1481F, 6.4705F)
                .texOffs(0, 277).addBox(-3.09F, -3.2352F, -12.0741F, 6.18F, 6.4705F, 24.1481F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, -1.0472F, 0F, 0F));
        p87.addOrReplaceChild("g_3", CubeListBuilder.create()
                .texOffs(431, 308).addBox(-3.75F, -9.1763F, -2.4588F, 1F, 18.3526F, 4.9176F, new CubeDeformation(0.04F))
                .texOffs(314, 308).addBox(-3.75F, -2.4588F, -9.1763F, 1F, 4.9176F, 18.3526F, new CubeDeformation(0.04F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p87.addOrReplaceChild("g_4", CubeListBuilder.create()
                .texOffs(443, 308).addBox(-3.67F, -9.1763F, -2.4588F, 0.84F, 18.3526F, 4.9176F, new CubeDeformation(0.04F))
                .texOffs(353, 308).addBox(-3.67F, -2.4588F, -9.1763F, 0.84F, 4.9176F, 18.3526F, new CubeDeformation(0.04F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, -0.5236F, 0F, 0F));
        p87.addOrReplaceChild("g_5", CubeListBuilder.create()
                .texOffs(455, 308).addBox(-3.59F, -9.1763F, -2.4588F, 0.68F, 18.3526F, 4.9176F, new CubeDeformation(0.04F))
                .texOffs(392, 308).addBox(-3.59F, -2.4588F, -9.1763F, 0.68F, 4.9176F, 18.3526F, new CubeDeformation(0.04F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, -1.0472F, 0F, 0F));
    }


    public void setupAnim(ArmoredCarEntity car, float partialTick) {
        setupAnim(car, partialTick, null);
    }

    /** gunnerView: the local gunner looking through the sight; the gun follows their view exactly and sits low. */
    public void setupAnim(ArmoredCarEntity car, float partialTick, net.minecraft.world.entity.player.Player gunnerView) {
        float wheel = Mth.lerp(partialTick, car.wheelRotO, car.wheelRot);
        for (ModelPart w : this.spin) {
            w.xRot = wheel;
        }
        // + steer = turning left; model yRot + = towards the vehicle's right
        float steer = -Mth.DEG_TO_RAD * Mth.lerp(partialTick, car.steerVisO, car.steerVis);
        this.steerL.yRot = steer;
        this.steerR.yRot = steer;
        this.steerR.visible = true;

        float yaw;
        float pitch;
        if (car.isWrecked()) {
            yaw = Mth.DEG_TO_RAD * (car.turretYaw + 25.0F + (car.getId() % 5) * 6.0F);
            pitch = 0.3F;
            this.steerR.visible = false;          // front right wheel blown off
        } else if (gunnerView != null) {
            float carYaw = Mth.rotLerp(partialTick, car.yRotO, car.getYRot());
            yaw = Mth.DEG_TO_RAD * Mth.wrapDegrees(gunnerView.getViewYRot(partialTick) - carYaw);
            pitch = Mth.DEG_TO_RAD * gunnerView.getViewXRot(partialTick);
        } else {
            yaw = Mth.DEG_TO_RAD * Mth.lerp(partialTick, car.turretYawO, car.turretYaw);
            pitch = Mth.DEG_TO_RAD * Mth.lerp(partialTick, car.gunPitchO, car.gunPitch);
        }
        this.turret.yRot = yaw;
        this.gun.xRot = pitch;
        this.gun.y = gunnerView != null ? GUNNER_GUN_Y : this.gunBaseY;
        this.gun.z = gunnerView != null ? GUNNER_GUN_Z : this.gunBaseZ;
        this.recoil.z = 2.0F * Mth.lerp(partialTick, car.recoilO, car.recoil);   // slides back on firing
    }

    /** Main pass. interiorView hides the upper hull and the whole cupola (drum, ports, gun) so the crew can see out. */
    public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay, boolean interiorView) {
        render(poseStack, consumer, light, overlay, interiorView, false);
    }

    /** gunnerView: like interiorView, but the MG itself stays visible (it shows at the bottom of the sight). */
    public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay, boolean interiorView,
                       boolean gunnerView) {
        for (ModelPart p : this.hidden) {
            p.visible = !interiorView;
        }
        this.turret.visible = !interiorView || gunnerView;     // hides every child too
        if (gunnerView) {
            this.turret.skipDraw = true;
            this.turret.getAllParts().forEach(p -> {
                if (p != this.turret && p != this.gun && p != this.recoil && p != this.flash && !isUnder(p, this.gun)) {
                    p.visible = false;
                }
            });
        }
        this.flash.visible = false;
        this.root.render(poseStack, consumer, light, overlay);
        this.turret.visible = true;
        this.turret.skipDraw = false;
        this.turret.getAllParts().forEach(p -> p.visible = true);
        this.flash.visible = false;
    }

    /** Muzzle position in the space of the given pose (call with the model's render pose). */
    public org.joml.Vector3f muzzlePoint(PoseStack poseStack) {
        poseStack.pushPose();
        this.turret.translateAndRotate(poseStack);
        this.gun.translateAndRotate(poseStack);
        this.recoil.translateAndRotate(poseStack);
        this.flash.translateAndRotate(poseStack);
        org.joml.Vector3f v = poseStack.last().pose().transformPosition(new org.joml.Vector3f(0.0F, 0.0F, 0.1F / 16.0F));
        poseStack.popPose();
        return v;
    }

    private boolean isUnder(ModelPart part, ModelPart parent) {
        return parent.getAllParts().anyMatch(p -> p == part);
    }

    /** Second pass drawing only the muzzle flash (full-bright). */
    public void renderFlash(PoseStack poseStack, VertexConsumer consumer, int overlay) {
        for (ModelPart p : this.allParts) {
            p.skipDraw = true;
        }
        for (ModelPart p : this.hidden) {
            p.visible = false;
        }
        this.flash.visible = true;
        this.flash.skipDraw = false;
        this.root.render(poseStack, consumer, 0xF000F0, overlay);
        for (ModelPart p : this.allParts) {
            p.skipDraw = false;
        }
        this.flash.visible = false;
    }
}
