# Changelog
All notable changes to this project will be documented in this file.

---
## [0.5] - 2024-02-23

### Added
- New Quest Type: Fish Item Quest
- New Quest Type: Increase Stat Quest (Running, Swimming, Horse Riding, and other fun stats)
- New Leaderboard:
  - New Command: `/quests leaderboard`
  - Each quest is assigned a number of stars based on its difficulty
  - Collect stars by completing quests and compete on the leaderboard
  - _Disclaimer: Upgrading might reset the existing leaderboard!_
- New heads-up message to players who have not used their skips yet 30 minutes before they reset.

### Changed
- Compatibility with Minecraft 1.20
  - Cherry Logs now progress 'Chop Wood' quests
  - New items like smithing templates can now appear in rewards
- List Quests Command: Admins can now use this command to list other players' quests `/quests list <player>`
- Server logs on quest completion

### Fixed
- Fixed bug where quests would not load when a player joins on version 1.20.4
- Fixed migration of config files
- Fixed bug where leaderboard placeholder were displayed in the wrong order
- Fixed bug where "skips have been reset" broadcast message was sent multiple times


---
## [0.4.1] - 2023-03-28

### Added
- Placeholder `%bquests_top_1%` to display a scoreboard of players with most completed quests
- Config option to change the save interval
- Config option to prevent quest progress in certain worlds

### Fixed
- Reduced unnecessary log messages


---
## [0.4] - 2022-08-18

### Added
- Reload Command
- Fully Localized Quests
- Placeholder `%bquests_completed%` to show total number of completed quests

### Changed
- All messages where completely overhauled (Credits to Log1x)
- Messages can now be fully customized in the `custom_messages.yml` file (Credits to Log1x)
- Commands redesign
- Permission redesign
- Improved Scoreboard (Can now display Quests with rewards)

### Fixed
- Fixed bug where `quantity-factor` `reward-factor` and `money-factor` would not get read correctly from config.
- Fixed bug where the `max-factor` could be exceeded which lead to huge quantities and rewards in quests.
- Fixed bug where quests would progress even though another plugin canceled the event (like GriefPrevention).


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
- Quest generation files in the plugin folder allow fine-tuning of Quest and Item-Reward generation
- Config option to limit progress notifications (requested by community)
- New sound on quest completion
- Partial translation for german (more languages can be supported in the future)

### Changed
- Enchantment in anvil now also progresses Enchantment Quests

### Fixed
- Deepslate ores now trigger Mine Block Quests
- Only 1.18: Removed structures from quests that could not be located by the plugin
- Remaining Item Rewards no longer disappear when closing the Reward Inventory. They now drop on the ground.
