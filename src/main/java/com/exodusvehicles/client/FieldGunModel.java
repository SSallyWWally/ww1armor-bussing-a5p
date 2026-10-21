package com.exodusvehicles.client;

import com.exodusvehicles.ArmoredCarMod;
import com.exodusvehicles.entity.FieldGunEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/** 7.7 cm FK 96 n.A. - GENERATED from truck/fk96_geom.py + build_fk96.py. Do not edit the geometry by hand. */
public class FieldGunModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ArmoredCarMod.id("fk96"), "main");

    private final ModelPart root, mount, cradle, barrel, scope;
    private final ModelPart[] wheels;

    public FieldGunModel(ModelPart root) {
        this.root = root;
        this.mount = root.getChild("mount");
        this.cradle = this.mount.getChild("cradle");
        this.barrel = this.cradle.getChild("barrel");
        this.scope = this.cradle.getChild("scope");
        this.wheels = new ModelPart[]{root.getChild("wheell"), root.getChild("wheelr")};
        String[] names = {"wheell", "wheelr"};
        for (int i = 0; i < 2; i++) {
            ModelPart disc = this.wheels[i].getChild(names[i] + "_spk");     // drawn at double size for texture detail
            disc.xScale = 0.5F;
            disc.yScale = 0.5F;
            disc.zScale = 0.5F;
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        build_body(root);
        build_trail_l(root);
        build_trail_r(root);
        build_mount(root);
        build_wheell(root);
        build_wheelr(root);
        return LayerDefinition.create(mesh, 256, 256);
    }

    private static void build_body(PartDefinition root) {
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(124, 90).addBox(-11.5F, -10.8F, -0.8F, 23F, 1.6F, 1.6F)
                .texOffs(98, 68).addBox(2.5F, -15F, -5F, 2F, 7F, 8F)
                .texOffs(98, 68).addBox(-4.5F, -15F, -5F, 2F, 7F, 8F)
                .texOffs(174, 90).addBox(-3F, -7.9F, 12.5F, 6F, 1.4F, 1.5F)
                .texOffs(174, 90).addBox(-3F, -5.7F, 26.5F, 6F, 1.4F, 1.5F)
                .texOffs(38, 90).addBox(-4.5F, -3F, 44F, 9F, 3F, 4F)
                .texOffs(158, 68).addBox(-0.8F, -13F, 45.5F, 1.6F, 10F, 1.5F)
                .texOffs(210, 90).addBox(-4F, -13.4F, 45.5F, 8F, 0.8F, 1.5F)
                .texOffs(118, 68).addBox(2.2F, -25F, -4.8F, 8.8F, 14F, 0.6F)
                .texOffs(118, 68).addBox(-11F, -25F, -4.8F, 8.8F, 14F, 0.6F)
                .texOffs(190, 90).addBox(-2.2F, -13.5F, -4.8F, 4.4F, 2.5F, 0.6F)
                .texOffs(92, 90).addBox(-2.2F, -25F, -4.8F, 4.4F, 4.5F, 0.6F)
                .texOffs(0, 90).addBox(-9F, -9.5F, -4.6F, 18F, 6F, 0.6F)
                .texOffs(88, 68).addBox(10.4F, -25F, -4.8F, 0.8F, 14F, 3.3F)
                .texOffs(88, 68).addBox(-11.2F, -25F, -4.8F, 0.8F, 14F, 3.3F)
                , PartPose.offsetAndRotation(0F, 24F, 0F, 0F, 0F, 0F));
    }

    private static void build_trail_l(PartDefinition root) {
        root.addOrReplaceChild("trail_l", CubeListBuilder.create()
                .texOffs(72, 0).addBox(1.6F, -0.9F, -41.5933F, 1.8F, 1.8F, 41.5933F)
                , PartPose.offsetAndRotation(0F, 15F, 3F, 2.9725F, 0F, 0F));
    }

    private static void build_trail_r(PartDefinition root) {
        root.addOrReplaceChild("trail_r", CubeListBuilder.create()
                .texOffs(72, 0).addBox(-3.4F, -0.9F, -41.5933F, 1.8F, 1.8F, 41.5933F)
                , PartPose.offsetAndRotation(0F, 15F, 3F, 2.9725F, 0F, 0F));
    }

    private static void build_mount(PartDefinition root) {
        PartDefinition p4 = root.addOrReplaceChild("mount", CubeListBuilder.create()
                .texOffs(138, 68).addBox(1.8F, -5F, -4F, 1F, 6F, 9F)
                .texOffs(138, 68).addBox(-2.8F, -5F, -4F, 1F, 6F, 9F)
                , PartPose.offsetAndRotation(0F, 11F, -1F, 0F, 0F, 0F));
        PartDefinition p5 = p4.addOrReplaceChild("cradle", CubeListBuilder.create()
                .texOffs(160, 0).addBox(-1.8F, 1.4F, -16F, 3.6F, 3.2F, 25F)
                .texOffs(194, 68).addBox(2F, -2F, -1F, 1.2F, 1.4F, 7F)
                .texOffs(230, 90).addBox(2.2F, -0.6F, 3F, 0.8F, 1.6F, 1F)
                .texOffs(166, 68).addBox(2.2F, -12.4F, 3F, 0.8F, 10.4F, 1F)
                , PartPose.offsetAndRotation(0F, -3F, 0F, 0F, 0F, 0F));
        p5.addOrReplaceChild("scope", CubeListBuilder.create()
                .texOffs(194, 68).addBox(1.8F, -14.4F, -0.5F, 1.6F, 2F, 6.5F)
                .texOffs(104, 90).addBox(2F, -15.6F, -0.5F, 1.2F, 1.2F, 2.5F)
                .texOffs(234, 90).addBox(2.1F, -14.2F, 6F, 1F, 1.6F, 1F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p5.addOrReplaceChild("barrel", CubeListBuilder.create()
                .texOffs(170, 68).addBox(-2.2F, -2.6F, 3F, 4.4F, 4F, 7F)
                .texOffs(46, 68).addBox(-1.6F, -2F, -14F, 3.2F, 2.8F, 17F)
                .texOffs(0, 68).addBox(-1.2F, -1.6F, -34F, 2.4F, 2F, 20F)
                .texOffs(114, 90).addBox(-1.4F, -1.8F, -35F, 2.8F, 2.4F, 2F)
                .texOffs(202, 90).addBox(-0.6F, -1.4F, 10F, 1.2F, 1.2F, 1.5F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
    }

    private static void build_wheell(PartDefinition root) {
        PartDefinition p8 = root.addOrReplaceChild("wheell", CubeListBuilder.create()
                .texOffs(212, 68).addBox(0.3F, -1.6F, -1.6F, 2.1F, 3.2F, 3.2F)
                , PartPose.offsetAndRotation(10.5F, 14F, 0F, 0F, 0F, 0F));
        p8.addOrReplaceChild("wheell_t0", CubeListBuilder.create()
                .texOffs(64, 90).addBox(-0.8F, -10F, -2.3824F, 1.6F, 1F, 4.7649F)
                .texOffs(78, 90).addBox(-0.7F, -9F, -2.3824F, 1.4F, 1F, 4.7649F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p8.addOrReplaceChild("wheell_t1", CubeListBuilder.create()
                .texOffs(64, 90).addBox(-0.8F, -10F, -2.3824F, 1.6F, 1F, 4.7649F)
                .texOffs(78, 90).addBox(-0.7F, -9F, -2.3824F, 1.4F, 1F, 4.7649F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0.4488F, 0F, 0F));
        p8.addOrReplaceChild("wheell_t2", CubeListBuilder.create()
                .texOffs(64, 90).addBox(-0.8F, -10F, -2.3824F, 1.6F, 1F, 4.7649F)
                .texOffs(78, 90).addBox(-0.7F, -9F, -2.3824F, 1.4F, 1F, 4.7649F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0.8976F, 0F, 0F));
        p8.addOrReplaceChild("wheell_t3", CubeListBuilder.create()
                .texOffs(64, 90).addBox(-0.8F, -10F, -2.3824F, 1.6F, 1F, 4.7649F)
                .texOffs(78, 90).addBox(-0.7F, -9F, -2.3824F, 1.4F, 1F, 4.7649F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 1.3464F, 0F, 0F));
        p8.addOrReplaceChild("wheell_t4", CubeListBuilder.create()
                .texOffs(64, 90).addBox(-0.8F, -10F, -2.3824F, 1.6F, 1F, 4.7649F)
                .texOffs(78, 90).addBox(-0.7F, -9F, -2.3824F, 1.4F, 1F, 4.7649F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 1.7952F, 0F, 0F));
        p8.addOrReplaceChild("wheell_t5", CubeListBuilder.create()
                .texOffs(64, 90).addBox(-0.8F, -10F, -2.3824F, 1.6F, 1F, 4.7649F)
                .texOffs(78, 90).addBox(-0.7F, -9F, -2.3824F, 1.4F, 1F, 4.7649F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 2.244F, 0F, 0F));
        p8.addOrReplaceChild("wheell_t6", CubeListBuilder.create()
                .texOffs(64, 90).addBox(-0.8F, -10F, -2.3824F, 1.6F, 1F, 4.7649F)
                .texOffs(78, 90).addBox(-0.7F, -9F, -2.3824F, 1.4F, 1F, 4.7649F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 2.6928F, 0F, 0F));
        p8.addOrReplaceChild("wheell_t7", CubeListBuilder.create()
                .texOffs(64, 90).addBox(-0.8F, -10F, -2.3824F, 1.6F, 1F, 4.7649F)
                .texOffs(78, 90).addBox(-0.7F, -9F, -2.3824F, 1.4F, 1F, 4.7649F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 3.1416F, 0F, 0F));
        p8.addOrReplaceChild("wheell_t8", CubeListBuilder.create()
                .texOffs(64, 90).addBox(-0.8F, -10F, -2.3824F, 1.6F, 1F, 4.7649F)
                .texOffs(78, 90).addBox(-0.7F, -9F, -2.3824F, 1.4F, 1F, 4.7649F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 3.5904F, 0F, 0F));
        p8.addOrReplaceChild("wheell_t9", CubeListBuilder.create()
                .texOffs(64, 90).addBox(-0.8F, -10F, -2.3824F, 1.6F, 1F, 4.7649F)
                .texOffs(78, 90).addBox(-0.7F, -9F, -2.3824F, 1.4F, 1F, 4.7649F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 4.0392F, 0F, 0F));
        p8.addOrReplaceChild("wheell_t10", CubeListBuilder.create()
                .texOffs(64, 90).addBox(-0.8F, -10F, -2.3824F, 1.6F, 1F, 4.7649F)
                .texOffs(78, 90).addBox(-0.7F, -9F, -2.3824F, 1.4F, 1F, 4.7649F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 4.488F, 0F, 0F));
        p8.addOrReplaceChild("wheell_t11", CubeListBuilder.create()
                .texOffs(64, 90).addBox(-0.8F, -10F, -2.3824F, 1.6F, 1F, 4.7649F)
                .texOffs(78, 90).addBox(-0.7F, -9F, -2.3824F, 1.4F, 1F, 4.7649F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 4.9368F, 0F, 0F));
        p8.addOrReplaceChild("wheell_t12", CubeListBuilder.create()
                .texOffs(64, 90).addBox(-0.8F, -10F, -2.3824F, 1.6F, 1F, 4.7649F)
                .texOffs(78, 90).addBox(-0.7F, -9F, -2.3824F, 1.4F, 1F, 4.7649F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 5.3856F, 0F, 0F));
        p8.addOrReplaceChild("wheell_t13", CubeListBuilder.create()
                .texOffs(64, 90).addBox(-0.8F, -10F, -2.3824F, 1.6F, 1F, 4.7649F)
                .texOffs(78, 90).addBox(-0.7F, -9F, -2.3824F, 1.4F, 1F, 4.7649F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 5.8344F, 0F, 0F));
        p8.addOrReplaceChild("wheell_spk", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-0.6F, -16.8F, -16.8F, 1.2F, 33.6F, 33.6F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
    }

    private static void build_wheelr(PartDefinition root) {
        PartDefinition p24 = root.addOrReplaceChild("wheelr", CubeListBuilder.create()
                .texOffs(212, 68).addBox(-2.4F, -1.6F, -1.6F, 2.1F, 3.2F, 3.2F)
                , PartPose.offsetAndRotation(-10.5F, 14F, 0F, 0F, 0F, 0F));
        p24.addOrReplaceChild("wheelr_t0", CubeListBuilder.create()
                .texOffs(64, 90).addBox(-0.8F, -10F, -2.3824F, 1.6F, 1F, 4.7649F)
                .texOffs(78, 90).addBox(-0.7F, -9F, -2.3824F, 1.4F, 1F, 4.7649F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p24.addOrReplaceChild("wheelr_t1", CubeListBuilder.create()
                .texOffs(64, 90).addBox(-0.8F, -10F, -2.3824F, 1.6F, 1F, 4.7649F)
                .texOffs(78, 90).addBox(-0.7F, -9F, -2.3824F, 1.4F, 1F, 4.7649F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0.4488F, 0F, 0F));
        p24.addOrReplaceChild("wheelr_t2", CubeListBuilder.create()
                .texOffs(64, 90).addBox(-0.8F, -10F, -2.3824F, 1.6F, 1F, 4.7649F)
                .texOffs(78, 90).addBox(-0.7F, -9F, -2.3824F, 1.4F, 1F, 4.7649F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0.8976F, 0F, 0F));
        p24.addOrReplaceChild("wheelr_t3", CubeListBuilder.create()
                .texOffs(64, 90).addBox(-0.8F, -10F, -2.3824F, 1.6F, 1F, 4.7649F)
                .texOffs(78, 90).addBox(-0.7F, -9F, -2.3824F, 1.4F, 1F, 4.7649F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 1.3464F, 0F, 0F));
        p24.addOrReplaceChild("wheelr_t4", CubeListBuilder.create()
                .texOffs(64, 90).addBox(-0.8F, -10F, -2.3824F, 1.6F, 1F, 4.7649F)
                .texOffs(78, 90).addBox(-0.7F, -9F, -2.3824F, 1.4F, 1F, 4.7649F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 1.7952F, 0F, 0F));
        p24.addOrReplaceChild("wheelr_t5", CubeListBuilder.create()
                .texOffs(64, 90).addBox(-0.8F, -10F, -2.3824F, 1.6F, 1F, 4.7649F)
                .texOffs(78, 90).addBox(-0.7F, -9F, -2.3824F, 1.4F, 1F, 4.7649F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 2.244F, 0F, 0F));
        p24.addOrReplaceChild("wheelr_t6", CubeListBuilder.create()
                .texOffs(64, 90).addBox(-0.8F, -10F, -2.3824F, 1.6F, 1F, 4.7649F)
                .texOffs(78, 90).addBox(-0.7F, -9F, -2.3824F, 1.4F, 1F, 4.7649F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 2.6928F, 0F, 0F));
        p24.addOrReplaceChild("wheelr_t7", CubeListBuilder.create()
                .texOffs(64, 90).addBox(-0.8F, -10F, -2.3824F, 1.6F, 1F, 4.7649F)
                .texOffs(78, 90).addBox(-0.7F, -9F, -2.3824F, 1.4F, 1F, 4.7649F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 3.1416F, 0F, 0F));
        p24.addOrReplaceChild("wheelr_t8", CubeListBuilder.create()
                .texOffs(64, 90).addBox(-0.8F, -10F, -2.3824F, 1.6F, 1F, 4.7649F)
                .texOffs(78, 90).addBox(-0.7F, -9F, -2.3824F, 1.4F, 1F, 4.7649F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 3.5904F, 0F, 0F));
        p24.addOrReplaceChild("wheelr_t9", CubeListBuilder.create()
                .texOffs(64, 90).addBox(-0.8F, -10F, -2.3824F, 1.6F, 1F, 4.7649F)
                .texOffs(78, 90).addBox(-0.7F, -9F, -2.3824F, 1.4F, 1F, 4.7649F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 4.0392F, 0F, 0F));
        p24.addOrReplaceChild("wheelr_t10", CubeListBuilder.create()
                .texOffs(64, 90).addBox(-0.8F, -10F, -2.3824F, 1.6F, 1F, 4.7649F)
                .texOffs(78, 90).addBox(-0.7F, -9F, -2.3824F, 1.4F, 1F, 4.7649F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 4.488F, 0F, 0F));
        p24.addOrReplaceChild("wheelr_t11", CubeListBuilder.create()
                .texOffs(64, 90).addBox(-0.8F, -10F, -2.3824F, 1.6F, 1F, 4.7649F)
                .texOffs(78, 90).addBox(-0.7F, -9F, -2.3824F, 1.4F, 1F, 4.7649F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 4.9368F, 0F, 0F));
        p24.addOrReplaceChild("wheelr_t12", CubeListBuilder.create()
                .texOffs(64, 90).addBox(-0.8F, -10F, -2.3824F, 1.6F, 1F, 4.7649F)
                .texOffs(78, 90).addBox(-0.7F, -9F, -2.3824F, 1.4F, 1F, 4.7649F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 5.3856F, 0F, 0F));
        p24.addOrReplaceChild("wheelr_t13", CubeListBuilder.create()
                .texOffs(64, 90).addBox(-0.8F, -10F, -2.3824F, 1.6F, 1F, 4.7649F)
                .texOffs(78, 90).addBox(-0.7F, -9F, -2.3824F, 1.4F, 1F, 4.7649F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 5.8344F, 0F, 0F));
        p24.addOrReplaceChild("wheelr_spk", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-0.6F, -16.8F, -16.8F, 1.2F, 33.6F, 33.6F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
    }


    public void setupAnim(FieldGunEntity gun, float partialTick, float traverse, float elevation, boolean lookingThroughSight) {
        this.scope.visible = !lookingThroughSight;     // the gunner's eye is at the eyepiece
        float wheel = Mth.lerp(partialTick, gun.wheelRotO, gun.wheelRot);
        for (ModelPart w : this.wheels) {
            w.xRot = wheel;
        }
        this.mount.yRot = Mth.DEG_TO_RAD * traverse;
        this.cradle.xRot = -Mth.DEG_TO_RAD * elevation;
        this.barrel.z = 7.0F * Mth.lerp(partialTick, gun.recoilO, gun.recoil);     // recoils along the cradle
    }

    public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay) {
        this.root.render(poseStack, consumer, light, overlay);
    }
}
