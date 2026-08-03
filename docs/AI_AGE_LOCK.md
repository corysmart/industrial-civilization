# AI Age Lock

AE2 rv6-stable-7 is fully reconstructed behind the AI Age. The reloadable content script captures every inherited AE2 crafting output, removes the original recipes, and adds AI-authorized replacements. The twelve foundation components use the durable Artificial Industrial Intelligence Core, an Industrial Control Processor, iron, and redstone. Every additional catalog output also consumes an AE2 Energy Acceptor and an IC2 advanced circuit, preserving the foundation tier while keeping the complete mod accessible.

```groovy
crafting.streamRecipes()
    .filter { recipe -> recipe.registryName?.toString()?.startsWith('appliedenergistics2:') }
    .removeAll()
```

The AI Core is manufactured only after Martian Autonomy and comparative Lite Matter research. Its container-item behavior returns it after crafting, making it an authorization key rather than a consumable. This prevents all pre-AI AE2 entry points while avoiding inaccessible late-catalog parts.
