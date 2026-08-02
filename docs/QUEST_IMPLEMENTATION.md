<!-- Generated from progression/*.json by tools/generate_progression_docs.py. -->

# Quest Implementation

Better Questing 3 reads `config/betterquesting/DefaultQuests.json`. Run `python3 tools/generate_objectives.py` after canonical edits; never hand-edit generated quests. The current projection has 16 quest lines and 108 quests.

Possession milestones use Standard Expansion `bq_standard:retrieval` tasks with NBT ignored, group detection enabled, and consumption disabled. Construction, operation, mastery, research, and transitions use manual checkboxes where no reliable runtime trigger exists. Each such quest lists final validation; placeholder-backed quests add a conspicuous `TEMPORARY VALIDATION` section.

Quest IDs are deterministic from chapter number and milestone order. Cross-chapter prerequisites use the same global numeric map. `pack_version` is 2. Import/update the default pack through Better Questing on a disposable test world if an existing world retains the older database.

The quest graph documents gates but does not yet intercept Galacticraft destination selection. Runtime destination and operation enforcement remains future work.
