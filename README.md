# itzMark Client (Fabric, Minecraft 1.21.11)

Dark red/black client-side mod: mod menu, HUD, zoom, skin/cape changer, FPS boost toggles.

## Get the jar (easiest, no setup)
1. Make a free GitHub account, create a new repository, and upload everything from this folder
   (keep the `.github` folder).
2. Open the repo's **Actions** tab. The "Build itzMark Client jar" workflow runs by itself
   (or click Run workflow).
3. When it turns green, open the run and download **itzmark-client-1.21.11-jar** from Artifacts.
   Unzip it: the .jar inside goes in `.minecraft/mods`.

## Build it yourself
JDK 21 + Gradle 9.2.1, then in this folder run `gradle build`.
Jar: `build/libs/itzmark-client-1.0.0.jar`

## Install
Fabric Loader 0.18.1+ and Fabric API for 1.21.11, then drop the jar in `mods`.

## Use
- Right Shift: open / close the menu (rebind in Controls > itzMark Client)
- C (hold): zoom (Zoom module must be on)
- Skins: put `skin.png` (64x64) and `cape.png` (64x32) in `.minecraft/itzmark/`,
  turn on Skin Changer / Custom Cape, click "Reload skin / cape".
  Client-side only: only you see it.
- Settings save to `config/itzmark-client.json`.

## FPS boost
The FPS Boost panel lowers video settings (restores vanilla defaults when off).
For big gains also install Sodium, Lithium, FerriteCore, Entity Culling and ImmediatelyFast.
