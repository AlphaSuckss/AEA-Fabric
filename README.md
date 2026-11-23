# AEA for Origins (Fabric 1.20.1)

AEA (Alpha's Everything Addon) bundles **Sync 1.11.8** and **Origins: Classes 1.7.0**, and ports the features of
**Overgrown's Apoli and Origins** to run on top of **Origins 1.10.2 / Apoli 2.9.2 for Fabric 1.20.1**. Ported types keep
their original namespaces (`sync:`, `apoli:`, `origins:`). When Apoli 2.9.2 already has an id, Apoli's own version is
kept.

Data pack formats match [AEA for NeoForge 1.21.1](https://github.com/AlphaSuckss/AEA-NeoForge), so packs work on both.

## Build
1. Copy these into `libs/` (they aren't included in this repo):
   - `Origins-1.10.2+mc.1.20.x.jar`, and `apoli-2.9.2+mc.1.20.x.jar` and `calio-1.11.2+mc.1.20.x.jar` from inside it
     (`META-INF/jars/`).
   - `sync-1.11.8.jar` and `Origins-Classes-1.20-1.7.0.jar`. They're merged into AEA's jar unchanged; the classes in
     `src/` replace their versions of the same class.
2. `gradlew build` (JDK 17). The optional mods AEA has compat for are fetched from Modrinth's Maven. The jar lands in
   `build/libs/`.

## Layout
| Package | What it does |
|---|---|
| `port/` | Overgrown's Apoli, ported to run on Apoli 2.9.2. Its own type system (`port/power`, `port/action`, `port/condition`, contexts in `port/condition/context`), data types and expressions (`port/data`), client rendering and models (`port/client`), networking (`port/network`), scaling (`port/scale`, AEA's own, no Pehkui needed), skills, scripts, ropes, tick rates and the rest. `port/mixin` holds its mixins. |
| `bridge/` | Registers the ported types into Apoli 2.9.2's registries once Apoli, Origins and AEA have all started (`AeaRegistrar`). Ids Apoli already has are left to Apoli. |
| `originsport/` | Overgrown Origins 1.49.0 features Origins 1.10.2 lacks: origin swapping, origin storage, origin tags, impact conditions and the origin transfer actions. |
| `port/compat/` | Optional compat, each gated on its mod: Trinkets and Accessories (`accessory`), Icarus, Pufferfish's Skills, Hardcore Revival, Walkers, Pehkui, Entity Model Features, Sable. |
| `compat/carryonledger/` | Carry On Ledger Logs, built in: with both Carry On and Ledger, blocks picked up and put down with Carry On are logged to Ledger. |
| `compat/spell_engine/` | Spell Engine types, registered only with Spell Engine 0.x (`SpellEngineTypes`, `SpellEngineVersion`). Spell Engine 0.15 builds the spell hotbar from the held item's spell container; `SpellContainerHelperMixin` adds granted spells to it, and `SpellHelperMixin` applies `aea:modify_spell` and `aea:prevent_spell_cast` (`aea-spell_engine.mixins.json`, gated by `SpellEngineMixinPlugin`). |
| `compat/appleskin/` | AppleSkin hook, loaded through AppleSkin's `appleskin` entrypoint: answers its `FoodValuesEvent` with Apoli's `modify_food` values. |
| `redstone/` | `aea:redstone_pulse` and `/aea redstone pulse`. Pulses are kept per world and added to `RedstoneView.getEmittedRedstonePower`/`getStrongRedstonePower` (`RedstoneViewPulseMixin`), which every redstone block reads from. |
| `mindcontrol/` | `aea:mind_control` and its action/conditions. The controller becomes a spectator whose camera is the victim; a `ControlBody` stays behind and forwards damage; mobs are driven on the server with their AI paused. `mindcontrol/client` captures the controller's input and replays it on a controlled player's client. |
| `hitbox/` | `aea:hitbox_part`: extra hit boxes that follow an entity's body, outlined with F3 + B. |
| `packassets/` | Data pack textures (`data/<namespace>/textures/`) and data pack assets (the `assets/` folder of the world's data packs, loaded on players' games as a resource pack). |
| `gui/` | AEA's data-driven GUI: power `aea:gui` and its actions and conditions. |
| `mobai/` | `aea:mob_ai` and `aea:mob_pathing`. |
| `seeinvisible/`, `effect/`, `spawnpoint/`, `power/` | `aea:see_invisible`, `aea:milk_immunity` and `effect_type`, `aea:prevent_spawn_point`, `aea:velocity_immunity`. |
| `gamerule/` | Data pack gamerules (`data/<ns>/gamerules/*.json`), the `aea:gamerule` condition and the `gamerule(...)` expression function. |
| `macro/` | 1.20.2 function macros (`$` lines, `/function <name> <arguments>` and `with`), backported. |
| `extra/` | Fields Apoli 2.9.2's own types lack but packs use: `effect_type` on `effect_immunity` and `status_effect`, and related fixes. |
| `command/` | `/aea validate`, `/aea validate fields`, `/aea redstone pulse`, and `/scale` when Pehkui isn't installed. |
| `dev/overgrown/sync/`, `io/github/apace100/originsclasses/` | AEA's fixes to Sync and Origins: Classes. These replace the same classes from the bundled jars. |

## Documentation
The docs are at https://alphasuckss.github.io/AEA-Docs/. Their source is in the [AEA-Docs](https://github.com/AlphaSuckss/AEA-Docs) repository.

## Config
`config/aea.toml` → `data_pack_textures.enabled` (default true), `data_pack_assets.enabled` (default true).

## Changes
See `CHANGELOG.md`. Every change goes in there, under the version it ships in.

## License
MIT, see `LICENSE`. Third-party material: see `THIRD_PARTY.md`.
