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
                .texOffs(94, 164).addBox(-13.5F, -13F, -1F, 27F, 2F, 2F)
                .texOffs(220, 84).addBox(5F, -24F, -8F, 2.5F, 14F, 14F)
                .texOffs(220, 84).addBox(-7.5F, -24F, -8F, 2.5F, 14F, 14F)
                .texOffs(154, 122).addBox(-7.5F, -11F, -8F, 15F, 2F, 14F)
                .texOffs(152, 164).addBox(-6F, -11F, 18F, 12F, 2F, 2F)
                .texOffs(152, 164).addBox(-6F, -8F, 34F, 12F, 2F, 2F)
                .texOffs(152, 164).addBox(-6F, -5.5F, 48F, 12F, 2F, 2F)
                .texOffs(140, 148).addBox(-7.5F, -4F, 57F, 15F, 4F, 5F)
                .texOffs(82, 164).addBox(-1.5F, -6F, 58F, 3F, 2F, 3F)
                .texOffs(76, 148).addBox(-1.2F, -14F, 59.5F, 2.4F, 10F, 1.5F)
                .texOffs(206, 164).addBox(-6F, -14.6F, 59.5F, 12F, 0.8F, 1.5F)
                .texOffs(128, 122).addBox(3.8F, -33F, -9F, 11.2F, 20.5F, 0.8F)
                .texOffs(128, 122).addBox(-15F, -33F, -9F, 11.2F, 20.5F, 0.8F)
                .texOffs(0, 164).addBox(-3.8F, -19F, -9F, 7.6F, 6.5F, 0.8F)
                .texOffs(106, 148).addBox(13.5F, -33F, -8.2F, 1.5F, 3F, 6.2F)
                .texOffs(106, 148).addBox(-15F, -33F, -8.2F, 1.5F, 3F, 6.2F)
                .texOffs(66, 164).addBox(8F, -15F, 1F, 3.5F, 1F, 4F)
                .texOffs(66, 164).addBox(-11.5F, -15F, 1F, 3.5F, 1F, 4F)
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
                .texOffs(56, 164).addBox(3F, -1.2F, -1.2F, 1.6F, 2.4F, 2.4F)
                .texOffs(56, 164).addBox(-4.6F, -1.2F, -1.2F, 1.6F, 2.4F, 2.4F)
                , PartPose.offsetAndRotation(0F, -7F, 0F, 0F, 0F, 0F));
        PartDefinition p6 = p5.addOrReplaceChild("barrel", CubeListBuilder.create()
                .texOffs(80, 122).addBox(-4.6F, -5.4F, 1F, 9.2F, 8.4F, 14F)
                .texOffs(158, 84).addBox(-3.6F, -4.8F, -22F, 7.2F, 7.2F, 23F)
                .texOffs(32, 122).addBox(-3F, -4.2F, -40F, 6F, 6F, 18F)
                .texOffs(86, 148).addBox(-3.5F, -4.7F, -41F, 7F, 7F, 3F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p7 = p6.addOrReplaceChild("breechblock", CubeListBuilder.create()
                .texOffs(202, 148).addBox(0F, -3.4F, 0F, 6.8F, 6.8F, 1.2F)
                , PartPose.offsetAndRotation(-3.4F, -1.2F, 15F, 0F, 0F, 0F));
        p7.addOrReplaceChild("crank", CubeListBuilder.create()
                .texOffs(180, 164).addBox(-0.6F, -0.6F, 0F, 6.2F, 1.2F, 1.2F)
                .texOffs(198, 164).addBox(4.4F, -0.8F, 1.2F, 1.4F, 1.6F, 2F)
                , PartPose.offsetAndRotation(3.4F, 0F, 1.2F, 0F, 0F, 0F));
        p6.addOrReplaceChild("round", CubeListBuilder.create()
                .texOffs(220, 148).addBox(-1.5F, -1.5F, -6F, 3F, 3F, 6F)
                .texOffs(238, 148).addBox(-1.3F, -1.3F, -12F, 2.6F, 2.6F, 6F)
                .texOffs(198, 164).addBox(-0.8F, -0.8F, -14F, 1.6F, 1.6F, 2F)
                , PartPose.offsetAndRotation(0F, -1.2F, 15F, 0F, 0F, 0F));
        p6.addOrReplaceChild("spent", CubeListBuilder.create()
                .texOffs(220, 148).addBox(-1.5F, -1.5F, -6F, 3F, 3F, 6F)
                , PartPose.offsetAndRotation(0F, -1.2F, 15F, 0F, 0F, 0F));
    }

    private static void build_wheell(PartDefinition root) {
        PartDefinition p11 = root.addOrReplaceChild("wheell", CubeListBuilder.create()
                .texOffs(18, 164).addBox(0.3F, -2F, -2F, 2.7F, 4F, 4F)
                , PartPose.offsetAndRotation(13F, 12F, 0F, 0F, 0F, 0F));
        p11.addOrReplaceChild("wheell_t0", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(32, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p11.addOrReplaceChild("wheell_t1", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(32, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0.5236F, 0F, 0F));
        p11.addOrReplaceChild("wheell_t2", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(32, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 1.0472F, 0F, 0F));
        p11.addOrReplaceChild("wheell_t3", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(32, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 1.5708F, 0F, 0F));
        p11.addOrReplaceChild("wheell_t4", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(32, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 2.0944F, 0F, 0F));
        p11.addOrReplaceChild("wheell_t5", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(32, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 2.618F, 0F, 0F));
        p11.addOrReplaceChild("wheell_t6", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(32, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 3.1416F, 0F, 0F));
        p11.addOrReplaceChild("wheell_t7", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(32, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 3.6652F, 0F, 0F));
        p11.addOrReplaceChild("wheell_t8", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(32, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 4.1888F, 0F, 0F));
        p11.addOrReplaceChild("wheell_t9", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(32, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 4.7124F, 0F, 0F));
        p11.addOrReplaceChild("wheell_t10", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(32, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 5.236F, 0F, 0F));
        p11.addOrReplaceChild("wheell_t11", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(32, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 5.7596F, 0F, 0F));
        p11.addOrReplaceChild("wheell_spk", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-0.6F, -20.8F, -20.8F, 1.2F, 41.6F, 41.6F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
    }

    private static void build_wheelr(PartDefinition root) {
        PartDefinition p25 = root.addOrReplaceChild("wheelr", CubeListBuilder.create()
                .texOffs(18, 164).addBox(-3F, -2F, -2F, 2.7F, 4F, 4F)
                , PartPose.offsetAndRotation(-13F, 12F, 0F, 0F, 0F, 0F));
        p25.addOrReplaceChild("wheelr_t0", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(32, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p25.addOrReplaceChild("wheelr_t1", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(32, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 0.5236F, 0F, 0F));
        p25.addOrReplaceChild("wheelr_t2", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(32, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 1.0472F, 0F, 0F));
        p25.addOrReplaceChild("wheelr_t3", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(32, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 1.5708F, 0F, 0F));
        p25.addOrReplaceChild("wheelr_t4", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(32, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 2.0944F, 0F, 0F));
        p25.addOrReplaceChild("wheelr_t5", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(32, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 2.618F, 0F, 0F));
        p25.addOrReplaceChild("wheelr_t6", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(32, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 3.1416F, 0F, 0F));
        p25.addOrReplaceChild("wheelr_t7", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(32, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 3.6652F, 0F, 0F));
        p25.addOrReplaceChild("wheelr_t8", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(32, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 4.1888F, 0F, 0F));
        p25.addOrReplaceChild("wheelr_t9", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(32, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 4.7124F, 0F, 0F));
        p25.addOrReplaceChild("wheelr_t10", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(32, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
                , PartPose.offsetAndRotation(0F, 0F, 0F, 5.236F, 0F, 0F));
        p25.addOrReplaceChild("wheelr_t11", CubeListBuilder.create()
                .texOffs(180, 148).addBox(-1.6F, -11.2F, -3.3154F, 3.2F, 1.4F, 6.6308F)
                .texOffs(32, 164).addBox(-2.9F, -12F, -2.7154F, 5.8F, 0.9F, 5.4308F)
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

    /**
     * Loading drill, timed in ticks of a 10 s reload (scaled for other reload times) so the breech sounds line up:
     * crank up and breech swings open, the spent case is pulled out and dropped, a new round is lifted in behind the
     * breech and rammed home, then the breech swings shut and the crank goes back down.
     */
    private void reloadAnim(MorserEntity gun, float partialTick) {
        int total = gun.getReloadTotal();
        int left = gun.getReload();
        float t = left > 0 ? (total - left + partialTick) * (200.0F / total) : 999.0F;
        float open = ease(t, 62, 74) - ease(t, 186, 197);
        float crank = ease(t, 56, 66) - ease(t, 193, 200);
        this.breech.yRot = -1.65F * open;
        this.crank.zRot = -1.4F * crank;
        // spent case: out along the bore, then it tips off and drops
        this.spent.visible = t > 74 && t < 108;
        float out = ease(t, 74, 90);
        float drop = ease(t, 90, 106);
        this.spent.z = this.spentZ + 16.0F * out + 4.0F * drop;
        this.spent.y = this.spentY + 14.0F * drop * drop;
        this.spent.xRot = 1.2F * drop;
        // new round: brought up behind the breech, then rammed in
        this.round.visible = t > 108 && t < 154;
        float lift = ease(t, 108, 122);
        float ram = ease(t, 126, 152);
        this.round.z = this.roundZ + 24.0F * (1.0F - ram);
        this.round.y = this.roundY + 8.0F * (1.0F - lift);
    }

    private float roundZ = Float.NaN, roundY, spentZ, spentY;

    public void setupAnim(MorserEntity gun, float partialTick, float traverse, float elevation) {
        if (Float.isNaN(this.roundZ)) {
            this.roundZ = this.round.z;
            this.roundY = this.round.y;
            this.spentZ = this.spent.z;
            this.spentY = this.spent.y;
        }
        reloadAnim(gun, partialTick);
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
