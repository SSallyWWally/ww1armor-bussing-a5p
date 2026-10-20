package com.exodusvehicles.compat;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import com.exodusvehicles.ModConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * Soft TACZ integration (no compile-time dependency).
 * Works out which TACZ gun / ammo produced a hit so the armor can let anti-tank weapons through.
 */
public final class TaczCompat {
    private static final String NS = "tacz";

    private TaczCompat() {}

    private static Boolean loaded;

    public static boolean isLoaded() {
        if (loaded == null) {
            loaded = FabricLoader.getInstance().isModLoaded("tacz");
        }
        return loaded;
    }

    // ------------------------------------------------------------------ MG 08 ammunition

    /** Rounds available to the gunner, or -1 for unlimited (creative, creative ammo box, config). */
    public static int countMgAmmo(Player player) {
        if (isFree(player)) {
            return -1;
        }
        int total = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(com.exodusvehicles.ArmoredCarMod.MG_BELT_ITEM)) {
                total += stack.getCount() * com.exodusvehicles.ModConfig.get().mgBeltSize;
                continue;
            }
            int n = roundsIn(stack);
            if (n < 0) {
                return -1;
            }
            total += n;
        }
        int off = roundsIn(player.getOffhandItem());
        return off < 0 ? -1 : total + off;
    }

    /** Removes up to {@code wanted} rounds from the gunner's inventory and returns how many were taken. */
    public static int takeMgAmmo(Player player, int wanted) {
        if (isFree(player)) {
            return wanted;
        }
        java.util.List<ItemStack> stacks = new java.util.ArrayList<>(player.getInventory().items);
        stacks.add(player.getOffhandItem());
        for (ItemStack stack : stacks) {
            if (roundsIn(stack) < 0) {
                return wanted;     // creative / infinite ammo box: never runs dry
            }
        }
        // a belt from the mod's own ammunition comes first: one belt fills the gun
        for (ItemStack stack : stacks) {
            if (stack.is(com.exodusvehicles.ArmoredCarMod.MG_BELT_ITEM)) {
                stack.shrink(1);
                player.getInventory().setChanged();
                return wanted;
            }
        }
        int taken = 0;
        // loose cartridges first, then ammo boxes
        for (int pass = 0; pass < 2 && taken < wanted; pass++) {
            for (ItemStack stack : stacks) {
                if (taken >= wanted) {
                    break;
                }
                if (stack.isEmpty() || !isTaczItem(stack) || isAmmoBox(stack) != (pass == 1)) {
                    continue;
                }
                int have = roundsIn(stack);
                if (have <= 0) {
                    continue;
                }
                int take = Math.min(have, wanted - taken);
                if (pass == 0) {
                    stack.shrink(take);
                } else {
                    setBoxCount(stack, have - take);
                }
                taken += take;
            }
        }
        if (taken > 0) {
            player.getInventory().setChanged();
        }
        return taken;
    }

    private static boolean isFree(Player player) {
        ModConfig cfg = ModConfig.get();
        return cfg.mgInfiniteAmmo || player.getAbilities().instabuild || (!isLoaded() && cfg.mgFreeAmmoWithoutTacz);
    }

    /** Usable MG rounds in one stack: 0 if none/wrong calibre, -1 if unlimited. */
    private static int roundsIn(ItemStack stack) {
        if (stack.isEmpty() || !isTaczItem(stack)) {
            return 0;
        }
        if (isAmmoBox(stack)) {
            if (boolCall(stack, "isAllTypeCreative", "AllTypeCreative")) {
                return -1;
            }
            String id = ammoIdOf(stack);
            if (id == null || !acceptsAmmo(id)) {
                return 0;
            }
            if (boolCall(stack, "isCreative", "Creative")) {
                return -1;
            }
            return Math.max(0, boxCount(stack));
        }
        String id = ammoIdOf(stack);
        return id != null && acceptsAmmo(id) ? stack.getCount() : 0;
    }

    private static boolean acceptsAmmo(String id) {
        java.util.List<String> ids = ModConfig.get().mgAmmoIds;
        return ids.contains("*") || ids.contains(id);
    }

    private static boolean isTaczItem(ItemStack stack) {
        return NS.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace());
    }

    private static boolean isAmmoBox(ItemStack stack) {
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return path.contains("ammo_box") || findMethod(stack.getItem().getClass(), "getAmmoCount", ItemStack.class) != null;
    }

    /** TACZ ammo id of a cartridge or ammo box (item interface first, then the stored "AmmoId" tag). */
    private static String ammoIdOf(ItemStack stack) {
        Object v = call(stack, "getAmmoId");
        if (v != null && !v.toString().isEmpty() && !v.toString().equals("tacz:empty")) {
            return v.toString();
        }
        CompoundTag tag = tagOf(stack);
        return tag.contains("AmmoId") ? tag.getString("AmmoId") : null;
    }

    private static int boxCount(ItemStack stack) {
        Object v = call(stack, "getAmmoCount");
        if (v instanceof Number n) {
            return n.intValue();
        }
        return tagOf(stack).getInt("AmmoCount");
    }

    private static void setBoxCount(ItemStack stack, int count) {
        try {
            Method m = findMethod(stack.getItem().getClass(), "setAmmoCount", ItemStack.class, int.class);
            if (m != null) {
                m.setAccessible(true);
                m.invoke(stack.getItem(), stack, count);
                return;
            }
        } catch (Throwable ignored) {
        }
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putInt("AmmoCount", count);
            if (count <= 0) {
                tag.remove("AmmoId");
            }
        });
    }

    private static boolean boolCall(ItemStack stack, String method, String tagKey) {
        Object v = call(stack, method);
        if (v instanceof Boolean b) {
            return b;
        }
        return tagOf(stack).getBoolean(tagKey);
    }

    /** Calls item.method(stack) if the TACZ item class has it. */
    private static Object call(ItemStack stack, String method) {
        try {
            Method m = findMethod(stack.getItem().getClass(), method, ItemStack.class);
            if (m != null) {
                m.setAccessible(true);
                return m.invoke(stack.getItem(), stack);
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static CompoundTag tagOf(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null ? data.copyTag() : new CompoundTag();
    }

    private static Method findMethod(Class<?> c, String name, Class<?>... params) {
        for (Class<?> k = c; k != null && k != Object.class; k = k.getSuperclass()) {
            try {
                return k.getDeclaredMethod(name, params);
            } catch (NoSuchMethodException ignored) {
            }
            for (Class<?> i : k.getInterfaces()) {
                try {
                    Method m = i.getMethod(name, params);
                    if (m != null) {
                        return m;
                    }
                } catch (NoSuchMethodException ignored) {
                }
            }
        }
        return null;
    }

    /** True if the damage came from a TACZ bullet or a TACZ damage type. */
    public static boolean isTaczDamage(DamageSource source) {
        boolean typeIsTacz = source.typeHolder().unwrapKey()
                .map(k -> NS.equals(k.location().getNamespace()))
                .orElse(false);
        if (typeIsTacz) {
            return true;
        }
        Entity direct = source.getDirectEntity();
        return direct != null && NS.equals(BuiltInRegistries.ENTITY_TYPE.getKey(direct.getType()).getNamespace());
    }

    /** Every id we can find for the weapon behind this hit: projectile type, gun id, ammo id. */
    public static Set<String> weaponIds(DamageSource source) {
        Set<String> ids = new HashSet<>();
        Entity direct = source.getDirectEntity();
        if (direct != null) {
            ids.add(BuiltInRegistries.ENTITY_TYPE.getKey(direct.getType()).toString());
            readId(direct, "getGunId", "gunId", ids);
            readId(direct, "getAmmoId", "ammoId", ids);
        }
        // Fallback: the gun the shooter is holding (TACZ stores it as "GunId" in custom data).
        if (isTaczDamage(source) && source.getEntity() instanceof LivingEntity shooter) {
            ItemStack held = shooter.getMainHandItem();
            CustomData data = held.get(DataComponents.CUSTOM_DATA);
            if (data != null) {
                CompoundTag tag = data.copyTag();
                if (tag.contains("GunId")) {
                    ids.add(tag.getString("GunId"));
                }
            }
        }
        return ids;
    }

    private static void readId(Object target, String methodName, String fieldName, Set<String> out) {
        try {
            Method m = findMethod(target.getClass(), methodName);
            if (m != null) {
                m.setAccessible(true);
                Object v = m.invoke(target);
                if (v != null) {
                    out.add(v.toString());
                    return;
                }
            }
        } catch (Throwable ignored) {
        }
        try {
            Field f = findField(target.getClass(), fieldName);
            if (f != null) {
                f.setAccessible(true);
                Object v = f.get(target);
                if (v != null) {
                    out.add(v.toString());
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private static Method findMethod(Class<?> c, String name) {
        for (Class<?> k = c; k != null && k != Object.class; k = k.getSuperclass()) {
            for (Method m : k.getDeclaredMethods()) {
                if (m.getName().equals(name) && m.getParameterCount() == 0) {
                    return m;
                }
            }
        }
        return null;
    }

    private static Field findField(Class<?> c, String name) {
        for (Class<?> k = c; k != null && k != Object.class; k = k.getSuperclass()) {
            try {
                return k.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
            }
        }
        return null;
    }
}
