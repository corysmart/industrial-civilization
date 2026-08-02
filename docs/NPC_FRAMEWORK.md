# NPC Framework

Custom NPCs July 2020 is the authoring/runtime framework. It supports human skins/models, roles, traders, guards, dialogue, factions, reputation, conditional hostility, scripted behavior, ranged weapons, and persistence.

Three pack-owned encounter blueprints live in `config/industrialcivilization/factions/`. They are deliberately data-only templates because generating Custom NPCs' world-specific binary clone database without launching a world would be unsafe. During the disposable-world test, use the NPC Wand to instantiate the three templates and attach the described roles/factions.

Direct Techguns animation/inventory integration with this Custom NPCs build was not statically provable. Player firearms remain Techguns; template guards use stable Reforged muskets/crossbows. Custom NPC ranged behavior and reputation take priority over cosmetic gun animations.
