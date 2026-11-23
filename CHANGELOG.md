# Changelog

Every change to AEA for Fabric 1.20.1, newest first.

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
