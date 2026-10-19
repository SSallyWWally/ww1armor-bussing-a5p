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

    private final ModelPart root, mount, cradle, barrel;
    private final ModelPart[] wheels;

    public MorserModel(ModelPart root) {
        this.root = root;
        this.mount = root.getChild("mount");
        this.cradle = this.mount.getChild("cradle");
        this.barrel = this.cradle.getChild("barrel");
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
                .texOffs(62, 164).addBox(-13.5F, -13F, -1F, 27F, 2F, 2F)
                .texOffs(220, 84).addBox(5F, -24F, -8F, 2.5F, 14F, 14F)
                .texOffs(220, 84).addBox(-7.5F, -24F, -8F, 2.5F, 14F, 14F)
                .texOffs(154, 122).addBox(-7.5F, -11F, -8F, 15F, 2F, 14F)
                .texOffs(120, 164).addBox(-6F, -11F, 18F, 12F, 2F, 2F)
                .texOffs(120, 164).addBox(-6F, -8F, 34F, 12F, 2F, 2F)
                .texOffs(120, 164).addBox(-6F, -5.5F, 48F, 12F, 2F, 2F)
                .texOffs(140, 148).addBox(-7.5F, -4F, 57F, 15F, 4F, 5F)
                .texOffs(50, 164).addBox(-1.5F, -6F, 58F, 3F, 2F, 3F)
                .texOffs(76, 148).addBox(-1.2F, -14F, 59.5F, 2.4F, 10F, 1.5F)
                .texOffs(156, 164).addBox(-6F, -14.6F, 59.5F, 12F, 0.8F, 1.5F)
                .texOffs(128, 122).addBox(3.8F, -33F, -9F, 11.2F, 20.5F, 0.8F)
                .texOffs(128, 122).addBox(-15F, -33F, -9F, 11.2F, 20.5F, 0.8F)
                .texOffs(202, 148).addBox(-3.8F, -19F, -9F, 7.6F, 6.5F, 0.8F)
                .texOffs(106, 148).addBox(13.5F, -33F, -8.2F, 1.5F, 3F, 6.2F)
                .texOffs(106, 148).addBox(-15F, -33F, -8.2F, 1.5F, 3F, 6.2F)
                .texOffs(34, 164).addBox(8F, -15F, 1F, 3.5F, 1F, 4F)
                .texOffs(34, 164).addBox(-11.5F, -15F, 1F, 3.5F, 1F, 4F)
                .texOffs(48, 148).addBox(-12F, -5F, 17F, 4.5F, 5F, 9F)
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
                .texOffs(0, 148).addBox(-5F, 0F, -6F, 10F, 2F, 14F)
                .texOffs(124, 148).addBox(5F, -6F, -2F, 1.5F, 4F, 6F)
                , PartPose.offsetAndRotation(0F, 4F, -2F, 0F, 0F, 0F));
        PartDefinition p5 = p4.addOrReplaceChild("cradle", CubeListBuilder.create()
                .texOffs(0, 84).addBox(1F, 3.2F, -20F, 2.4F, 3.3F, 34F)
                .texOffs(0, 84).addBox(-3.4F, 3.2F, -20F, 2.4F, 3.3F, 34F)
                .texOffs(74, 84).addBox(-3.6F, 3F, -20F, 7.2F, 1F, 34F)
                .texOffs(24, 164).addBox(3F, -1.2F, -1.2F, 1.6F, 2.4F, 2.4F)
                .texOffs(24, 164).addBox(-4.6F, -1.2F, -1.2F, 1.6F, 2.4F, 2.4F)
                , PartPose.offsetAndRotation(0F, -7F, 0F, 0F, 0F, 0F));
        p5.addOrReplaceChild("barrel", CubeListBuilder.create()
                .texOffs(80, 122).addBox(-4.6F, -5.4F, 1F, 9.2F, 8.4F, 14F)
                .texOffs(148, 164).addBox(-0.8F, -1F, 15F, 1.6F, 2F, 2F)
                .texOffs(158, 84).addBox(-3.6F, -4.8F, -22F, 7.2F, 7.2F, 23F)
                .texOffs(32, 122).addBox(-3F, -4.2F, -40F, 6F, 6F, 18F)
                .texOffs(86, 148).addBox(-3.5F, -4.7F, -41F, 7F, 7F, 3F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
    }

    private static void build_wheell(PartDefinition root) {
        PartDefinition p7 = root.addOrReplaceChild("wheell", CubeListBuilder.create()
                .texOffs(220, 148).addBox(0.3F, -2F, -2F, 2.7F, 4F, 4F)
                , PartPose.offsetAndRotation(13F, 12F, 0F, 0F, 0F, 0F));
        p7.addOrReplaceChild("wheell_t0", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(0, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p7.addOrReplaceChild("wheell_t1", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(0, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0.5236F, 0F, 0F));
        p7.addOrReplaceChild("wheell_t2", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(0, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 1.0472F, 0F, 0F));
        p7.addOrReplaceChild("wheell_t3", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(0, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 1.5708F, 0F, 0F));
        p7.addOrReplaceChild("wheell_t4", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(0, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 2.0944F, 0F, 0F));
        p7.addOrReplaceChild("wheell_t5", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(0, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 2.618F, 0F, 0F));
        p7.addOrReplaceChild("wheell_t6", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(0, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 3.1416F, 0F, 0F));
        p7.addOrReplaceChild("wheell_t7", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(0, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 3.6652F, 0F, 0F));
        p7.addOrReplaceChild("wheell_t8", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(0, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 4.1888F, 0F, 0F));
        p7.addOrReplaceChild("wheell_t9", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(0, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 4.7124F, 0F, 0F));
        p7.addOrReplaceChild("wheell_t10", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(0, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 5.236F, 0F, 0F));
        p7.addOrReplaceChild("wheell_t11", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(0, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 5.7596F, 0F, 0F));
        p7.addOrReplaceChild("wheell_spk", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-0.6F, -20.8F, -20.8F, 1.2F, 41.6F, 41.6F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
    }

    private static void build_wheelr(PartDefinition root) {
        PartDefinition p21 = root.addOrReplaceChild("wheelr", CubeListBuilder.create()
                .texOffs(220, 148).addBox(-3F, -2F, -2F, 2.7F, 4F, 4F)
                , PartPose.offsetAndRotation(-13F, 12F, 0F, 0F, 0F, 0F));
        p21.addOrReplaceChild("wheelr_t0", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(0, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p21.addOrReplaceChild("wheelr_t1", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(0, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0.5236F, 0F, 0F));
        p21.addOrReplaceChild("wheelr_t2", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(0, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 1.0472F, 0F, 0F));
        p21.addOrReplaceChild("wheelr_t3", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(0, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 1.5708F, 0F, 0F));
        p21.addOrReplaceChild("wheelr_t4", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(0, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 2.0944F, 0F, 0F));
        p21.addOrReplaceChild("wheelr_t5", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(0, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 2.618F, 0F, 0F));
        p21.addOrReplaceChild("wheelr_t6", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(0, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 3.1416F, 0F, 0F));
        p21.addOrReplaceChild("wheelr_t7", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(0, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 3.6652F, 0F, 0F));
        p21.addOrReplaceChild("wheelr_t8", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(0, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 4.1888F, 0F, 0F));
        p21.addOrReplaceChild("wheelr_t9", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(0, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 4.7124F, 0F, 0F));
        p21.addOrReplaceChild("wheelr_t10", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(0, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 5.236F, 0F, 0F));
        p21.addOrReplaceChild("wheelr_t11", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(0, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 5.7596F, 0F, 0F));
        p21.addOrReplaceChild("wheelr_spk", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-0.6F, -20.8F, -20.8F, 1.2F, 41.6F, 41.6F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
    }


    public void setupAnim(MorserEntity gun, float partialTick) {
        float wheel = Mth.lerp(partialTick, gun.wheelRotO, gun.wheelRot);
        for (ModelPart w : this.wheels) {
            w.xRot = wheel;
        }
        this.mount.yRot = Mth.DEG_TO_RAD * gun.getTraverse();
        this.cradle.xRot = -Mth.DEG_TO_RAD * gun.getElevation();
        this.barrel.z = 9.0F * Mth.lerp(partialTick, gun.recoilO, gun.recoil);     // runs back along the cradle
    }

    public void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay) {
        this.root.render(poseStack, consumer, light, overlay);
    }
}
