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

    private final ModelPart root, prop, propb, pilot, wtopl, wtopr, wmidl, wmidr, wlowl, wlowr, strutl, strutr, rudder, wheelr;

    public FokkerModel(ModelPart root) {
        this.root = root;
        this.prop = root.getChild("prop");
        this.pilot = root.getChild("pilot");
        this.propb = root.getChild("propb");
        this.wtopl = root.getChild("wtopl");
        this.wtopr = root.getChild("wtopr");
        this.wmidl = root.getChild("wmidl");
        this.wmidr = root.getChild("wmidr");
        this.wlowl = root.getChild("wlowl");
        this.wlowr = root.getChild("wlowr");
        this.strutl = root.getChild("strutl");
        this.strutr = root.getChild("strutr");
        this.rudder = root.getChild("rudder");
        this.wheelr = root.getChild("wheelr");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        build_fuselage(root);
        build_prop(root);
        build_propb(root);
        build_wtopl(root);
        build_wtopr(root);
        build_wmidl(root);
        build_wmidr(root);
        build_wlowl(root);
        build_wlowr(root);
        build_strutl(root);
        build_strutr(root);
        build_legf_l(root);
        build_legr_l(root);
        build_legf_r(root);
        build_legr_r(root);
        build_wheell(root);
        build_wheelr(root);
        build_rudder(root);
        build_skid(root);
        build_gunl(root);
        build_gunr(root);
        build_pilot(root);
        return LayerDefinition.create(mesh, 256, 256);
    }

    private static void build_fuselage(PartDefinition root) {
        root.addOrReplaceChild("fuselage", CubeListBuilder.create()
                .texOffs(16, 0).addBox(-6F, -29F, -20F, 12F, 14F, 16F)
                .texOffs(78, 37).addBox(-6F, -18F, -4F, 12F, 3F, 18F)
                .texOffs(128, 0).addBox(5F, -29F, -4F, 1F, 11F, 18F)
                .texOffs(128, 0).addBox(-6F, -29F, -4F, 1F, 11F, 18F)
                .texOffs(176, 130).addBox(-6F, -29F, -4F, 12F, 3F, 6F)
                .texOffs(14, 140).addBox(-6F, -29.5F, 14F, 12F, 2F, 4F)
                .texOffs(72, 0).addBox(-6F, -29F, 14F, 12F, 13F, 16F, new CubeDeformation(-0.4F, 0F, 0F))
                .texOffs(166, 0).addBox(-4.5F, -28.45F, 30F, 9F, 11F, 14F, new CubeDeformation(-0.1F, -0.05F, 0F))
                .texOffs(138, 37).addBox(-3.5F, -28.05F, 44F, 7F, 9F, 12F, new CubeDeformation(-0.3F, -0.45F, 0F))
                .texOffs(0, 114).addBox(-2F, -27.05F, 56F, 4F, 6F, 10F, new CubeDeformation(0F, -0.45F, 0F))
                .texOffs(0, 140).addBox(-1.5F, -26F, 66F, 3F, 3F, 4F, new CubeDeformation(-0.3F, 0F, 0F))
                .texOffs(178, 140).addBox(-6.5F, -29.9F, 1.9F, 13F, 1F, 1F, new CubeDeformation(-0.3F, -0.1F, -0.1F))
                .texOffs(178, 140).addBox(-6.5F, -29.9F, 13.1F, 13F, 1F, 1F, new CubeDeformation(-0.3F, -0.1F, -0.1F))
                .texOffs(102, 114).addBox(5.2F, -29.9F, 2.5F, 1F, 1F, 11F, new CubeDeformation(0F, -0.1F, -0.3F))
                .texOffs(102, 114).addBox(-6.2F, -29.9F, 2.5F, 1F, 1F, 11F, new CubeDeformation(0F, -0.1F, -0.3F))
                .texOffs(0, 37).addBox(-7.5F, -29.5F, -27F, 15F, 15F, 7F)
                .texOffs(192, 96).addBox(-8.5F, -27.5F, -27F, 17F, 11F, 7F)
                .texOffs(212, 0).addBox(-6F, -30.5F, -27F, 12F, 17F, 7F)
                .texOffs(126, 114).addBox(-5.5F, -27.5F, -27.4F, 11F, 11F, 1F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(46, 140).addBox(-1.5F, -23.5F, -29.75F, 3F, 3F, 3F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(0, 78).addBox(-20F, -45.7F, -17F, 40F, 2F, 16F, new CubeDeformation(0F, -0.3F, 0F))
                .texOffs(202, 37).addBox(5.5F, -28.2F, -14F, 2F, 3F, 16F, new CubeDeformation(-0.1F, 0F, 0F))
                .texOffs(202, 37).addBox(-7.5F, -28.2F, -14F, 2F, 3F, 16F, new CubeDeformation(-0.1F, 0F, 0F))
                .texOffs(112, 78).addBox(-20F, -15.2F, -15F, 40F, 2F, 16F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(240, 96).addBox(4.4F, -44F, -13.6F, 1F, 15F, 2F, new CubeDeformation(-0.1F, 0F, -0.4F))
                .texOffs(240, 96).addBox(4.4F, -44F, -4.1F, 1F, 15F, 2F, new CubeDeformation(-0.1F, 0F, -0.4F))
                .texOffs(240, 96).addBox(-5.4F, -44F, -13.6F, 1F, 15F, 2F, new CubeDeformation(-0.1F, 0F, -0.4F))
                .texOffs(240, 96).addBox(-5.4F, -44F, -4.1F, 1F, 15F, 2F, new CubeDeformation(-0.1F, 0F, -0.4F))
                .texOffs(0, 130).addBox(-16F, -7F, -13F, 32F, 3F, 7F)
                .texOffs(96, 130).addBox(-16F, -25.6F, 58F, 32F, 1F, 8F)
                .texOffs(58, 140).addBox(-11F, -25.6F, 66F, 22F, 1F, 4F)
                .texOffs(128, 140).addBox(-1F, -29.95F, -1.5F, 2F, 1F, 3F, new CubeDeformation(-0.1F, -0.05F, 0F))
                , PartPose.offsetAndRotation(0F, 24F, 0F, 0F, 0F, 0F));
    }

    private static void build_prop(PartDefinition root) {
        root.addOrReplaceChild("prop", CubeListBuilder.create()
                .texOffs(110, 140).addBox(-1.5F, -1.5F, -1F, 3F, 3F, 2F)
                .texOffs(68, 37).addBox(-1.5F, -21.25F, -1F, 3F, 20F, 2F, new CubeDeformation(-0.2F, -0.25F, -0.4F))
                , PartPose.offsetAndRotation(0F, 2F, -30F, 0F, 0F, 0F));
    }

    private static void build_propb(PartDefinition root) {
        root.addOrReplaceChild("propb", CubeListBuilder.create()
                .texOffs(68, 37).addBox(-1.5F, 1.25F, -1F, 3F, 20F, 2F, new CubeDeformation(-0.2F, -0.25F, -0.4F))
                , PartPose.offsetAndRotation(0F, 2F, -30F, 0F, 0F, 0F));
    }

    private static void build_wtopl(PartDefinition root) {
        root.addOrReplaceChild("wtopl", CubeListBuilder.create()
                .texOffs(0, 96).addBox(0F, -1F, -8F, 38F, 2F, 16F, new CubeDeformation(0F, -0.3F, 0F))
                , PartPose.offsetAndRotation(20F, -20.7F, -9F, 0F, 0F, 0F));
    }

    private static void build_wtopr(PartDefinition root) {
        root.addOrReplaceChild("wtopr", CubeListBuilder.create()
                .texOffs(0, 96).addBox(-38F, -1F, -8F, 38F, 2F, 16F, new CubeDeformation(0F, -0.3F, 0F))
                , PartPose.offsetAndRotation(-20F, -20.7F, -9F, 0F, 0F, 0F));
    }

    private static void build_wmidl(PartDefinition root) {
        root.addOrReplaceChild("wmidl", CubeListBuilder.create()
                .texOffs(50, 59).addBox(0F, -1F, -8F, 44F, 2F, 16F, new CubeDeformation(0F, -0.4F, 0F))
                , PartPose.offsetAndRotation(6F, -2.6F, -6F, 0F, 0F, 0F));
    }

    private static void build_wmidr(PartDefinition root) {
        root.addOrReplaceChild("wmidr", CubeListBuilder.create()
                .texOffs(50, 59).addBox(-44F, -1F, -8F, 44F, 2F, 16F, new CubeDeformation(0F, -0.4F, 0F))
                , PartPose.offsetAndRotation(-6F, -2.6F, -6F, 0F, 0F, 0F));
    }

    private static void build_wlowl(PartDefinition root) {
        root.addOrReplaceChild("wlowl", CubeListBuilder.create()
                .texOffs(108, 96).addBox(0F, -1F, -8F, 26F, 2F, 16F, new CubeDeformation(0F, -0.4F, 0F))
                , PartPose.offsetAndRotation(20F, 9.8F, -7F, 0F, 0F, 0F));
    }

    private static void build_wlowr(PartDefinition root) {
        root.addOrReplaceChild("wlowr", CubeListBuilder.create()
                .texOffs(108, 96).addBox(-26F, -1F, -8F, 26F, 2F, 16F, new CubeDeformation(0F, -0.4F, 0F))
                , PartPose.offsetAndRotation(-20F, 9.8F, -7F, 0F, 0F, 0F));
    }

    private static void build_strutl(PartDefinition root) {
        root.addOrReplaceChild("strutl", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-0.5F, -29.6F, -3.5F, 1F, 30F, 7F, new CubeDeformation(0F, -0.4F, 0F))
                , PartPose.offsetAndRotation(38F, 9.2F, -7.5F, 0F, 0F, 0F));
    }

    private static void build_strutr(PartDefinition root) {
        root.addOrReplaceChild("strutr", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-0.5F, -29.6F, -3.5F, 1F, 30F, 7F, new CubeDeformation(0F, -0.4F, 0F))
                , PartPose.offsetAndRotation(-38F, 9.2F, -7.5F, 0F, 0F, 0F));
    }

    private static void build_legf_l(PartDefinition root) {
        root.addOrReplaceChild("legf_l", CubeListBuilder.create()
                .texOffs(46, 114).addBox(7F, -0.5F, -12.5519F, 1F, 1F, 13F, new CubeDeformation(0F, 0F, -0.4481F))
                , PartPose.offsetAndRotation(0F, 9F, -17F, 2.2391F, 0F, 0F));
    }

    private static void build_legr_l(PartDefinition root) {
        root.addOrReplaceChild("legr_l", CubeListBuilder.create()
                .texOffs(46, 114).addBox(7F, -0.5F, -12.8738F, 1F, 1F, 13F, new CubeDeformation(0F, 0F, -0.1262F))
                , PartPose.offsetAndRotation(0F, 9F, -1F, 0.8409F, 0F, 0F));
    }

    private static void build_legf_r(PartDefinition root) {
        root.addOrReplaceChild("legf_r", CubeListBuilder.create()
                .texOffs(46, 114).addBox(-8F, -0.5F, -12.5519F, 1F, 1F, 13F, new CubeDeformation(0F, 0F, -0.4481F))
                , PartPose.offsetAndRotation(0F, 9F, -17F, 2.2391F, 0F, 0F));
    }

    private static void build_legr_r(PartDefinition root) {
        root.addOrReplaceChild("legr_r", CubeListBuilder.create()
                .texOffs(46, 114).addBox(-8F, -0.5F, -12.8738F, 1F, 1F, 13F, new CubeDeformation(0F, 0F, -0.1262F))
                , PartPose.offsetAndRotation(0F, 9F, -1F, 0.8409F, 0F, 0F));
    }

    private static void build_wheell(PartDefinition root) {
        root.addOrReplaceChild("wheell", CubeListBuilder.create()
                .texOffs(30, 59).addBox(-1.5F, -6F, -3.5F, 3F, 12F, 7F, new CubeDeformation(-0.3F, -0.4F, -0.1F))
                .texOffs(0, 59).addBox(-1.5F, -3.5F, -6F, 3F, 7F, 12F, new CubeDeformation(-0.3F, -0.1F, -0.4F))
                .texOffs(176, 37).addBox(-1.5F, -5F, -5F, 3F, 10F, 10F, new CubeDeformation(-0.3F, -0.2F, -0.2F))
                .texOffs(28, 114).addBox(0.85F, -4F, -4F, 1F, 8F, 8F, new CubeDeformation(-0.4F, -0.4F, -0.4F))
                , PartPose.offsetAndRotation(14F, 18.4F, -9.5F, 0F, 0F, 0F));
    }

    private static void build_wheelr(PartDefinition root) {
        root.addOrReplaceChild("wheelr", CubeListBuilder.create()
                .texOffs(30, 59).addBox(-1.5F, -6F, -3.5F, 3F, 12F, 7F, new CubeDeformation(-0.3F, -0.4F, -0.1F))
                .texOffs(0, 59).addBox(-1.5F, -3.5F, -6F, 3F, 7F, 12F, new CubeDeformation(-0.3F, -0.1F, -0.4F))
                .texOffs(176, 37).addBox(-1.5F, -5F, -5F, 3F, 10F, 10F, new CubeDeformation(-0.3F, -0.2F, -0.2F))
                .texOffs(28, 114).addBox(-1.85F, -4F, -4F, 1F, 8F, 8F, new CubeDeformation(-0.4F, -0.4F, -0.4F))
                , PartPose.offsetAndRotation(-14F, 18.4F, -9.5F, 0F, 0F, 0F));
    }

    private static void build_rudder(PartDefinition root) {
        root.addOrReplaceChild("rudder", CubeListBuilder.create()
                .texOffs(44, 37).addBox(-1F, -12F, -4F, 2F, 12F, 10F, new CubeDeformation(-0.4F, 0F, 0F))
                .texOffs(198, 114).addBox(-1F, -13.75F, -2.25F, 2F, 4F, 7F, new CubeDeformation(-0.4F, -0.25F, -0.25F))
                .texOffs(228, 130).addBox(-1F, 0F, 4F, 2F, 5F, 3F, new CubeDeformation(-0.4F, 0F, 0F))
                , PartPose.offsetAndRotation(0F, -2F, 66F, 0F, 0F, 0F));
    }

    private static void build_skid(PartDefinition root) {
        root.addOrReplaceChild("skid", CubeListBuilder.create()
                .texOffs(238, 130).addBox(-1F, -0.5F, -5.8284F, 2F, 1F, 6F, new CubeDeformation(-0.4F, 0F, -0.1716F))
                , PartPose.offsetAndRotation(0F, 3F, 60F, 2.3562F, 0F, 0F));
    }

    private static void build_gunl(PartDefinition root) {
        root.addOrReplaceChild("gunl", CubeListBuilder.create()
                .texOffs(78, 130).addBox(1F, -8.6F, -4F, 2F, 3F, 7F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(238, 130).addBox(1F, -8.85F, -3.25F, 2F, 1F, 6F, new CubeDeformation(-0.15F, -0.35F, -0.25F))
                .texOffs(212, 140).addBox(1.5F, -9.35F, -3.05F, 1F, 1F, 1F, new CubeDeformation(-0.35F, -0.15F, -0.35F))
                .texOffs(176, 114).addBox(1F, -8.1F, -13F, 2F, 2F, 9F, new CubeDeformation(-0.1F, -0.45F, 0F))
                .texOffs(176, 114).addBox(1F, -8.1F, -13F, 2F, 2F, 9F, new CubeDeformation(-0.45F, -0.1F, 0F))
                .texOffs(176, 114).addBox(1F, -8.1F, -13F, 2F, 2F, 9F, new CubeDeformation(-0.25F, -0.25F, 0F))
                .texOffs(160, 140).addBox(1F, -8.1F, -4.8F, 2F, 2F, 1F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(160, 140).addBox(1F, -8.1F, -13.7F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.3F))
                .texOffs(166, 140).addBox(1.5F, -7.6F, -15.1F, 1F, 1F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.3F))
                .texOffs(212, 140).addBox(1.5F, -6F, -11.4F, 1F, 1F, 1F, new CubeDeformation(-0.2F, 0F, -0.1F))
                .texOffs(120, 140).addBox(0.225F, -8.25F, -3F, 1F, 2F, 3F, new CubeDeformation(-0.225F, -0.35F, -0.4F))
                .texOffs(212, 140).addBox(0.225F, -7.75F, -3.25F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.15F, -0.35F))
                .texOffs(172, 140).addBox(1.5F, -6.275F, -4F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.275F, -0.3F))
                .texOffs(160, 140).addBox(1F, -8.1F, 2.725F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.2F, -0.275F))
                .texOffs(206, 140).addBox(1F, -8.825F, -3.85F, 2F, 1F, 1F, new CubeDeformation(-0.05F, -0.375F, -0.15F))
                .texOffs(212, 130).addBox(2.56F, -7.525F, -4F, 1F, 1F, 7F, new CubeDeformation(-0.44F, -0.325F, -0.4F))
                .texOffs(212, 130).addBox(2.56F, -6.925F, -4F, 1F, 1F, 7F, new CubeDeformation(-0.44F, -0.375F, -0.4F))
                .texOffs(212, 140).addBox(1.5F, -8.8F, -12.675F, 1F, 1F, 1F, new CubeDeformation(-0.4F, -0.2F, -0.325F))
                .texOffs(138, 140).addBox(1F, -8.6F, -11.2F, 2F, 3F, 1F, new CubeDeformation(-0.05F, -0.45F, -0.2F))
                .texOffs(172, 140).addBox(1.5F, -6F, -1F, 1F, 1F, 2F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
    }

    private static void build_gunr(PartDefinition root) {
        root.addOrReplaceChild("gunr", CubeListBuilder.create()
                .texOffs(78, 130).addBox(-3F, -8.6F, -4F, 2F, 3F, 7F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(238, 130).addBox(-3F, -8.85F, -3.25F, 2F, 1F, 6F, new CubeDeformation(-0.15F, -0.35F, -0.25F))
                .texOffs(212, 140).addBox(-2.5F, -9.35F, -3.05F, 1F, 1F, 1F, new CubeDeformation(-0.35F, -0.15F, -0.35F))
                .texOffs(176, 114).addBox(-3F, -8.1F, -13F, 2F, 2F, 9F, new CubeDeformation(-0.1F, -0.45F, 0F))
                .texOffs(176, 114).addBox(-3F, -8.1F, -13F, 2F, 2F, 9F, new CubeDeformation(-0.45F, -0.1F, 0F))
                .texOffs(176, 114).addBox(-3F, -8.1F, -13F, 2F, 2F, 9F, new CubeDeformation(-0.25F, -0.25F, 0F))
                .texOffs(160, 140).addBox(-3F, -8.1F, -4.8F, 2F, 2F, 1F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(160, 140).addBox(-3F, -8.1F, -13.7F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.3F))
                .texOffs(166, 140).addBox(-2.5F, -7.6F, -15.1F, 1F, 1F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.3F))
                .texOffs(212, 140).addBox(-2.5F, -6F, -11.4F, 1F, 1F, 1F, new CubeDeformation(-0.2F, 0F, -0.1F))
                .texOffs(120, 140).addBox(-1.225F, -8.25F, -3F, 1F, 2F, 3F, new CubeDeformation(-0.225F, -0.35F, -0.4F))
                .texOffs(212, 140).addBox(-1.225F, -7.75F, -3.25F, 1F, 1F, 1F, new CubeDeformation(-0.225F, -0.15F, -0.35F))
                .texOffs(172, 140).addBox(-2.5F, -6.275F, -4F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.275F, -0.3F))
                .texOffs(160, 140).addBox(-3F, -8.1F, 2.725F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.2F, -0.275F))
                .texOffs(206, 140).addBox(-3F, -8.825F, -3.85F, 2F, 1F, 1F, new CubeDeformation(-0.05F, -0.375F, -0.15F))
                .texOffs(212, 130).addBox(-3.56F, -7.525F, -4F, 1F, 1F, 7F, new CubeDeformation(-0.44F, -0.325F, -0.4F))
                .texOffs(212, 130).addBox(-3.56F, -6.925F, -4F, 1F, 1F, 7F, new CubeDeformation(-0.44F, -0.375F, -0.4F))
                .texOffs(212, 140).addBox(-2.5F, -8.8F, -12.675F, 1F, 1F, 1F, new CubeDeformation(-0.4F, -0.2F, -0.325F))
                .texOffs(138, 140).addBox(-3F, -8.6F, -11.2F, 2F, 3F, 1F, new CubeDeformation(-0.05F, -0.45F, -0.2F))
                .texOffs(172, 140).addBox(-2.5F, -6F, -1F, 1F, 1F, 2F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
    }

    private static void build_pilot(PartDefinition root) {
        root.addOrReplaceChild("pilot", CubeListBuilder.create()
                .texOffs(150, 114).addBox(-4.5F, -7F, 8F, 9F, 7F, 4F)
                .texOffs(74, 114).addBox(-3.5F, -13.9F, 4.7F, 7F, 7F, 7F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(144, 140).addBox(-3.5F, -10.9F, 4.4F, 7F, 2F, 1F, new CubeDeformation(0F, -0.3F, -0.2F))
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
    }

    public void setupAnim(float propAngle, boolean showPilot) {
        setupAnim(propAngle, showPilot, false);
    }

    /** {@code wrecked}: top-right panel torn away, top-left snapped and hanging, the rest bent; one blade gone. */
    public void setupAnim(float propAngle, boolean showPilot, boolean wrecked) {
        this.root.getAllParts().forEach(ModelPart::resetPose);
        this.prop.zRot = wrecked ? 0.6F : propAngle;
        this.propb.zRot = this.prop.zRot;
        this.pilot.visible = showPilot;
        this.propb.visible = !wrecked;
        this.wtopr.visible = !wrecked;
        this.strutl.visible = !wrecked;
        this.strutr.visible = !wrecked;
        if (wrecked) {
            bend(this.wtopl, 0.15F, 0.0F, 0.62F);
            bend(this.wmidl, 0.0F, 0.12F, 0.14F);
            bend(this.wmidr, 0.0F, 0.22F, -0.2F);
            bend(this.wlowl, 0.0F, 0.0F, -0.18F);
            bend(this.wlowr, 0.0F, -0.1F, 0.22F);
            bend(this.rudder, 0.0F, 0.45F, 0.0F);
            bend(this.wheelr, 0.0F, 0.0F, -0.5F);
        }
    }

    private static void bend(ModelPart p, float x, float y, float z) {
        p.xRot += x;
        p.yRot += y;
        p.zRot += z;
    }

    public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay) {
        this.root.render(poseStack, consumer, light, overlay);
    }
}
