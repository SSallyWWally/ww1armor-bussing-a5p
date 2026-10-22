package com.exodusvehicles.client;

import com.exodusvehicles.ArmoredCarMod;
import com.exodusvehicles.entity.PlaneEntity;
import com.exodusvehicles.network.FireGunPayload;
import com.exodusvehicles.network.PlaneInputPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Flying the Fokker: mouse aim (the aircraft turns toward where you look), throttle held where you leave it (W / S),
 * rudder (A / D), free look (Z by default, rebindable). First person sits you in the cockpit behind the twin guns with
 * a gun sight in the middle; third person chases the aircraft along your view with the gun sight drawn where the guns
 * actually point (just above the aircraft). Also the engine, fly-by and gun sounds for every aircraft nearby.
 */
public final class PlaneClient {
    private PlaneClient() {}

    public static KeyMapping FREELOOK;
    private static boolean freelook;
    private static float holdYaw, holdPitch;
    /** Last camera this frame (for projecting the sights onto the HUD). */
    private static Vec3 camPos = Vec3.ZERO;
    private static float camYaw, camPitch, camRoll;
    private static boolean camValid;

    public static void init() {
        FREELOOK = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.exodus_vehicles.freelook",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.categories.exodus_vehicles"));
    }

    @Nullable
    public static PlaneEntity plane() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && mc.player.getVehicle() instanceof PlaneEntity p ? p : null;
    }

    public static boolean piloting() {
        return plane() != null;
    }

    private static boolean cockpitView() {
        return Minecraft.getInstance().options.getCameraType().isFirstPerson();
    }

    // ------------------------------------------------------------------ input

    public static void startTick(Minecraft mc) {
        PlaneEntity p = plane();
        if (p == null || mc.player == null) {
            freelook = false;
            return;
        }
        if (mc.screen == null) {
            boolean trigger = GunSight.physicallyDown(mc, mc.options.keyAttack);
            mc.options.keyAttack.setDown(false);
            while (mc.options.keyAttack.consumeClick()) {
                // no punching from the cockpit
            }
            mc.options.keyUse.setDown(false);
            while (mc.options.keyUse.consumeClick()) {
                // nor using items
            }
            if (trigger) {
                ClientPlayNetworking.send(new FireGunPayload());
            }
            boolean fl = GunSight.physicallyDown(mc, FREELOOK);
            while (FREELOOK.consumeClick()) {
                // read physically above
            }
            if (fl && !freelook) {
                holdYaw = mc.player.getYRot();
                holdPitch = mc.player.getXRot();
            } else if (!fl && freelook) {
                // back from looking around: aim where we were steering, so the aircraft doesn't swerve
                mc.player.setYRot(holdYaw);
                mc.player.yRotO = holdYaw;
                mc.player.setXRot(holdPitch);
                mc.player.xRotO = holdPitch;
            }
            freelook = fl;
        }
        boolean up = mc.screen == null && GunSight.physicallyDown(mc, mc.options.keyUp);
        boolean down = mc.screen == null && GunSight.physicallyDown(mc, mc.options.keyDown);
        boolean left = mc.screen == null && GunSight.physicallyDown(mc, mc.options.keyLeft);
        boolean right = mc.screen == null && GunSight.physicallyDown(mc, mc.options.keyRight);
        p.inThrottle = up ? 1.0F : down ? -1.0F : 0.0F;
        p.inRudder = right ? 1.0F : left ? -1.0F : 0.0F;
        p.inYaw = freelook ? holdYaw : mc.player.getYRot();
        p.inPitch = freelook ? holdPitch : mc.player.getXRot();
        p.hasInput = true;
    }

    public static void endTick(Minecraft mc) {
        PlaneEntity p = plane();
        if (p != null && p.isControlledByLocalInstance()) {
            ClientPlayNetworking.send(new PlaneInputPayload(p.throttle, p.roll, p.crashedLocally));
            p.crashedLocally = false;
        }
        tickSounds(mc);
    }

    // ------------------------------------------------------------------ cameras

    /** {x, y, z, yaw, pitch, detached(1/0)} for the pilot's camera, or null. */
    @Nullable
    public static double[] cameraView(float pt) {
        Minecraft mc = Minecraft.getInstance();
        PlaneEntity p = plane();
        camValid = false;
        if (p == null || mc.player == null) {
            return null;
        }
        Vec3 base = p.getPosition(pt);
        float yaw = Mth.rotLerp(pt, p.yRotO, p.getYRot());
        float pitch = Mth.lerp(pt, p.xRotO, p.getXRot()) - Mth.lerp(pt, p.groundTiltO, p.groundTilt);
        float roll = Mth.lerp(pt, p.rollO, p.roll);
        float lookYaw = mc.player.getViewYRot(pt), lookPitch = mc.player.getViewXRot(pt);
        double[] out;
        if (cockpitView()) {
            Vec3 eye = PlaneEntity.bodyPoint(base, yaw, pitch, roll, PlaneEntity.EYE[0], PlaneEntity.EYE[1], PlaneEntity.EYE[2]);
            if (freelook) {
                out = new double[]{eye.x, eye.y, eye.z, lookYaw, lookPitch, 0};
                camRoll = 0.0F;
            } else {
                out = new double[]{eye.x, eye.y, eye.z, yaw, pitch, 0};
                camRoll = roll;
            }
        } else {
            // chase camera along the pilot's view, a little above
            Vec3 cg = base.add(0, PlaneEntity.CG_UP, 0);
            Vec3 look = Vec3.directionFromRotation(lookPitch, lookYaw);
            Vec3 want = cg.subtract(look.scale(14.0)).add(0, 3.2, 0);
            BlockHitResult hit = mc.level.clip(new ClipContext(cg, want, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, mc.player));
            Vec3 cam = want;
            if (hit.getType() != HitResult.Type.MISS) {
                Vec3 d = want.subtract(cg);
                cam = cg.add(d.normalize().scale(Math.max(1.0, hit.getLocation().distanceTo(cg) - 0.3)));
            }
            out = new double[]{cam.x, cam.y, cam.z, lookYaw, lookPitch, 1};
            camRoll = 0.0F;
        }
        camPos = new Vec3(out[0], out[1], out[2]);
        camYaw = (float) out[3];
        camPitch = (float) out[4];
        camValid = true;
        return out;
    }

    /** Bank applied to the view in the cockpit (GameRenderer mixin). */
    public static float viewRoll() {
        return camValid && piloting() && cockpitView() && !freelook ? camRoll : 0.0F;
    }

    // ------------------------------------------------------------------ HUD

    /** Screen position of a world point for the current camera, or null if behind. */
    @Nullable
    private static float[] project(Vec3 world, int w, int h) {
        Minecraft mc = Minecraft.getInstance();
        Vec3 f = Vec3.directionFromRotation(camPitch, camYaw);
        double yr = Math.toRadians(camYaw);
        Vec3 r = new Vec3(-Math.cos(yr), 0, -Math.sin(yr));
        Vec3 u = r.cross(f).normalize();
        if (u.y < 0) {
            u = u.scale(-1);
        }
        Vec3 d = world.subtract(camPos);
        double z = d.dot(f);
        if (z < 0.1) {
            return null;
        }
        double x = d.dot(r), y = d.dot(u);
        double rr = Math.toRadians(camRoll);
        double x2 = x * Math.cos(rr) - y * Math.sin(rr), y2 = x * Math.sin(rr) + y * Math.cos(rr);
        double fov = Math.toRadians(mc.options.fov().get());
        double k = (h / 2.0) / Math.tan(fov / 2.0);
        return new float[]{(float) (w / 2.0 + x2 / z * k), (float) (h / 2.0 - y2 / z * k)};
    }

    private static void ring(GuiGraphics g, float cx, float cy, int r, int col) {
        for (int i = 0; i < 36; i++) {
            double a = i * Math.PI * 2 / 36;
            int x = Math.round(cx + (float) Math.cos(a) * r), y = Math.round(cy + (float) Math.sin(a) * r);
            g.fill(x, y, x + 1, y + 1, col);
        }
    }

    private static void cross(GuiGraphics g, float cx, float cy, int s, int col) {
        int x = Math.round(cx), y = Math.round(cy);
        g.fill(x - s, y, x - 1, y + 1, col);
        g.fill(x + 2, y, x + s + 1, y + 1, col);
        g.fill(x, y - s, x + 1, y - 1, col);
        g.fill(x, y + 2, x + 1, y + s + 1, col);
    }

    public static void drawOverlay(GuiGraphics g, float pt) {
        Minecraft mc = Minecraft.getInstance();
        PlaneEntity p = plane();
        if (p == null || mc.options.hideGui || !camValid) {
            return;
        }
        int w = g.guiWidth(), h = g.guiHeight();
        int white = 0xF0F0ECDC, aimCol = 0xC0F0ECDC;
        float yaw = Mth.rotLerp(pt, p.yRotO, p.getYRot());
        float pitch = Mth.lerp(pt, p.xRotO, p.getXRot()) - Mth.lerp(pt, p.groundTiltO, p.groundTilt);
        float roll = Mth.lerp(pt, p.rollO, p.roll);
        Vec3 base = p.getPosition(pt);
        // the guns fire along the pilot's line of sight down the nose: dead centre in the cockpit, just above the
        // aircraft from outside
        Vec3 gunsAt = PlaneEntity.bodyPoint(base, yaw, pitch, roll, PlaneEntity.EYE[0], PlaneEntity.EYE[1], PlaneEntity.EYE[2])
                .add(Vec3.directionFromRotation(pitch, yaw).scale(150.0));
        float sy = freelook ? holdYaw : mc.player.getViewYRot(pt), sp = freelook ? holdPitch : mc.player.getViewXRot(pt);
        Vec3 steerAt = base.add(0, PlaneEntity.CG_UP, 0).add(Vec3.directionFromRotation(sp, sy).scale(200.0));
        float[] gs = cockpitView() && !freelook ? new float[]{w / 2.0F, h / 2.0F} : project(gunsAt, w, h);
        if (gs != null) {
            ring(g, gs[0], gs[1], 9, white);           // gun sight
            g.fill(Math.round(gs[0]), Math.round(gs[1]), Math.round(gs[0]) + 1, Math.round(gs[1]) + 1, white);
        }
        // top left, like the Eisenwolf: seat, then engine / brake state
        Component seat = Component.translatable("seat.exodus_vehicles.plane.pilot");
        g.drawString(mc.font, seat, 8, 8, 0xFFE8E4D0, true);
        int sx = 8 + mc.font.width(seat) + 8;
        if (!p.isEngineOn() && !p.isDowned()) {
            Component off = Component.translatable("hud.exodus_vehicles.engine_off");
            g.drawString(mc.font, off, sx, 8, 0xFFB9B6A3, true);
            sx += mc.font.width(off) + 6;
        }
        if (p.isParkingBrakeOn()) {
            g.drawString(mc.font, Component.translatable("hud.exodus_vehicles.parking_brake"), sx, 8, 0xFFE0A040, true);
        }
        // instruments, bottom left: speed, height, throttle, guns, airframe
        int x = 10, y = h - 64;
        double kmh = (p.isControlledByLocalInstance() ? p.speed : p.syncedSpeed()) * 20.0 * 3.6;
        g.drawString(mc.font, Component.translatable("hud.exodus_vehicles.plane_speed", Math.round(kmh)), x, y, white, true);
        g.drawString(mc.font, Component.translatable("hud.exodus_vehicles.plane_alt", Math.round(p.getY())), x, y + 11, white, true);
        g.drawString(mc.font, Component.translatable("hud.exodus_vehicles.plane_throttle"), x, y + 24, 0xFFB9B6A3, true);
        int bx = x + 52;
        g.fill(bx, y + 26, bx + 60, y + 30, 0x80000000);
        g.fill(bx, y + 26, bx + Math.round(60 * p.throttle), y + 30, 0xFFC9A04A);
        float heat = p.getHeat();
        g.drawString(mc.font, Component.translatable("hud.exodus_vehicles.heat"), x, y + 35, 0xFFB9B6A3, true);
        g.fill(bx, y + 37, bx + 60, y + 41, 0x80000000);
        g.fill(bx, y + 37, bx + Math.round(60 * heat), y + 41, p.isOverheated() ? 0xFFE04A2A : heat > 0.7F ? 0xFFE0A030 : 0xFF6FA8C8);
        float hp = p.getHealth() / p.getMaxHealth();
        g.fill(x, y + 48, x + 112, y + 51, 0x80000000);
        g.fill(x, y + 48, x + Math.round(112 * Mth.clamp(hp, 0, 1)), y + 51, hp > 0.5F ? 0xFF8D977A : hp > 0.25F ? 0xFFD9A441 : 0xFFC4553A);
        if (freelook) {
            Component fl = Component.translatable("hud.exodus_vehicles.plane_freelook");
            g.drawString(mc.font, fl, w / 2 - mc.font.width(fl) / 2, 8, 0xFFE0C060, true);
        }
        if (p.speed < com.exodusvehicles.ModConfig.get().planeStallSpeed && !p.onGround() && p.isControlledByLocalInstance()) {
            boolean blink = (mc.player.tickCount / 5) % 2 == 0;
            Component s = Component.translatable("hud.exodus_vehicles.plane_stall");
            g.drawString(mc.font, s, w / 2 - mc.font.width(s) / 2, h / 2 + 30, blink ? 0xFFFF5A3A : 0xFFB04030, true);
        }
    }

    // ------------------------------------------------------------------ sounds

    private static final Map<Integer, PlaneLoopSound[]> ENGINES = new HashMap<>();
    private static final Map<Integer, Integer> FLYBY_COOLDOWN = new HashMap<>();
    private static final Map<Integer, Double> LAST_DIST = new HashMap<>();

    private static void tickSounds(Minecraft mc) {
        if (mc.level == null || mc.player == null) {
            ENGINES.clear();
            return;
        }
        Set<Integer> seen = new HashSet<>();
        Vec3 me = mc.gameRenderer.getMainCamera().getPosition();
        for (PlaneEntity p : mc.level.getEntitiesOfClass(PlaneEntity.class, mc.player.getBoundingBox().inflate(640.0))) {
            seen.add(p.getId());
            PlaneLoopSound[] s = ENGINES.computeIfAbsent(p.getId(), k -> new PlaneLoopSound[5]);
            PlaneLoopSound.Kind[] kinds = PlaneLoopSound.Kind.values();
            net.minecraft.sounds.SoundEvent[] evs = {ArmoredCarMod.PLANE_ENG_LOW_EXT, ArmoredCarMod.PLANE_ENG_HIGH_EXT,
                    ArmoredCarMod.PLANE_ENG_LOW_INT, ArmoredCarMod.PLANE_ENG_HIGH_INT, ArmoredCarMod.PLANE_ENG_DISTANT};
            if (mc.player.tickCount % 10 == 0) {
                for (int i = 0; i < 5; i++) {
                    if (s[i] == null || !s[i].isFor(p) || !mc.getSoundManager().isActive(s[i])) {
                        if (s[i] != null) {
                            mc.getSoundManager().stop(s[i]);
                        }
                        s[i] = new PlaneLoopSound(p, evs[i], kinds[i]);
                        mc.getSoundManager().play(s[i]);
                    }
                }
            }
            // fly-by: someone else's aircraft roaring past close
            if (!p.hasPassenger(mc.player) && p.getPilot() != null) {
                double d = me.distanceTo(p.position());
                Double last = LAST_DIST.put(p.getId(), d);
                int cd = FLYBY_COOLDOWN.getOrDefault(p.getId(), 0);
                if (cd > 0) {
                    FLYBY_COOLDOWN.put(p.getId(), cd - 1);
                } else if (last != null && d < 45.0 && d > last && last < 45.0 && p.syncedSpeed() > 0.8F) {
                    float vol = (float) Mth.clamp(1.6 - d / 40.0, 0.4, 1.6);
                    mc.getSoundManager().play(new SimpleSoundInstance(ArmoredCarMod.PLANE_FLYBY, net.minecraft.sounds.SoundSource.PLAYERS,
                            vol, 0.95F + p.getRandom().nextFloat() * 0.1F, SoundInstance.createUnseededRandom(), p.getX(), p.getY() + 1.4, p.getZ()));
                    FLYBY_COOLDOWN.put(p.getId(), 120);
                }
            }
        }
        ENGINES.entrySet().removeIf(e -> {
            if (seen.contains(e.getKey())) {
                return false;
            }
            for (PlaneLoopSound s : e.getValue()) {
                if (s != null) {
                    mc.getSoundManager().stop(s);
                }
            }
            return true;
        });
        LAST_DIST.keySet().retainAll(seen);
    }
}
