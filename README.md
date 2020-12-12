# BasicQuests
A plugin for Bukkit servers that implements randomly generated basic quests with rewards for players.


## General
Author: Tonnanto  
Current Version: 0.1.0

In this version an ***economy plugin*** needs to be connected via ***Vault*** for BasicQuests to work.


## How it works
Every player is given a set amount of quests initially (default: 3).
You can view your active quests by using `/quests` or `/quests detail`.
When a player completes a quest the reward can be received by using `/getreward`.
As soon as a quests reward is collected it disappears from the list of quests and a newly generated quest will be added to the list.  
By default a player is allowed to skip one quest every 24h by using `/skipquest <index>`.
Some quests require the player to complete an advancement before they can be generated. Quests in the nether for example require the player to have completed the ***"Diamonds!"*** advancement.  
Also newly generated quests will increase in their quantities related to the players playtime on the server: While a new player might receive a quest like ***"Mine 32 Iron Ore"*** a player with lots of playtime on the server would rather receive a quest like this ***"Mine 512 Iron Ore"***.
This feature can be precisely adjusted in the `config.yml` or be turned of entirely.  
Some quests are incredibly rare but promise very high rewards once completed.


## Quests
Quests are randomly generated in Basic Quests.
Currently available quest types along with some examples are listed below:

* Break Block
  - "Chop 64 Logs."
  - "Break a Spawner."

* Mine Block
  - "Mine 16 Diamond Ore."
  - "Mine 128 Iron Ore."

* Harvest Block
  - "Harvest 64 Sugar Cane."
  - "Harvest 32 Nether Warts."

* Kill Entity
  - "Kill 12 Enderman."
  - "Kill 50 Cows."

* Enchant Item
  - "Enchant 5 Books."
  - "Enchant a Diamond Sword with Sharpness III+."

* Find Structure
  - "Find a Village."
  - "Find a Bastion Remnant."

* Reach Level
  - "Reach Level 40."

* Gain Level
  - "Level up 25 times."

Quest ideas I am thinking about implementing in the near future:
* Trade with Villager
* Fish Item
* Breed Animal
* Tame Animal
* Smelt Item
* Brew Potion


## Rewards
In this version rewards only consist of money (an ***economy plugin*** needs to be connected via ***Vault***).
A quests reward is proportional to the value of the quest and is multiplied by the `reward-factor` which can be adjusted in the `config.yml`.
The value of a given quest is determined by a number of factors along it's generation process.

I am definitely planning on implementing items as rewards very soon. This will also make the plugin work without an economy plugin.


## Building
To build BasicQuests, you need JDK 8 or higher installed on your system. Then, run the following command:
```sh
./gradlew build
```

...or if you're on windows run the following command:

```batch
gradlew build
```

The jar can be found in `build/libs/`.


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
