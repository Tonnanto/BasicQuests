

# BasicQuests Quest Generation

### _DISCLAIMER_: Only tweak the generation files if you know your way around yaml files.  Messing up the format (indentation, hyphens, etc..) can break the plugin.

This file explains how to use the generation files within this folder.
It allows you to tweak and adjust the generation of new quests within the plugin so it meets your preferences.
On the one hand side there are a lot of possibilities to fine tune the generation.
On the other hand side there are a few thing to keep in mind when doing so.

#### What you should keep in mind:
- You can always delete a generation file and reload or restart the server to have the default file regenerated.
- Every generation file has a quick guide on what can be changed within this file.
- Changing the generation files WILL change the experience the plugin provides for players. Don't change too much if you players have a great experience.
- The default settings are decent for most people.
- Unrealistic settings may break the plugin.
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
- When generating new quests a quest type is chosen at first. After that one of the available options is chosen for that quest type
- `quest_types.yml` is used in every generation to choose a quest type. This file gives the broadest control over quest generation.
- Each quest has a hidden value which represents its difficulty. The reward is being generated based on this value.
    - increasing the `value` of a quest will lead to higher rewards for this quest.
    - increasing the `value` of a specific item reward will lead to smaller rewards with this item.
- All generations work based on weights. The plugin chooses between a list of `options` based on their `weight`
- `weight` always represents the probability of this option being chosen in comparison to all the other weights in the same list.
    - to make a quest less likely reduce the `weight`. To make it more likely increase the `weight`.
    - Setting the `weight` to 0 prevents this option from being generated.
    - same for item rewards
- Disable quests, quest types or rewards by commenting out an entire section (putting a # in front of the row)