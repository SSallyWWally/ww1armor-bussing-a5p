package com.exodusvehicles;

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
 * Settings live in config/exodus_vehicles.json (created on first launch).
 * Edit the file and restart the game/server to apply changes.
 */
public class ModConfig {
    /** Bumped when a default changes in a way old config files must pick up. Don't edit. */
    public int configVersion = 0;
    private static final int CURRENT_VERSION = 13;

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

    // ---- Daimler-Marienfelde DR lorry. The real one did about 16-21 km/h; it's faster here so it outruns the car.
    public float truckMaxSpeed = 0.55F;          // ~40 km/h
    public float truckMaxReverseSpeed = 0.25F;
    public float truckAcceleration = 0.0055F;    // ~5 s to full speed
    public float truckMaxSteerAngle = 26.0F;     // 4.4 m wheelbase: ~9 block turning radius
    public float truckMaxHealth = 150.0F;        // unarmoured: every hit counts

    // ---- 21 cm Mörser 16 artillery (Battlefield-style). Distances in blocks, times in ticks (20 = 1 s).
    public float morserMaxHealth = 200.0F;
    public double artilleryMinRange = 95.0;
    public double artilleryMaxRange = 1000.0;
    /** Shells are fired on arcs that stay below this height (clouds are at 192), flattening out if needed. */
    public double artilleryShellCeiling = 180.0;
    public float artilleryTraverse = 30.0F;         // degrees either side of where the gun was deployed
    public int artilleryReloadTicks = 200;          // 10 s, automatic
    public int artilleryDeployTicks = 80;           // 4 s to deploy or pack up
    /** Scatter radius around the aim point: at minimum range ... at maximum range. */
    public double artilleryDispersionMin = 8.0;
    public double artilleryDispersionMax = 22.0;
    /** Each shot uses a 21 cm Shell from the operator's inventory (creative: free). */
    public boolean artilleryRequiresShells = true;
    public float shellDamage = 60.0F;               // at the point of impact, falling to 0 at the blast radius
    public float shellBlastRadius = 8.0F;
    public double shellKnockback = 1.6;
    public float shellShockRadius = 10.0F;          // players this close are dazed (slowness / nausea)
    public float shellVehicleDamage = 400.0F;       // extra damage to vehicles at the point of impact...
    public double shellVehicleRadius = 5.0;         // ...falling to nothing at this distance
    /**
     * Crater size (explosion power; TNT is 4). Craters are switched on or off per world with the game rule
     * exodusArtilleryCrater (/exodusartillerycrater true|false); never inside spawn protection or where the firing
     * player may not build.
     */
    public float shellBlockPower = 3.2F;
    /** Danny's AoT: shell headshot strength (their field cannon is 25), limb-severing reach, and how close to the nape kills. */
    public float aotShellStrength = 40.0F;
    public double aotLimbReach = 3.0;
    public double aotNapeKillRadius = 4.0;
    // ---- 7.7 cm FK 96 n.A. field gun: direct fire along a shallow arc. Angles in degrees.
    public float fieldGunMaxHealth = 140.0F;
    public float fieldGunTraverse = 25.0F;
    public float fieldGunMaxElevation = 16.0F;
    public float fieldGunMaxDepression = 8.0F;
    /** Muzzle speed, blocks per tick: 9 gives ~500 m at full elevation on a slight arc. */
    public double fieldGunMuzzleSpeed = 9.0;
    /** Random angular error per shot. */
    public float fieldGunSpread = 0.35F;
    public int fieldGunReloadTicks = 110;           // 5.5 s
    public int fieldGunDeployTicks = 50;            // 2.5 s
    /** Deployed, the gunner can roll it back and forth (W / S) this far from where it was dug in before it must be packed up. */
    public double fieldGunShiftRadius = 4.0;
    public double fieldGunShiftSpeed = 0.035;
    /** Deployed, A / D swing the whole gun round this far (degrees) either way from how it was dug in. */
    public float fieldGunPivotLimit = 45.0F;
    public float fieldGunPivotSpeed = 0.8F;      // degrees per tick
    public float fieldShellDamage = 34.0F;          // near miss, at the burst, falling to 0 at the radius
    public float fieldShellBlastRadius = 4.5F;
    public double fieldShellKnockback = 1.1;
    public float fieldShellShockRadius = 6.5F;
    /** A round bursting this close to someone (or hitting them) kills outright. */
    public double fieldShellKillRadius = 1.6;
    /** Fraction of a vehicle's health one hit takes: armoured (Eisenwolf) dies to 2, anything else to 1. */
    public float fieldShellArmoredFraction = 0.55F;
    public float fieldShellSoftFraction = 1.05F;
    public double fieldShellVehicleRadius = 2.5;
    public float fieldShellBlockPower = 1.1F;
    /** Danny's AoT: a round passing this close to a nape kills the titan; eyes and limbs on its path are hit too. */
    public double fieldAotNapeReach = 2.5;
    public float fieldAotShellStrength = 60.0F;
    public double fieldAotLimbReach = 2.0;

