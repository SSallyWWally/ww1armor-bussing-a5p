package com.exodusvehicles.compat;

import com.exodusvehicles.ArmoredCarMod;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Danny's AoT support, by reflection (soft dependency: nothing here runs without the mod).
 * Uses the same entry points the AoT mod's own guns and field cannon use, so titans react exactly as they would to
 * those: eyes are blinded by a strong enough projectile hit, a shell that hits the nape kills, a shell that hits the
 * head can take it off, and a shell tearing past a limb severs it.
 */
public final class AotCompat {
    private static final boolean LOADED = FabricLoader.getInstance().isModLoaded("dannys-aot");
    private static Method isEye, isNape, hitLethal, tryHeadshot, titanOf, limbsShot;
    private static boolean ready;

    private AotCompat() {}

    private static boolean init() {
        if (!LOADED) {
            return false;
        }
        if (ready) {
            return isEye != null;
        }
        ready = true;
        try {
            Class<?> eye = Class.forName("daot.gun.GunEyeTarget");
            Class<?> nape = Class.forName("daot.gun.GunNapeDamage");
            Class<?> decap = Class.forName("daot.Decapitation");
            Class<?> limbs = Class.forName("daot.TitanLimbs");
            isEye = eye.getMethod("isEye", Entity.class);
            isNape = nape.getMethod("isNape", Entity.class);
            hitLethal = nape.getMethod("hitLethal", Entity.class, DamageSource.class);
            tryHeadshot = decap.getMethod("tryHeadshot", Entity.class, Entity.class, Vec3.class, Vec3.class, DamageSource.class,
                    float.class, boolean.class);
            titanOf = decap.getMethod("titanOf", Entity.class);
            limbsShot = limbs.getMethod("shot", ServerLevel.class, Vec3.class, Vec3.class, double.class);
            ArmoredCarMod.LOGGER.info("Danny's AoT found: MG blinds titan eyes, artillery kills / beheads / dismembers titans");
            return true;
        } catch (Throwable t) {
            ArmoredCarMod.LOGGER.warn("Danny's AoT is installed but its titan hooks could not be found; titan compat off", t);
            isEye = null;
            return false;
        }
    }

    public static boolean active() {
        return init();
    }

    private static boolean call(Method m, Object... args) {
        try {
            Object r = m.invoke(null, args);
            return r instanceof Boolean b && b;
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean isEye(Entity e) {
        return init() && call(isEye, e);
    }

    public static boolean isNape(Entity e) {
        return init() && call(isNape, e);
    }

    @Nullable
    public static Entity titanOf(Entity e) {
        if (!init()) {
            return null;
        }
        try {
            return (Entity) titanOf.invoke(null, e);
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * The nearest titan eye the line from..to passes through (within reach of the titan the ray first meets, so the
     * eye is preferred over the head around it), or empty.
     */
    public static Optional<Entity> eyeAlong(ServerLevel level, Vec3 from, Vec3 to, @Nullable Entity firstHit, double firstHitDist) {
        if (!init()) {
            return Optional.empty();
        }
        Entity best = null;
        double bestD = Double.MAX_VALUE;
        for (Entity e : level.getEntities((Entity) null, new AABB(from, to).inflate(2.0), AotCompat::isEye)) {
            Optional<Vec3> hit = e.getBoundingBox().inflate(0.35).clip(from, to);
            if (hit.isEmpty()) {
                continue;
            }
            double d = hit.get().distanceTo(from);
            boolean sameTitan = firstHit != null && titanOf(firstHit) != null && titanOf(firstHit) == titanOf(e);
            if ((firstHit == null || sameTitan || d <= firstHitDist + 0.5) && d < bestD) {
                best = e;
                bestD = d;
            }
        }
        return Optional.ofNullable(best);
    }

    /** Distance from point p to the segment a-b. */
    private static double segDist(Vec3 p, Vec3 a, Vec3 b) {
        Vec3 ab = b.subtract(a);
        double l2 = ab.lengthSqr();
        double t = l2 < 1.0E-9 ? 0.0 : Math.max(0.0, Math.min(1.0, p.subtract(a).dot(ab) / l2));
        return p.distanceTo(a.add(ab.scale(t)));
    }

    /**
     * A field-gun round flying from..to this tick: any nape it passes within napeReach of is a kill, an eye on its path
     * is put out, and limbs on its path are torn off. Cheap when no titan is about (one entity query).
     */
    public static void alongPath(ServerLevel level, @Nullable Entity owner, Entity gun, Vec3 from, Vec3 to, DamageSource src,
                                 double napeReach, double limbReach) {
        if (!init()) {
            return;
        }
        try {
            AABB box = new AABB(from, to).inflate(napeReach + 2.0);
            java.util.List<Entity> napes = level.getEntities((Entity) null, box.inflate(14.0), AotCompat::isNape);
            if (napes.isEmpty()) {
                return;      // no titan anywhere near the round's path this tick
            }
            for (Entity e : napes) {
                if (segDist(e.getBoundingBox().getCenter(), from, to) <= napeReach + e.getBbWidth() * 0.5) {
                    call(hitLethal, e, src);
                }
            }
            for (Entity e : level.getEntities((Entity) null, new AABB(from, to).inflate(1.5), AotCompat::isEye)) {
                if (e.getBoundingBox().inflate(0.6).clip(from, to).isPresent()) {
                    DamageSource eyeSrc = owner instanceof net.minecraft.world.entity.LivingEntity le
                            ? level.damageSources().mobProjectile(gun, le) : src;
                    e.hurt(eyeSrc, 20.0F);
                }
            }
            call(limbsShot, level, from, to, limbReach);
        } catch (Throwable t) {
            // never let a foreign mod's hiccup take the server down
        }
    }

    /** A heavy shell striking at 'point' moving along 'dir' (unit). 'hit' = the entity it struck, if any. */
    public static void shellImpact(ServerLevel level, @Nullable Entity owner, @Nullable Entity hit, Vec3 point, Vec3 dir,
                                   DamageSource src, float strength, double limbReach, double napeKillRadius) {
        if (!init()) {
            return;
        }
        try {
            if (hit != null && titanOf(hit) != null) {
                call(tryHeadshot, owner, hit, point, dir, src, strength, true);     // can take the head clean off
                if (isNape(hit)) {
                    call(hitLethal, hit, src);
                }
            }
            // the blast at the nape is fatal to any titan whose nape is close enough
            for (Entity e : level.getEntities((Entity) null, new AABB(point, point).inflate(napeKillRadius + 2.0), AotCompat::isNape)) {
                if (e.getBoundingBox().getCenter().distanceTo(point) <= napeKillRadius) {
                    call(hitLethal, e, src);
                }
            }
            // limbs the shell tears past (coming in) and the blast throws out sideways
            call(limbsShot, level, point.subtract(dir.scale(6.0)), point.add(dir.scale(1.0)), limbReach);
            Vec3 side = new Vec3(-dir.z, 0, dir.x);
            if (side.lengthSqr() > 1.0E-6) {
                side = side.normalize();
                call(limbsShot, level, point.subtract(side.scale(limbReach)), point.add(side.scale(limbReach)), limbReach * 0.6);
            }
        } catch (Throwable t) {
            // never let a foreign mod's hiccup take the server down
        }
    }
}
