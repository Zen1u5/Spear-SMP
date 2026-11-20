# SpearSMP 1.0.0 — Paper 1.21.11

This is a source project. GitHub Actions compiles the JAR; this ZIP itself is not a JAR.

## Build the plugin
1. Upload/extract the contents of this folder into your GitHub repository (the `.github` folder must be included).
2. Open the repository's **Actions** tab and select **Build SpearSMP**. Run workflow, or push a commit to `main`/`master`.
3. Open the successful run and download the **SpearSMP-Plugin** artifact. Extract that artifact ZIP; inside is `SpearSMP-1.0.0.jar`.

## Install and test
1. Stop the Paper 1.21.11 server.
2. Put `SpearSMP-1.0.0.jar` in `plugins/` and start the server.
3. In game run `/spear help`, then `/spear start`.
4. `/spear stats` shows the held/carried spear tier and your total kills/progress.
5. Operators can use `/spear give <player> <tier>` (tier names: wood, stone, coal, copper, redstone, iron, lapis, gold, diamond, netherite, god).
6. Right-click while holding the spear in your main hand to use Void Dash (5 second cooldown).

## Included mechanics
- Kill progression: Wood→Stone→Coal→Copper (1 kill per step); Copper→Redstone→Iron→Lapis→Gold (2 kills per step); Gold→Diamond→Netherite→God (3 kills per step).
- Per-player total kills and current tier progress saved in the plugin config.
- Bound spear cannot be dropped.
- Sharpness/fire aspect scale by tier; God spear gains Sharpness after kills up to X.
- Void Dash, cooldown, particles and sound.

## Resource pack
Use the separate `SpearSMP-ResourcePack.zip`. It contains 11 custom item textures/models and a trident custom-model-data dispatch for values 1001–1011. Upload the ZIP to a direct-download host, then put its direct URL and SHA-1 in `server.properties`. Keep `require-resource-pack=false` while testing; change it to `true` only after clients successfully download and display the pack.

## Scope / testing note
This is a first functional implementation of the core loop, not a polished final release. It uses a trident as the base item, not a native spear item. Build it with the included GitHub Actions workflow and check the Actions log; the source has not been compiled in this environment. Back up your world and test on a copy before putting it on a public server.
