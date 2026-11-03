# Architecture

Technical routing for the current implementation, checked against game `0.98a-RC8`. Code paths below are relative to `jars/src/`; data paths are repository-relative. Search the named owner before adding another implementation.

| Reference | Scope |
|---|---|
| [CLAUDE.md](../CLAUDE.md) | Workflow, build gate and document upkeep |
| [RELEASE.md](RELEASE.md) | Version numbers, release builder, package contents and publishing |
| [CHANGELOG.md](CHANGELOG.md) | Change-note format and upkeep for every implementation task |

## Start here

| Change / symptom | Route |
|---|---|
| Lamp dialog options missing or doubled | `data/campaign/rules.csv` `moreColours_fusionLampMainOptions_dOff` / `_dOn` (trigger `FusionLampMainOptions`, keyed on `$isDiscoMode`) over vanilla `fusionLampMainOptions` |
| Add or change a fixed colour | `data/campaign/rules.csv` `moreColours_fusionLamp_changeColor` (option list) -> one `moreColours_fusionLamp<Name>` row (`SetColor` on both lamp keys) |
| Disco mode does not start or stop | rules `moreColours_engageDiscoMode` / `moreColours_disengageDiscoMode` -> `MoreColour_DiscoControls ON/OFF` -> `DiscoBallScript` |
| Disco colours or music wrong, too loud, out of sync | `disco/scripts/DiscoBallScript.advance -> playDansen -> colourList`, `getAdjustedAmt` |
| Disco mode silent and frozen after loading a save | `disco/plugins/ModPlugin.beforeGameSave -> DiscoBallScript.setMuted`; [save data](#save-data) |
| Kanta's Den lamp | `disco/scripts/KantaLampInterceptorScript -> KantaCMD.FusionLampColorChanger` swapped for `DiscoBallScript` |
| Music file or volume | `data/config/sounds.json` `carameldansen` -> `sound/carameldansen.ogg` |

## Registration and lifecycle

| Registry / hook | Owners |
|---|---|
| `mod_info.json` | id `disco`; jar `jars/MoreLampColourOptions.jar`; `modPlugin` `disco.plugins.ModPlugin`; no dependencies |
| `ModPlugin.onGameLoad` | Adds a new transient `KantaLampInterceptorScript` to the sector every load |
| `ModPlugin.beforeGameSave` / `afterGameSave` | Set `setMuted(true)` / `setMuted(false)` on every `DiscoBallScript` in `Global.getSector().getScripts()` |
| `MoreColour_DiscoControls ON` | Sets `$isDiscoMode` on the lamp's local memory; `Global.getSector().addScript(new DiscoBallScript(lamp.getOrbitFocus().getMarket()))` |
| `MoreColour_DiscoControls OFF` | Clears `$isDiscoMode`; `setDone(true)` on every sector `DiscoBallScript` whose market id matches the lamp's |
| `DiscoBallScript` constructor | `BaseCampaignEventListenerAndScript()` calls `addListener(this)`, so each script is also a saved sector listener for `reportFleetJumped`; `setDone(true)` removes it |
| `data/campaign/rules.csv` | Main options, colour options, disco on/off rows (below) |
| `data/config/sounds.json` | Sound id `carameldansen` |
| `data/config/LunaSettingsConfig.json` | LunaLib settings icon for `disco`: `graphics/discoballs/icon/icon.png`; the mod has no settings |
| `data/config/version/version_files.csv` | Version Checker entry for `DiscoBalls.version` |

### Rules

Vanilla `fusionLampBeginPlayer` (`OpenInteractionDialog`, `$customType == fusion_lamp`, `$faction.id == player`) fires `FusionLampMainOptions`. The mod's two `FusionLampMainOptions` rows each have one condition, so they beat vanilla's unconditioned `fusionLampMainOptions` and replace its option list: vanilla colours (`fusionLamp_changeColor`), the mod's colours (`moreColours_fusionLamp_changeColor`), engage or disengage, help, leave. Non-player lamps (Kanta's Den) never show this menu. Colour rows write `$core_lampGlowColor` and `$core_lampLightColor`, the keys `FusionLampEntityPlugin` reads (`GLOW_COLOR_KEY`, `LIGHT_COLOR_KEY`). Picking a fixed colour does not end disco mode; the next disco tick overwrites it.

