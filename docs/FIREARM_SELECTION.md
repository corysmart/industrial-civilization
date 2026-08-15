# Firearm Selection

Techguns `1.12.2-2.0.2.0_pre3.2` is the sole primary firearm framework.

It was selected over overlapping alternatives because the pinned 1.12.2 build combines projectile firearms, magazines, ammunition, reload behavior, optics/attachments, grenades, launchers, turrets, broad weapon classes, industrial machines, NPC damage hooks, and CraftTweaker-visible item registrations in one mod. It has a large but self-contained asset footprint and no required magic progression.

Risks are explicit: the upstream build is labeled beta and uses an FML core plugin. Its world structures are disabled, duplicate copper/tin/lead/uranium generation and duplicate common ingots are disabled, unsafe firing mode is OP-restricted, machines require power, and only human bandit natural spawning remains enabled. These choices protect the established Tekkit ore and world-generation balance.

Techguns is All Rights Reserved and its upstream license forbids reuploading the mod. The Astra release archive therefore omits the Techguns JAR and uses ModDirector to retrieve CurseForge project `244201`, file `2958103`, from the author-hosted distribution path on first launch. The downloaded file must match SHA-256 `154d3d794cfd74252f2cec979a6e72f5187bb9c21897ed4b42f45771a0e558f7`.
