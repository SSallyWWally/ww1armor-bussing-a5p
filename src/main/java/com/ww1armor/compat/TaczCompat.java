package com.ww1armor.compat;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

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
