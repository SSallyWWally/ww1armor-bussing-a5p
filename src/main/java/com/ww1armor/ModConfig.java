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
    public float steerSpeed = 1.1F;         // degrees per tick the wheel can be turned (heavy steering)

    // ---- MG 08
    public float mgDamage = 5.0F;
    public int mgFireIntervalTicks = 2;
    public double mgRange = 80.0;
    public double mgSpread = 0.012;
    public float mgMaxElevation = 20.0F;
    public float mgMaxDepression = 12.0F;

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
        try (Writer w = Files.newBufferedWriter(path)) {
            GSON.toJson(cfg, w); // writes defaults for any new fields
        } catch (Exception e) {
            ArmoredCarMod.LOGGER.warn("Could not write {}", path, e);
        }
        return cfg;
    }
}
