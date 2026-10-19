package com.exodusvehicles.client;

import com.exodusvehicles.ArmoredCarMod;
import com.exodusvehicles.ModConfig;
import com.exodusvehicles.artillery.ShellManager;
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

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(ShellFiredPayload.TYPE, (p, ctx) -> {
            ClientShell s = new ClientShell(p.shellId(), p.gunId(), new Vec3(p.x(), p.y(), p.z()), new Vec3(p.vx(), p.vy(), p.vz()));
            SHELLS.add(s);
            Minecraft mc = ctx.client();
            if (mc.player != null && mc.player.getVehicle() != null && mc.player.getVehicle().getId() == p.gunId()) {
                addShake(1.6F);          // the whole gun jumps
                if (ModConfig.get().shellCamera) {
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
            double d = mc.player.getEyePosition().distanceTo(at);
            float r = Math.max(2.0F, p.radius());
            // screen shake reaches well beyond the blast; shell shock only close in
            float s = (float) Math.max(0.0, 1.0 - d / (r * 6.0));
            addShake(7.0F * s * s);
            double near = r * 1.6;
            if (d < near) {
                float k = (float) (1.0 - d / near);
                flash = Math.max(flash, 0.55F * k + 0.25F);
                dim = Math.max(dim, 60.0F + 80.0F * k);
                mc.getSoundManager().play(SimpleSoundInstance.forUI(ArmoredCarMod.SHELL_SHOCK, 1.0F, 0.35F + 0.65F * k));
            }
        });
    }

    private static void addShake(float amp) {
        shakeAmp = Math.min(8.0F, Math.max(shakeAmp, amp));
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

    public static float localRange(MorserEntity gun, Player p, float partialTick) {
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
        float pitch = Mth.clamp(p.getXRot(), MorserEntity.PITCH_FAR, MorserEntity.PITCH_NEAR);
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

    public static void endTick(Minecraft mc) {
        shakeAmpO = shakeAmp;
        shakeAmp *= 0.86F;
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
        // where the shells will fall: a ring the size of the scatter (only where the ground is loaded)
        MorserEntity gun = operatedGun();
        if (gun != null && mc.player.tickCount % 4 == 0) {
            float trav = localTraverse(gun, mc.player, 1.0F);
            float range = localRange(gun, mc.player, 1.0F);
            Vec3 aim = gun.aimPoint(trav, range);
            if (mc.level.hasChunkAt(BlockPos.containing(aim))) {
                float r = gun.dispersionAt(range);
                DustParticleOptions dust = new DustParticleOptions(new Vector3f(1.0F, 0.25F, 0.15F), 3.0F);
                int n = 20;
                for (int i = 0; i < n; i++) {
                    double a = (i + (mc.player.tickCount / 4) % 2 * 0.5) / n * Mth.TWO_PI;
                    double x = aim.x + Math.cos(a) * r, z = aim.z + Math.sin(a) * r;
                    int bx = Mth.floor(x), bz = Mth.floor(z);
                    double y = mc.level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, bx, bz) + 0.3;
                    mc.level.addAlwaysVisibleParticle(dust, true, x, y, z, 0, 0, 0);
                }
                mc.level.addAlwaysVisibleParticle(dust, true, aim.x, aim.y + 0.3, aim.z, 0, 0, 0);
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
            cam = keepInRange(mc, cam);
            double[] r = lookAt(cam, look);
            lastCamPos = cam;
            lastCamYaw = (float) r[3];
            lastCamPitch = (float) r[4];
            return r;
        }
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

    public static void drawOverlay(GuiGraphics g, float pt) {
        Minecraft mc = Minecraft.getInstance();
        int w = g.guiWidth(), h = g.guiHeight();
        // shell shock: a white flash, then the edges of the world go dark for a few seconds
        float f = Mth.lerp(pt, flashO, flash);
        if (f > 0.01F) {
            g.fill(0, 0, w, h, ((int) (Math.min(1.0F, f) * 255) << 24) | 0xFFF4E8);
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
        if (mc.player.getVehicle() instanceof MorserEntity gun && !gun.isDeployed()) {
            int work = gun.getWork();
            Component line = work != 0
                    ? Component.translatable("hud.exodus_vehicles.morser_deploying", Math.abs(work) * 100 / gun.deployTicks())
                    : Component.translatable("hud.exodus_vehicles.morser_packed_help", ArmoredCarClient.ENGINE.getTranslatedKeyMessage());
            g.drawString(mc.font, line, 8, 8, 0xFFE8E4D0, true);
            return;
        }
        MorserEntity gun = operatedGun();
        if (gun == null || phase == Phase.FOLLOW || phase == Phase.HOLD) {
            return;
        }
        int cx = w / 2, cy = h / 2;
        // reticle: a small open cross; range under it; reload bar under that
        int white = 0xFFF0ECDC;
        g.fill(cx - 7, cy, cx - 2, cy + 1, white);
        g.fill(cx + 2, cy, cx + 7, cy + 1, white);
        g.fill(cx, cy - 7, cx + 1, cy - 2, white);
        g.fill(cx, cy + 2, cx + 1, cy + 7, white);
        float range = localRange(gun, mc.player, pt);
        String rs = Math.round(range) + " m";
        g.drawString(mc.font, rs, cx - mc.font.width(rs) / 2, cy + 11, white, true);
        float trav = localTraverse(gun, mc.player, pt);
        if (Math.abs(trav) >= gun.traverseLimit() - 0.05F) {
            // traverse stop reached on this side
            int sx = trav > 0 ? cx + 18 : cx - 20;
            g.fill(sx, cy - 6, sx + 2, cy + 7, 0xFFE0603A);
        }
        int reload = gun.getReload();
        int bw = 40, by = cy + 23;
        if (reload > 0) {
            float done = 1.0F - reload / (float) gun.getReloadTotal();
            g.fill(cx - bw / 2, by, cx + bw / 2, by + 2, 0x80000000);
            g.fill(cx - bw / 2, by, cx - bw / 2 + (int) (bw * done), by + 2, 0xFFD9A441);
        } else {
            Component ready = Component.translatable("hud.exodus_vehicles.artillery_ready");
            g.drawString(mc.font, ready, cx - mc.font.width(ready) / 2, by - 2, 0xFF9FD08A, true);
        }
        // a quiet help line, ammo and the gun's condition
        Component help = Component.translatable("hud.exodus_vehicles.artillery_help",
                ArmoredCarClient.SHELL_CAM.getTranslatedKeyMessage(),
                Component.translatable(ModConfig.get().shellCamera ? "options.on" : "options.off"));
        g.drawString(mc.font, help, 8, 8, 0xFFB9B6A3, true);
        if (ModConfig.get().artilleryRequiresShells && !mc.player.getAbilities().instabuild) {
            int shells = 0;
            for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
                if (mc.player.getInventory().getItem(i).is(ArmoredCarMod.SHELL_ITEM)) {
                    shells += mc.player.getInventory().getItem(i).getCount();
                }
            }
            g.drawString(mc.font, Component.translatable("hud.exodus_vehicles.artillery_shells", shells), 8, 20,
                    shells > 0 ? 0xFFE8E4D0 : 0xFFE0603A, true);
        }
        float hp = gun.getHealth() / gun.getMaxHealth();
        g.fill(8, 32, 68, 34, 0x80000000);
        g.fill(8, 32, 8 + (int) (60 * Math.max(0, Math.min(1, hp))), 34, hp > 0.5F ? 0xFF8D977A : hp > 0.25F ? 0xFFD9A441 : 0xFFC4553A);
    }
}
