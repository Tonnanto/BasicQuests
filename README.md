# BasicQuests
A plugin for Bukkit servers that implements randomly generated basic quests with rewards for players.


## General
Author: Tonnanto

more to come...

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
