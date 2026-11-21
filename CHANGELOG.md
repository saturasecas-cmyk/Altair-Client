# Changelog

## 2.2 - Beta 2 (2026-11-21)
Fixed / changed after the second round of beta testing:
- Walk Aura: much more intense (4 larger particles per tick).
- Minimal Motion Blur: much stronger, wider edge streaks.
- Simple XP Bar: the vanilla experience bar is now hidden (Hud mixin).
- Clean Inventory: hides the potion-effects panel in the inventory (mixin on `EffectsInInventory`).
- Fullbright: fills the lightmap like full night vision (mixin on `LightmapRenderStateExtractor`) on top of the gamma change.
- Removed the whole Textures > Movement Animations category.
- Title background and Altair buttons are ON by default (also for older config files).
- New safety guard: all modules are turned off automatically if the previous session crashed, or if FPS stays below 8 for 5 s in a world.

## Beta 1 (2026-11-20)
- Title screen background, custom crosshair, pumpkin overlay, short swords, clear water/lava, block-break particles.
- Menu remembers the last page; duplicates and Freelook removed; everything in English.
- Compass size setting, animated RAM graph, stronger Ultra Optimizer, lighter starfield.
