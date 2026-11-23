# Changelog

Every change to AEA for Fabric 1.20.1, newest first.

## 1.6.7

### Added
- `aea:redstone_pulse` (block action): sends a short redstone signal into a block (`receive`) or out of a position (`emit`), with `strength`, `duration`, `strong` and `sides`. Nothing is placed: blocks see the power the way they see a lever's, so pistons, wire, doors and modded machines react.
- `/aea redstone pulse <pos> [receive|emit] [strength] [duration] [strong]`, the same pulse from commands and functions.
- AppleSkin compatibility, active only when AppleSkin is installed: its tooltips and hunger bar preview show the values `apoli:modify_food` gives.
- Spell Engine compatibility, active only with Spell Engine 0.x (what 1.20.1 packs run):
  - `aea:grant_spell`: gives spells as if from equipment, on Spell Engine's own spell hotbar. They work whatever the player holds, unless `always` is false; then they're added only to a held spell item that takes spell books.
  - `aea:modify_spell`: changes `cast_time`, `cooldown` and `range` with Apoli modifiers.
  - `aea:prevent_spell_cast`: blocks casting, with an optional `message`.
  - `aea:action_on_spell_cast`: runs an entity action on the caster and a bi-entity action on each target.
  - Entity and bi-entity action `aea:cast_spell`: casts any spell at once, with or without its cost (`use_cost`).
  - Conditions: entity `aea:casting_spell`, `aea:spell_on_cooldown` and `aea:knows_spell`; item `aea:has_spell`; damage `aea:spell_damage` (by school).
  - Every spell type takes `spell` and `school` to limit it to some spells. Spell Engine 0.x has no spell tags, so `#`tags are read but match nothing.

## 1.6.6

### Added
- Mind control: the bi-entity action `aea:mind_control` lets a player take over a mob or another player, at any distance and in any dimension, with the same fields as AEA for NeoForge (`controls`, `duration`, `release_condition`, `struggle`, `kill_credit`, `close_screens`, `body_damage`, `share_damage`, `on_start`, `on_release`). Also adds the entity action `aea:release_mind_control` and the conditions `aea:mind_controlled`, `aea:mind_controlling` and `aea:mind_controls`.

## 1.6.5

### Added
- `aea:prevent_spawn_point`: stops the player's spawn point from being set or moved by beds, respawn anchors, `/spawnpoint` or anything else, chosen with `source`, with an optional `message`.

## 1.6.4

### Changed
- Brought in the latest NeoForge fixes.
- `aea:mob_ai` no longer undoes its changes while another source still grants it.

### Fixed
- Powers resolved on the server are synced to the client, so `zoom`, `modify_fov`, `modify_camera` and `modify_hearing_range` work.

### Removed
- Power tag files.

## 1.6.3

### Removed
- The `apoli:see_invisible` and `apoli:milk_immunity` names; use `aea:see_invisible` and `aea:milk_immunity`.

## 1.6.2

### Changed
- Formats matched to AEA for NeoForge: `apoli:status_effect` takes exactly one of `effect` and `effect_type`, and data pack gamerules need `type`.

## 1.6.1

### Added
- `aea:see_invisible` and `aea:milk_immunity`.
- `effect_type` (`beneficial`, `harmful` or `neutral`) for `apoli:effect_immunity` and `apoli:status_effect`.
- Data pack gamerules, read with the `aea:gamerule` condition or the `gamerule(ns:name)` expression function.

## 1.6.0

### Added
- `aea:hitbox_part`, data pack textures and data pack assets.
- `config/aea.toml`.
- A built-in `/scale` when Pehkui isn't installed.
- `/aea validate`.