    // ---- MG 08 on the sled mount (field MG). Belt, rate of fire, heat and damage are the MG 08 settings above.
    public float fieldMgMaxHealth = 60.0F;
    public float fieldMgTraverse = 45.0F;            // degrees either side of how it was set down
    public float fieldMgMaxElevation = 15.0F;
    public float fieldMgMaxDepression = 12.0F;
    public double fieldMgRange = 160.0;
    /** Degrees per tick the whole gun swings round when the gunner keeps turning past the traverse stop. */
    public float fieldMgSwingSpeed = 2.5F;
    /** Zoom while holding right-click on the sights. */
    public float fieldMgSightZoom = 1.6F;
    /** Suppression: rounds passing within this many blocks of a player rattle them (screen effects, shakier aim). */
    public boolean suppressionEnabled = true;
    public double suppressionRadius = 4.0;
    public float suppressionPerRound = 0.09F;

    // ---- Fokker Dr.I triplane (speeds in blocks per tick: 1.6 = 115 km/h)
    public float planeMaxHealth = 70.0F;
    public float planeMaxSpeed = 1.6F;
    public float planeIdleSpeed = 0.45F;        // (unused since 2.3.5)
    /** How quickly holding W / S changes the speed (blocks per tick, per tick): 0 to full in ~4 s. */
    public float planeAccel = 0.02F;
    public float planeStallSpeed = 0.6F;
    public float planeTakeoffSpeed = 0.75F;
    public float planeTurnRate = 3.2F;          // degrees per tick at full control authority
    public float planeCeiling = 260.0F;         // can't climb above this height
    public float planeCrashSpeed = 0.55F;       // hitting something faster than this is a crash
    public float planeCrashDamage = 40.0F;
    /** A destroyed aircraft lies burning where it fell for this long before it is cleared away. */
    public int planeWreckSeconds = 300;
    public float planeGunDamage = 4.5F;
    public double planeGunRange = 150.0;
    public double planeGunSpread = 0.008;
    public float planeGunHeatPerShot = 0.02F;   // ~5 s of fire to overheat
    public float planeGunCooling = 0.012F;

    /** Client: size of the shell burst effects (1 = full, lower for weaker PCs). */
    public float shellEffectScale = 1.0F;
    /** Client: follow the shell with the camera after firing (toggle in game with the shell-camera key). */
    public boolean shellCamera = true;

    // ---- MG 08 (7.92 mm, 250-round fabric belt, ~450-500 rounds/min, water-cooled jacket)
    public float mgDamage = 5.0F;
    public float mgRoundsPerMinute = 600.0F;  // MG 08: ~400-600 rpm; 600 = exactly one round every 2 ticks
    public int mgBeltSize = 250;
    public int mgReloadTicks = 90;           // 4.5 s to feed a new belt
    public double mgRange = 80.0;
    public double mgSpread = 0.012;
    public float mgMaxElevation = 20.0F;
    public float mgMaxDepression = 12.0F;
    /** Heat 0..1. Continuous fire overheats the gun after about 75 rounds (~7.5 s at 600 rpm). */
    public float mgHeatPerShot = 0.0295F;
    /** Full heat cools off in ~6 s; an overheated gun unlocks after ~4.5 s. */
    public float mgCoolingPerTick = 0.008F;
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

