# AI Age Lock

AE2 rv6-stable-7 is installed so IDs, future compatibility, and pack direction are visible, but `groovy/postInit/industrial_civilization.groovy` removes every crafting-table recipe whose registry namespace is `appliedenergistics2`:

```groovy
crafting.streamRecipes()
    .filter { recipe -> recipe.registryName?.toString()?.startsWith('appliedenergistics2:') }
    .removeAll()
```

This removes every AE2 crafting recipe, a conservative hard lock that prevents processors, terminals, controllers, storage cells, and all alternative entry points. The objective database supplies only a locked-era teaser; it grants nothing. Removing ProjectE and the IC2 UU-Matter add-on also prevents the intended future matter/replication systems from leaking into the slice.

Future unlock work should replace the blanket removal with a researched AI Age component produced only after validated Lite Matter records, restore a curated processor/controller chain, and then separately design UU-Matter, replication, and exotic-material progression. None of that is implemented here.