## Data identity

| Stable ID | Meaning |
|---|---|
| `disco` | Mod id (`mod_info.json`, LunaLib config key) |
| `$isDiscoMode` | Lamp local memory flag, `MoreColour_DiscoControls.DISCO_MODE_ENGAGED` |
| `carameldansen` | Sound id |
| `moreColours_*` | Rule ids and option ids; the mod was first named More Lamp Colour Options, which also survives in the jar name `MoreLampColourOptions.jar` and the IDE module `DiscoLamps.iml` |

## Source owners

Use `rg --files jars/src` for the complete inventory.

| File | Owner / connection |
|---|---|
| `disco/plugins/ModPlugin.java` | Kanta interceptor registration and the save mute |
| `disco/scripts/DiscoBallScript.java` | One per lamp in disco mode: music, colour cycling, jump handling. Saved |
| `disco/scripts/KantaLampInterceptorScript.java` | Transient; replaces vanilla's Kanta's Den colour changer with a `DiscoBallScript` once, then `done` |
| `com/fs/starfarer/api/impl/campaign/rulecmd/MoreColour_DiscoControls.java` | Rule command `MoreColour_DiscoControls ON/OFF`. Lives in the vanilla rule command package so `rules.csv` finds it by simple name |
| `disco/tools/ReleaseBuild.java`, `ReleaseBuildChecks.java` | Release builder and its checks, never shipped; owned by [RELEASE.md](RELEASE.md) |

## Contracts

### DiscoBallScript

- `runWhilePaused()` is true so `playDansen` can zero the volume while paused; it zeroes it when paused, muted, the player is in another location, or any dialog, menu or interaction dialog is open, and still advances `total`.
- The lamp is found each frame as the `Entities.FUSION_LAMP` custom entity whose orbit focus is `market.getPrimaryEntity()`. No lamp found: `setDone(true)`.
- Colour step: every 0.365 s of adjusted time, `picker.pickAndRemove()` from `colourList` (13 colours), refilled when empty, so every colour shows once per cycle.
- Music: `playSound("carameldansen", ...)` at the lamp; volume `min(1, 250 / distance)`; restarted when `total > 178` s, the track length. `getAdjustedAmt` halves `amount` in fast forward so music timing and colour steps stay in real time.
- `caramelDansen` is `transient`: after a load the next frame starts the track again.
- `isDone()` is `done || market.getContainingLocation() == null`.
- `reportFleetJumped` restarts or mutes the music as soon as the player fleet jumps in or out, before the next frame.

### Save data

- Saved: `DiscoBallScript` (sector scripts and listeners, or Kanta's Den entity scripts) with `market`, `picker`, `done`, `isMuted`, `count`, `total`; `$isDiscoMode` in lamp memory. Class names `disco.scripts.DiscoBallScript` and the rule command must not move or be renamed.
- `beforeGameSave` runs before the save is written, so `isMuted` is saved as `true`; `afterGameSave` only clears it in the running game. Nothing clears it on load, so a player lamp's disco mode stays silent with frozen colours after loading until it is disengaged and engaged again. Kanta's Den's script is an entity script, not in `getScripts()`, and is never muted.

### Kanta's Den

- Vanilla `KantaCMD` `installFusionLamp` adds `KantaCMD.FusionLampColorChanger` to `kantas_den`'s primary entity. `KantaLampInterceptorScript` polls every unpaused frame because no callback marks that install; it stops once the entity has a `DiscoBallScript`, so after the swap it ends on the first frame of each load.
- The Kanta lamp is pirate-owned, so its dialog never shows the mod's options; its disco mode cannot be turned off.

## Dead or dormant

| Component | State |
|---|---|
| `KantaLampInterceptorScript` | Polls `kantas_den` every frame until the Kanta lamp is installed; idle in games where that never happens |
