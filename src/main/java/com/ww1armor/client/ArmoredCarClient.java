package com.ww1armor.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.ww1armor.ArmoredCarMod;
import com.ww1armor.entity.ArmoredCarEntity;
import com.ww1armor.network.FireGunPayload;
import com.ww1armor.network.SwitchSeatPayload;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public class ArmoredCarClient implements ClientModInitializer {
    public static KeyMapping SWITCH_SEAT;

    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(ArmoredCarModel.LAYER, ArmoredCarModel::createBodyLayer);
        EntityRendererRegistry.register(ArmoredCarMod.BUSSING_A5P, ArmoredCarRenderer::new);
        EntityRendererRegistry.register(ArmoredCarMod.HULL_SEGMENT, NoopRenderer::new);

        // engine + rolling sounds start when a car is loaded on this client
        ClientEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof ArmoredCarEntity car && !car.isWrecked()) {
                Minecraft mc = Minecraft.getInstance();
                mc.getSoundManager().play(new CarSoundInstance(car, ArmoredCarMod.ENGINE, CarSoundInstance.Kind.ENGINE));
                mc.getSoundManager().play(new CarSoundInstance(car, SoundEvents.MINECART_RIDING, CarSoundInstance.Kind.ROLL));
            }
        });

        SWITCH_SEAT = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.ww1armor.switch_seat", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, "key.categories.ww1armor"));

        // Gunner trigger: runs before vanilla handles the attack key, so clicks don't punch.
        ClientTickEvents.START_CLIENT_TICK.register(mc -> {
            if (mc.player == null || mc.screen != null) {
                return;
            }
            if (mc.player.getVehicle() instanceof ArmoredCarEntity car
                    && !car.isWrecked()
                    && car.seatOf(mc.player) == ArmoredCarEntity.GUNNER
                    && !isHoldingTaczGun(mc.player.getMainHandItem())) {
                if (mc.options.keyAttack.isDown()) {
                    ClientPlayNetworking.send(new FireGunPayload());
                }
                while (mc.options.keyAttack.consumeClick()) {
                    // swallow the click
                }
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (SWITCH_SEAT.consumeClick()) {
                if (mc.player != null && mc.player.getVehicle() instanceof ArmoredCarEntity) {
                    ClientPlayNetworking.send(new SwitchSeatPayload());
                }
            }
        });

        HudRenderCallback.EVENT.register((graphics, tickCounter) -> drawHud(graphics));
    }

    /** Lets TACZ handle shooting if the gunner is holding one of its guns. */
    private static boolean isHoldingTaczGun(ItemStack stack) {
        return !stack.isEmpty() && "tacz".equals(BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace());
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

        Component seatLine = Component.translatable("hud.ww1armor.seat",
                Component.translatable("seat.ww1armor." + seat), SWITCH_SEAT.getTranslatedKeyMessage());
        g.drawString(mc.font, seatLine, x, y, 0xFFE8E4D0, true);

        float hp = car.getHealth(), max = car.getMaxHealth();
        Component hull = Component.translatable("hud.ww1armor.hull",
                String.valueOf((int) Math.ceil(hp)), String.valueOf((int) max));
        g.drawString(mc.font, hull, x, y + 12, 0xFFE8E4D0, true);

        int barW = 100;
        int filled = (int) (barW * Math.max(0.0F, Math.min(1.0F, hp / max)));
        int color = hp / max > 0.5F ? 0xFF8D977A : hp / max > 0.25F ? 0xFFD9A441 : 0xFFC4553A;
        g.fill(x, y + 23, x + barW, y + 27, 0x80000000);
        g.fill(x, y + 23, x + filled, y + 27, color);

        if (seat == ArmoredCarEntity.GUNNER) {
            g.drawString(mc.font, Component.translatable("hud.ww1armor.gunner_hint"), x, y + 32, 0xFFB9B6A3, true);
        }
    }
}
