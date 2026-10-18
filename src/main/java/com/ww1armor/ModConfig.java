package com.ww1armor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Settings live in config/ww1armor.json (created on first launch).
 * Edit the file and restart the game/server to apply changes.
 */
public class ModConfig {
    /** Bumped when a default changes in a way old config files must pick up. Don't edit. */
    public int configVersion = 0;
    private static final int CURRENT_VERSION = 2;

    // ---- armor / damage
    public float maxHealth = 300.0F;
    /** Any single hit at or above this damage penetrates, whatever the weapon (e.g. TACZ M107 / M95). */
    public float penetrationThreshold = 25.0F;
    /** TACZ gun ids, ammo ids or projectile entity ids that always penetrate. */
    public List<String> antiTankWeapons = new ArrayList<>(List.of(
            "tacz:m107", "tacz:m95", "tacz:rpg7", "tacz:50bmg", "tacz:rpg_rocket", "tacz:m320", "tacz:40mm"));
    public float antiTankDamageMultiplier = 1.0F;
    public float explosionDamageMultiplier = 1.0F;

    // ---- destruction
    public int wreckSeconds = 180;
    public float destroyExplosionPower = 3.5F;
    public boolean destroyExplosionBreaksBlocks = false;
    public boolean destroyExplosionStartsFire = true;

    // ---- driving: 10 t truck with a 90 hp engine. Speeds in blocks per tick (1 block = 1 m).
    public float maxSpeed = 0.45F;          // ~32 km/h (historical top speed ~35 km/h)
    public float maxReverseSpeed = 0.25F;
    public float acceleration = 0.0045F;    // ~5 s to full speed
    public float braking = 0.02F;
    public float maxSteerAngle = 22.0F;     // degrees; with the 4.4 m wheelbase gives an ~11 block turning radius
    public float steerSpeed = 1.6F;         // max degrees per tick the wheel turns in (eased, heavy steering)
    public float steerReturnSpeed = 2.2F;   // max degrees per tick it self-centres
    public float turnSmoothing = 0.35F;     // 0..1, lower = hull swings into turns more gradually

    // ---- MG 08 (7.92 mm, 250-round fabric belt, ~450-500 rounds/min, water-cooled jacket)
    public float mgDamage = 5.0F;
    public float mgRoundsPerMinute = 500.0F;
    public int mgBeltSize = 250;
    public int mgReloadTicks = 90;           // 4.5 s to feed a new belt
    public double mgRange = 80.0;
    public double mgSpread = 0.012;
    public float mgMaxElevation = 20.0F;
    public float mgMaxDepression = 12.0F;
    /** Heat 0..1. Continuous fire overheats the gun after about 40 rounds (~5 s at 500 rpm). */
    public float mgHeatPerShot = 0.039F;
    /** Full heat cools off in ~8.5 s; an overheated gun unlocks after ~6 s. */
    public float mgCoolingPerTick = 0.0058F;
    /** After overheating the gun is locked until heat falls back to this. */
    public float mgOverheatResume = 0.3F;
    /** TACZ ammo ids the belt accepts (7.92 mm Mauser equivalents). Add "*" to accept any TACZ ammo. */
    public List<String> mgAmmoIds = new ArrayList<>(List.of(
            "tacz:308", "tacz:30_06", "tacz:762x54", "tacz:792x57", "tacz:8x57", "tacz:303"));
    /** Belts refill for free (no ammo needed). Creative players and TACZ creative ammo boxes are always free. */
    public boolean mgInfiniteAmmo = false;
    /** Without TACZ installed there is no ammo item, so belts refill for free unless this is false. */
    public boolean mgFreeAmmoWithoutTacz = true;
    /** Zoom while holding use in the gun sight (1 = none, max 3). */
    public float mgSightZoom = 3.0F;

    // ---- repair
    public float repairPerIngot = 20.0F;

    private static ModConfig INSTANCE;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static ModConfig get() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    private static ModConfig load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("ww1armor.json");
        ModConfig cfg = null;
        if (Files.exists(path)) {
            try (Reader r = Files.newBufferedReader(path)) {
                cfg = GSON.fromJson(r, ModConfig.class);
            } catch (Exception e) {
                ArmoredCarMod.LOGGER.error("Could not read {}, using defaults", path, e);
            }
        }
        if (cfg == null) {
            cfg = new ModConfig();
        }
        if (cfg.antiTankWeapons == null) {
            cfg.antiTankWeapons = new ArrayList<>();
        }
        if (cfg.mgAmmoIds == null) {
            cfg.mgAmmoIds = new ArrayList<>();
        }
        if (cfg.configVersion < 2) {
            // 1.1.x heat values made the MG practically impossible to overheat
            ModConfig d = new ModConfig();
            cfg.mgHeatPerShot = d.mgHeatPerShot;
            cfg.mgCoolingPerTick = d.mgCoolingPerTick;
            cfg.mgOverheatResume = d.mgOverheatResume;
        }
        cfg.configVersion = CURRENT_VERSION;
        cfg.mgBeltSize = Math.max(1, cfg.mgBeltSize);
        cfg.mgSightZoom = Math.max(1.0F, Math.min(3.0F, cfg.mgSightZoom));
        cfg.turnSmoothing = Math.max(0.05F, Math.min(1.0F, cfg.turnSmoothing));
        try (Writer w = Files.newBufferedWriter(path)) {
            GSON.toJson(cfg, w); // writes defaults for any new fields
        } catch (Exception e) {
            ArmoredCarMod.LOGGER.warn("Could not write {}", path, e);
        }
        return cfg;
    }
}
