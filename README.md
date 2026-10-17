# WW1 Armored Cars: Büssing A5P (Fabric 1.21.1)

A drivable Büssing A5P armored car (8 m long, 3.8 m tall, four wheels on two driven axles) modelled from the reference photograph, with a turret MG 08, four seats, TACZ-aware armor, an engine sound, and a burning wreck when destroyed.

## Build it (one command)

You need **Java 21** (the same one Minecraft 1.21 uses works. On Windows, the Adoptium Temurin 21 JDK is the easy option).

```
cd ww1armor-bussing-a5p
./gradlew build          (Windows: gradlew.bat build)
```

The first build downloads Minecraft and Fabric, which takes a few minutes. The finished mod is at:

```
build/libs/ww1armor-bussing-a5p-1.0.0.jar
```

Put that jar in your `mods` folder along with **Fabric API**. For guns, also add **TaCZ: Refabricated** (Fabric port of TACZ) and **Forge Config API Port**. The car works without TACZ too.

To test in a dev client instead: `./gradlew runClient`

## In game

| What | How |
|---|---|
| Get it | Creative tab *Combat*, or craft: iron blocks, dispenser, blast furnace, 2 minecarts (see recipe) |
| Place | Right-click the ground with the item |
| Get in | Right-click the car (first free seat: driver → gunner → passenger → passenger) |
| Drive | W/S throttle and brake/reverse, A/D steer (driver seat) |
| Fire MG 08 | Hold attack/left mouse in the gunner seat. The turret follows where you look. If you hold a TACZ gun, TACZ fires instead. |
| Switch seat | **N** by default (rebind it under Controls → *WW1 Armored Cars*) |
| Get out | Sneak (Shift) |
| Repair | Right-click with an iron ingot (+20 hull) |
| Pack up | Sneak + right-click with an empty hand, when the hull is full and nobody is inside |

Crafting recipe:
```
[Iron Block] [Dispenser]     [Iron Block]
[Iron Block] [Blast Furnace] [Iron Block]
[Minecart]   [   empty   ]   [Minecart]
```

## Armor rules

A hit only damages the car if one of these is true:

1. **It's an explosion** (RPG blast, grenade, TNT, creeper…).
2. **The TACZ weapon is on the anti-tank list.** The mod checks the bullet's gun ID, its ammo ID, and the projectile type. The default list is `tacz:m107`, `tacz:m95`, `tacz:rpg7`, `tacz:50bmg`, `tacz:rpg_rocket`, `tacz:m320`, `tacz:40mm`.
3. **The single hit does at least `penetrationThreshold` damage** (default 25). This lets very high-damage weapons like the M107 through, even from gun packs that aren't on the list.

Anything else (normal TACZ rifles, pistols, SMGs, swords, arrows) ricochets: you see sparks, hear a metallic ping, and the car takes 0 damage. Fire and lava don't hurt it. Passengers are inside the hull, so shots hit the armor instead of the crew.

When the hull reaches 0, the car **explodes**, throws the crew out, and becomes a **charred wreck**. The wreck burns for about 30 seconds, smoulders, and despawns after **3 minutes**. The turret is knocked askew, a wheel is blown off, and you can't enter or drive it.

## Config: `config/ww1armor.json`

This file is created on first launch. Restart after editing it.

| Key | Default | Meaning |
|---|---|---|
| `maxHealth` | 300 | Hull points |
| `penetrationThreshold` | 25 | A single hit at least this strong penetrates |
| `antiTankWeapons` | see above | Gun, ammo, or projectile IDs that always penetrate. Add gun-pack IDs here. |
| `antiTankDamageMultiplier` | 1.0 | Multiplier for listed weapons |
| `explosionDamageMultiplier` | 1.0 | Multiplier for explosions |
| `wreckSeconds` | 180 | How long the wreck stays |
| `destroyExplosionPower` | 3.5 | Death explosion size |
| `destroyExplosionBreaksBlocks` | false | Whether the death explosion breaks blocks |
| `destroyExplosionStartsFire` | true | Whether the death explosion starts fires |
| `maxSpeed` / `maxReverseSpeed` | 0.45 / 0.25 | Blocks per tick (about 32 / 18 km/h) |
| `acceleration` / `braking` | 0.0045 / 0.02 | Heavy, slow throttle response |
| `maxSteerAngle` / `steerSpeed` | 22 / 1.1 | Front-wheel angle (deg) and how fast the wheel turns; ~11-block turning circle |
| `mgDamage` / `mgFireIntervalTicks` | 5 / 2 | MG 08: 5 damage, about 600 rounds per minute |
| `mgRange` / `mgSpread` | 80 / 0.012 | MG range in blocks / spread |
| `repairPerIngot` | 20 | Hull points restored per iron ingot |

Tip: to find a gun's ID, hold it and press F3+H to show advanced tooltips, or look in the gun pack's `data/<namespace>/guns` folder. Then add the ID to `antiTankWeapons`.

## Files

- `src/main/java/com/ww1armor/entity/ArmoredCarEntity.java`: driving, seats, armor rules, MG, destruction
- `src/main/java/com/ww1armor/compat/TaczCompat.java`: TACZ detection (by reflection, so there's no hard dependency)
- `src/main/java/com/ww1armor/client/*`: model, renderer, keybind, HUD
- `src/main/java/com/ww1armor/client/ArmoredCarModel.java`: generated model (93 parts, 266 blocks)
- `src/main/resources/assets/ww1armor/textures/entity/`: normal and charred textures (512×512, box UV)
- `src/main/resources/assets/ww1armor/sounds/engine_loop.ogg`: engine loop
