# BasicQuests
A plugin for Spigot servers that implements randomly generated basic quests with rewards for players.


## General
Author: Tonnanto  
Current Version: 0.3

A **<ins>spigot</ins>** compatible Server is required to run this plugin!

Download the plugin and find the Project Page on [SpigotMC](https://www.spigotmc.org/resources/basicquests.87972/) and [Bukkit](https://dev.bukkit.org/projects/basicquests).


## How it works
Every player is given a set amount of quests initially (default: 3).
You can view your active quests by using `/quests` or `/quests detail`.
When a player completes a quest the reward can be received by clicking the **"Collect Reward"** button in the chat or by using `/getreward`.
As soon as a quests reward is collected it disappears from the list of quests, and a newly generated quest will be added to the list.  
By default, a player is allowed to skip one quest every 24h by using `/skipquest`.
Some quests require the player to complete an advancement before they can be generated. Quests in the nether for example require the player to have completed the ***"Diamonds!"*** advancement.  
Also, newly generated quests will increase in their quantities proportional to the players' playtime on the server: While a new player might receive a quest like ***"Mine 32 Iron Ore"*** a player with lots of playtime on the server would rather receive a quest like this ***"Mine 512 Iron Ore"***.
This feature can be precisely adjusted in the `config.yml` or be turned off entirely.  
Some quests are incredibly rare but promise very high rewards once completed.


## Quests
Quests are randomly generated in Basic Quests.
Currently available quest types along with some examples are listed below:

* Mine Block
* Kill Entity
* Harvest Block
* Chop Wood
* Enchant Item
* Find Structure
* Trade with Villager (new)
* Gain Level
* Reach Level
* Break Block

Quest ideas I am thinking about implementing in the future:

* Fish Item
* Breed Animal
* Tame Animal
* Smelt Item
* Brew Potion


## Rewards
Possible Rewards are either ***Items***, ***Money*** or ***XP***. You can enable or disable each of these reward types in the `config.yml`.
By default, only item-rewards are enabled. In order to use money-rewards you need to have an ***economy plugin*** connected via ***Vault***.
If multiple reward types are enabled one will be chosen at random when a new quest is generated - at least one reward type must be enabled or BasicQuests will not work.  
The value of a reward is proportional to the value of the quest and is multiplied by the `reward-factor` which can also be adjusted in the `config.yml`.
The value of a given quest is determined by a number of factors along its generation process.  
While money and xp-rewards are self-explanatory I'll list some examples for item-rewards below:

* Tools (Iron - Netherite) (Maybe enchanted)
* Armor (Chainmail - Netherite) (Maybe enchanted)
* Enchanted Books
* Potions (Only positive effects - extended (+) and upgraded (II) variants)
* Food
* Resources (Most of the valuable things you can find underground - From Flint to Netherite)
* Rare Items (Enchanted Golden Apple, Music Disks, Saddle, ...)


## Quest Generation Customization
BasicQuest allows admins to fine tune the quest generation on their servers using a bunch of yaml files in the `quest_generation` directory.  
This allows for:
- adjusting or removing the probability of quest types
- adjusting or removing the probability specific quests within a quest type
- making certain quests more or less valuable. This will be reflected in the value of the reward.
- adjusting the amounts that appear in quests.

For further information about how to fine tune the generation process check out the readme file at `plugins/BasicQuests/quest_generation/README.md`


## Example Quests
You now know what type of Quests and Rewards are available.
Here are some examples of randomly generated Quests along with their Rewards.  
Remember that there are a lot of possibilities to tweak the Quest and Reward generation in the `config.yml` and in the `quest_generation` files.


---
#### Mine 48 Iron Ore
Reward:
- 1 Iron Shovel: Efficiency IV

---
#### Mine 192 Coal Ore
Reward:
- 1 Enchanted Book: Sharpness V
- 1 Enchanted Golden Apple

---
#### Chop 64 Spruce Logs
Reward:
- 3 Saddle

---
#### Kill 190 Skeletons
Reward:
- 1 Netherite Sword

---
#### Trade with a Librarian 10 times
Reward:
- 1 Diamond Leggings: Unbreaking III

---
#### Find a Fortress
Reward:
- 1 Enchanted Golden Apple
- 6 Potion: Water Breathing
- 3 Potion: Invisibility +

---
#### Find a Mansion
Reward:
- 1 Netherite Chestplate: Fire Protection IV
- 1 Netherite Boots: Feather Falling III
- 24 Ender Pearl
- 24 Slime Ball

---
#### Kill 100 Zombies
Reward:
- 1 Diamond Sword: Looting I

---
#### Chop 224 Logs  
Reward:
  - *$336*

---
#### Harvest 64 Beetroot  
Reward:
  - *1 Iron Chestplate*
  - *16 Coal*

---
#### Enchant Diamond Boots with Protection III+  
Reward:
  - *737 XP*

---
#### Mine 48 Nether Gold Ore  
Reward:
  - *1 Enchanted Book: Looting III*

---
####  Find a Swamp Hut
Reward:
  - *1 Enchanted Book: Mending*

---
#### Kill 40 Cows
Reward:
- 20 Iron Ingot
- 1 Iron Chestplate

---
#### Chop 160 Logs
Reward:
- 4 Potion: Instant Health II

---
#### Enchant 10 Books  
Reward:
  - *64 Iron Ingot*

---
#### Find a Ruined Portal  
Reward:
  - *$480*

---
#### Mine 192 Iron Ore  
Reward:
  - *1 Enchanted Book: Thorns II*
  - *1 Jukebox*

---
#### Harvest 16 Sugar Cane  
Reward:
  - *1 Iron Boots*
  - *1 Iron Pickaxe*

---
#### Find a Shipwreck  
Reward:
  - *1 Enchanted Golden Apple*
  - *1 Bow*

---
#### Level up 35 times  
Reward:
  - *840 XP*

---
#### Break 2 Amethyst Cluster
Reward:
- 44 Cooked Chicken

---
#### Harvest 16 Carrot
Reward:
- 1 Golden Apple
- 1 Iron Sword

---
#### Find a Fortress  
Reward:
  - *864 XP*

---
#### Kill 2 Wither Skeletons  
Reward:
  - *52 Cooked Porkchop*

---
#### Kill 20 Pigs  
Reward:
  - *$160*

---
#### Enchant 12 Books  
Reward:
  - *3 Diamond*

---
#### Kill 7 Glow Squids
Reward:
- 1 Diamond Sword
- 6 Splash Potion: Speed +

---
#### Find a Village
Reward:

---
#### Find an Ocean Ruin  
Reward:
  - *1 Enchanted Book: Protection IV*
  - *2 Splash Potion: Regeneration +*

---
#### Enchant a Diamond Pickaxe with Fortune II+  
Reward:
  - *$1,536*

---
#### Enchant a Diamond Pickaxe with Efficiency IV+
Reward:
- 36 Golden Carrot
- 3 Splash Potion: Night Vision +

---
#### Trade with a Fisherman 14 times
Reward:
- 12 Amethyst Shard

---
#### Break 104 Glowstone  
Reward:
  - *64 Gold Ingot*
  - *1 Enchanted Book: Silk Touch*

---
### The following quests were generated with a `quantity-factor` of 3.0 instead of 1.0
---
#### Kill 140 Sheep  
Reward:
  - *672 XP*

---
#### Find a Fortress  
Reward:
  - *$1,440*

---
#### Mine 1856 Coal Ore  
  - *1 Netherite Pickaxe: Efficiency V*

---
#### Kill 60 Phantoms  
Reward:
  - *12 Golden Apple*

---
#### Harvest 144 Carrot  
Reward:
  - *276 XP*

---
#### Kill 108 Wither Skeletons  
Reward:
  - *64 Diamond*
  - *1 Netherite Sword: Unbreaking III*
  - *1 Enchanted Book: Fire Aspect II*
  - *1 Bow: Power V*

---
#### Level up 110 times  
Reward:
  - *1 Netherite Shovel: Mending*
  - *1 Enchanted Book: Respiration III*

---
#### Kill 330 Creepers  
Reward:
  - *1 Netherite Chestplate*
  - *64 Gold Ingot*

---  


## License
Copyright (C) 2020 Anton Stamme anton@stamme.de

BasicQuests is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

BasicQuests is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with BasicQuests.  If not, see <https://www.gnu.org/licenses/>.
