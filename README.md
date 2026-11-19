# ✦ Altair Client

A star-themed Minecraft **Forge** client mod inspired by Altair, constellations and deep-space nebulae. Client-only.

## Visual identity
- Deep-space black, midnight blue and violet nebula accents, electric-blue starlight
- Starry skies everywhere: title screen, Options, Singleplayer, Multiplayer and the loading screen
- Dark rounded buttons with a violet glow on hover

## Features

Open the menu with **Right Shift** (configurable in Controls). The menu has three folders and a ⚙ shortcut to Settings in the header.

### Mods
| Category | Modules |
|---|---|
| Combat | Combo Counter, Reach Display, Hit Indicator, CPS Counter, Health Display, Damage Indicator, Totem Counter, Arrow Counter, Custom Crosshair |
| Render | FPS, Hitbox, Armor HUD, Keystrokes, Ping, Freecam, Freelook (360° orbit), Custom / Dynamic FOV, Perspective, Fullbright, Time and Weather changers, Block Outline, Clear Water, No Hurt Camera, Custom Nametags, Better Chat Bubbles, Hide Boss Bar, Hide Scoreboard |
| Player | Potion Status, Smooth Zoom, Toggle Sprint, Toggle Sneak, Item Name, Auto Respawn, Auto Tool |
| World | Memory Display, Direction HUD, Server Address, World Info |
| Misc | Ultra Optimizer, Auto GG, Chat Timestamps, Fast Game |
| Apps | **Notes**, **Dynamic Island**, Screenshot, Screenshot Upload |

### Apps
- **Notes** – a small Word-style window inside Minecraft: bold, italic, underline, strikethrough, 8 text colors, bullet / numbered / task lists, a Coords button, copy / cut / paste, undo / redo (Ctrl+Z, Ctrl+Y). Movable and resizable, saved automatically to `config/altairclient-notes.json`.
- **Dynamic Island** – a pill at the top with the music playing on your PC (title, artist, progress, pause / next / previous). Windows reads the system media controls; Linux needs `playerctl`. PC notifications (Discord, etc.) cannot be read from a mod.
- **Screenshot / Screenshot Upload** – clean screenshots, optionally sent to your own Discord webhook (`altairclient-webhook.txt`).

### Textures
Over 100 effects grouped in PvP, Build, Breaking, Movement, Walk, Animations, Extra and Misc. The resource pack is generated at runtime from the vanilla assets, so nothing is bundled. Misc includes the **Altair Sky** (navy and purple starry sky, no sun or moon), **Altair Rain** and **No Animation**.

### Settings
- **Auto-save** – modules, their settings, texture toggles and HUD positions are saved to `config/altairclient-modules.json` and restored on start. Also *Save now*, *Load saved* and *Disable all*.
- **Title Background** – the Altair title screen, menu backgrounds and loading screen.
- **Round Buttons** – dark rounded vanilla buttons.
- **Tab Fleur-de-lis** and **Altair Link** – a ⚜ before the name of Altair users in the TAB list. Link recognises other users through one hidden chat message sent a few seconds after you join a server. Some servers do not allow automatic chat messages: turn Link off there.

There is also a **HUD Editor** to drag every overlay where you want it.

## Compatibility
Minecraft **26.2**, Forge **26.2-65.1.6**, ForgeGradle **7.0.40**, Gradle **9.3.1**, Java **25**. Optional mixins are registered through the jar manifest and fail soft (`required: false`), so a mixin that does not match a Minecraft update disables only its own feature.

## Build
With JDK 25 installed (the Gradle wrapper downloads Gradle on its own):

```bash
./gradlew build
```

The mod JAR is written to `build/libs/`. The GitHub Actions workflow builds it on every push and pull request and uploads the JAR as an artifact. Versions live in `gradle.properties` (`minecraft_version`, `forge_version`, `mod_version`).

## Status
Everything compiles in CI, but most visual features are built without a local game to test them, so expect rough edges. Please report anything that looks wrong.

## Scope
Client-side UI, accessibility, visual and debug utilities, and private-world experimentation. Some modules (Freecam, Fullbright, Hitbox display, Auto GG, Altair Link) can be restricted or banned on some servers: check the rules before using them online.
