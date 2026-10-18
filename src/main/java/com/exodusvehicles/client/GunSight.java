package com.exodusvehicles.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.exodusvehicles.ModConfig;
import com.exodusvehicles.entity.ArmoredCarEntity;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

/**
 * Gunner's sight. In first person the MG 08 iron sight is always shown (no zoom);
 * holding use zooms in (up to 3x). In third person, holding use jumps into the sight, zoomed,
 * and letting go returns to the previous camera.
 */
public final class GunSight {
    private static boolean aiming;
    private static CameraType restoreCamera;
    private static float zoom = 1.0F, zoomO = 1.0F;

    private GunSight() {}

    /** True while the local player is the gunner and the MG (not a TACZ gun) is in their hands. */
    public static boolean isGunner(Minecraft mc) {
        return mc.player != null
                && mc.player.getVehicle() instanceof ArmoredCarEntity car
                && !car.isWrecked()
                && car.seatOf(mc.player) == ArmoredCarEntity.GUNNER
                && !isHoldingTaczGun(mc.player.getMainHandItem());
    }

    public static boolean isHoldingTaczGun(ItemStack stack) {
        return !stack.isEmpty() && "tacz".equals(BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace());
    }

    /** Sight overlay visible: gunner looking through the first-person view. */
    public static boolean inSight() {
        Minecraft mc = Minecraft.getInstance();
        return isGunner(mc) && mc.options.getCameraType().isFirstPerson();
    }

    public static boolean isAiming() {
        return aiming;
    }

    public static float zoomAmount(float partialTick) {
        return Mth.lerp(partialTick, zoomO, zoom);
    }

    /** FOV multiplier applied by the GameRenderer mixin. */
    public static double fovScale() {
        Minecraft mc = Minecraft.getInstance();
        float pt = mc.getTimer().getGameTimeDeltaPartialTick(true);
        float z = zoomAmount(pt);
        return z <= 1.001F ? 1.0 : 1.0 / z;
    }

    /** Mouse sensitivity multiplier while zoomed. */
    public static double sensitivityScale() {
        return zoom <= 1.001F ? 1.0 : 1.0 / zoom;
    }

    /** Physical state of a key or mouse button, independent of the KeyMapping's own state. */
    public static boolean physicallyDown(Minecraft mc, KeyMapping mapping) {
        InputConstants.Key key = KeyBindingHelper.getBoundKeyOf(mapping);
        long window = mc.getWindow().getWindow();
        if (key.getType() == InputConstants.Type.MOUSE) {
            return GLFW.glfwGetMouseButton(window, key.getValue()) == GLFW.GLFW_PRESS;
        }
        if (key.getType() == InputConstants.Type.KEYSYM && key.getValue() != InputConstants.UNKNOWN.getValue()) {
            return InputConstants.isKeyDown(window, key.getValue());
        }
        return false;
    }

    /** Called at the start of every client tick (before vanilla reads the use key). */
    public static void tick(Minecraft mc) {
        boolean gunner = mc.screen == null && isGunner(mc);
        boolean useDown = false;
        if (gunner) {
            useDown = physicallyDown(mc, mc.options.keyUse);
            // swallow right clicks so they don't eat food or open things while aiming
            mc.options.keyUse.setDown(false);
            while (mc.options.keyUse.consumeClick()) {
            }
            // keep the view inside the MG's elevation limits so the sight matches where rounds go
            ModConfig cfg = ModConfig.get();
            float pitch = Mth.clamp(mc.player.getXRot(), -cfg.mgMaxElevation, cfg.mgMaxDepression);
            if (pitch != mc.player.getXRot()) {
                mc.player.setXRot(pitch);
                mc.player.xRotO = Mth.clamp(mc.player.xRotO, -cfg.mgMaxElevation, cfg.mgMaxDepression);
            }
        }

        if (useDown && !aiming) {
            aiming = true;
            if (!mc.options.getCameraType().isFirstPerson()) {
                restoreCamera = mc.options.getCameraType();
                mc.options.setCameraType(CameraType.FIRST_PERSON);
            }
        } else if (!useDown && aiming) {
            aiming = false;
            if (restoreCamera != null) {
                mc.options.setCameraType(restoreCamera);
                restoreCamera = null;
            }
        }

        zoomO = zoom;
        float target = aiming && mc.options.getCameraType().isFirstPerson() ? ModConfig.get().mgSightZoom : 1.0F;
        zoom += (target - zoom) * 0.45F;
        if (Math.abs(target - zoom) < 0.01F) {
            zoom = target;
        }
    }
}
