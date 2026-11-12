# ✦ Altair Client

A star-themed Minecraft Fabric client mod inspired by Altair, constellations, and deep-space nebulae.

## Visual identity
- Deep-space black and midnight blue
- Electric-blue starlight with violet nebula accents
- Minimal, constellation-inspired interface direction

## Current status
The client includes a module menu and HUD editor, plus optional FPS, CPS, armor, keystrokes, potion, and world-information overlays and a hold-to-zoom key.

## Compatibility
The project targets Minecraft **26.2**, Fabric Loader **0.20.1**, Fabric API **0.161.0+26.2**, and Java **25**.

## Build
With JDK 25 and Gradle 9.9.0 installed, run:

```bash
gradle build
```

If successful, the Fabric mod JAR will be in `build/libs/`. The included GitHub Actions workflow builds it on each push and pull request and uploads the JAR as an artifact.

## Scope
This client is for client-side UI, accessibility, benign visual/debug utilities, and private-world experimentation. It does not implement combat automation or competitive multiplayer cheats.