    // ---- repair (wrench, hold on the vehicle) and fuel (jerry can, hold on the vehicle)
    public float repairPerIngot = 20.0F;           // (unused since the wrench)
    public float repairPerSecond = 2.0F;           // health per second of wrench work
    public float refuelLitresPerSecond = 1.6F;     // a 20 litre can takes ~12 s
    public float repairStationPerSecond = 20.0F;   // stopped at a service station
    public float fuelStationLitresPerSecond = 6.5F; // stopped at a service station
    public double serviceStationRange = 7.0;        // blocks from the station a stopped vehicle is serviced
    public float fuelCapacity = 100.0F;            // Eisenwolf tank, litres
    public float fuelPerMinuteDriving = 1.6F;      // ~1 hour flat out on a full tank
    public float fuelIdlePerMinute = 0.25F;        // engine running, standing still
    public float truckFuelCapacity = 80.0F;
    public float truckFuelPerMinuteDriving = 1.2F;

    private static ModConfig INSTANCE;
    private static Path PATH;

    /** Writes the current settings back (used for client-side toggles). */
    public static void save() {
        if (INSTANCE == null || PATH == null) {
            return;
        }
        try (Writer w = Files.newBufferedWriter(PATH)) {
            GSON.toJson(INSTANCE, w);
        } catch (Exception e) {
            ArmoredCarMod.LOGGER.warn("Could not write {}", PATH, e);
        }
    }
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static ModConfig get() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    private static ModConfig load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("exodus_vehicles.json");
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
        if (cfg.configVersion < 4) {
            // 1.3/1.4: fire rate and heat rebalanced for the MG 08 sound
            ModConfig d = new ModConfig();
            cfg.mgRoundsPerMinute = d.mgRoundsPerMinute;
            cfg.mgHeatPerShot = d.mgHeatPerShot;
            cfg.mgCoolingPerTick = d.mgCoolingPerTick;
        }
        if (cfg.configVersion == 6 && cfg.artilleryMaxRange == 400.0) {
            cfg.artilleryMaxRange = new ModConfig().artilleryMaxRange;     // 1.8.2: a bit more range
        }
        if (cfg.configVersion < 8) {
            // 1.8.5: range band 95 - 750
            if (cfg.artilleryMinRange == 60.0) {
                cfg.artilleryMinRange = 95.0;
            }
            if (cfg.artilleryMaxRange == 400.0 || cfg.artilleryMaxRange == 550.0) {
                cfg.artilleryMaxRange = 750.0;
            }
        }
        if (cfg.configVersion < 9 && cfg.artilleryDispersionMax == 14.0) {
            cfg.artilleryDispersionMax = 22.0;     // 1.9.0: scatter widens more with range
        }
        if (cfg.configVersion < 10 && cfg.artilleryMaxRange == 750.0) {
            cfg.artilleryMaxRange = 1000.0;      // 1.9.9: 1 km
        }
        if (cfg.configVersion < 11) {
            // 2.0.0: quicker wrench and jerry can
            if (cfg.repairPerSecond == 1.0F) {
                cfg.repairPerSecond = 1.5F;
            }
            if (cfg.refuelLitresPerSecond == 1.0F) {
                cfg.refuelLitresPerSecond = 1.25F;
            }
        }
        if (cfg.configVersion < 12) {
            // 2.0.2: quicker repairs and refuelling everywhere
            if (cfg.repairPerSecond == 1.5F) {
                cfg.repairPerSecond = 2.0F;
            }
            if (cfg.refuelLitresPerSecond == 1.25F) {
                cfg.refuelLitresPerSecond = 1.6F;
            }
            if (cfg.repairStationPerSecond == 15.0F) {
                cfg.repairStationPerSecond = 20.0F;
            }
            if (cfg.fuelStationLitresPerSecond == 5.0F) {
                cfg.fuelStationLitresPerSecond = 6.5F;
            }
        }
        if (cfg.configVersion < 13 && cfg.shellBlockPower == 2.5F) {
            cfg.shellBlockPower = 3.2F;           // 2.1.0: bigger craters (now on by default via the game rule)
        }
        cfg.configVersion = CURRENT_VERSION;
        cfg.mgBeltSize = Math.max(1, cfg.mgBeltSize);
        cfg.mgSightZoom = Math.max(1.0F, Math.min(3.0F, cfg.mgSightZoom));
        cfg.turnSmoothing = Math.max(0.05F, Math.min(1.0F, cfg.turnSmoothing));
        PATH = path;
        try (Writer w = Files.newBufferedWriter(path)) {
            GSON.toJson(cfg, w); // writes defaults for any new fields
        } catch (Exception e) {
            ArmoredCarMod.LOGGER.warn("Could not write {}", path, e);
        }
        return cfg;
    }
}
