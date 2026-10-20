package com.exodusvehicles.client;

import com.exodusvehicles.ArmoredCarMod;
import com.exodusvehicles.entity.MorserEntity;
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

/** 21 cm Mörser 16 - GENERATED from truck/morser_geom.py + build_morser.py. Do not edit the geometry by hand. */
public class MorserModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ArmoredCarMod.id("morser16"), "main");

    private final ModelPart root, mount, cradle, barrel, breech, crank, round, spent;
    private final ModelPart[] wheels;

    public MorserModel(ModelPart root) {
        this.root = root;
        this.mount = root.getChild("mount");
        this.cradle = this.mount.getChild("cradle");
        this.barrel = this.cradle.getChild("barrel");
        this.breech = this.barrel.getChild("breechblock");
        this.crank = this.breech.getChild("crank");
        this.round = this.barrel.getChild("round");
        this.spent = this.barrel.getChild("spent");
        this.wheels = new ModelPart[]{root.getChild("wheell"), root.getChild("wheelr")};
        String[] names = {"wheell", "wheelr"};
        for (int i = 0; i < 2; i++) {
            ModelPart disc = this.wheels[i].getChild(names[i] + "_spk");
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
                .texOffs(12, 162).addBox(-13.5F, -13F, -1F, 27F, 2F, 2F)
                .texOffs(220, 84).addBox(5F, -24F, -8F, 2.5F, 14F, 14F)
                .texOffs(220, 84).addBox(-7.5F, -24F, -8F, 2.5F, 14F, 14F)
                .texOffs(128, 122).addBox(-7.5F, -11F, -8F, 15F, 2F, 14F)
                .texOffs(70, 162).addBox(-6F, -11F, 18F, 12F, 2F, 2F)
                .texOffs(70, 162).addBox(-6F, -8F, 34F, 12F, 2F, 2F)
                .texOffs(70, 162).addBox(-6F, -5.5F, 48F, 12F, 2F, 2F)
                .texOffs(74, 148).addBox(-7.5F, -4F, 57F, 15F, 4F, 5F)
                .texOffs(0, 162).addBox(-1.5F, -6F, 58F, 3F, 2F, 3F)
                .texOffs(28, 148).addBox(-1.2F, -14F, 59.5F, 2.4F, 10F, 1.5F)
                .texOffs(124, 162).addBox(-6F, -14.6F, 59.5F, 12F, 0.8F, 1.5F)
                .texOffs(238, 148).addBox(8F, -15F, 1F, 3.5F, 1F, 4F)
                .texOffs(238, 148).addBox(-11.5F, -15F, 1F, 3.5F, 1F, 4F)
                .texOffs(0, 148).addBox(-12F, -5F, 17F, 4.5F, 5F, 9F)
                , PartPose.offsetAndRotation(0F, 24F, 0F, 0F, 0F, 0F));
    }

    private static void build_trail_l(PartDefinition root) {
        root.addOrReplaceChild("trail_l", CubeListBuilder.create()
                .texOffs(88, 0).addBox(4.5F, -1.3F, -53.9351F, 2.5F, 2.6F, 53.9351F)
                , PartPose.offsetAndRotation(0F, 11F, 5F, 2.9551F, 0F, 0F));
    }

    private static void build_trail_r(PartDefinition root) {
        root.addOrReplaceChild("trail_r", CubeListBuilder.create()
                .texOffs(88, 0).addBox(-7F, -1.3F, -53.9351F, 2.5F, 2.6F, 53.9351F)
                , PartPose.offsetAndRotation(0F, 11F, 5F, 2.9551F, 0F, 0F));
    }

    private static void build_mount(PartDefinition root) {
        PartDefinition p4 = root.addOrReplaceChild("mount", CubeListBuilder.create()
                .texOffs(0, 122).addBox(3.6F, -10F, -6F, 1.4F, 12F, 14F)
                .texOffs(0, 122).addBox(-5F, -10F, -6F, 1.4F, 12F, 14F)
                .texOffs(186, 122).addBox(-5F, 0F, -6F, 10F, 2F, 14F)
                .texOffs(58, 148).addBox(5F, -6F, -2F, 1.5F, 4F, 6F)
                , PartPose.offsetAndRotation(0F, 4F, -2F, 0F, 0F, 0F));
        PartDefinition p5 = p4.addOrReplaceChild("cradle", CubeListBuilder.create()
                .texOffs(0, 84).addBox(1F, 3.2F, -20F, 2.4F, 3.3F, 34F)
                .texOffs(0, 84).addBox(-3.4F, 3.2F, -20F, 2.4F, 3.3F, 34F)
                .texOffs(74, 84).addBox(-3.6F, 3F, -20F, 7.2F, 1F, 34F)
                .texOffs(228, 148).addBox(3F, -1.2F, -1.2F, 1.6F, 2.4F, 2.4F)
                .texOffs(228, 148).addBox(-4.6F, -1.2F, -1.2F, 1.6F, 2.4F, 2.4F)
                , PartPose.offsetAndRotation(0F, -7F, 0F, 0F, 0F, 0F));
        PartDefinition p6 = p5.addOrReplaceChild("barrel", CubeListBuilder.create()
                .texOffs(80, 122).addBox(-4.6F, -5.4F, 1F, 9.2F, 8.4F, 14F)
                .texOffs(158, 84).addBox(-3.6F, -4.8F, -22F, 7.2F, 7.2F, 23F)
                .texOffs(32, 122).addBox(-3F, -4.2F, -40F, 6F, 6F, 18F)
                .texOffs(38, 148).addBox(-3.5F, -4.7F, -41F, 7F, 7F, 3F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p7 = p6.addOrReplaceChild("breechblock", CubeListBuilder.create()
                .texOffs(136, 148).addBox(0F, -3.4F, 0F, 6.8F, 6.8F, 1.2F)
                , PartPose.offsetAndRotation(-3.4F, -1.2F, 15F, 0F, 0F, 0F));
        p7.addOrReplaceChild("crank", CubeListBuilder.create()
                .texOffs(98, 162).addBox(-0.6F, -0.6F, 0F, 6.2F, 1.2F, 1.2F)
                .texOffs(116, 162).addBox(4.4F, -0.8F, 1.2F, 1.4F, 1.6F, 2F)
                , PartPose.offsetAndRotation(3.4F, 0F, 1.2F, 0F, 0F, 0F));
        p6.addOrReplaceChild("round", CubeListBuilder.create()
                .texOffs(154, 148).addBox(-1.5F, -1.5F, -6F, 3F, 3F, 6F)
                .texOffs(172, 148).addBox(-1.3F, -1.3F, -12F, 2.6F, 2.6F, 6F)
                .texOffs(116, 162).addBox(-0.8F, -0.8F, -14F, 1.6F, 1.6F, 2F)
                , PartPose.offsetAndRotation(0F, -1.2F, 15F, 0F, 0F, 0F));
        p6.addOrReplaceChild("spent", CubeListBuilder.create()
                .texOffs(154, 148).addBox(-1.5F, -1.5F, -6F, 3F, 3F, 6F)
                , PartPose.offsetAndRotation(0F, -1.2F, 15F, 0F, 0F, 0F));
    }

    private static void build_wheell(PartDefinition root) {
        PartDefinition p11 = root.addOrReplaceChild("wheell", CubeListBuilder.create()
                .texOffs(190, 148).addBox(0.3F, -2F, -2F, 2.7F, 4F, 4F)
                , PartPose.offsetAndRotation(13F, 12F, 0F, 0F, 0F, 0F));
        p11.addOrReplaceChild("wheell_t0", CubeListBuilder.create()
                .texOffs(114, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(204, 148).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p11.addOrReplaceChild("wheell_t1", CubeListBuilder.create()
                .texOffs(114, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(204, 148).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0.5236F, 0F, 0F));
        p11.addOrReplaceChild("wheell_t2", CubeListBuilder.create()
                .texOffs(114, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(204, 148).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 1.0472F, 0F, 0F));
        p11.addOrReplaceChild("wheell_t3", CubeListBuilder.create()
                .texOffs(114, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(204, 148).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 1.5708F, 0F, 0F));
        p11.addOrReplaceChild("wheell_t4", CubeListBuilder.create()
                .texOffs(114, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(204, 148).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 2.0944F, 0F, 0F));
        p11.addOrReplaceChild("wheell_t5", CubeListBuilder.create()
                .texOffs(114, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(204, 148).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 2.618F, 0F, 0F));
        p11.addOrReplaceChild("wheell_t6", CubeListBuilder.create()
                .texOffs(114, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(204, 148).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 3.1416F, 0F, 0F));
        p11.addOrReplaceChild("wheell_t7", CubeListBuilder.create()
                .texOffs(114, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(204, 148).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 3.6652F, 0F, 0F));
        p11.addOrReplaceChild("wheell_t8", CubeListBuilder.create()
                .texOffs(114, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(204, 148).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 4.1888F, 0F, 0F));
        p11.addOrReplaceChild("wheell_t9", CubeListBuilder.create()
                .texOffs(114, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(204, 148).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 4.7124F, 0F, 0F));
        p11.addOrReplaceChild("wheell_t10", CubeListBuilder.create()
                .texOffs(114, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(204, 148).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 5.236F, 0F, 0F));
        p11.addOrReplaceChild("wheell_t11", CubeListBuilder.create()
                .texOffs(114, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(204, 148).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 5.7596F, 0F, 0F));
        p11.addOrReplaceChild("wheell_spk", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-0.6F, -20.8F, -20.8F, 1.2F, 41.6F, 41.6F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
    }

    private static void build_wheelr(PartDefinition root) {
        PartDefinition p25 = root.addOrReplaceChild("wheelr", CubeListBuilder.create()
                .texOffs(190, 148).addBox(-3F, -2F, -2F, 2.7F, 4F, 4F)
                , PartPose.offsetAndRotation(-13F, 12F, 0F, 0F, 0F, 0F));
        p25.addOrReplaceChild("wheelr_t0", CubeListBuilder.create()
                .texOffs(114, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(204, 148).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p25.addOrReplaceChild("wheelr_t1", CubeListBuilder.create()
                .texOffs(114, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(204, 148).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0.5236F, 0F, 0F));
        p25.addOrReplaceChild("wheelr_t2", CubeListBuilder.create()
                .texOffs(114, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(204, 148).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 1.0472F, 0F, 0F));
        p25.addOrReplaceChild("wheelr_t3", CubeListBuilder.create()
                .texOffs(114, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(204, 148).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 1.5708F, 0F, 0F));
        p25.addOrReplaceChild("wheelr_t4", CubeListBuilder.create()
                .texOffs(114, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(204, 148).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 2.0944F, 0F, 0F));
        p25.addOrReplaceChild("wheelr_t5", CubeListBuilder.create()
                .texOffs(114, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(204, 148).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 2.618F, 0F, 0F));
        p25.addOrReplaceChild("wheelr_t6", CubeListBuilder.create()
                .texOffs(114, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(204, 148).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 3.1416F, 0F, 0F));
        p25.addOrReplaceChild("wheelr_t7", CubeListBuilder.create()
                .texOffs(114, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(204, 148).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 3.6652F, 0F, 0F));
        p25.addOrReplaceChild("wheelr_t8", CubeListBuilder.create()
                .texOffs(114, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(204, 148).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 4.1888F, 0F, 0F));
        p25.addOrReplaceChild("wheelr_t9", CubeListBuilder.create()
                .texOffs(114, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(204, 148).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 4.7124F, 0F, 0F));
        p25.addOrReplaceChild("wheelr_t10", CubeListBuilder.create()
                .texOffs(114, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(204, 148).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 5.236F, 0F, 0F));
        p25.addOrReplaceChild("wheelr_t11", CubeListBuilder.create()
                .texOffs(114, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(204, 148).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 5.7596F, 0F, 0F));
        p25.addOrReplaceChild("wheelr_spk", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-0.6F, -20.8F, -20.8F, 1.2F, 41.6F, 41.6F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
    }


    /** 0..1 progress of t between a and b, eased. */
    private static float ease(float t, float a, float b) {
        float x = Mth.clamp((t - a) / (b - a), 0.0F, 1.0F);
        return x * x * (3.0F - 2.0F * x);
    }

    private float roundZ = Float.NaN, roundY, spentZ, spentY;

    public void setupAnim(MorserEntity gun, float partialTick, float traverse, float elevation) {
        if (Float.isNaN(this.roundZ)) {
            this.roundZ = this.round.z;
            this.roundY = this.round.y;
            this.spentZ = this.spent.z;
            this.spentY = this.spent.y;
        }
        // no loading animation: breech shut, no round or case showing
        this.breech.yRot = 0.0F;
        this.crank.zRot = 0.0F;
        this.round.visible = false;
        this.spent.visible = false;
        float wheel = Mth.lerp(partialTick, gun.wheelRotO, gun.wheelRot);
        for (ModelPart w : this.wheels) {
            w.xRot = wheel;
        }
        this.mount.yRot = Mth.DEG_TO_RAD * traverse;
        this.cradle.xRot = -Mth.DEG_TO_RAD * elevation;
        this.barrel.z = 9.0F * Mth.lerp(partialTick, gun.recoilO, gun.recoil);     // runs back along the cradle
    }

    public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay) {
        this.root.render(poseStack, consumer, light, overlay);
    }
}
