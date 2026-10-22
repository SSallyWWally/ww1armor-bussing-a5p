package com.exodusvehicles.client;

import com.exodusvehicles.ModConfig;
import com.exodusvehicles.compat.TaczCompat;
import com.exodusvehicles.entity.FieldMgEntity;
import com.exodusvehicles.network.FireGunPayload;
import com.exodusvehicles.network.SuppressPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Client side of the MG 08 field gun: the gunner's view down the iron sights (right-click to bring the eye in closer),
 * the trigger, the HUD, the gunner's hands on the spade grips, and being suppressed (for everyone).
 */
public final class FieldMgClient {
    private FieldMgClient() {}

    private static float zoom = 1.0F, zoomO = 1.0F;
    private static boolean zoomHeld;
    private static int ammoCache, ammoAge;

    // suppression felt by the local player
    private static float supp, suppO;
    private static int suppHold;
    private static long seed = 7;

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(SuppressPayload.TYPE, (p, ctx) -> {
            supp = Math.min(1.0F, supp + p.amount());
            suppHold = 20;
            seed++;
        });
    }

    @Nullable
    public static FieldMgEntity gun() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && mc.player.getVehicle() instanceof FieldMgEntity mg ? mg : null;
    }

    /** Gunner looking down the sights (first person). */
    public static boolean onSights() {
        Minecraft mc = Minecraft.getInstance();
        return gun() != null && mc.options.getCameraType().isFirstPerson();
    }

    public static void startTick(Minecraft mc) {
        FieldMgEntity mg = gun();
        if (mg == null || mc.screen != null) {
            zoomHeld = false;
            return;
        }
        boolean trigger = GunSight.physicallyDown(mc, mc.options.keyAttack);
        mc.options.keyAttack.setDown(false);
        while (mc.options.keyAttack.consumeClick()) {
            // no punching from the gun
        }
        if (trigger) {
            ClientPlayNetworking.send(new FireGunPayload());
        }
        zoomHeld = GunSight.physicallyDown(mc, mc.options.keyUse);
        mc.options.keyUse.setDown(false);
        while (mc.options.keyUse.consumeClick()) {
            // swallow
        }
        float pitch = FieldMgEntity.clampPitch(mc.player.getXRot());
        if (pitch != mc.player.getXRot()) {
            mc.player.setXRot(pitch);
            mc.player.xRotO = pitch;
        }
    }

    public static void endTick(Minecraft mc) {
        zoomO = zoom;
        float target = zoomHeld && onSights() ? Math.max(1.0F, ModConfig.get().fieldMgSightZoom) : 1.0F;
        zoom += (target - zoom) * 0.45F;
        if (Math.abs(target - zoom) < 0.01F) {
            zoom = target;
        }
        suppO = supp;
        if (suppHold > 0) {
            suppHold--;
        } else {
            supp = Math.max(0.0F, supp - 0.02F);
        }
        if (mc.player != null && gun() != null && ++ammoAge >= 10) {
            ammoAge = 0;
            ammoCache = TaczCompat.countMgAmmo(mc.player);
        }
    }

    public static double fovScale(float pt) {
        float z = Mth.lerp(pt, zoomO, zoom);
        return z <= 1.001F ? 1.0 : 1.0 / z;
    }

    public static double sensitivityScale() {
        return zoom <= 1.001F ? 1.0 : 1.0 / zoom;
    }

    /** {x, y, z, yaw, pitch}: the gunner's eye just behind the rear sight; null when not on the sights. */
    @Nullable
    public static double[] cameraView(float pt) {
        Minecraft mc = Minecraft.getInstance();
        FieldMgEntity mg = gun();
        if (mg == null || !mc.options.getCameraType().isFirstPerson()) {
            return null;
        }
        float viewYaw = mc.player.getViewYRot(pt);
        float rel = mg.relYaw(viewYaw);
        float pitch = FieldMgEntity.clampPitch(mc.player.getViewXRot(pt));
        Vec3 base = new Vec3(Mth.lerp(pt, mg.xo, mg.getX()), Mth.lerp(pt, mg.yo, mg.getY()), Mth.lerp(pt, mg.zo, mg.getZ()));
        float sled = Mth.rotLerp(pt, mg.yRotO, mg.getYRot());
        Vec3 eye = mg.gunPoint(base, sled, rel, pitch, 0.0, FieldMgEntity.EYE_UP, -FieldMgEntity.EYE_BACK);
        return new double[]{eye.x, eye.y, eye.z, sled + rel, pitch};
    }

    /** Camera jitter while suppressed. */
    @Nullable
    public static float[] shake(float pt) {
        float s = Mth.lerp(pt, suppO, supp);
        if (s < 0.02F) {
            return null;
        }
        Minecraft mc = Minecraft.getInstance();
        float t = (mc.level != null ? mc.level.getGameTime() : 0) + pt;
        float a = 0.9F * s * s;
        return new float[]{(float) (Math.sin(t * 9.1 + seed) * a), (float) (Math.sin(t * 7.7 + seed * 1.3) * a)};
    }

    /** Both hands on the spade grips: {right xRot, right yRot, left xRot, left yRot}, or null. */
    @Nullable
    public static float[] armPose(LivingEntity entity) {
        if (!(entity.getVehicle() instanceof FieldMgEntity mg)) {
            return null;
        }
        Minecraft mc = Minecraft.getInstance();
        float pt = mc.getTimer().getGameTimeDeltaPartialTick(true);
        float rel = mg.aimYaw(), pitch = mg.aimPitch();
        if (mc.player == entity) {
            rel = mg.relYaw(entity.getViewYRot(pt));
            pitch = FieldMgEntity.clampPitch(entity.getViewXRot(pt));
        }
        Vec3 base = mg.getPosition(pt);
        float sled = Mth.rotLerp(pt, mg.yRotO, mg.getYRot());
        double by = Math.toRadians(Mth.rotLerp(pt, entity.yBodyRotO, entity.yBodyRot));
        Vec3 fwd = new Vec3(-Math.sin(by), 0, Math.cos(by));
        Vec3 right = new Vec3(-Math.cos(by), 0, -Math.sin(by));
        Vec3 pos = entity.getPosition(pt);
        float[] out = new float[4];
        for (int side = 0; side < 2; side++) {
            double s = side == 0 ? 1 : -1;          // 0 = right arm
            Vec3 grip = mg.gunPoint(base, sled, rel, pitch, s * 1.1 / 16.0, 1.4 / 16.0, -9.0 / 16.0);
            Vec3 shoulder = pos.add(right.scale(s * 5.0 / 16.0 * 0.9375)).add(0, 22.0 / 16.0 * 0.9375, 0);
            Vec3 d = grip.subtract(shoulder);
            double mx = -d.dot(right), my = -d.y, mz = -d.dot(fwd);
            double len = Math.sqrt(mx * mx + my * my + mz * mz);
            if (len < 1.0E-4) {
                return null;
            }
            out[side * 2] = (float) -Math.acos(Mth.clamp(my / len, -1.0, 1.0));
            out[side * 2 + 1] = (float) Math.atan2(-mx / len, -mz / len);
        }
        return out;
    }

    // ------------------------------------------------------------------ HUD

    public static void drawOverlay(GuiGraphics g, float pt) {
        Minecraft mc = Minecraft.getInstance();
        int w = g.guiWidth(), h = g.guiHeight();
        // being suppressed: the edges of the world close in and darken
        float s = Mth.lerp(pt, suppO, supp);
        if (s > 0.01F) {
            int band = (int) (Math.max(w, h) * (0.12F + 0.28F * s));
            int edge = ((int) (Math.min(1.0F, s) * 0.85F * 255) << 24) | 0x080604;
            g.fillGradient(0, 0, w, band, edge, 0);
            g.fillGradient(0, h - band, w, h, 0, edge);
            // sides: horizontal gradients drawn as thin vertical strips
            int strips = 24;
            for (int i = 0; i < strips; i++) {
                float k = 1.0F - i / (float) strips;
                int a = (int) (k * k * Math.min(1.0F, s) * 0.85F * 255);
                int sw = Math.max(1, band / strips);
                g.fill(i * sw, 0, (i + 1) * sw, h, (a << 24) | 0x080604);
                g.fill(w - (i + 1) * sw, 0, w - i * sw, h, (a << 24) | 0x080604);
            }
            g.fill(0, 0, w, h, ((int) (s * 0.18F * 255) << 24) | 0x0A0806);
        }
        FieldMgEntity mg = gun();
        if (mg == null || mc.options.hideGui) {
            return;
        }
        ModConfig cfg = ModConfig.get();
        int x = 8, y = 8, barW = 100;
        String reserve = ammoCache < 0 ? "∞" : String.valueOf(ammoCache);
        g.drawString(mc.font, Component.translatable("hud.exodus_vehicles.mg_ammo", String.valueOf(mg.getBelt()),
                String.valueOf(cfg.mgBeltSize), reserve), x, y, 0xFFE8E4D0, true);
        g.fill(x, y + 11, x + barW, y + 14, 0x80000000);
        g.fill(x, y + 11, x + (int) (barW * mg.getBelt() / (float) Math.max(1, cfg.mgBeltSize)), y + 14, 0xFFC9B37A);
        float heat = mg.getHeat();
        int heatColor = mg.isOverheated() ? 0xFFE04A2A : heat > 0.7F ? 0xFFE0A030 : 0xFF6FA8C8;
        g.drawString(mc.font, Component.translatable("hud.exodus_vehicles.heat"), x, y + 18, 0xFFB9B6A3, true);
        g.fill(x + 32, y + 20, x + barW, y + 24, 0x80000000);
        g.fill(x + 32, y + 20, x + 32 + (int) ((barW - 32) * heat), y + 24, heatColor);
        int status = y + 30;
        if (mg.getReloadTicks() > 0) {
            g.drawString(mc.font, Component.translatable("hud.exodus_vehicles.reloading"), x, status, 0xFFE0C060, true);
            status += 11;
        }
        if (mg.isOverheated()) {
            boolean blink = (mc.player.tickCount / 5) % 2 == 0;
            g.drawString(mc.font, Component.translatable("hud.exodus_vehicles.overheated"), x, status, blink ? 0xFFFF5A3A : 0xFFB04030, true);
        }
        float hp = mg.getHealth() / mg.getMaxHealth();
        g.fill(x, h - 14, x + 60, h - 12, 0x80000000);
        g.fill(x, h - 14, x + (int) (60 * Mth.clamp(hp, 0, 1)), h - 12, hp > 0.5F ? 0xFF8D977A : hp > 0.25F ? 0xFFD9A441 : 0xFFC4553A);
    }
}
