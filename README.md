# ✦ Altair Client

A star-themed Minecraft Forge client-mod starter inspired by Altair, constellations, and deep-space nebulae.

## Visual identity
- Deep-space black and midnight blue
- Electric-blue starlight with violet nebula accents
- Minimal, constellation-inspired interface direction

## Current status
This repository contains **source code**, not a prebuilt/installable JAR. It is a starter scaffold; it does not yet include a finished GUI or feature modules.

## Compatibility
The initial configuration targets Minecraft **26.2**, Forge **65.1.5**, and Java 21. Verify the Minecraft/Forge versions and ForgeGradle plugin against official Forge downloads. If that exact combination is unavailable, update to a supported version pair.

## Build
With JDK 21 and Gradle installed, run:

```bash
gradle build
```

If successful, the JAR will be in `build/libs/`. The included GitHub Actions workflow attempts this build on each push and pull request and uploads the JAR as an artifact only if compilation succeeds.

## Scope
This starter is for client-side UI, accessibility, benign visual/debug utilities, and private-world experimentation. It does not implement combat automation or competitive multiplayer cheats.
