# Changelog
All notable changes to this project will be documented in this file.

---
## [0.3] - 2022-07-19
### Added
- New Quest Type: Trade with Villager
- New items are now considered in Quest generation (1.17, 1.18, 1.19)
- Quest generation files in plugin folder allow fine-tuning of Quest and Item-Reward generation
- Config option to limit progress notifications (requested by community)
- New sound on quest completion
- Partial translation for german (more languages can be supported in the future)

### Changed
- Enchantment in anvil now also progresses Enchantment Quests

### Fixed
- Deepslate ores now trigger Mine Block Quests
- Only 1.18: Removed structures from quests that could not be located by the plugin
- Remaining Item Rewards no longer disappear when closing the Reward Inventory. They now drop on the ground.