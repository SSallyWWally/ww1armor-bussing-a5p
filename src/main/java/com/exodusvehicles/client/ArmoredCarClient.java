package com.exodusvehicles.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.exodusvehicles.ArmoredCarMod;
import com.exodusvehicles.entity.ArmoredCarEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.exodusvehicles.ModConfig;
import com.exodusvehicles.compat.TaczCompat;
import com.exodusvehicles.network.FireGunPayload;
import com.exodusvehicles.network.ReloadPayload;
import com.exodusvehicles.network.ToggleEnginePayload;
import com.exodusvehicles.network.ToggleBrakePayload;
import com.exodusvehicles.network.TracerPayload;
import com.exodusvehicles.network.SwitchSeatPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

public class ArmoredCarClient implements ClientModInitializer {
    public static KeyMapping SWITCH_SEAT;
    public static KeyMapping RELOAD;
    public static KeyMapping ENGINE;
    public static KeyMapping BRAKE;
    /** Physical key state last tick, per mapping (for edge detection). */
    private static final java.util.Map<KeyMapping, Boolean> WAS_DOWN = new java.util.HashMap<>();

    private static final ResourceLocation SIGHT_ZOOM = ArmoredCarMod.id("textures/gui/sight_zoom.png");
    private static int ammoCache = 0;
    private static int ammoCacheAge = 0;

    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(ArmoredCarModel.LAYER, ArmoredCarModel::createBodyLayer);
        EntityRendererRegistry.register(ArmoredCarMod.EISENWOLF, ArmoredCarRenderer::new);
        EntityRendererRegistry.register(ArmoredCarMod.HULL_SEGMENT, NoopRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(TruckModel.LAYER, TruckModel::createBodyLayer);
        EntityRendererRegistry.register(ArmoredCarMod.DAIMLER_DR, TruckRenderer::new);
        EntityRendererRegistry.register(ArmoredCarMod.TRUCK_SEGMENT, NoopRenderer::new);

        // the engine loop is the car's only driving sound
        ClientEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof ArmoredCarEntity car && !car.isWrecked()) {
                Minecraft.getInstance().getSoundManager()
                        .play(new CarSoundInstance(car, ArmoredCarMod.ENGINE, CarSoundInstance.Kind.ENGINE));
            }
        });

        SWITCH_SEAT = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.exodus_vehicles.switch_seat", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, "key.categories.exodus_vehicles"));
        // not R: TACZ uses R for its own reload
        RELOAD = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.exodus_vehicles.reload", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, "key.categories.exodus_vehicles"));
        ENGINE = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.exodus_vehicles.engine", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_P, "key.categories.exodus_vehicles"));
        BRAKE = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.exodus_vehicles.parking_brake", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, "key.categories.exodus_vehicles"));

        ClientPlayNetworking.registerGlobalReceiver(TracerPayload.TYPE, (payload, context) -> {
            Minecraft mc = context.client();
            if (mc.level != null && mc.level.getEntity(payload.carId()) instanceof ArmoredCarEntity car) {
                net.minecraft.world.phys.Vec3 start = new net.minecraft.world.phys.Vec3(payload.sx(), payload.sy(), payload.sz());
                if (GunSight.inSight() && mc.player != null && car.seatOf(mc.player) == ArmoredCarEntity.GUNNER) {
                    // from the muzzle of the gun as the gunner sees it (closer, just under the sight)
                    net.minecraft.world.phys.Vec3 end = new net.minecraft.world.phys.Vec3(payload.ex(), payload.ey(), payload.ez());
                    net.minecraft.world.phys.Vec3 d = end.subtract(start).normalize();
                    net.minecraft.world.phys.Vec3 right = new net.minecraft.world.phys.Vec3(-d.z, 0.0, d.x).normalize();
                    net.minecraft.world.phys.Vec3 down = d.cross(right).normalize();
                    if (down.y > 0) {
                        down = down.scale(-1.0);
                    }
                    start = start.subtract(d.scale(ArmoredCarEntity.MUZZLE_DIST - ArmoredCarEntity.GUNNER_MUZZLE_DIST))
                            .add(down.scale(ArmoredCarEntity.BARREL_BELOW_SIGHT))
                            .add(right.scale(ArmoredCarEntity.GUNNER_MUZZLE_RIGHT));
                }
                car.tracers.add(new ArmoredCarEntity.Tracer(start,
                        new net.minecraft.world.phys.Vec3(payload.ex(), payload.ey(), payload.ez())));
            }
        });

        // Gunner trigger + sight: runs before vanilla reads the mouse buttons, so clicks don't punch,
        // mine blocks or use items while manning the MG.
        ClientTickEvents.START_CLIENT_TICK.register(mc -> {
            if (mc.player == null) {
                return;
            }
            GunSight.tick(mc);
            if (mc.screen == null && GunSight.isGunner(mc)) {
                boolean trigger = GunSight.physicallyDown(mc, mc.options.keyAttack);
                mc.options.keyAttack.setDown(false);
                while (mc.options.keyAttack.consumeClick()) {
                    // swallow the click
                }
                if (trigger) {
                    ClientPlayNetworking.send(new FireGunPayload());
                }
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            // Vehicle keys are read straight from the keyboard while riding, so another mod bound to the
            // same key (very common for N, G, P...) can't swallow them.
            boolean riding = mc.player != null && mc.player.getVehicle() instanceof ArmoredCarEntity && mc.screen == null;
            if (pressed(mc, SWITCH_SEAT, riding)) {
                ClientPlayNetworking.send(new SwitchSeatPayload());
            }
            if (pressed(mc, ENGINE, riding)) {
                ClientPlayNetworking.send(new ToggleEnginePayload());
            }
            if (pressed(mc, BRAKE, riding)) {
                ClientPlayNetworking.send(new ToggleBrakePayload());
            }
            if (pressed(mc, RELOAD, riding)) {
                ClientPlayNetworking.send(new ReloadPayload());
            }
            for (KeyMapping k : new KeyMapping[]{SWITCH_SEAT, ENGINE, BRAKE, RELOAD}) {
                while (k.consumeClick()) {
                    // handled above from the physical key
                }
            }
            if (mc.player != null && ++ammoCacheAge >= 10) {
                ammoCacheAge = 0;
                ammoCache = TaczCompat.countMgAmmo(mc.player);
            }
        });

        HudRenderCallback.EVENT.register((graphics, tickCounter) -> {
            drawSight(graphics, tickCounter.getGameTimeDeltaPartialTick(true));
            drawHud(graphics);
        });
    }

    /** True on the tick the mapping's bound key goes down (only counted while riding). */
    private static boolean pressed(Minecraft mc, KeyMapping mapping, boolean riding) {
        boolean down = riding && GunSight.physicallyDown(mc, mapping);
        boolean was = WAS_DOWN.getOrDefault(mapping, false);
        WAS_DOWN.put(mapping, down);
        return down && !was;
    }

    /**
     * Gun sight: WW1-style reticle meeting at screen centre (the point the gun is aimed at).
     * Zooming in closes in a dark vision-port vignette.
     */
    private static void drawSight(GuiGraphics g, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || !GunSight.inSight()) {
            return;
        }
        int w = g.guiWidth(), h = g.guiHeight();
        float maxZoom = Math.max(1.001F, ModConfig.get().mgSightZoom);
        float z = GunSight.zoomAmount(partialTick);
        float t = Math.max(0.0F, Math.min(1.0F, (z - 1.0F) / (maxZoom - 1.0F)));

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        // WW1 reticle: one line across the whole sight, and a post from the centre down; no centre dot.
        // A faint light edge keeps it readable against dark ground. Drawn first so the zoom vignette crops it.
        int cx = w / 2, cy = h / 2;
        g.fill(0, cy, w, cy + 1, 0xFF000000);
        g.fill(cx - 1, cy, cx + 1, h, 0xFF000000);
        // the sight circle and black surround are always there in first person; zoom only narrows the view
        {
            int size = (int) (h * 1.15F);
            int x0 = (w - size) / 2, y0 = (h - size) / 2;
            g.blit(SIGHT_ZOOM, x0, y0, size, size, 0.0F, 0.0F, 512, 512, 512, 512);
            g.fill(0, 0, Math.max(0, x0), h, 0xFF000000);
            g.fill(x0 + size, 0, w, h, 0xFF000000);
            g.fill(x0, 0, x0 + size, Math.max(0, y0), 0xFF000000);
            g.fill(x0, y0 + size, x0 + size, h, 0xFF000000);
        }
        RenderSystem.disableBlend();
    }

    private static void drawHud(GuiGraphics g) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) {
            return;
        }
        if (!(mc.player.getVehicle() instanceof ArmoredCarEntity car)) {
            return;
        }
        int seat = Math.max(0, car.seatOf(mc.player));
        int x = 8, y = 8;

        Component seatLine = Component.translatable("hud.exodus_vehicles.seat",
                Component.translatable(car.seatKey(seat)), SWITCH_SEAT.getTranslatedKeyMessage());
        g.drawString(mc.font, seatLine, x, y, 0xFFE8E4D0, true);
        if (seat == ArmoredCarEntity.DRIVER) {
            int sx = x + mc.font.width(seatLine) + 8;
            if (!car.isEngineOn()) {
                g.drawString(mc.font, Component.translatable("hud.exodus_vehicles.engine_off"), sx, y, 0xFFB9B6A3, true);
                sx += mc.font.width(Component.translatable("hud.exodus_vehicles.engine_off")) + 6;
            }
            if (car.isParkingBrakeOn()) {
                g.drawString(mc.font, Component.translatable("hud.exodus_vehicles.parking_brake"), sx, y, 0xFFE0A040, true);
            }
        }

        float hp = car.getHealth(), max = car.getMaxHealth();
        Component hull = Component.translatable(car.healthHudKey(),
                String.valueOf((int) Math.ceil(hp)), String.valueOf((int) max));
        g.drawString(mc.font, hull, x, y + 12, 0xFFE8E4D0, true);

        int barW = 100;
        int filled = (int) (barW * Math.max(0.0F, Math.min(1.0F, hp / max)));
        int color = hp / max > 0.5F ? 0xFF8D977A : hp / max > 0.25F ? 0xFFD9A441 : 0xFFC4553A;
        g.fill(x, y + 23, x + barW, y + 27, 0x80000000);
        g.fill(x, y + 23, x + filled, y + 27, color);

        if (seat == ArmoredCarEntity.GUNNER && car.hasGun()) {
            int gy = y + 34;
            String reserve = ammoCache < 0 ? "\u221e" : String.valueOf(ammoCache);
            g.drawString(mc.font, Component.translatable("hud.exodus_vehicles.mg_ammo",
                    String.valueOf(car.getBelt()), String.valueOf(ModConfig.get().mgBeltSize), reserve),
                    x, gy, 0xFFE8E4D0, true);

            // belt
            int beltFill = (int) (barW * car.getBelt() / (float) Math.max(1, ModConfig.get().mgBeltSize));
            g.fill(x, gy + 11, x + barW, gy + 14, 0x80000000);
            g.fill(x, gy + 11, x + beltFill, gy + 14, 0xFFC9B37A);

            // water-jacket heat
            float heat = car.getHeat();
            int heatColor = car.isOverheated() ? 0xFFE04A2A : heat > 0.7F ? 0xFFE0A030 : 0xFF6FA8C8;
            g.drawString(mc.font, Component.translatable("hud.exodus_vehicles.heat"), x, gy + 18, 0xFFB9B6A3, true);
            int hx = x + 32;
            g.fill(hx, gy + 20, hx + barW - 32, gy + 24, 0x80000000);
            g.fill(hx, gy + 20, hx + (int) ((barW - 32) * heat), gy + 24, heatColor);

            int status = gy + 30;
            if (car.getReloadTicks() > 0) {
                g.drawString(mc.font, Component.translatable("hud.exodus_vehicles.reloading"), x, status, 0xFFE0C060, true);
                status += 11;
            }
            if (car.isOverheated()) {
                boolean blink = (mc.player.tickCount / 5) % 2 == 0;
                g.drawString(mc.font, Component.translatable("hud.exodus_vehicles.overheated"), x, status,
                        blink ? 0xFFFF5A3A : 0xFFB04030, true);
                status += 11;
            }
        }
    }
}
