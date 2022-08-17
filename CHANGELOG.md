# Changelog
All notable changes to this project will be documented in this file.

---
## [0.4] - Upcoming

### Added
- New Reload Command
- Localized Quests

### Changed
- All messages where completely overhauled (Credits to Log1x)
- Messages can now be fully customized within the plugin folder (Credits to Log1x)
- Commands redesign
- Permission redesign
- Improved Scoreboard (Can now display Quests with rewards)

### Fixed
- Fixed bug where `quantity-factor` `reward-factor` and `money-factor` would not get read correctly from config.
- Fixed bug where the `max-factor` could be exceeded which lead to huge quantities and rewards in quests.


---
## [0.3.2] - 2022-08-03

### Added
- PlaceholderAPI Support
- Partial Spanish Localization (Credits to paperrain)
- Partial Russian Localization (Credits to Lemurzin4ik)

---
## [0.3.1] - 2022-07-26

### Fixed
- Fixed bug where harvest block quests would not progress
- Fixed issue where find structure quests could cause freezes

### Changed
- The showing of the scoreboard now persists if a player logs out
- The scoreboard can now be disabled completely in the config


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
