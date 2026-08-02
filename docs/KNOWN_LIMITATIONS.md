# Known Limitations

- Runtime startup and gameplay are intentionally untested; static success is not a claim of runtime compatibility.
- Techguns is a beta FML coremod and is the highest-risk addition alongside the existing performance/coremod stack.
- Custom NPC encounter files are blueprints, not pre-instantiated world NPCs. The disposable-world test must instantiate them with the NPC Wand and verify faction behavior.
- Custom NPCs guards use stable Reforged ranged items; Techguns weapon animations on Custom NPC entities are not claimed.
- Better Questing checkbox objectives are player-attested capabilities. The integration mod independently records Moon/Mars order and Analyzer completion, but it does not auto-complete every quest.
- Analyzer environmental readings describe dimension ambient conditions, not room seal integrity. Use Galacticraft's Oxygen Detector redstone output for sealed habitats.
- Existing `saves/New World` predates the conversion and may retain removed-mod IDs; it was not migrated or validated.
- Technic may overwrite local files if the user explicitly reinstalls/resets the original public Tekkit 2 pack. Use the rollback/export instructions and avoid “reinstall pack” for this local customized instance.
- GroovyScript hot reload covers the `groovy/postInit` recipe, HEI visibility, and tooltip layer. JAR changes, Java classes, mod additions/removals, most configuration changes, and pre-init registrations still require a complete game restart.
- Reloading is a development convenience, not an end-to-end Minecraft simulator. A clean startup test remains necessary before calling a build shippable.
- The core mod migrates known inherited key conflicts only while each action still has its original conflicting value. Explicit player key choices are preserved.
