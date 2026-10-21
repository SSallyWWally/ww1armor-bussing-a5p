package com.exodusvehicles.client;

import com.exodusvehicles.ArmoredCarMod;
import com.exodusvehicles.ModConfig;
import com.exodusvehicles.artillery.ShellManager;
import com.exodusvehicles.entity.FieldGunEntity;
import com.exodusvehicles.entity.MorserEntity;
import com.exodusvehicles.network.ShellFiredPayload;
import com.exodusvehicles.network.ShellImpactPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Client side of the artillery: the aiming camera behind the gun, the shell-follow camera, visual copies of shells in
 * flight (trail only; the real shell lives on the server), screen shake and shell shock. Nothing here moves the player
 * or affects the server; cameras stay within the client's own render distance so they never show unloaded ground.
 */
public final class ArtilleryClient {
    private ArtilleryClient() {}

    private static final class ClientShell {
        final int id, gunId;
        Vec3 pos, prev, vel;
        boolean alive = true;
        int age;

        ClientShell(int id, int gunId, Vec3 pos, Vec3 vel) {
            this.id = id;
            this.gunId = gunId;
            this.pos = pos;
            this.prev = pos;
            this.vel = vel;
        }
    }

    private enum Phase { NONE, FOLLOW, HOLD, RETURN }

    private static final List<ClientShell> SHELLS = new ArrayList<>();
    private static Phase phase = Phase.NONE;
    private static int camShell = -1, phaseTicks;
    private static Vec3 lastCamPos, impactAt;
    private static float lastCamYaw, lastCamPitch;
    private static float shakeAmp, shakeAmpO;
    private static float flash, flashO, dim, dimO;
    private static long shakeSeed = 1;
    private static boolean triggerWas, useWas;

