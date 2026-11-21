# SpearSMP 1.1.0 — Paper 1.21.11

This source ZIP is built for GitHub Actions; it is not itself the installable plugin JAR.

## Build
1. Upload the contents of this ZIP to the root of your GitHub repository (include `.github`).
2. Open **Actions** → **Build SpearSMP**. Download the `SpearSMP-Plugin` artifact from a successful run.
3. Extract the artifact and place `SpearSMP-1.1.0.jar` in the server's `plugins` folder.

## Commands
- `/spear start` — receive a wooden spear.
- `/spear stats` — view kills and progress.
- `/spear give <player> <tier>` — operator-only tier grant.
- `/spear reload` — reload configuration.
- `/player [name]` — operator-only Citizens player NPC for kill-progression testing. Requires Citizens installed.

## Features in this source
- 11 tiers and configured kill progression.
- Per-tier weapon damage, unbreakable bound spears, and custom model data 1001–1011.
- One unique right-click ability and individual cooldown per tier. Cooldown is shown in the action bar above the hotbar.
- God-tier Thunder I: visual-only lightning and electric particles on each living target hit; no fire or extra lightning damage.
- Citizens integration is reflective/optional; plugin still loads without Citizens, but `/player` reports that Citizens is required.

## Resource pack
Use `SpearSMP-ResourcePack-1.1.0.zip` as the server resource pack. It contains separate pixel-art textures and item models for each tier. Configure your host's direct-download URL and the SHA-1 of the exact ZIP in `server.properties`. Test with `require-resource-pack=false` first.

## Important testing note
The build environment used for this package did not have Maven installed, so the Java source has **not been compiled or tested here**. Use the included GitHub Actions workflow to compile it. If the workflow fails, send the build log before installing it. Back up the server first. This is a feature-complete source draft, not a verified production build.
