

# BasicQuests Quest Generation

### _DISCLAIMER_: Only tweak the generation files if you know your way around yaml files.  Messing up the format (indentation, hyphens, etc..) can break the plugin.

This file explains how to use the generation files within this folder.
It allows you to tweak and adjust the generation of new quests within the plugin, so it meets your preferences.
There are a lot of possibilities to fine tune the generation.
BUT you need to keep a few things in mind when doing so:

#### What you should keep in mind:
- You can always delete a generation file and reload or restart the server to have the default file regenerated.
- Every generation file has a quick guide on what can be changed within this file.
- Changing the generation files WILL change the experience for players. Don't change too much if your players are having a great experience.
- The default settings are decent for most people.
- Unrealistic settings may break the plugin:
    - Don't set the weight of every option in a list to 0
    - Don't have duplicate options within a list of options
    - Don't use negative values!

#### What you CAN NOT do:
- Add new quest types
- Add new quests within a quest type

#### What you CAN do
- Disable specific quests or entire quest types
- Change the reward value of specific quests or entire quest types
- Change the probability of newly generated quests being of a specific quest of certain quest type
- Disable certain items in reward generation
- Change the value of specific item rewards

## General Hints
- The files in this folder `/quest_generation` are responsible for the generation of new quests.
- The files in the folder `/quest_generation/item_reward_generation` are responsible for the generation of new item rewards for quests.
- When generating new quests, a quest type is chosen first. After that, one of the available options within that quest type is chosen.
- `quest_types.yml` is used in every generation to choose a quest type. This file gives the broadest control over quest generation.
- Each quest has a hidden value which represents its difficulty. The reward is being generated based on this value.
    - increasing the `value` of a quest will lead to higher rewards for this quest.
    - increasing the `value` of a specific item reward will lead to smaller rewards with this item.
- All generations work based on weights. The plugin chooses between a list of `options` based on their `weight`
- `weight` always represents the probability of an option being chosen in comparison to all the other weights in the same list.
    - to make an option less likely reduce the `weight`. To make it more likely increase the `weight`.
    - Setting the `weight` to 0 prevents this option from being generated.
- Disable options by commenting out an entire section (putting a # in front of the row).

## Example Generation
1. The plugin chooses a quest type (from quest_types.yml)  
-> Mine Block (value = 1)
   
2. The plugin chooses one of the available options for mine block quests (from mine_block.yml)  
-> Iron Ore (value_base = 20, value_per_unit = 4)
   
3. The plugin chooses an amount for the quest (min = 32, max = 160)  
-> 64
   
- The Quest is: "Mine 64 Iron Ore"
- The total value is: 1 * (20 + (4 * 64)) = 276

- An item reward with the value 276 will be generated (if item rewards are enables in the config.yml)
- Of all the items within every file in the directory (item_reward_generation), one or more are chosen with a random amount until the total reward value is greater than the quest value.