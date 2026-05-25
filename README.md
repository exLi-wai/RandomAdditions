# RandomAdditions

[中文版](README_zh-CN.md)

Minecraft 1.12.2 Forge mod. Some useful small functions have been added, and some features that the author finds useful。

### AE2
- **Wireless Terminal Shortcut** (ALT+V) — Directly store held items into ME network, automatically searches for wireless terminal from inventory or baubles slots
- **TOP Storage Display** — When holding a wireless terminal, TOP shows total storage of target item in ME network (item count / fluid mB), craftable items marked with `[Craftable]`
- **TOP Node Count Display** — Shows the number of AE2 grid nodes connected at current location
- **Item Tooltip Enhancement** — When holding a wireless terminal, all item tooltips show ME network storage count, with `[Craftable]` if the item is craftable
- **JEI Fluid Tooltip** — When holding a wireless terminal, JEI fluid tooltips show total amount of that fluid in ME network, with `[Craftable]` if the fluid is craftable

### Draconic Evolution + Baubles
- **Teleport Charm** — Advanced teleport charm can be worn as Baubles accessory and opened via shortcut key to access GUI, with fuel validation

### QuantumThings
- **Time in a Bottle** — Time in a Bottle can be worn as a Baubles accessory and used via shortcut key
### Thaumcraft
- **Cauldron Blacklist** — Prevent specified items from dissolving in cauldrons (configurable)
- **Smeltery Blacklist** — Prevent specified items from smelting in smelteries

### Thaumic Additions
- **Advanced Distillation Tower Blacklist** — Same as above, for TA's advanced distillation tower

### Tinkers' Construct
- **Thaumonomicon Tool Placeholder Fix** — Prevent TiC tools from being recognized by Thaumonomicon as "enchanted items" placeholders
- **Forget Modifier** — Removes all inscriptions (embossments) from a Tinkers' tool, clearing all extra traits added by them

You can freely add it to your modpack，as long as you indicate the source

## Building

```bash
./gradlew build
```

Java 25 + RetroFuturaGradle.