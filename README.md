# ✦ Altair Client

A star-themed Minecraft Forge client mod inspired by Altair, constellations, and deep-space nebulae.

## Visual identity
- Deep-space black and midnight blue
- Electric-blue starlight with violet nebula accents
- Minimal, constellation-inspired interface direction

## Current status
The client includes a module menu and HUD editor, plus optional FPS, CPS, armor, keystrokes, potion, and world-information overlays and a hold-to-zoom key.

## Compatibility
The project targets Minecraft **26.2**, Forge **26.2-65.1.6**, ForgeGradle **7.0.40**, Gradle **9.3.1**, and Java **25**. It is a client-only mod.

## Build
With JDK 25 installed, run (the Gradle wrapper downloads Gradle 9.3.1 on its own):

```bash
./gradlew build
```

If successful, the Forge mod JAR will be in `build/libs/`. The included GitHub Actions workflow builds it on each push and pull request and uploads the JAR as an artifact.

Versions live in `gradle.properties` (`minecraft_version`, `forge_version`).

## Scope
This client is for client-side UI, accessibility, benign visual/debug utilities, and private-world experimentation. It does not implement combat automation or competitive multiplayer cheats.
