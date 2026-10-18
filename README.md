# Exodus Vehicles (Fabric 1.21.1)

Drivable vehicles for the Exodus server. First vehicle: the **M.15 Eisenwolf** armored car.

## M.15 Eisenwolf
An 8 m, four-wheeled WW1-style armored car with a turret MG 08, four crew seats, armor that shrugs off normal
TACZ rounds (anti-tank weapons and very heavy hits get through), and a burning wreck that stays for a few minutes.

### Controls (all rebindable under Controls > Key Binds > Exodus Vehicles)
| Action | Default |
|---|---|
| Enter | Right-click the vehicle |
| Start / stop engine (driver) | `P` |
| Drive | `W` `A` `S` `D` |
| Switch seat | `N` |
| Fire MG 08 (gunner) | Hold left mouse |
| Zoom in the sight (gunner) | Hold right mouse (from third person this jumps into the sight) |
| Reload belt | `B` (automatic when the belt is empty) |
| Repair | Right-click with an iron ingot |
| Get out | Sneak |

### MG 08
250-round belts at 600 rounds/min, reloaded from TACZ rifle ammunition (or ammo boxes; creative boxes are unlimited).
The water jacket overheats after about 75 rounds of continuous fire and unlocks after a few seconds of cooling.

### Settings
`config/exodus.json` (created on first launch): armor, health, speeds, steering, fire rate, heat, ammo types, zoom.

## Requirements
Fabric Loader, Fabric API. Optional: TaCZ Refabricated.
Install the jar on the server **and** on every client.