    /**
     * Where the shells will fall: a red ring the size of the scatter drawn on the ground. Only on the operator's own
     * screen, only while they are on the gun (gone the moment they get off), only where their client has the ground.
     */
    private static void renderRing(net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext ctx) {
        Minecraft mc = Minecraft.getInstance();
        MorserEntity gun = operatedGun();
        if (gun == null || mc.player == null || mc.level == null || ctx.consumers() == null || ctx.matrixStack() == null) {
            return;
        }
        float pt = ctx.tickCounter().getGameTimeDeltaPartialTick(true);
        if (gun instanceof FieldGunEntity fg) {
            renderArc(ctx, fg, pt);
            return;
        }
        float range = localRange(gun, mc.player, pt);
        Vec3 aim = gun.aimPoint(localTraverse(gun, mc.player, pt), range);
        if (!mc.level.hasChunkAt(BlockPos.containing(aim))) {
            return;
        }
        float r = gun.dispersionAt(range);
        Vec3 cam = ctx.camera().getPosition();
        com.mojang.blaze3d.vertex.PoseStack ps = ctx.matrixStack();
        ps.pushPose();
        ps.translate(-cam.x, -cam.y, -cam.z);
        com.mojang.blaze3d.vertex.PoseStack.Pose pose = ps.last();
        com.mojang.blaze3d.vertex.VertexConsumer vc = ctx.consumers().getBuffer(net.minecraft.client.renderer.RenderType.lines());
        int n = 48;
        Vec3 prev = null;
        for (int i = 0; i <= n; i++) {
            double a = i * Mth.TWO_PI / n;
            double x = aim.x + Math.cos(a) * r, z = aim.z + Math.sin(a) * r;
            double y = mc.level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, Mth.floor(x), Mth.floor(z)) + 0.15;
            Vec3 p = new Vec3(x, y, z);
            if (prev != null) {
                line(vc, pose, prev, p);
            }
            prev = p;
        }
        double cy = aim.y + 0.15;
        line(vc, pose, new Vec3(aim.x - 1.5, cy, aim.z), new Vec3(aim.x + 1.5, cy, aim.z));
        line(vc, pose, new Vec3(aim.x, cy, aim.z - 1.5), new Vec3(aim.x, cy, aim.z + 1.5));
        ps.popPose();
    }

    /** Predicted flight of the next round from the local gunner's aim: muzzle to where it meets the ground. */
    public static List<Vec3> predictedArc(FieldGunEntity gun, Player p, float pt) {
        float trav = localTraverse(gun, p, pt);
        float elev = localRange(gun, p, pt);
        float yaw = Mth.rotLerp(pt, gun.yRotO, gun.getYRot());
        Vec3 base = new Vec3(Mth.lerp(pt, gun.xo, gun.getX()), Mth.lerp(pt, gun.yo, gun.getY()), Mth.lerp(pt, gun.zo, gun.getZ()));
        Vec3 muzzle = gun.muzzle(base, yaw, trav, elev);
        Vec3 vel = gun.boreDir(yaw, trav, elev).scale(gun.muzzleSpeed());
        return FieldGunEntity.trajectory(p.level(), muzzle, vel, 300);
    }

    private static List<Vec3> arcCache;
    private static long arcFrame = -1;

    /** The glowing white arc of the round's path, and a ring where it comes down. Only on the gunner's own screen. */
    private static void renderArc(net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext ctx, FieldGunEntity gun, float pt) {
        Minecraft mc = Minecraft.getInstance();
        List<Vec3> pts = predictedArc(gun, mc.player, pt);
        arcCache = pts;
        arcFrame = mc.level.getGameTime();
        if (pts.size() < 2) {
            return;
        }
        Vec3 cam = ctx.camera().getPosition();
        com.mojang.blaze3d.vertex.PoseStack ps = ctx.matrixStack();
        ps.pushPose();
        ps.translate(-cam.x, -cam.y, -cam.z);
        org.joml.Matrix4f m = ps.last().pose();
        com.mojang.blaze3d.vertex.VertexConsumer vc = ctx.consumers().getBuffer(net.minecraft.client.renderer.RenderType.lightning());
        int n = pts.size();
        for (int i = 0; i < n - 1; i++) {
            Vec3 a = pts.get(i), b = pts.get(i + 1);
            float fa = arcAlpha(i, n), fb = arcAlpha(i + 1, n);
            ribbon(vc, m, cam, a, b, fa, fb);
        }
        // landing ring
        Vec3 end = pts.get(n - 1);
        double r = 1.2 + end.distanceTo(cam) * 0.004;
        Vec3 prev = null;
        for (int i = 0; i <= 32; i++) {
            double ang = i * Mth.TWO_PI / 32;
            Vec3 q = new Vec3(end.x + Math.cos(ang) * r, end.y + 0.08, end.z + Math.sin(ang) * r);
            if (prev != null) {
                ribbon(vc, m, cam, prev, q, 0.9F, 0.9F);
            }
            prev = q;
        }
        ps.popPose();
    }

    private static float arcAlpha(int i, int n) {
        float t = i / (float) Math.max(1, n - 1);
        return 0.35F + 0.55F * Mth.clamp(t * 6.0F, 0.0F, 1.0F);     // faint at the muzzle, bright down range
    }

    /** One camera-facing strip from a to b (both windings, so it shows from any side). */
    private static void ribbon(com.mojang.blaze3d.vertex.VertexConsumer vc, org.joml.Matrix4f m, Vec3 cam, Vec3 a, Vec3 b, float aa, float ab) {
        Vec3 d = b.subtract(a);
        if (d.lengthSqr() < 1.0E-8) {
            return;
        }
        Vec3 sa = d.cross(a.subtract(cam));
        Vec3 sb = d.cross(b.subtract(cam));
        if (sa.lengthSqr() < 1.0E-10 || sb.lengthSqr() < 1.0E-10) {
            return;
        }
        double wa = 0.035 + a.distanceTo(cam) * 0.0022, wb = 0.035 + b.distanceTo(cam) * 0.0022;
        sa = sa.normalize().scale(wa);
        sb = sb.normalize().scale(wb);
        int ca = (int) (aa * 255), cb = (int) (ab * 255);
        Vec3[] q = {a.add(sa), a.subtract(sa), b.subtract(sb), b.add(sb)};
        int[] al = {ca, ca, cb, cb};
        for (int k = 0; k < 4; k++) {
            vc.addVertex(m, (float) q[k].x, (float) q[k].y, (float) q[k].z).setColor(255, 252, 240, al[k]);
        }
        for (int k = 3; k >= 0; k--) {
            vc.addVertex(m, (float) q[k].x, (float) q[k].y, (float) q[k].z).setColor(255, 252, 240, al[k]);
        }
    }

    private static void line(com.mojang.blaze3d.vertex.VertexConsumer vc, com.mojang.blaze3d.vertex.PoseStack.Pose pose, Vec3 a, Vec3 b) {
        Vec3 n = b.subtract(a).normalize();
        vc.addVertex(pose, (float) a.x, (float) a.y, (float) a.z).setColor(230, 50, 35, 255).setNormal(pose, (float) n.x, (float) n.y, (float) n.z);
        vc.addVertex(pose, (float) b.x, (float) b.y, (float) b.z).setColor(230, 50, 35, 255).setNormal(pose, (float) n.x, (float) n.y, (float) n.z);
    }

    public static void init() {
        net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.AFTER_ENTITIES.register(ArtilleryClient::renderRing);
        ClientPlayNetworking.registerGlobalReceiver(com.exodusvehicles.network.HitMarkerPayload.TYPE, (p, ctx) -> {
            hitTicks = 50;
            hitKill = hitKill && hitTicks > 0 || p.kill();
            hitLine = p.line();
            ctx.client().getSoundManager().play(SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, 0.6F, 0.4F));
        });
        ClientPlayNetworking.registerGlobalReceiver(ShellFiredPayload.TYPE, (p, ctx) -> {
            ClientShell s = new ClientShell(p.shellId(), p.gunId(), new Vec3(p.x(), p.y(), p.z()), new Vec3(p.vx(), p.vy(), p.vz()));
            SHELLS.add(s);
            Minecraft mc = ctx.client();
            // rushing air travelling with the shell (heard by the shell camera, and by anyone it passes over)
            mc.getSoundManager().play(new ShellFlightSound(ArmoredCarMod.SHELL_FLIGHT, () -> s.alive && SHELLS.contains(s) ? s.pos : null));
            if (mc.player != null && mc.player.getVehicle() != null && mc.player.getVehicle().getId() == p.gunId()) {
                boolean field = mc.player.getVehicle() instanceof FieldGunEntity;
                addShake(field ? 0.8F : 1.6F);          // the whole gun jumps
                camZoom = 1.0F;
                if (!field && ModConfig.get().shellCamera) {
                    phase = Phase.FOLLOW;
                    camShell = p.shellId();
                    phaseTicks = 0;
                }
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(ShellImpactPayload.TYPE, (p, ctx) -> {
            Vec3 at = new Vec3(p.x(), p.y(), p.z());
            for (ClientShell s : SHELLS) {
                if (s.id == p.shellId()) {
                    s.alive = false;
                    s.prev = at;
                    s.pos = at;
                }
            }
            if (phase == Phase.FOLLOW && camShell == p.shellId()) {
                phase = Phase.HOLD;
                phaseTicks = 0;
                impactAt = at;
            }
            Minecraft mc = ctx.client();
            if (mc.player == null) {
                return;
            }
            boolean small = p.radius() < 0.0F;
            if (small) {
                smallBlast(mc, at);
            } else {
                blast(mc, at);
            }
            double d = mc.player.getEyePosition().distanceTo(at);
            float r = Math.max(2.0F, Math.abs(p.radius()));
            // screen shake reaches well beyond the blast; shell shock only close in
            float s = (float) Math.max(0.0, 1.0 - d / (r * 7.0));
            addShake((small ? 9.0F : 16.0F) * (float) Math.pow(s, 1.5));
            double near = r * 1.8;
            if (d < near) {
                float k = (float) (1.0 - d / near);
                flash = Math.max(flash, 0.55F * k + 0.25F);
                dim = Math.max(dim, 90.0F + 90.0F * k);
                shakeLong = Math.max(shakeLong, 40 + (int) (60 * k));     // the ground keeps trembling a while
                mc.getSoundManager().play(SimpleSoundInstance.forUI(ArmoredCarMod.SHELL_SHOCK, 1.0F, 0.45F + 0.55F * k));
                splatter(mc, k);
            }
        });
    }

    /**
     * The visible burst, drawn locally from the impact packet: flash, fireballs, a fountain of whatever the ground is
     * made of, a ring of dust rolling outward, and a smoke cloud with a tall column that hangs for a while.
     */
    private static void blast(Minecraft mc, Vec3 c) {
        net.minecraft.client.multiplayer.ClientLevel lvl = mc.level;
        if (lvl == null) {
            return;
        }
        float k = Math.max(0.25F, ModConfig.get().shellEffectScale);
        java.util.Random r = new java.util.Random();
        int n;
        lvl.addAlwaysVisibleParticle(ParticleTypes.FLASH, true, c.x, c.y + 1.0, c.z, 0, 0, 0);
        // fireballs
        lvl.addAlwaysVisibleParticle(ParticleTypes.EXPLOSION_EMITTER, true, c.x, c.y + 0.5, c.z, 0, 0, 0);
        for (int i = 0; i < 4; i++) {
            double a = i * Math.PI / 2 + r.nextDouble();
            lvl.addAlwaysVisibleParticle(ParticleTypes.EXPLOSION_EMITTER, true, c.x + Math.cos(a) * 2.2 * k,
                    c.y + 0.8 + r.nextDouble() * 1.5 * k, c.z + Math.sin(a) * 2.2 * k, 0, 0, 0);
        }
        n = (int) (22 * k);
        for (int i = 0; i < n; i++) {
            lvl.addAlwaysVisibleParticle(ParticleTypes.EXPLOSION, true, c.x + (r.nextDouble() - 0.5) * 6 * k,
                    c.y + r.nextDouble() * 3.5 * k, c.z + (r.nextDouble() - 0.5) * 6 * k, 0, 0, 0);
        }
        // earth thrown up in a column and raining back down
        net.minecraft.world.level.block.state.BlockState ground = lvl.getBlockState(BlockPos.containing(c.x, c.y - 1.0, c.z));
        if (ground.isAir()) {
            ground = net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState();
        }
        net.minecraft.core.particles.BlockParticleOption dirt =
                new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK, ground);
        n = (int) (90 * k);
        for (int i = 0; i < n; i++) {
            double sp = 0.25 + r.nextDouble() * 0.45;
            double a = r.nextDouble() * Math.PI * 2;
            lvl.addAlwaysVisibleParticle(dirt, true, c.x + (r.nextDouble() - 0.5) * 2, c.y + r.nextDouble(), c.z + (r.nextDouble() - 0.5) * 2,
                    Math.cos(a) * sp * 0.6 * k, (0.7 + r.nextDouble() * 1.3) * k, Math.sin(a) * sp * 0.6 * k);
        }
        // shock ring of dust along the ground
        n = (int) (28 * k);
        for (int i = 0; i < n; i++) {
            double a = i * Math.PI * 2 / n;
            lvl.addAlwaysVisibleParticle(ParticleTypes.POOF, true, c.x + Math.cos(a), c.y + 0.3, c.z + Math.sin(a),
                    Math.cos(a) * 0.55 * k, 0.02, Math.sin(a) * 0.55 * k);
        }
        // smoke cloud and the column that marks the strike from afar
        n = (int) (40 * k);
        for (int i = 0; i < n; i++) {
            lvl.addAlwaysVisibleParticle(ParticleTypes.LARGE_SMOKE, true, c.x + (r.nextDouble() - 0.5) * 6 * k,
                    c.y + 0.5 + r.nextDouble() * 4 * k, c.z + (r.nextDouble() - 0.5) * 6 * k,
                    (r.nextDouble() - 0.5) * 0.08, 0.04 + r.nextDouble() * 0.08, (r.nextDouble() - 0.5) * 0.08);
        }
        n = (int) (12 * k);
        for (int i = 0; i < n; i++) {
            lvl.addAlwaysVisibleParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, true, c.x + (r.nextDouble() - 0.5) * 3 * k,
                    c.y + 0.5 + r.nextDouble() * 2, c.z + (r.nextDouble() - 0.5) * 3 * k, 0, 0.05 + r.nextDouble() * 0.05, 0);
        }
    }

    /** A 7.7 cm burst: a sharp flash, a black-brown fountain of earth, a smaller smoke cloud. */
    private static void smallBlast(Minecraft mc, Vec3 c) {
        net.minecraft.client.multiplayer.ClientLevel lvl = mc.level;
        if (lvl == null) {
            return;
        }
        float k = Math.max(0.25F, ModConfig.get().shellEffectScale);
        java.util.Random r = new java.util.Random();
        lvl.addAlwaysVisibleParticle(ParticleTypes.FLASH, true, c.x, c.y + 0.6, c.z, 0, 0, 0);
        int n = Math.max(2, (int) (4 * k));
        for (int i = 0; i < n; i++) {
            lvl.addAlwaysVisibleParticle(ParticleTypes.EXPLOSION, true, c.x + (r.nextDouble() - 0.5) * 1.2,
                    c.y + 0.3 + r.nextDouble() * 0.9, c.z + (r.nextDouble() - 0.5) * 1.2, 0, 0, 0);
        }
        net.minecraft.world.level.block.state.BlockState ground = lvl.getBlockState(BlockPos.containing(c.x, c.y - 1.0, c.z));
        if (ground.isAir()) {
            ground = net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState();
        }
        net.minecraft.core.particles.BlockParticleOption dirt =
                new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK, ground);
        n = (int) (45 * k);
        for (int i = 0; i < n; i++) {
            double sp = 0.15 + r.nextDouble() * 0.3;
            double a = r.nextDouble() * Math.PI * 2;
            lvl.addAlwaysVisibleParticle(dirt, true, c.x + (r.nextDouble() - 0.5), c.y + r.nextDouble() * 0.5, c.z + (r.nextDouble() - 0.5),
                    Math.cos(a) * sp * 0.5 * k, (0.5 + r.nextDouble() * 0.8) * k, Math.sin(a) * sp * 0.5 * k);
        }
        n = (int) (10 * k);
        for (int i = 0; i < n; i++) {
            lvl.addAlwaysVisibleParticle(ParticleTypes.LARGE_SMOKE, true, c.x + (r.nextDouble() - 0.5) * 1.8,
                    c.y + 0.3 + r.nextDouble() * 1.2, c.z + (r.nextDouble() - 0.5) * 1.8,
                    (r.nextDouble() - 0.5) * 0.05, 0.03 + r.nextDouble() * 0.05, (r.nextDouble() - 0.5) * 0.05);
        }
        for (int i = 0; i < Math.max(2, (int) (4 * k)); i++) {
            lvl.addAlwaysVisibleParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, true, c.x + (r.nextDouble() - 0.5) * 1.5,
                    c.y + 0.4 + r.nextDouble(), c.z + (r.nextDouble() - 0.5) * 1.5, 0, 0.03 + r.nextDouble() * 0.03, 0);
        }
    }

    private static int hitTicks, hitLine = -1;
    private static boolean hitKill;

    // dirt thrown onto the screen by a close hit
    private static final class Splat {
        final int tex;
        final float x, y, size;
        int life;
        final int maxLife;

        Splat(int tex, float x, float y, float size, int life) {
            this.tex = tex;
            this.x = x;
            this.y = y;
            this.size = size;
            this.life = life;
            this.maxLife = life;
        }
    }

    private static final List<Splat> SPLATS = new ArrayList<>();
    private static final net.minecraft.resources.ResourceLocation[] SPLAT_TEX = {
            ArmoredCarMod.id("textures/gui/dirt_splat_0.png"), ArmoredCarMod.id("textures/gui/dirt_splat_1.png"),
            ArmoredCarMod.id("textures/gui/dirt_splat_2.png")};
    private static int shakeLong;

    private static void splatter(Minecraft mc, float k) {
        java.util.Random r = new java.util.Random();
        int n = 3 + (int) (9 * k);
        for (int i = 0; i < n && SPLATS.size() < 24; i++) {
            float size = 0.18F + r.nextFloat() * (0.2F + 0.25F * k);      // fraction of screen height
            SPLATS.add(new Splat(r.nextInt(SPLAT_TEX.length), r.nextFloat(), r.nextFloat() * 0.9F, size,
                    70 + r.nextInt(50) + (int) (60 * k)));
        }
    }

    private static void addShake(float amp) {
        shakeAmp = Math.min(16.0F, Math.max(shakeAmp, amp));
        shakeSeed++;
    }

    @org.jetbrains.annotations.Nullable
    public static MorserEntity operatedGun() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getVehicle() instanceof MorserEntity gun && gun.isDeployed() && gun.getWork() == 0) {
            return gun;
        }
        return null;
    }

    /** True while one of our cameras replaces the first-person view. */
    public static boolean cameraActive() {
        return operatedGun() != null || phase != Phase.NONE;
    }

    /** Traverse the local operator is aiming at (client prediction for a smooth barrel and camera). */
    public static float localTraverse(MorserEntity gun, Player p, float partialTick) {
        float lim = gun.traverseLimit();
        return Mth.clamp(Mth.wrapDegrees(p.getViewYRot(partialTick) - gun.getYRot()), -lim, lim);
    }

    /** World position of the left rear corner of the breech this frame (where the gunner's hand rests). */
    public static Vec3 breechPoint(MorserEntity gun, float pt) {
        Minecraft mc = Minecraft.getInstance();
        float trav = gun.getTraverse();
        float elev = Mth.lerp(pt, gun.elevVisO, gun.elevVis);
        if (mc.player != null && gun.isOperatedBy(mc.player)) {
            trav = localTraverse(gun, mc.player, pt);
            elev = gun.shownElevation(localRange(gun, mc.player, pt));
        }
        float gunYaw = Mth.rotLerp(pt, gun.yRotO, gun.getYRot());
        double gy = Math.toRadians(gunYaw);
        Vec3 base = new Vec3(Mth.lerp(pt, gun.xo, gun.getX()), Mth.lerp(pt, gun.yo, gun.getY()), Mth.lerp(pt, gun.zo, gun.getZ()));
        Vec3 trunnion = base.add(-Math.sin(gy) * gun.trunnionFwd(), gun.trunnionUp(), Math.cos(gy) * gun.trunnionFwd());
        double yaw = Math.toRadians(gunYaw + trav), e = Math.toRadians(elev);
        Vec3 bore = new Vec3(-Math.sin(yaw) * Math.cos(e), Math.sin(e), Math.cos(yaw) * Math.cos(e));
        Vec3 left = new Vec3(Math.cos(yaw), 0, Math.sin(yaw));
        Vec3 up = new Vec3(Math.sin(yaw) * Math.sin(e), Math.cos(e), -Math.cos(yaw) * Math.sin(e));
        double[] o = gun.breechOffset();
        return trunnion.add(bore.scale(-o[0])).add(up.scale(o[1])).add(left.scale(o[2]));
    }

    /**
     * Arm pose for a player standing at a deployed howitzer: right hand on the breech (following it as the barrel
     * rises and falls), left arm loose. Angles in the player model's own space. Null when not at a gun.
     */
    public static float[] gunnerArmPose(net.minecraft.world.entity.LivingEntity entity) {
        if (!(entity.getVehicle() instanceof MorserEntity gun) || !gun.isDeployed() || gun.getWork() != 0) {
            return null;
        }
        Minecraft mc = Minecraft.getInstance();
        float pt = mc.getTimer().getGameTimeDeltaPartialTick(true);
        double by = Math.toRadians(Mth.rotLerp(pt, entity.yBodyRotO, entity.yBodyRot));
        Vec3 fwd = new Vec3(-Math.sin(by), 0, Math.cos(by));
        Vec3 right = new Vec3(-Math.cos(by), 0, -Math.sin(by));
        Vec3 pos = entity.getPosition(pt);
        Vec3 shoulder = pos.add(right.scale(5.0 / 16.0 * 0.9375)).add(0, 22.0 / 16.0 * 0.9375, 0);
        Vec3 d = breechPoint(gun, pt).subtract(shoulder);
        // player model space: x = left, y = down, z = backward
        double mx = -d.dot(right), my = -d.y, mz = -d.dot(fwd);
        double len = Math.sqrt(mx * mx + my * my + mz * mz);
        if (len < 1.0E-4) {
            return null;
        }
        mx /= len;
        my /= len;
        mz /= len;
        float xRot = (float) -Math.acos(Mth.clamp(my, -1.0, 1.0));
        float yRot = (float) Math.atan2(-mx, -mz);
        return new float[]{xRot, yRot, -0.35F, 0.08F};
    }

    /** The local gunner's aim value: range for the Mörser, barrel elevation for the field gun. */
    public static float localRange(MorserEntity gun, Player p, float partialTick) {
        if (gun instanceof FieldGunEntity fg) {
            return fg.elevationForPitch(p.getViewXRot(partialTick));
        }
        return MorserEntity.rangeForPitch(p.getViewXRot(partialTick), gun.minRange(), gun.maxRange());
    }

    // ------------------------------------------------------------------ ticking

    /** Start of client tick: trigger, skip, aim limits. */
    public static void startTick(Minecraft mc) {
        MorserEntity gun = operatedGun();
        if (gun == null || mc.player == null) {
            triggerWas = false;
            useWas = false;
            if (mc.player == null || !(mc.player.getVehicle() instanceof MorserEntity)) {
                if (phase != Phase.NONE && phase != Phase.RETURN) {
                    phase = Phase.NONE;
                }
            }
            return;
        }
        // keep the view inside the gun's traverse and the range band
        Player p = mc.player;
        float lim = gun.traverseLimit();
        float rel = Mth.wrapDegrees(p.getYRot() - gun.getYRot());
        if (Math.abs(rel) > lim) {
            float yaw = gun.getYRot() + Math.signum(rel) * lim;
            p.setYRot(yaw);
            p.yRotO = yaw;
            p.setYHeadRot(yaw);
        }
        float pitch = gun instanceof FieldGunEntity fg ? Mth.clamp(p.getXRot(), -fg.maxElevation(), -fg.minElevation())
                : Mth.clamp(p.getXRot(), MorserEntity.PITCH_FAR, MorserEntity.PITCH_NEAR);
        if (pitch != p.getXRot()) {
            p.setXRot(pitch);
            p.xRotO = pitch;
        }
        if (mc.screen != null) {
            return;
        }
        boolean trigger = GunSight.physicallyDown(mc, mc.options.keyAttack);
        mc.options.keyAttack.setDown(false);
        while (mc.options.keyAttack.consumeClick()) {
            // swallow: no punching while on the gun
        }
        if (trigger && !triggerWas && phase == Phase.NONE) {
            ClientPlayNetworking.send(new com.exodusvehicles.network.FireGunPayload());
        }
        triggerWas = trigger;
        boolean use = GunSight.physicallyDown(mc, mc.options.keyUse);
        mc.options.keyUse.setDown(false);
        while (mc.options.keyUse.consumeClick()) {
            // swallow
        }
        if (use && !useWas && (phase == Phase.FOLLOW || phase == Phase.HOLD)) {
            beginReturn();        // skip the shell camera
        }
        useWas = use;
        zoomHeld = use && gun instanceof FieldGunEntity && mc.options.getCameraType().isFirstPerson();
    }

    /** Toggle from the shell-camera key. */
    public static void toggleShellCamera(Minecraft mc) {
        ModConfig cfg = ModConfig.get();
        cfg.shellCamera = !cfg.shellCamera;
        ModConfig.save();
        if (!cfg.shellCamera && (phase == Phase.FOLLOW || phase == Phase.HOLD)) {
            beginReturn();
        }
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.translatable(cfg.shellCamera
                    ? "msg.exodus_vehicles.shell_cam_on" : "msg.exodus_vehicles.shell_cam_off"), true);
        }
    }

    private static void beginReturn() {
        phase = Phase.RETURN;
        phaseTicks = 0;
    }

    /** Looping aim / push sounds for each howitzer nearby; restarted if the sound engine ever dropped them. */
    private static final java.util.Map<Integer, GunLoopSound[]> GUN_SOUNDS = new java.util.HashMap<>();

    private static void tickGunSounds(Minecraft mc) {
        if (mc.level == null || mc.player == null) {
            GUN_SOUNDS.clear();
            return;
        }
        java.util.Set<Integer> seen = new java.util.HashSet<>();
        for (MorserEntity gun : mc.level.getEntitiesOfClass(MorserEntity.class, mc.player.getBoundingBox().inflate(48.0))) {
            seen.add(gun.getId());
            GunLoopSound[] s = GUN_SOUNDS.computeIfAbsent(gun.getId(), k -> new GunLoopSound[2]);
            GunLoopSound.Kind[] kinds = GunLoopSound.Kind.values();
            for (int i = 0; i < 2; i++) {
                if (s[i] == null || !s[i].isFor(gun) || !mc.getSoundManager().isActive(s[i])) {
                    if (s[i] != null) {
                        mc.getSoundManager().stop(s[i]);
                    }
                    net.minecraft.sounds.SoundEvent ev = gun instanceof FieldGunEntity ? ArmoredCarMod.FK_MOVE
                            : i == 0 ? ArmoredCarMod.GUN_TRAVERSE : ArmoredCarMod.GUN_PUSH;
                    s[i] = new GunLoopSound(gun, ev, kinds[i]);
                    mc.getSoundManager().play(s[i]);
                }
            }
        }
        GUN_SOUNDS.entrySet().removeIf(e -> {
            if (seen.contains(e.getKey())) {
                return false;
            }
            for (GunLoopSound s : e.getValue()) {
                if (s != null) {
                    mc.getSoundManager().stop(s);
                }
            }
            return true;
        });
    }

    private static boolean zoomHeld;
    private static float sightZoom = 1.0F, sightZoomO = 1.0F;
    /** Shell camera zoom: when the camera has to hang back (it never goes past the loaded world), it zooms in instead. */
    private static float camZoom = 1.0F, camZoomWant = 1.0F;

    /** FOV multiplier from the artillery views (field-gun sight zoom, shell camera zoom). */
    public static double fovScale(float pt) {
        float z = 1.0F;
        if (sightView()) {
            z = Mth.lerp(pt, sightZoomO, sightZoom);
        } else if (phase == Phase.FOLLOW || phase == Phase.HOLD) {
            z = camZoom;
        }
        return z <= 1.001F ? 1.0 : 1.0 / z;
    }

    public static double sensitivityScale() {
        return sightView() && sightZoom > 1.001F ? 1.0 / sightZoom : 1.0;
    }

    /** First-person sight of the field gun (the operator looks along the barrel). */
    public static boolean sightView() {
        Minecraft mc = Minecraft.getInstance();
        return operatedGun() instanceof FieldGunEntity && phase == Phase.NONE && mc.options.getCameraType().isFirstPerson();
    }

    public static void endTick(Minecraft mc) {
        sightZoomO = sightZoom;
        float zt = zoomHeld && sightView() ? 2.0F : 1.0F;      // x2 scope
        sightZoom += (zt - sightZoom) * 0.4F;
        if (Math.abs(zt - sightZoom) < 0.01F) {
            sightZoom = zt;
        }

        if (mc.player != null && mc.player.tickCount % 10 == 0) {
            tickGunSounds(mc);
        }
        if (hitTicks > 0 && --hitTicks == 0) {
            hitKill = false;
        }
        shakeAmpO = shakeAmp;
        shakeAmp *= 0.9F;
        if (shakeLong > 0) {
            shakeLong--;
            shakeAmp = Math.max(shakeAmp, 0.6F * Math.min(1.0F, shakeLong / 30.0F));     // lingering tremor
        }
        for (int i = SPLATS.size() - 1; i >= 0; i--) {
            if (--SPLATS.get(i).life <= 0) {
                SPLATS.remove(i);
            }
        }
        if (shakeAmp < 0.02F) {
            shakeAmp = 0.0F;
        }
        flashO = flash;
        flash *= 0.8F;
        dimO = dim;
        dim = Math.max(0.0F, dim - 1.0F);
        if (mc.level == null || mc.player == null) {
            SHELLS.clear();
            phase = Phase.NONE;
            return;
        }
        // visual shells: same flight as on the server, a smoke trail, stop at the ground
        Vec3 eye = mc.player.getEyePosition();
        for (int i = SHELLS.size() - 1; i >= 0; i--) {
            ClientShell s = SHELLS.get(i);
            s.age++;
            s.prev = s.pos;
            if (!s.alive) {
                if (phase == Phase.FOLLOW && camShell == s.id) {
                    phase = Phase.HOLD;
                    phaseTicks = 0;
                    impactAt = s.pos;
                }
                SHELLS.remove(i);
                continue;
            }
            Vec3 next = ShellManager.step(s.pos, s.vel);
            if (mc.level.hasChunkAt(BlockPos.containing(next))) {
                BlockHitResult hit = mc.level.clip(new ClipContext(s.pos, next, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY,
                        net.minecraft.world.phys.shapes.CollisionContext.empty()));
                if (hit.getType() != HitResult.Type.MISS) {
                    next = hit.getLocation();
                    s.alive = false;
                }
            }
            s.pos = next;
            s.vel = s.vel.add(0, -ShellManager.GRAVITY, 0);
            if (s.pos.distanceToSqr(eye) < 200 * 200) {
                mc.level.addAlwaysVisibleParticle(ParticleTypes.SMOKE, true, s.pos.x, s.pos.y, s.pos.z, 0, 0, 0);
            }
            if (s.age > 1200 || s.pos.y < mc.level.getMinBuildHeight() - 32) {
                SHELLS.remove(i);
            }
        }
        // camera phases
        if (phase == Phase.FOLLOW && findShell(camShell) == null) {
            phase = Phase.HOLD;
            phaseTicks = 0;
        }
        if (phase != Phase.NONE) {
            phaseTicks++;
            if (phase == Phase.HOLD && phaseTicks > 30) {
                beginReturn();
            } else if (phase == Phase.RETURN && phaseTicks > 12) {
                phase = Phase.NONE;
            }
            if (!(mc.player.getVehicle() instanceof MorserEntity)) {
                phase = Phase.NONE;
            }
        }
    }

    @org.jetbrains.annotations.Nullable
    private static ClientShell findShell(int id) {
        for (ClientShell s : SHELLS) {
            if (s.id == id) {
                return s;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------ cameras

    /** {x, y, z, yaw, pitch} for the camera, or null for the normal view. */
    public static double[] cameraView(float pt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return null;
        }
        double[] aim = aimView(mc, pt);
        if (phase == Phase.NONE) {
            return aim;
        }
        if (phase == Phase.FOLLOW || phase == Phase.HOLD) {
            Vec3 cam;
            Vec3 look;
            ClientShell s = findShell(camShell);
            if (phase == Phase.FOLLOW && s != null) {
                Vec3 p = s.prev.lerp(s.pos, pt);
                Vec3 v = s.vel.lengthSqr() < 1.0E-6 ? new Vec3(0, -1, 0) : s.vel.normalize();
                Vec3 flat = new Vec3(v.x, 0, v.z);
                flat = flat.lengthSqr() < 1.0E-6 ? new Vec3(0, 0, 1) : flat.normalize();
                cam = p.subtract(flat.scale(9.0)).add(0, 3.5 - v.y * 3.0, 0);
                look = p.add(v.scale(3.0));
            } else {
                cam = lastCamPos != null ? lastCamPos : mc.player.getEyePosition(pt);
                look = impactAt != null ? impactAt : cam.add(0, -1, 1);
            }
            Vec3 wanted = cam;
            cam = keepInRange(mc, cam);
            // held back at the edge of the loaded world: zoom in so the strike still fills the view (up to 3.5x)
            double back = cam.distanceTo(look);
            camZoomWant = wanted.distanceToSqr(cam) > 1.0 ? (float) Mth.clamp(back / 75.0, 1.0, 3.5) : 1.0F;
            camZoom += (camZoomWant - camZoom) * 0.08F;
            double[] r = lookAt(cam, look);
            lastCamPos = cam;
            lastCamYaw = (float) r[3];
            lastCamPitch = (float) r[4];
            return r;
        }
        camZoom = 1.0F;
        // RETURN: ease from where the shell camera left off back to the gun
        if (aim == null || lastCamPos == null) {
            return aim;
        }
        float t = Mth.clamp((phaseTicks + pt) / 12.0F, 0.0F, 1.0F);
        t = t * t * (3 - 2 * t);
        Vec3 from = lastCamPos;
        return new double[]{Mth.lerp(t, from.x, aim[0]), Mth.lerp(t, from.y, aim[1]), Mth.lerp(t, from.z, aim[2]),
                lastCamYaw + Mth.wrapDegrees((float) aim[3] - lastCamYaw) * t, Mth.lerp(t, lastCamPitch, (float) aim[4])};
    }

    /** Behind and above the breech, looking out along the line of fire; up/down follows the range set. */
    private static double[] aimView(Minecraft mc, float pt) {
        MorserEntity gun = operatedGun();
        if (gun == null) {
            return null;
        }
        Player p = mc.player;
        float trav = localTraverse(gun, p, pt);
        float gunYaw = Mth.rotLerp(pt, gun.yRotO, gun.getYRot());
        double yaw = Math.toRadians(gunYaw + trav);
        Vec3 base = new Vec3(Mth.lerp(pt, gun.xo, gun.getX()), Mth.lerp(pt, gun.yo, gun.getY()), Mth.lerp(pt, gun.zo, gun.getZ()));
        if (gun instanceof FieldGunEntity fg) {
            float elev = localRange(fg, p, pt);
            if (mc.options.getCameraType().isFirstPerson()) {
                // eye at the panoramic sight's eyepiece, up over the shield on the left, looking along the barrel
                double e = Math.toRadians(elev);
                Vec3 bore = fg.boreDir(gunYaw, trav, elev);
                Vec3 left = new Vec3(Math.cos(yaw), 0, Math.sin(yaw));
                Vec3 up = new Vec3(Math.sin(yaw) * Math.sin(e), Math.cos(e), -Math.cos(yaw) * Math.sin(e));
                double gy = Math.toRadians(gunYaw);
                Vec3 trunnion = base.add(-Math.sin(gy) * fg.trunnionFwd(), fg.trunnionUp(), Math.cos(gy) * fg.trunnionFwd());
                Vec3 eye = trunnion.add(bore.scale(-8.0 / 16.0)).add(up.scale(13.4 / 16.0)).add(left.scale(2.6 / 16.0));
                return new double[]{eye.x, eye.y, eye.z, gunYaw + trav, -elev};
            }
            Vec3 cam = base.add(Math.sin(yaw) * 4.6, fg.trunnionUp() + 1.9, -Math.cos(yaw) * 4.6);
            return new double[]{cam.x, cam.y, cam.z, gunYaw + trav, p.getViewXRot(pt) * 0.5F + 9.0F};
        }
        Vec3 cam = base.add(Math.sin(yaw) * 6.0, MorserEntity.TRUNNION_UP + 2.6, -Math.cos(yaw) * 6.0);
        float pitch = p.getViewXRot(pt) * 0.6F + 4.0F;
        return new double[]{cam.x, cam.y, cam.z, gunYaw + trav, pitch};
    }

    /** Never further from the player than the client has the world loaded. */
    private static Vec3 keepInRange(Minecraft mc, Vec3 cam) {
        Vec3 me = mc.player.position();
        double max = Math.max(2, mc.options.getEffectiveRenderDistance() - 2) * 16.0;
        Vec3 d = new Vec3(cam.x - me.x, 0, cam.z - me.z);
        if (d.lengthSqr() > max * max) {
            d = d.normalize().scale(max);
            cam = new Vec3(me.x + d.x, cam.y, me.z + d.z);
        }
        return cam;
    }

    private static double[] lookAt(Vec3 cam, Vec3 target) {
        Vec3 d = target.subtract(cam);
        double h = Math.sqrt(d.x * d.x + d.z * d.z);
        float yaw = (float) Math.toDegrees(Math.atan2(-d.x, d.z));
        float pitch = (float) -Math.toDegrees(Math.atan2(d.y, h));
        return new double[]{cam.x, cam.y, cam.z, yaw, pitch};
    }

    /** {yaw, pitch} offsets for screen shake this frame, or null. */
    public static float[] shake(float pt) {
        float a = Mth.lerp(pt, shakeAmpO, shakeAmp);
        if (a <= 0.01F) {
            return null;
        }
        Minecraft mc = Minecraft.getInstance();
        float t = (mc.level != null ? mc.level.getGameTime() : 0) + pt;
        float sx = (float) (Math.sin(t * 2.9 + shakeSeed) + 0.5 * Math.sin(t * 7.3 + shakeSeed * 1.7));
        float sy = (float) (Math.sin(t * 3.7 + shakeSeed * 2.3) + 0.5 * Math.sin(t * 6.1 + shakeSeed * 0.9));
        return new float[]{a * sx * 0.6F, a * sy * 0.6F};
    }

    // ------------------------------------------------------------------ HUD

    private static final net.minecraft.resources.ResourceLocation FK_SCOPE = ArmoredCarMod.id("textures/gui/fk_scope.png");

    /** Looking into the x2 panoramic sight: round eyepiece, black around it, a German-style chevron reticle. */
    private static void drawScope(GuiGraphics g, int w, int h, float t) {
        int cx = w / 2, cy = h / 2;
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        int size = (int) (h * Mth.lerp(t, 1.6F, 0.78F));        // the eyepiece closes in as you put your eye to it
        int x0 = (w - size) / 2, y0 = (h - size) / 2;
        g.setColor(1.0F, 1.0F, 1.0F, t);
        g.blit(FK_SCOPE, x0, y0, size, size, 0.0F, 0.0F, 512, 512, 512, 512);
        int black = ((int) (t * 255) << 24);
        g.fill(0, 0, Math.max(0, x0), h, black);
        g.fill(x0 + size, 0, w, h, black);
        g.fill(Math.max(0, x0), 0, Math.min(w, x0 + size), Math.max(0, y0), black);
        g.fill(Math.max(0, x0), y0 + size, Math.min(w, x0 + size), h, black);
        g.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        int ink = ((int) (t * 235) << 24) | 0x101010;
        int r = (int) (size * 0.43F);
        // horizontal hairline broken at the centre, with range marks
        g.fill(cx - r, cy, cx - 14, cy + 1, ink);
        g.fill(cx + 14, cy, cx + r, cy + 1, ink);
        for (int i = 1; i * 12 < r - 14; i++) {
            int len = i % 5 == 0 ? 5 : 2;
            g.fill(cx - 14 - i * 12, cy - len, cx - 13 - i * 12, cy + 1 + len, ink);
            g.fill(cx + 13 + i * 12, cy - len, cx + 14 + i * 12, cy + 1 + len, ink);
        }
        // inverted V (the aiming chevron), its point on the line of the bore
        for (int i = 0; i < 9; i++) {
            g.fill(cx - i - 1, cy + i, cx - i + 1, cy + i + 1, ink);
            g.fill(cx + i, cy + i, cx + i + 2, cy + i + 1, ink);
        }
        // a post from below up to the chevron
        g.fill(cx, cy + 16, cx + 1, cy + r, ink);
        com.mojang.blaze3d.systems.RenderSystem.disableBlend();
    }

    private static void drawFieldGunHud(GuiGraphics g, Minecraft mc, FieldGunEntity gun, float pt, int w, int h) {
        int cx = w / 2, cy = h / 2;
        int white = 0xFFF0ECDC;
        float scoped = sightView() ? Mth.clamp((Mth.lerp(pt, sightZoomO, sightZoom) - 1.0F) / 1.0F, 0.0F, 1.0F) : 0.0F;
        if (scoped > 0.02F) {
            drawScope(g, w, h, scoped);
        } else {
            g.fill(cx - 7, cy, cx - 2, cy + 1, white);
            g.fill(cx + 2, cy, cx + 7, cy + 1, white);
            g.fill(cx, cy - 7, cx + 1, cy - 2, white);
            g.fill(cx, cy + 2, cx + 1, cy + 7, white);
        }
        float elev = localRange(gun, mc.player, pt);
        List<Vec3> arc = arcCache != null && mc.level != null && mc.level.getGameTime() - arcFrame < 3 ? arcCache : predictedArc(gun, mc.player, pt);
        Vec3 land = arc.get(arc.size() - 1);
        double dist = Math.sqrt((land.x - gun.getX()) * (land.x - gun.getX()) + (land.z - gun.getZ()) * (land.z - gun.getZ()));
        int lx = 12, ly = cy - 26;
        g.pose().pushPose();
        g.pose().translate(lx, ly, 0);
        g.pose().scale(1.5F, 1.5F, 1.0F);
        g.drawString(mc.font, String.format(java.util.Locale.ROOT, "%+.1f\u00b0", elev), 0, 0, white, true);
        g.pose().popPose();
        g.drawString(mc.font, Math.round(dist) + " m", lx, ly + 15, 0xFFB9B6A3, true);
        float trav = localTraverse(gun, mc.player, pt);
        if (Math.abs(trav) >= gun.traverseLimit() - 0.05F) {
            int sx = trav > 0 ? cx + 46 : cx - 48;
            g.fill(sx, cy - 6, sx + 2, cy + 7, 0xFFE0603A);
        }
        int reload = gun.getReload();
        int bw = 60, by = ly + 28;
        float done = reload > 0 ? 1.0F - reload / (float) gun.getReloadTotal() : 1.0F;
        g.fill(lx, by, lx + bw, by + 4, 0x80000000);
        g.fill(lx, by, lx + (int) (bw * done), by + 4, reload > 0 ? 0xFFD9A441 : 0xFF9FD08A);
        Component label = Component.translatable(reload > 0 ? "hud.exodus_vehicles.artillery_reload" : "hud.exodus_vehicles.artillery_ready");
        g.drawString(mc.font, label, lx, by + 7, reload > 0 ? 0xFFB9B6A3 : 0xFF9FD08A, true);
        Component where = Component.translatable("hud.exodus_vehicles.field_target", Mth.floor(land.x), Mth.floor(land.z));
        g.drawString(mc.font, where, cx - mc.font.width(where) / 2, 8, white, true);
        if (ModConfig.get().artilleryRequiresShells && !mc.player.getAbilities().instabuild) {
            int shells = 0;
            for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
                if (mc.player.getInventory().getItem(i).is(ArmoredCarMod.FIELD_SHELL_ITEM)) {
                    shells += mc.player.getInventory().getItem(i).getCount();
                }
            }
            g.drawString(mc.font, Component.translatable("hud.exodus_vehicles.field_shells", shells), 8, 8,
                    shells > 0 ? 0xFFE8E4D0 : 0xFFE0603A, true);
        }
        float hp = gun.getHealth() / gun.getMaxHealth();
        g.fill(8, 20, 68, 22, 0x80000000);
        g.fill(8, 20, 8 + (int) (60 * Math.max(0, Math.min(1, hp))), 22, hp > 0.5F ? 0xFF8D977A : hp > 0.25F ? 0xFFD9A441 : 0xFFC4553A);
    }

    public static void drawOverlay(GuiGraphics g, float pt) {
        Minecraft mc = Minecraft.getInstance();
        int w = g.guiWidth(), h = g.guiHeight();
        // shell shock: a white flash, then the edges of the world go dark for a few seconds
        float f = Mth.lerp(pt, flashO, flash);
        if (f > 0.01F) {
            g.fill(0, 0, w, h, ((int) (Math.min(1.0F, f) * 255) << 24) | 0xFFF4E8);
        }
        if (!SPLATS.isEmpty()) {
            com.mojang.blaze3d.systems.RenderSystem.enableBlend();
            com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
            for (Splat sp : SPLATS) {
                float a = Math.min(1.0F, (sp.life - pt) / 30.0F);
                int px = (int) (sp.size * h);
                int x = (int) (sp.x * w) - px / 2;
                int y = (int) (sp.y * h) - px / 2 + (int) ((sp.maxLife - sp.life + pt) * 0.08F);   // slides down slowly
                g.setColor(1.0F, 1.0F, 1.0F, a);
                g.blit(SPLAT_TEX[sp.tex], x, y, px, px, 0.0F, 0.0F, 128, 128, 128, 128);
            }
            g.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            com.mojang.blaze3d.systems.RenderSystem.disableBlend();
        }
        float d = Mth.lerp(pt, dimO, dim);
        if (d > 0.5F) {
            float k = Math.min(1.0F, d / 60.0F) * 0.55F;
            int edge = (int) (k * 255) << 24;
            int band = Math.max(8, h / 5);
            g.fillGradient(0, 0, w, band, edge, 0);
            g.fillGradient(0, h - band, w, h, 0, edge);
            g.fill(0, 0, w, h, ((int) (k * 0.35F * 255) << 24));
        }
        if (mc.options.hideGui || mc.player == null) {
            return;
        }
        if (hitTicks > 0) {
            // "Successful hit" in red, low on the screen, fading out
            float a = Math.min(1.0F, (hitTicks - pt) / 15.0F);
            int alpha = Math.max(8, (int) (a * 255)) << 24;
            Component msg = Component.translatable(hitLine >= 0 ? "hud.exodus_vehicles.field_kill_" + hitLine
                    : hitKill ? "hud.exodus_vehicles.hit_kill" : "hud.exodus_vehicles.hit");
            int tw = mc.font.width(msg);
            g.pose().pushPose();
            g.pose().translate(w / 2.0F, h - 78, 0);
            g.pose().scale(1.5F, 1.5F, 1.0F);
            g.drawString(mc.font, msg, -tw / 2, 0, alpha | 0xE02A2A, true);
            g.pose().popPose();
        }
        if (mc.player.getVehicle() instanceof MorserEntity gun && !gun.isDeployed()) {
            int work = gun.getWork();
            if (work != 0) {
                Component line = Component.translatable("hud.exodus_vehicles.morser_deploying", Math.abs(work) * 100 / gun.deployTicks());
                g.drawString(mc.font, line, 8, 8, 0xFFE8E4D0, true);
            }
            return;
        }
        MorserEntity gun = operatedGun();
        if (gun == null || phase == Phase.FOLLOW || phase == Phase.HOLD) {
            return;
        }
        int cx = w / 2, cy = h / 2;
        int white = 0xFFF0ECDC;
        if (gun instanceof FieldGunEntity fg) {
            drawFieldGunHud(g, mc, fg, pt, w, h);
            return;
        }
        // reticle: a small open cross; range under it; reload bar under that
        g.fill(cx - 7, cy, cx - 2, cy + 1, white);
        g.fill(cx + 2, cy, cx + 7, cy + 1, white);
        g.fill(cx, cy - 7, cx + 1, cy - 2, white);
        g.fill(cx, cy + 2, cx + 1, cy + 7, white);
        float range = localRange(gun, mc.player, pt);
        // left side: range, and the reload bar with its label under it
        int lx = 12, ly = cy - 20;
        String rs = Math.round(range) + " m";
        g.pose().pushPose();
        g.pose().translate(lx, ly, 0);
        g.pose().scale(1.5F, 1.5F, 1.0F);
        g.drawString(mc.font, rs, 0, 0, white, true);
        g.pose().popPose();
        float trav = localTraverse(gun, mc.player, pt);
        if (Math.abs(trav) >= gun.traverseLimit() - 0.05F) {
            // traverse stop reached on this side
            int sx = trav > 0 ? cx + 18 : cx - 20;
            g.fill(sx, cy - 6, sx + 2, cy + 7, 0xFFE0603A);
        }
        int reload = gun.getReload();
        int bw = 60, by = ly + 18;
        float done = reload > 0 ? 1.0F - reload / (float) gun.getReloadTotal() : 1.0F;
        g.fill(lx, by, lx + bw, by + 4, 0x80000000);
        g.fill(lx, by, lx + (int) (bw * done), by + 4, reload > 0 ? 0xFFD9A441 : 0xFF9FD08A);
        Component label = Component.translatable(reload > 0 ? "hud.exodus_vehicles.artillery_reload" : "hud.exodus_vehicles.artillery_ready");
        g.drawString(mc.font, label, lx, by + 7, reload > 0 ? 0xFFB9B6A3 : 0xFF9FD08A, true);
        // where the shells will fall, top centre (the centre of the scatter circle)
        Vec3 aim = gun.aimPoint(trav, range);
        Component where = Component.translatable("hud.exodus_vehicles.artillery_target",
                Mth.floor(aim.x), Mth.floor(aim.z), Math.round(gun.dispersionAt(range)));
        g.drawString(mc.font, where, cx - mc.font.width(where) / 2, 8, 0xFFF0ECDC, true);
        if (ModConfig.get().artilleryRequiresShells && !mc.player.getAbilities().instabuild) {
            int shells = 0;
            for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
                if (mc.player.getInventory().getItem(i).is(ArmoredCarMod.SHELL_ITEM)) {
                    shells += mc.player.getInventory().getItem(i).getCount();
                }
            }
            g.drawString(mc.font, Component.translatable("hud.exodus_vehicles.artillery_shells", shells), 8, 8,
                    shells > 0 ? 0xFFE8E4D0 : 0xFFE0603A, true);
        }
        float hp = gun.getHealth() / gun.getMaxHealth();
        g.fill(8, 20, 68, 22, 0x80000000);
        g.fill(8, 20, 8 + (int) (60 * Math.max(0, Math.min(1, hp))), 22, hp > 0.5F ? 0xFF8D977A : hp > 0.25F ? 0xFFD9A441 : 0xFFC4553A);
    }
}
