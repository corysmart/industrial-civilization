<!-- Generated from progression/*.json by tools/generate_progression_docs.py. -->

# Main Questline Creative Progression Test Plan

## Scope and pass condition

This plan tests only the **16 numbered chapters and their 116 main-line quests**. Independent faction, vehicle, salvage, weapon, and strategic-defense side quests are excluded. The two main transition quests that support a side-route must be tested through their numbered-chapter construction route in this run.

A pass requires every quest to complete from its declared automatic evidence, every prerequisite boundary to behave correctly, every matching vanilla advancement to agree with Better Questing, and every broader gameplay assertion to be checked separately. Inventory evidence proves acquisition only; it does not prove that the represented structure or machine genuinely works.

Current main-line detection split: **72 non-consuming inventory tasks** and **44 runtime-advancement tasks**.

## Test-world preparation

1. Fully quit Minecraft and restart the Technic pack so the latest integration JAR and generated quest database are loaded.
2. Create a new disposable Creative world with cheats enabled. Do not use a valued survival world and do not reuse a player whose advancements already contain Industrial Civilization progress.
3. Let the world warmup overlay finish. Open F6 and verify 16 numbered chapter tabs appear, the first quest is centered, all future chapters remain aspirationally visible, and locked quests cannot be opened or progressed.
4. Open Pause → Advancements and verify the Industrial Civilization root and ordered quest nodes exist. Return to the world and use F6 as the primary guide.
5. Do not use `/advancement grant`, Better Questing editing/complete commands, `/ic_test`, NBT editors, or the runtime space-gate bypass. Creative item giving is allowed only for the inventory-evidence steps listed below.
6. Create a labeled staging area with chests for quest evidence, IC2 power sources/storage, Industrial Civilization machines, ComputerCraft computers, Galacticraft launch/habitat equipment, and sample materials.
7. Keep the player's inventory free of evidence for quests that have not unlocked. After an inventory quest completes, return its evidence to the staging chest unless an upcoming runtime step requires it. This prevents a newly unlocked retrieval task from completing before its negative assertion can be observed.
8. Use Creative search or `/give @p <namespace:item> <count> <metadata>` for listed evidence. A trailing `:*` means any metadata accepted by the quest; prefer the exact machine or tool described by the quest.
9. Allow at least two seconds for inventory tasks and up to five seconds for ordinary runtime telemetry. Sustained off-world base mastery requires 24 five-second samples, or two continuous minutes.
10. After every action, compare F6 and Pause → Advancements. Record the quest ID, evidence used, result, unexpected unlocks, and screenshots. Run `/ic_status` when runtime evidence appears delayed.

## Global negative-gate checks

- Moon travel must fail before the Orbital Research Archive is recorded.
- Quantum Technology must remain locked before the Lunar Engineering Archive quest completes.
- Mars travel must fail without both the Lunar Quantum Component and Mars Mission Authorization.
- AI Age Entry must remain incomplete until the AI Core, Martian Autonomy, and Lite Matter runtime records all exist.
- The Technical Phase Pearl must have no usable pre-AI acquisition route and must not complete from a Creative-spawned pearl.
- Future visible quests must not accumulate progress while locked.

## Ordered quest procedure

## Chapter 01 — Early Survival and Workshop

**Chapter purpose:** Secure a compact workshop and gather the material families needed for electrification.

**Entry check:** Confirm this opening quest is available immediately in the fresh world and every later chapter is visible but locked.

### 1. Industrial Starter Materials (`first_resources`)

- **Prerequisites:** none; this is the opening quest.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: 8× `minecraft:coal`, 8× `minecraft:iron_ingot`, `ic2:blockmetal:0`, `ic2:blockmetal:1`, `ic2:itemmisc:450`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `basic_storage`, `defensive_readiness`, `manual_production_area`.
- **Gameplay assertion:** Obtain coal, iron, copper, tin, and rubber.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 2. Copper or Iron Storage (`basic_storage`)

- **Prerequisites:** `first_resources`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `ironchest:iron_chest:3`, `ironchest:iron_chest:0`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `secure_workshop`.
- **Gameplay assertion:** Build either a Copper Chest or an Iron Chest and keep it in your inventory long enough for the questbook to recognize it.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 3. Workshop Defense (`defensive_readiness`)

- **Prerequisites:** `first_resources`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `techguns:pistol`, `techguns:itemshared:11`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete; no numbered main-line quest directly depends on it.
- **Gameplay assertion:** Obtain a basic defensive weapon and ammunition.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 4. Manual Production Area (`manual_production_area`)

- **Prerequisites:** `first_resources`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `minecraft:crafting_table`, `minecraft:furnace`, `minecraft:chest`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `secure_workshop`.
- **Gameplay assertion:** Arrange crafting, furnaces, storage, and safe lighting as a usable workshop.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 5. Secure Workshop (`secure_workshop`)

- **Prerequisites:** `basic_storage` AND `manual_production_area`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `minecraft:iron_door`, 16× `minecraft:torch`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `first_ic2_generator`. This is the declared completion milestone for Chapter 1.
- **Gameplay assertion:** Maintain a lit, enclosed workshop and prepare the materials for an IC2 Generator.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

**Chapter exit check:** Confirm `secure_workshop` is complete, the next numbered chapter is visible and correctly unlocked when applicable, and unrelated later chapters remain locked.

## Chapter 02 — Electrification

**Chapter purpose:** Establish safe IC2 generation, storage, machine processing, and ore doubling.

**Entry check:** Before completing this chapter's opening quest, confirm the chapter is visible for browsing but its locked quest cannot be opened or progressed. After the previous chapter's completion milestone is satisfied, confirm the opening quest becomes available without a reload.

### 6. First IC2 Generator (`first_ic2_generator`)

- **Prerequisites:** `secure_workshop`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `ic2:blockgenerator:*`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `low_voltage_network`.
- **Gameplay assertion:** Build and fuel an IC2 Generator.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 7. Safe Low-Voltage Network (`low_voltage_network`)

- **Prerequisites:** `first_ic2_generator`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: 8× `ic2:itemcable:*`, `ic2:blockelectric:*`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `electric_processing`, `electric_tools`.
- **Gameplay assertion:** Connect generation, storage, and machines at safe voltage.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 8. Electric Processing Suite (`electric_processing`)

- **Prerequisites:** `low_voltage_network`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: 4× `ic2:blockmachinelv:*`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `ore_doubling`, `voltage_literacy`.
- **Gameplay assertion:** Successfully operate a Macerator, Electric Furnace, Extractor, and Compressor.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 9. Demonstrated Ore Doubling (`ore_doubling`)

- **Prerequisites:** `electric_processing`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `minecraft:iron_ore`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `stable_electrical_workshop`.
- **Gameplay assertion:** Macerate and smelt ore for at least twice the direct-smelting output.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 10. Electric Tools (`electric_tools`)

- **Prerequisites:** `low_voltage_network`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `ic2:itemdrills:*`, `ic2:itemtoolwrenchelectric:*`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete; no numbered main-line quest directly depends on it.
- **Gameplay assertion:** Build and charge basic IC2 electric tools.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 11. Voltage and Safety (`voltage_literacy`)

- **Prerequisites:** `electric_processing`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `minecraft:redstone_torch`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `stable_electrical_workshop`.
- **Gameplay assertion:** Explain or demonstrate voltage tiers, transformer placement, and insulation.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 12. Stable Electrical Workshop (`stable_electrical_workshop`)

- **Prerequisites:** `ore_doubling` AND `voltage_literacy`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: 4× `ic2:blockmachinelv:*`, `ic2:blockelectric:*`, 16× `ic2:itemcable:*`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `automated_ore_processing`. This is the declared completion milestone for Chapter 2.
- **Gameplay assertion:** Operate the full processing suite without cable loss or recurring power collapse.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

**Chapter exit check:** Confirm `stable_electrical_workshop` is complete, the next numbered chapter is visible and correctly unlocked when applicable, and unrelated later chapters remain locked.

## Chapter 03 — Automated Industry

**Chapter purpose:** Replace repeated mining, transport, processing, and crafting labor with physical automation.

**Entry check:** Before completing this chapter's opening quest, confirm the chapter is visible for browsing but its locked quest cannot be opened or progressed. After the previous chapter's completion milestone is satisfied, confirm the opening quest becomes available without a reload.

### 13. Automated Ore Processing (`automated_ore_processing`)

- **Prerequisites:** `stable_electrical_workshop`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `minecraft:hopper`, `ic2:blockmachinelv:*`, `minecraft:chest`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `quarry_extraction`, `automation_throughput`.
- **Gameplay assertion:** Route raw ore through processing into organized output without hand transfer.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 14. Quarry Extraction (`quarry_extraction`)

- **Prerequisites:** `automated_ore_processing`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `buildcraftbuilders:quarry`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `physical_logistics`.
- **Gameplay assertion:** Build and run a BuildCraft Quarry.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 15. Physical Logistics Network (`physical_logistics`)

- **Prerequisites:** `quarry_extraction`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: 4× `minecraft:hopper`, 2× `minecraft:chest`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `loaded_industry`, `wireless_control`, `early_autocrafting`.
- **Gameplay assertion:** Route Quarry output with BuildCraft pipes, ProjectRed tubes, Logistics Pipes, or rail.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 16. Loaded Industrial Area (`loaded_industry`)

- **Prerequisites:** `physical_logistics`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `minecraft:clock`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete; no numbered main-line quest directly depends on it.
- **Gameplay assertion:** Keep the automated industrial area loaded using an appropriate chunk-loading system.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 17. Remote Factory Control (`wireless_control`)

- **Prerequisites:** `physical_logistics`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `minecraft:lever`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete; no numbered main-line quest directly depends on it.
- **Gameplay assertion:** Start or stop a production subsystem using wireless redstone.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 18. Early Autocrafting (`early_autocrafting`)

- **Prerequisites:** `physical_logistics`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `minecraft:crafting_table`, `minecraft:hopper`, `minecraft:chest`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `automation_throughput`, `railcraft_steel`, `refined_fuel`. This is the declared completion milestone for Chapter 3.
- **Gameplay assertion:** Use a fixed-recipe automatic crafting table with physical ingredient routing for a repeated recipe.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 19. Automation Throughput Demonstration (`automation_throughput`)

- **Prerequisites:** `early_autocrafting` AND `automated_ore_processing`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `minecraft:minecart`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete; no numbered main-line quest directly depends on it.
- **Gameplay assertion:** Demonstrate sustained throughput above manual production.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

**Chapter exit check:** Confirm `early_autocrafting` is complete, the next numbered chapter is visible and correctly unlocked when applicable, and unrelated later chapters remain locked.

## Chapter 04 — Heavy Industry

**Chapter purpose:** Grow the workshop into a large steel, fuel, freight, power, and defense installation.

**Entry check:** Before completing this chapter's opening quest, confirm the chapter is visible for browsing but its locked quest cannot be opened or progressed. After the previous chapter's completion milestone is satisfied, confirm the opening quest becomes available without a reload.

### 20. Automated Railcraft Steel (`railcraft_steel`)

- **Prerequisites:** `early_autocrafting`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `railcraft:tool_pickaxe_steel`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `advanced_ic2`, `freight_infrastructure`, `industrial_armament`, `heavy_industry`.
- **Gameplay assertion:** Produce Railcraft steel in a repeatable industrial process.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 21. Oil Refining and Fluid Storage (`refined_fuel`)

- **Prerequisites:** `early_autocrafting`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `buildcraftfactory:distiller`, 4× `buildcraftfactory:tank`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `freight_infrastructure`, `heavy_industry`.
- **Gameplay assertion:** Refine oil and buffer fuel at industrial scale.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 22. Advanced IC2 Processing (`advanced_ic2`)

- **Prerequisites:** `railcraft_steel`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `ic2:blockmachinemv:*`, `ic2:blockelectric:*`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `mffs_installation`, `heavy_industry`.
- **Gameplay assertion:** Operate advanced IC2 machines on an appropriately transformed network.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 23. Industrial Freight (`freight_infrastructure`)

- **Prerequisites:** `railcraft_steel` AND `refined_fuel`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `minecraft:minecart`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete; no numbered main-line quest directly depends on it.
- **Gameplay assertion:** Move bulk materials or fuel using rail or an equivalent logistics trunk.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 24. Industrial Armament (`industrial_armament`)

- **Prerequisites:** `railcraft_steel`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `techguns:m4`, `techguns:itemshared:13`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete; no numbered main-line quest directly depends on it.
- **Gameplay assertion:** Manufacture a firearm and ammunition through the factory.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 25. Human Factions (`faction_contacts`)

- **Prerequisites:** `underworld_lead`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** Find and approach members of the Frontier Cooperative, Survey Detachment 7, and Ashline Raiders so all three factions are discovered. Complete at least one real IC Credit trade; merely opening a merchant is insufficient.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete; no numbered main-line quest directly depends on it.
- **Gameplay assertion:** Trade with a friendly settlement, survive a hostile encounter, and approach an ambiguous armed faction.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 26. Industrial Containment (`mffs_installation`)

- **Prerequisites:** `advanced_ic2`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `modularforcefieldsystem:projector`, `modularforcefieldsystem:capacitor`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete; no numbered main-line quest directly depends on it.
- **Gameplay assertion:** Build an MFFS defensive, restricted-area, or containment installation.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 27. Heavy Industrial Complex (`heavy_industry`)

- **Prerequisites:** `railcraft_steel` AND `refined_fuel` AND `advanced_ic2`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `railcraft:tool_pickaxe_steel`, `buildcraftfactory:distiller`, `ic2:blockmachinemv:*`, `minecraft:minecart`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `industrial_capacity_access`.
- **Gameplay assertion:** Sustain steel, refined fuel, advanced processing, powered storage, and bulk logistics.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 28. Industrial Capacity Secured (`industrial_capacity_access`)

- **Prerequisites:** `heavy_industry` OR `abandoned_factory_operational`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** Complete Heavy Industrial Complex on the main construction route. Remain in the world for at least five seconds so the route-transition telemetry can grant the advancement. Do not use the abandoned-factory side-route during this main-line run.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `computer_online`. This is the declared completion milestone for Chapter 4.
- **Gameplay assertion:** Complete the Heavy Industrial Complex construction route OR restore the side-path abandoned factory to safe operation.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

**Chapter exit check:** Confirm `industrial_capacity_access` is complete, the next numbered chapter is visible and correctly unlocked when applicable, and unrelated later chapters remain locked.

## Chapter 05 — Programmable Manufacturing

**Chapter purpose:** Use ComputerCraft monitoring and programmable production to reduce downtime and manual scheduling.

**Entry check:** Before completing this chapter's opening quest, confirm the chapter is visible for browsing but its locked quest cannot be opened or progressed. After the previous chapter's completion milestone is satisfied, confirm the opening quest becomes available without a reload.

### 29. Computer Online (`computer_online`)

- **Prerequisites:** `industrial_capacity_access`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `computercraft:computer`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `factory_telemetry`.
- **Gameplay assertion:** Boot a ComputerCraft computer and save a working factory-control program.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 30. Factory Telemetry (`factory_telemetry`)

- **Prerequisites:** `computer_online`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `computercraft:computer`, `minecraft:comparator`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `electric_fabricator`, `programmable_manufacturing`.
- **Gameplay assertion:** Monitor energy storage, one machine, and a fuel or fluid tank.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 31. Electric Fabricator (`electric_fabricator`)

- **Prerequisites:** `factory_telemetry`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `industrialcivilizationcore:electric_fabricator`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `programmable_assembler`.
- **Gameplay assertion:** Power an Electric Fabricator and use it to complete one component recipe.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 32. Programmable Assembler (`programmable_assembler`)

- **Prerequisites:** `electric_fabricator`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `industrialcivilizationcore:programmable_assembler`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `production_queue`.
- **Gameplay assertion:** Power a Programmable Assembler, select a recipe, and complete one production order.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 33. Queued Production Order (`production_queue`)

- **Prerequisites:** `programmable_assembler`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** Place and power a Programmable Assembler, attach a ComputerCraft computer, wrap the assembler peripheral, select `control_processor`, and queue five operations. Supply the required Precision Frames, Blank Data Cartridges, and Redstone. The first completed operation grants this quest.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `multi_step_manufacturing`.
- **Gameplay assertion:** Queue a production order and report progress and missing ingredients.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 34. Automated Multi-Step Manufacturing (`multi_step_manufacturing`)

- **Prerequisites:** `production_queue`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** Leave the same five-control-processor queue running with routed inputs and output space. The third completed operation grants this quest.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `programmable_manufacturing`.
- **Gameplay assertion:** Complete a multi-step product with automatic routing, monitored buffers, and no routine hand-crafting.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 35. Programmable Manufacturing (`programmable_manufacturing`)

- **Prerequisites:** `factory_telemetry` AND `multi_step_manufacturing`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** Keep telemetry attached and let the fifth queued Control Processor finish. Confirm the assembler reports completed operations and can recover after one deliberately removed ingredient is restored.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `programmable_capacity_access`.
- **Gameplay assertion:** Operate monitored, queued production that can identify shortages and recover from replenishment.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 36. Programmable Capacity Secured (`programmable_capacity_access`)

- **Prerequisites:** `programmable_manufacturing` OR `recovered_factory_control_system`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** After Programmable Manufacturing completes, remain in the world for at least five seconds so the validated main-route transition is recorded. Do not use the recovered-control-system side-route.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `nuclear_reactor`. This is the declared completion milestone for Chapter 5.
- **Gameplay assertion:** Complete the programmable manufacturing route OR restore and audit the captured factory control system.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

**Chapter exit check:** Confirm `programmable_capacity_access` is complete, the next numbered chapter is visible and correctly unlocked when applicable, and unrelated later chapters remain locked.

## Chapter 06 — Nuclear Age

**Chapter purpose:** Operate monitored high-output nuclear power suitable for orbital development.

**Entry check:** Before completing this chapter's opening quest, confirm the chapter is visible for browsing but its locked quest cannot be opened or progressed. After the previous chapter's completion milestone is satisfied, confirm the opening quest becomes available without a reload.

### 37. IC2 Nuclear Reactor (`nuclear_reactor`)

- **Prerequisites:** `programmable_capacity_access`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `ic2:blockchambers:*`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `reactor_output`, `nuclear_containment`.
- **Gameplay assertion:** Build a complete IC2 nuclear reactor and safe support area.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 38. Sustained Nuclear Output (`reactor_output`)

- **Prerequisites:** `nuclear_reactor`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `minecraft:redstone_block`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `reactor_telemetry`, `monitored_nuclear_power`.
- **Gameplay assertion:** Produce useful sustained power with a stable reactor design.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 39. Reactor Telemetry (`reactor_telemetry`)

- **Prerequisites:** `reactor_output`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `ic2:blockchambers:*`, `computercraft:computer`, `minecraft:comparator`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `emergency_shutdown`.
- **Gameplay assertion:** Monitor heat, output, and operating state.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 40. Remote Emergency Shutdown (`emergency_shutdown`)

- **Prerequisites:** `reactor_telemetry`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `ic2:blockchambers:*`, `minecraft:lever`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `monitored_nuclear_power`.
- **Gameplay assertion:** Trigger a remote shutdown and watch reactor output fall to zero.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 41. Nuclear Containment (`nuclear_containment`)

- **Prerequisites:** `nuclear_reactor`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `ic2:blockchambers:*`, `modularforcefieldsystem:projector`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete; no numbered main-line quest directly depends on it.
- **Gameplay assertion:** Use MFFS or equivalent containment appropriate to the reactor risk.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 42. Monitored Nuclear Power (`monitored_nuclear_power`)

- **Prerequisites:** `reactor_output` AND `emergency_shutdown`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `ic2:blockchambers:*`, `computercraft:computer`, `modularforcefieldsystem:projector`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `tier1_orbital_launch`, `extreme_voltage_industry`. This is the declared completion milestone for Chapter 6.
- **Gameplay assertion:** Operate stable, monitored high-output nuclear infrastructure for advanced manufacturing.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

**Chapter exit check:** Confirm `monitored_nuclear_power` is complete, the next numbered chapter is visible and correctly unlocked when applicable, and unrelated later chapters remain locked.

## Chapter 07 — Orbital Age

**Chapter purpose:** Make Earth orbit the first mandatory destination and establish a survivable, expandable station.

**Entry check:** Before completing this chapter's opening quest, confirm the chapter is visible for browsing but its locked quest cannot be opened or progressed. After the previous chapter's completion milestone is satisfied, confirm the opening quest becomes available without a reload.

### 43. Tier 1 Orbital Launch (`tier1_orbital_launch`)

- **Prerequisites:** `monitored_nuclear_power`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** Build and fuel a Galacticraft Tier 1 rocket, launch it, and arrive in an Earth-orbit space-station dimension. Wait up to five seconds after arrival for telemetry sampling.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `orbital_habitat`.
- **Gameplay assertion:** Build a Tier 1 launch system and safely reach Earth orbit; this alone does not authorize Moon access.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 44. Pressurized Orbital Habitat (`orbital_habitat`)

- **Prerequisites:** `tier1_orbital_launch`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** In orbit, build a sealed room with a working Galacticraft breathable-air system and place an Oxygen Detector inside it. Wait for the detector to emit redstone, then remain within 10 blocks until the next five-second telemetry sample. Merely carrying oxygen equipment or running a sealer without an active detector must not complete this quest.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `orbital_power`, `orbital_communications`, `functional_orbital_station`.
- **Gameplay assertion:** Establish pressure, oxygen, storage, and safe habitation in orbit.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 45. Orbital Power and Eclipse Storage (`orbital_power`)

- **Prerequisites:** `orbital_habitat`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `industrialcivilizationcore:environmental_solar_array`, `ic2:blockelectric:*`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `research_station`, `functional_orbital_station`.
- **Gameplay assertion:** Install IC2 solar generation and enough stored power to survive an eclipse.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 46. Orbital Communications and Telemetry (`orbital_communications`)

- **Prerequisites:** `orbital_habitat`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** While within 10 blocks of the active orbital Oxygen Detector, place a ComputerCraft computer or recognized telemetry block within 32 blocks. Wait for the next five-second sample.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `research_station`, `functional_orbital_station`.
- **Gameplay assertion:** Bring a ComputerCraft telemetry and communications system online.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 47. Research Station (`research_station`)

- **Prerequisites:** `orbital_power` AND `orbital_communications`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `industrialcivilizationcore:research_station`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `orbital_experiment_module`.
- **Gameplay assertion:** Install and power a Research Station aboard the orbital facility.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 48. Orbital Experiment Module (`orbital_experiment_module`)

- **Prerequisites:** `research_station`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `industrialcivilizationcore:orbital_experiment_module`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `functional_orbital_station`.
- **Gameplay assertion:** Install and power an Orbital Experiment Module where it can collect orbital observations.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 49. Functional Orbital Station (`functional_orbital_station`)

- **Prerequisites:** `orbital_habitat` AND `orbital_power` AND `orbital_communications` AND `orbital_experiment_module`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** Within 10 blocks of an active Oxygen Detector and 32 blocks of the other infrastructure, maintain recognized computer/telemetry, a Research Station, an Orbital Experiment Module, and a powered block or charged Environmental Solar Array. Keep the habitat stable for 24 five-second samples—two continuous minutes.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `orbital_experiments`. This is the declared completion milestone for Chapter 7.
- **Gameplay assertion:** Sustain habitation, oxygen, power, storage, telemetry, research equipment, and safe arrival infrastructure.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

**Chapter exit check:** Confirm `functional_orbital_station` is complete, the next numbered chapter is visible and correctly unlocked when applicable, and unrelated later chapters remain locked.

## Chapter 08 — Orbital Research

**Chapter purpose:** Prove sustained station operation through experiments and produce the archive that gates Moon access.

**Entry check:** Before completing this chapter's opening quest, confirm the chapter is visible for browsing but its locked quest cannot be opened or progressed. After the previous chapter's completion milestone is satisfied, confirm the opening quest becomes available without a reload.

### 50. Orbital Experiment Program (`orbital_experiments`)

- **Prerequisites:** `functional_orbital_station`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** In orbit, power an Orbital Experiment Module, insert one Blank Data Cartridge, and complete the `record_orbital_data` operation. Take or leave the resulting orbit-tagged Research Data in the output slot.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `orbital_operational_data`.
- **Gameplay assertion:** Complete the vacuum-materials, radiation, telescope, microgravity, life-support, communications, and eclipse experiments available at the Research Station.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 51. Orbital Operational Data (`orbital_operational_data`)

- **Prerequisites:** `orbital_experiments`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** The same completed `record_orbital_data` operation grants this evidence. Verify both quests complete from the machine operation, not from a spawned Research Data item.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `orbital_solar_industry`, `orbital_research_complete`.
- **Gameplay assertion:** Record station, oxygen, power, solar, eclipse, manufacturing, and transmission uptime.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 52. Orbital Solar Industry (`orbital_solar_industry`)

- **Prerequisites:** `orbital_operational_data`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** In orbit, place an Environmental Solar Array with clear sky, connect an IC2 load or storage block, and right-click the array once to attribute operation to the tester. Let it generate at least 10,000 EU.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `orbital_tracking_array`.
- **Gameplay assertion:** Run an automated orbital production line from enhanced orbital solar generation.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 53. Advanced Tracking Solar Array (`orbital_tracking_array`)

- **Prerequisites:** `orbital_solar_industry`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** In orbit, repeat the previous test with the Advanced Tracking Solar Array. Right-click it once and let lifetime generation reach at least 10,000 EU.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete; no numbered main-line quest directly depends on it.
- **Gameplay assertion:** Build an advanced tracking array and operate it in orbit, where active tracking can deliver 15–25× its Earth output.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 54. Orbital Research Archive (`orbital_research_complete`)

- **Prerequisites:** `orbital_operational_data`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `industrialcivilizationcore:orbital_research_archive`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `moon_access`, `mars_mission_authorization`, `ai_prerequisite_audit`. This is the declared completion milestone for Chapter 8.
- **Gameplay assertion:** Complete the orbital research program and assemble its results into an Orbital Research Archive.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 55. Moon Access (`moon_access`)

- **Prerequisites:** `orbital_research_complete`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** Produce or spawn the Orbital Research Archive only after its quest unlocks, keep it in inventory for at least one second so authorization records, then use Galacticraft travel to enter the Moon. Confirm travel is denied before the archive and accepted afterward.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `lunar_landing`.
- **Gameplay assertion:** Use the Orbital Research Archive to authorize lunar navigation, descent, and environmental systems.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

**Chapter exit check:** Confirm `orbital_research_complete` is complete, the next numbered chapter is visible and correctly unlocked when applicable, and unrelated later chapters remain locked.

## Chapter 09 — Lunar Settlement

**Chapter purpose:** Establish a permanent Moon base with local extraction, processing, manufacturing, and cargo capability.

**Entry check:** Before completing this chapter's opening quest, confirm the chapter is visible for browsing but its locked quest cannot be opened or progressed. After the previous chapter's completion milestone is satisfied, confirm the opening quest becomes available without a reload.

### 56. Authorized Lunar Landing (`lunar_landing`)

- **Prerequisites:** `moon_access`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** Complete an authorized Moon transfer after Moon Access. Confirm this arrives through Galacticraft travel rather than a dimension command; wait for the advancement task to update.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `lunar_habitat`.
- **Gameplay assertion:** Land on the Moon only after Orbital Research completion.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 57. Lunar Habitat (`lunar_habitat`)

- **Prerequisites:** `lunar_landing`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** On the Moon, build a sealed room, place an Oxygen Detector inside it, and wait for the detector to emit redstone. Remain within 10 blocks until the next five-second sample.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `lunar_power`, `functional_lunar_base`.
- **Gameplay assertion:** Establish pressure, oxygen, thermal control, communications, and safe habitation.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 58. Lunar Power Through Darkness (`lunar_power`)

- **Prerequisites:** `lunar_habitat`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** On the Moon, place an Environmental Solar Array with clear sky, connect storage/load, right-click it for attribution, and accumulate at least 10,000 EU generation.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `lunar_mining`, `functional_lunar_base`.
- **Gameplay assertion:** Provide generation and storage capable of maintaining the base through a lunar darkness cycle.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 59. Automated Lunar Mining (`lunar_mining`)

- **Prerequisites:** `lunar_power`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** While inside the breathable lunar habitat, keep a recognized quarry or automated miner within 32 blocks until the next five-second sample.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `lunar_manufacturing`, `functional_lunar_base`.
- **Gameplay assertion:** Extract lunar resources automatically and deliver them to local processing.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 60. Local Lunar Manufacturing (`lunar_manufacturing`)

- **Prerequisites:** `lunar_mining`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** Inside the lunar habitat, run at least one operation in a nearby Electric Fabricator, Programmable Assembler, or Robotic Manufacturing Cell. The machine must have a completed-operation count above zero.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `lunar_cargo`, `functional_lunar_base`.
- **Gameplay assertion:** Process lunar materials and manufacture a useful spacecraft or precision component locally.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 61. Lunar Cargo Return (`lunar_cargo`)

- **Prerequisites:** `lunar_manufacturing`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `minecraft:minecart`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete; no numbered main-line quest directly depends on it.
- **Gameplay assertion:** Return cargo or establish an automated Earth-orbit-Moon supply route.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 62. Functional Lunar Base (`functional_lunar_base`)

- **Prerequisites:** `lunar_habitat` AND `lunar_power` AND `lunar_mining` AND `lunar_manufacturing`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** Maintain an active Oxygen Detector within 10 blocks plus recognized communications, automated mining, a locally operated manufacturing machine, and power within 32 blocks. Keep all conditions stable for 24 five-second samples—two continuous minutes.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `lunar_science_program`. This is the declared completion milestone for Chapter 9.
- **Gameplay assertion:** Sustain habitation, local power, mining, processing, manufacturing, research readiness, and communications.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

**Chapter exit check:** Confirm `functional_lunar_base` is complete, the next numbered chapter is visible and correctly unlocked when applicable, and unrelated later chapters remain locked.

## Chapter 10 — Lunar Research

**Chapter purpose:** Use lunar science and sustained local industry to unlock Quantum Technology—not Mars directly.

**Entry check:** Before completing this chapter's opening quest, confirm the chapter is visible for browsing but its locked quest cannot be opened or progressed. After the previous chapter's completion milestone is satisfied, confirm the opening quest becomes available without a reload.

### 63. Lunar Science Program (`lunar_science_program`)

- **Prerequisites:** `functional_lunar_base`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** On the Moon, power an Orbital Experiment Module, insert a Blank Data Cartridge, and complete `record_lunar_data` to create Moon-tagged Research Data.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `lunar_darkness_mastery`, `lunar_precision_manufacturing`.
- **Gameplay assertion:** Complete the regolith, metallurgy, radiation, seismic, isotope, habitat, mining, communications, and precision-manufacturing studies available at the lunar base.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 64. Darkness-Cycle Habitat Mastery (`lunar_darkness_mastery`)

- **Prerequisites:** `lunar_science_program`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** On the Moon, right-click an unobstructed Environmental Solar Array to attribute it to the tester, then leave it loaded through 12,000 nighttime ticks while the habitat remains usable. Do not use `/time set day` during the interval.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `lunar_research_complete`.
- **Gameplay assertion:** Maintain habitation and industry through a full lunar darkness challenge.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 65. Lunar Precision Manufacturing (`lunar_precision_manufacturing`)

- **Prerequisites:** `lunar_science_program`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** After Lunar Science, use a lunar Research Station to complete `lunar_archive` with Moon-tagged Research Data plus the Orbital Research Archive. The resulting Lunar Engineering Archive is intentionally manufactured before its retrieval quest unlocks. Then use a powered lunar Robotic Manufacturing Cell to complete `lunar_quantum_component` with one Control Processor, that Lunar Engineering Archive, and one raw Meteoric Iron.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `lunar_research_complete`.
- **Gameplay assertion:** Manufacture a precision component locally and transmit production telemetry.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 66. Lunar Engineering Archive (`lunar_research_complete`)

- **Prerequisites:** `lunar_darkness_mastery` AND `lunar_precision_manufacturing`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `industrialcivilizationcore:lunar_engineering_archive`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `quantum_research_access`, `lunar_quantum_component`, `mars_mission_authorization`, `ai_prerequisite_audit`. This is the declared completion milestone for Chapter 10.
- **Gameplay assertion:** Complete the lunar research program and assemble its results into a Lunar Engineering Archive.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 67. Quantum Research Access (`quantum_research_access`)

- **Prerequisites:** `lunar_research_complete`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `industrialcivilizationcore:lunar_engineering_archive`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `extreme_voltage_industry`.
- **Gameplay assertion:** Use the Lunar Engineering Archive to open Quantum Technology research; Mars remains locked.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

**Chapter exit check:** Confirm `lunar_research_complete` is complete, the next numbered chapter is visible and correctly unlocked when applicable, and unrelated later chapters remain locked.

## Chapter 11 — Quantum Technology

**Chapter purpose:** Combine lunar manufacturing with high-tier IC2 power and programmable production for Mars readiness.

**Entry check:** Before completing this chapter's opening quest, confirm the chapter is visible for browsing but its locked quest cannot be opened or progressed. After the previous chapter's completion milestone is satisfied, confirm the opening quest becomes available without a reload.

### 68. Extreme-Voltage Industry (`extreme_voltage_industry`)

- **Prerequisites:** `quantum_research_access` AND `monitored_nuclear_power`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `ic2:blockmachinehv:*`, `ic2:blockelectric:*`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `robotic_manufacturing_cell`, `nanosuit_and_tools`, `mfsu_bank_baseline`.
- **Gameplay assertion:** Operate high-tier generation, storage, transformation, and advanced processing safely.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 69. Robotic Manufacturing Cell (`robotic_manufacturing_cell`)

- **Prerequisites:** `extreme_voltage_industry`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `industrialcivilizationcore:robotic_manufacturing_cell`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `lunar_quantum_component`, `mars_readiness_trial`.
- **Gameplay assertion:** Power a Robotic Manufacturing Cell and complete one local automated production cycle.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 70. Lunar Quantum Component (`lunar_quantum_component`)

- **Prerequisites:** `robotic_manufacturing_cell` AND `lunar_research_complete`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `industrialcivilizationcore:lunar_quantum_component`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `quantumsuit`.
- **Gameplay assertion:** Manufacture a Lunar Quantum Component using the precision industry established on the Moon.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 71. NanoSuit and Powered Tools (`nanosuit_and_tools`)

- **Prerequisites:** `extreme_voltage_industry`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `ic2:itemarmornanohelmet:*`, `ic2:itemarmornanochestplate:*`, `ic2:itemarmornanolegs:*`, `ic2:itemarmornanoboots:*`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `quantumsuit`.
- **Gameplay assertion:** Manufacture NanoSuit equipment and advanced charged tools.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 72. Complete Charged QuantumSuit (`quantumsuit`)

- **Prerequisites:** `lunar_quantum_component` AND `nanosuit_and_tools`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `ic2:itemarmorquantumhelmet:*`, `ic2:itemarmorquantumchestplate:*`, `ic2:itemarmorquantumlegs:*`, `ic2:itemarmorquantumboots:*`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `mars_readiness_trial`.
- **Gameplay assertion:** Manufacture a complete QuantumSuit and demonstrate sufficient infrastructure to charge it.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 73. Mars Readiness Trial (`mars_readiness_trial`)

- **Prerequisites:** `quantumsuit` AND `robotic_manufacturing_cell`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `ic2:itemarmorquantumhelmet:*`, `industrialcivilizationcore:lunar_quantum_component`, `industrialcivilizationcore:mars_mission_authorization`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `quantum_technology_complete`.
- **Gameplay assertion:** Demonstrate thermal, radiation, power, life-support compatibility, and automated spare-part production.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 74. Quantum Technology Complete (`quantum_technology_complete`)

- **Prerequisites:** `mars_readiness_trial`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `industrialcivilizationcore:lunar_quantum_component`, `ic2:itemarmorquantumhelmet:*`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `mars_mission_authorization`, `ai_prerequisite_audit`. This is the declared completion milestone for Chapter 11.
- **Gameplay assertion:** Master conventional high-tier technology and Mars-ready automated manufacturing.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 75. Single-MFSU Baseline (`mfsu_bank_baseline`)

- **Prerequisites:** `extreme_voltage_industry`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** Connect one real MFSU to a tier-3-compatible first-party manufacturing machine, begin a genuine recipe, and complete it while the machine records at least one 512-EU packet in an operation tick.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `mfsu_bank_quad`.
- **Gameplay assertion:** Connect a real MFSU to a tier-3-compatible Industrial Civilization manufacturing machine. Start a genuine recipe and complete it under live IC2 EnergyNet power. Run the operation with at least one 512-EU packet reaching the machine.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 76. Four-MFSU Parallel Bank (`mfsu_bank_quad`)

- **Prerequisites:** `mfsu_bank_baseline`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** Repeat genuine manufacturing with four independently oriented MFSUs. Verify at least four MFSU-class packets and approximately 2,048 EU/t are accepted in one tick without false overvoltage.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `mfsu_bank_ten`.
- **Gameplay assertion:** Build four independently connected and correctly oriented MFSUs. Complete a real recipe with a peak of at least four 512-EU packets in one tick. Complete the operation at higher aggregate throughput without exceeding the voltage of any individual packet.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 77. Ten-MFSU Burst Bank (`mfsu_bank_ten`)

- **Prerequisites:** `mfsu_bank_quad`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** Scale the physical bank to ten independently connected MFSUs and complete a genuine recipe. Verify a peak of ten packets and approximately 5,120 EU/t.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `mfsu_bank_fifty`.
- **Gameplay assertion:** Scale the parallel bank to ten real MFSUs without combining them into one illegal packet. Complete a real recipe with a peak of at least ten 512-EU packets in one tick. Observe the expected throughput increase through the machine GUI or ComputerCraft telemetry.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 78. Fifty-MFSU Power Bank (`mfsu_bank_fifty`)

- **Prerequisites:** `mfsu_bank_ten`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** Scale the physical bank to fifty independently connected MFSUs and complete a genuine recipe. Verify a peak of fifty packets and approximately 25,600 EU/t.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `blink_manufacturing`.
- **Gameplay assertion:** Charge, orient, and connect fifty real MFSUs to a tier-3-compatible machine. Complete a real recipe with a peak of at least fifty 512-EU packets in one tick. Use the fifty-unit bank to accelerate real work while every individual packet remains within the machine's voltage limit.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 79. Blink Manufacturing (`blink_manufacturing`)

- **Prerequisites:** `mfsu_bank_fifty`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** Use the fifty-MFSU bank on a Robotic Manufacturing Cell or another energy-limited first-party machine. Complete one genuine recipe in eight active ticks or fewer while the same operation records a peak of at least fifty MFSU-class packets.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete; no numbered main-line quest directly depends on it.
- **Gameplay assertion:** Use a Robotic Manufacturing Cell or another compatible energy-limited first-party machine. Deliver at least fifty MFSU-class packets in one operation tick. Complete the genuine recipe in no more than eight active ticks; precharging or possessing the output does not count.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 80. Mars Mission Authorization (`mars_mission_authorization`)

- **Prerequisites:** `quantum_technology_complete` AND `orbital_research_complete` AND `lunar_research_complete`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `industrialcivilizationcore:mars_mission_authorization`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `tier2_mars_launch`.
- **Gameplay assertion:** Combine the required Quantum, lunar, and readiness work into a Mars Mission Authorization.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

**Chapter exit check:** Confirm `quantum_technology_complete` is complete, the next numbered chapter is visible and correctly unlocked when applicable, and unrelated later chapters remain locked.

## Chapter 12 — Mars Settlement

**Chapter purpose:** Establish a distant semi-autonomous industrial colony and recover the real Desh sample used by Lite Matter Engineering.

**Entry check:** Before completing this chapter's opening quest, confirm the chapter is visible for browsing but its locked quest cannot be opened or progressed. After the previous chapter's completion milestone is satisfied, confirm the opening quest becomes available without a reload.

### 81. Authorized Tier 2 Mars Launch (`tier2_mars_launch`)

- **Prerequisites:** `mars_mission_authorization`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** Hold both the Lunar Quantum Component and Mars Mission Authorization long enough for authorization to record, unlock the Tier 2 schematic page, build/fuel a Tier 2 rocket, and travel to Mars. Confirm Mars rejects entry before either authorization item is recorded.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `martian_habitat`.
- **Gameplay assertion:** Build a Tier 2 spacecraft and travel to Mars with mission authorization.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 82. Martian Habitat (`martian_habitat`)

- **Prerequisites:** `tier2_mars_launch`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** On Mars, build a sealed room, place an Oxygen Detector inside it, and wait for the detector to emit redstone. Remain within 10 blocks until the next five-second sample.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `martian_power`, `functional_martian_base`.
- **Gameplay assertion:** Establish pressure, oxygen, water, radiation protection, communications, and safe habitation.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 83. Reliable Martian Power (`martian_power`)

- **Prerequisites:** `martian_habitat`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** On Mars, place an Environmental Solar Array with clear sky, connect storage/load, right-click it for attribution, and accumulate at least 10,000 EU despite the deterministic dust derating.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `martian_mining`, `functional_martian_base`.
- **Gameplay assertion:** Maintain local generation and storage through dust-related solar fluctuation; nuclear backup is encouraged.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 84. Automated Martian Mining (`martian_mining`)

- **Prerequisites:** `martian_power`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** While inside the breathable Martian habitat, keep a recognized quarry or automated miner within 32 blocks until the next five-second sample.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `martian_manufacturing`, `mars_sample`, `functional_martian_base`.
- **Gameplay assertion:** Mine and route Martian resources without direct labor.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 85. Local Martian Manufacturing (`martian_manufacturing`)

- **Prerequisites:** `martian_mining`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** Inside the Martian habitat, run at least one operation in a nearby Electric Fabricator, Programmable Assembler, or Robotic Manufacturing Cell.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `martian_cargo`, `functional_martian_base`.
- **Gameplay assertion:** Process Martian materials and manufacture colony components locally.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 86. Martian Desh Sample (`mars_sample`)

- **Prerequisites:** `martian_mining`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `galacticraftplanets:item_basic_mars:2`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `functional_martian_base`, `molecular_analyzer`, `material_pattern_record`.
- **Gameplay assertion:** Obtain Galacticraft Desh (metadata 2), the existing real Mars Sample used by the Molecular Analyzer.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 87. Martian Cargo and Return (`martian_cargo`)

- **Prerequisites:** `martian_manufacturing`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `minecraft:minecart`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete; no numbered main-line quest directly depends on it.
- **Gameplay assertion:** Establish a return or cargo route without relying on repeated emergency Earth supply trips.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 88. Functional Martian Base (`functional_martian_base`)

- **Prerequisites:** `martian_habitat` AND `martian_power` AND `martian_mining` AND `martian_manufacturing` AND `mars_sample`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** Keep Galacticraft Desh metadata 2 in inventory and maintain an active Oxygen Detector within 10 blocks plus communications, automated mining, local manufacturing, and power within 32 blocks for 24 five-second samples—two continuous minutes.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `martian_science_program`. This is the declared completion milestone for Chapter 12.
- **Gameplay assertion:** Sustain habitat, water, power, communications, automated mining, local manufacturing, research readiness, and return capability.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

**Chapter exit check:** Confirm `functional_martian_base` is complete, the next numbered chapter is visible and correctly unlocked when applicable, and unrelated later chapters remain locked.

## Chapter 13 — Martian Autonomy Research

**Chapter purpose:** Prove autonomous off-world industry and expose the limits of conventional programmable control.

**Entry check:** Before completing this chapter's opening quest, confirm the chapter is visible for browsing but its locked quest cannot be opened or progressed. After the previous chapter's completion milestone is satisfied, confirm the opening quest becomes available without a reload.

### 89. Martian Science Program (`martian_science_program`)

- **Prerequisites:** `functional_martian_base`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** On Mars, power an Orbital Experiment Module, insert a Blank Data Cartridge, and complete `record_martian_data` to create Mars-tagged Research Data.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `autonomous_resource_response`, `autonomous_power_response`.
- **Gameplay assertion:** Complete the atmospheric, subsurface, radiation-computation, water-recovery, semiconductor, and robotics studies available on Mars.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 90. Autonomous Resource Response (`autonomous_resource_response`)

- **Prerequisites:** `martian_science_program`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** On Mars, power a Research Station and complete `martian_autonomy` using Mars-tagged Research Data and one Control Processor. This implemented operation grants the resource-response evidence.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `unattended_martian_production`.
- **Gameplay assertion:** Detect missing materials and automatically replenish or reschedule production.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 91. Autonomous Power Response (`autonomous_power_response`)

- **Prerequisites:** `martian_science_program`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** The same completed `martian_autonomy` operation currently grants the power-response evidence. During the mechanics check, interrupt and restore power and confirm the installation recovers safely.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `unattended_martian_production`.
- **Gameplay assertion:** Detect power interruption, shed load safely, and restore production automatically.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 92. Unattended Martian Production (`unattended_martian_production`)

- **Prerequisites:** `autonomous_resource_response` AND `autonomous_power_response`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** The same completed `martian_autonomy` operation grants unattended-production evidence after both response quests are satisfied. Confirm the output is a Martian Autonomy Archive.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `martian_autonomy_complete`.
- **Gameplay assertion:** Complete local production without direct crafting while habitat and telemetry remain stable.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 93. Martian Autonomy Archive (`martian_autonomy_complete`)

- **Prerequisites:** `unattended_martian_production`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `industrialcivilizationcore:martian_autonomy_archive`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `molecular_analyzer`, `ai_prerequisite_audit`. This is the declared completion milestone for Chapter 13.
- **Gameplay assertion:** Complete the Martian autonomy program and assemble its results into a Martian Autonomy Archive.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

**Chapter exit check:** Confirm `martian_autonomy_complete` is complete, the next numbered chapter is visible and correctly unlocked when applicable, and unrelated later chapters remain locked.

## Chapter 14 — Lite Matter Engineering

**Chapter purpose:** Use the real Molecular Analyzer and Martian Desh sample to record matter without enabling replication.

**Entry check:** Before completing this chapter's opening quest, confirm the chapter is visible for browsing but its locked quest cannot be opened or progressed. After the previous chapter's completion milestone is satisfied, confirm the opening quest becomes available without a reload.

### 94. Molecular Analyzer (`molecular_analyzer`)

- **Prerequisites:** `mars_sample` AND `martian_autonomy_complete`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `industrialcivilizationcore:molecular_analyzer`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `analyzer_power`.
- **Gameplay assertion:** Craft the existing Molecular Analyzer from meteorite iron, an advanced computer, steel, an IC2 MV machine block, advanced circuits, and Desh.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 95. Power the Analyzer (`analyzer_power`)

- **Prerequisites:** `molecular_analyzer`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** Place and supply the Molecular Analyzer with at least 6,250 EU. Insert one supported sample and complete an analysis; the first successful analysis grants this quest.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `material_pattern_record`.
- **Gameplay assertion:** Supply the Analyzer with at least 6,250 EU for a Desh analysis.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 96. Martian Material Pattern Record (`material_pattern_record`)

- **Prerequisites:** `analyzer_power` AND `mars_sample`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `industrialcivilizationcore:material_pattern_record`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `comparative_molecular_analysis`.
- **Gameplay assertion:** Insert Galacticraft Desh metadata 2 and complete the 6,250 EU analysis to produce a non-consumable pattern record.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 97. Comparative Molecular Characterization (`comparative_molecular_analysis`)

- **Prerequisites:** `material_pattern_record`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** Complete three successful Analyzer runs using an Earth Iron Ingot, lunar raw Meteoric Iron, and Galacticraft Desh metadata 2. Verify each resulting Material Pattern Record carries the correct origin tag.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `advanced_material_recovery`, `lite_matter_complete`.
- **Gameplay assertion:** Record comparable material observations from Earth, the Moon, and Mars without attempting arbitrary transmutation.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 98. Advanced Material Recovery (`advanced_material_recovery`)

- **Prerequisites:** `comparative_molecular_analysis`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `ic2:blockmachinelv:*`, `industrialcivilizationcore:material_pattern_record`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete; no numbered main-line quest directly depends on it.
- **Gameplay assertion:** Demonstrate efficient recycling or high-purity recovery using existing industrial machines.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 99. Lite Matter Engineering Complete (`lite_matter_complete`)

- **Prerequisites:** `comparative_molecular_analysis`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** The third distinct Earth/Moon/Mars Analyzer origin grants this completion together with comparative analysis. Confirm spawned pattern records alone do not grant it.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `ai_prerequisite_audit`. This is the declared completion milestone for Chapter 14.
- **Gameplay assertion:** Demonstrate molecular analysis, durable pattern records, and advanced recovery without UU-Matter or replication.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

**Chapter exit check:** Confirm `lite_matter_complete` is complete, the next numbered chapter is visible and correctly unlocked when applicable, and unrelated later chapters remain locked.

## Chapter 15 — AI Age

**Chapter purpose:** Enter—not finish—the endgame by creating an industrial AI core from all major research eras.

**Entry check:** Before completing this chapter's opening quest, confirm the chapter is visible for browsing but its locked quest cannot be opened or progressed. After the previous chapter's completion milestone is satisfied, confirm the opening quest becomes available without a reload.

### 100. Civilization Capability Audit (`ai_prerequisite_audit`)

- **Prerequisites:** `orbital_research_complete` AND `lunar_research_complete` AND `quantum_technology_complete` AND `martian_autonomy_complete` AND `lite_matter_complete`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `industrialcivilizationcore:orbital_research_archive`, `industrialcivilizationcore:lunar_engineering_archive`, `industrialcivilizationcore:martian_autonomy_archive`, `industrialcivilizationcore:material_pattern_record`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `artificial_industrial_intelligence_core`.
- **Gameplay assertion:** Bring together the Orbital and Lunar archives with the achievements of Quantum Technology, Martian Autonomy, and Lite Matter Engineering.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 101. Artificial Industrial Intelligence Core (`artificial_industrial_intelligence_core`)

- **Prerequisites:** `ai_prerequisite_audit`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `industrialcivilizationcore:artificial_industrial_intelligence_core`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `ai_age_entry`.
- **Gameplay assertion:** Synthesize the accumulated research archives and advanced components into an Artificial Industrial Intelligence Core.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 102. Enter the AI Age (`ai_age_entry`)

- **Prerequisites:** `artificial_industrial_intelligence_core`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** After the prerequisite audit, obtain the AI Core and keep it in inventory for at least one second. The player must already have the Lite Matter completion record and the canonical Martian Autonomy Archive record. Confirm the scrolling credits appear once and return to the playable world.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `technical_phase_pearl`, `ae2_entry`. This is the declared completion milestone for Chapter 15.
- **Gameplay assertion:** Enter the continuously expanding endgame and authorize AI-coordinated digital industry.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 103. Technical Phase Pearl (`technical_phase_pearl`)

- **Prerequisites:** `ai_age_entry`.
- **Detection mode:** Runtime advancement.
- **Exact test action:** After AI Age Entry, craft the Technical Phase Pearl through its real recipe with the durable AI Core catalyst. Spawning an Ender Pearl must not complete this quest; a real crafting event must.
- **Negative assertion:** Before the final triggering event, confirm that merely holding or spawning the quest icon does not complete the task.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete; no numbered main-line quest directly depends on it.
- **Gameplay assertion:** Manufacture a Technical Phase Pearl only after the AI Age has unlocked.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 104. Applied Energistics Entry (`ae2_entry`)

- **Prerequisites:** `ai_age_entry`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `appliedenergistics2:controller`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `ae2_autocrafting`.
- **Gameplay assertion:** Use the real AI Core as a durable authorization catalyst and bring an AE2 Controller online.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

**Chapter exit check:** Confirm `ai_age_entry` is complete, the next numbered chapter is visible and correctly unlocked when applicable, and unrelated later chapters remain locked.

## Chapter 16 — Continuously Expanding Post-AI Civilization

**Chapter purpose:** Branch into scale, matter engineering, megastructures, autonomy, and new colonies without blocking AI entry.

**Entry check:** Before completing this chapter's opening quest, confirm the chapter is visible for browsing but its locked quest cannot be opened or progressed. After the previous chapter's completion milestone is satisfied, confirm the opening quest becomes available without a reload.

### 105. AE2 Network Crafting (`ae2_autocrafting`)

- **Prerequisites:** `ae2_entry`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `appliedenergistics2:crafting_unit`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `ai_factory_coordination`.
- **Gameplay assertion:** Encode and execute an ordinary multi-step crafting request while physical machine processes retain real throughput.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 106. AI Factory Coordination (`ai_factory_coordination`)

- **Prerequisites:** `ae2_autocrafting`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `industrialcivilizationcore:interplanetary_cargo_controller`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `cross_planetary_logistics`, `predictive_production`, `uu_matter_research`.
- **Gameplay assertion:** Coordinate inventory-aware production across multiple physical machine lines.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 107. Cross-Planetary AI Logistics (`cross_planetary_logistics`)

- **Prerequisites:** `ai_factory_coordination`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `industrialcivilizationcore:interplanetary_cargo_network_key`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `orbital_megastructures`, `autonomous_colony_expansion`.
- **Gameplay assertion:** Coordinate automated orbital cargo, autonomous lunar industry, and autonomous Martian industry.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 108. Predictive Production (`predictive_production`)

- **Prerequisites:** `ai_factory_coordination`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `industrialcivilizationcore:artificial_industrial_intelligence_core`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `autonomous_colony_expansion`.
- **Gameplay assertion:** Maintain demand forecasts, resilient buffers, and automatic shortage response.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 109. UU-Matter Research (`uu_matter_research`)

- **Prerequisites:** `ai_factory_coordination`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `industrialcivilizationcore:matter_replicator`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `uu_matter_production`, `fusion_and_antimatter`.
- **Gameplay assertion:** Complete the AI-assisted research program that establishes a controlled energy-to-matter process.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 110. Controlled UU-Matter Production (`uu_matter_production`)

- **Prerequisites:** `uu_matter_research`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `industrialcivilizationcore:uu_matter_capsule`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `controlled_replication`.
- **Gameplay assertion:** Produce UU-Matter through a high-energy, time-based physical process.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 111. Controlled Matter Replication (`controlled_replication`)

- **Prerequisites:** `uu_matter_production`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `industrialcivilizationcore:controlled_replication_record`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `civilization_scale_ai`, `continuous_civilization`.
- **Gameplay assertion:** Replicate an authorized pattern with material, energy, and throughput constraints.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 112. Fusion and Antimatter (`fusion_and_antimatter`)

- **Prerequisites:** `uu_matter_research`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `industrialcivilizationcore:contained_antimatter_capsule`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete; no numbered main-line quest directly depends on it.
- **Gameplay assertion:** Develop fusion power, exotic materials, and contained antimatter research.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 113. Orbital Megastructures (`orbital_megastructures`)

- **Prerequisites:** `cross_planetary_logistics`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `industrialcivilizationcore:megastructure_control_record`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `civilization_scale_ai`.
- **Gameplay assertion:** Build advanced orbital solar arrays, automated shipyards, and large interplanetary vessels.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 114. Autonomous Colony Expansion (`autonomous_colony_expansion`)

- **Prerequisites:** `cross_planetary_logistics` AND `predictive_production`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `industrialcivilizationcore:autonomous_colony_charter`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete. These direct main-line successors become eligible after their other prerequisites are satisfied: `civilization_scale_ai`.
- **Gameplay assertion:** Establish a new colony with self-repairing factories and connected logistics.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 115. Civilization-Scale AI Core (`civilization_scale_ai`)

- **Prerequisites:** `controlled_replication` AND `orbital_megastructures` AND `autonomous_colony_expansion`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `industrialcivilizationcore:civilization_scale_ai_core`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete; no numbered main-line quest directly depends on it.
- **Gameplay assertion:** Coordinate a planetary-scale manufacturing and research network.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

### 116. Continuously Expanding Civilization (`continuous_civilization`)

- **Prerequisites:** `controlled_replication`.
- **Detection mode:** Non-consuming inventory retrieval.
- **Exact test action:** Before this quest unlocks, keep its matching evidence out of the player inventory. After it unlocks, obtain or Creative-give all of the following at the same time: `industrialcivilizationcore:civilization_scale_ai_core`. Wait at least two seconds for Better Questing inventory detection; the items must remain unconsumed.
- **Negative assertion:** Confirm the quest did not gain progress while locked and that removing/re-adding the evidence works without a manual checkbox or claim button.
- **Expected quest result:** The quest completes in F6 and its matching vanilla advancement is complete; no numbered main-line quest directly depends on it. This is the declared completion milestone for Chapter 16.
- **Gameplay assertion:** Continue the Explore → Research → Automate → Scale → Colonize → Connect → Build loop; optional branches remain open-ended.
- **Record:** Mark pass/fail, note completion time, and capture F6 plus Advancements screenshots for any mismatch.

**Chapter exit check:** Confirm `continuous_civilization` is complete, the next numbered chapter is visible and correctly unlocked when applicable, and unrelated later chapters remain locked.

## End-of-run acceptance

1. Confirm all 116 numbered-chapter quests are complete in F6 and exactly their matching main-line advancements are complete.
2. Confirm unfinished independent side-path tabs do not prevent completion of the numbered route.
3. Confirm AI entry displayed the Industrial Civilization credits containing `corysmart` exactly once and returned control to the same playable world.
4. Confirm Chapter 16 completion does not show a victory/end screen or disable continued play.
5. Save and quit, reload the same world, and verify all quest, advancement, authorization, artifact, machine-operation, and AI-credit state persists.
6. Inspect `logs/latest.log`, `logs/groovy.log`, and `crafttweaker.log`. Record every error with the current quest ID and attach screenshots plus `/ic_status` output where relevant.

## Failure record template

```text
Quest title / ID:
Chapter:
Expected prerequisite state:
Evidence or runtime action performed:
F6 result:
Advancement result:
/ic_status result:
Unexpected unlocks or early progress:
Reload persistence result:
Relevant log lines:
Screenshot filenames:
```
