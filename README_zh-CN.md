# RandomAdditions

Minecraft 1.12.2 Forge mod. 加了一些好用的小功能和一些作者觉得好用的功能。

### AE2
- **无线存入快捷键** (ALT+V) — 将主手物品直接存入 ME 网络，自动从背包或饰品栏查找无线终端
- **TOP 显示存储量** — 持有无线终端时，TOP 显示目标物品在 ME 网络中的总存储量（物品数 / 流体 mB），可合成物品标注 `[可合成]`
- **TOP 显示节点数** — 显示所在位置连接的 AE2 网格节点数量
- **物品悬浮提示** — 持有无线终端时，所有物品 tooltip 追加 ME 网络存储量
- **JEI 流体提示** — 持有无线终端时，JEI 流体 tooltip 显示 ME 网络中该流体的总量

### Draconic Evolution + Baubles
- **传送符咒** — 高级传送符咒可作为 Baubles 饰品佩戴 并使用快捷键打开gui


### Thaumcraft
- **坩埚黑名单** — 禁止指定物品在坩埚中溶解（可配置）
- **冶炼炉黑名单** — 禁止指定物品在冶炼炉中熔炼

### Thaumic Additions
- **高级蒸馏炉黑名单** — 同上，针对 TA 的高级蒸馏炉

### Tinkers' Construct
- **Thaumonomicon 工具占位修复** — 防止 TiC 工具被魔导手册识别为"已附魔物品"占位

只要注明来源，你可以自由地把它添加到你的模组包里

## 构建

```bash
./gradlew build
```

Java 25 + RetroFuturaGradle。
