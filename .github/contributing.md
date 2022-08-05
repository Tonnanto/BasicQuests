
# Adding a new language

In order for BasicQuest to support a new language, the `messages.properties` file needs to be translated.
The new file should be named `messages_<language code>.properties`. The file can be found [here](https://github.com/Tonnanto/BasicQuests/blob/basicQuestsPlugin/src/basicQuestsPlugin/resources/messages.properties).

I recommend copy and pasting the contents of the `message.properties` file into the new file, so hints about placeholders are included.

### Plural forms
Translated minecraft names like items, mobs etc. are automatically downloaded by the plugin. Plural forms are not included though! 
There are 3 ways you can handle plural forms of minecraft names in your language file:

1. Use the plural property of the quest title: 
There are separate properties for quest title with singular and plural forms. 
It is **MANDATORY** to provide singular and plural forms even if they are the same. 
The plural form always contains one additional placeholder in comparison to the singular form. 
The following example shows how these can be used:
```
# {0} - amount, {1} - material
quest.enchantItem.any.plural = Enchant {0} {1}s
# {0} - material
quest.enchantItem.any.singular = Enchant a {0}
```
2. Default plural forms: For all quest types that use (items/blocks/mobs) there is an **OPTIONAL** property that can be used to format all (items/blocks/entities) of that quest in the same way. The following 6 properties can be used to do that. In this example an "s" is appended to the singular name whenever a plural form is used:
```
quest.killEntity.default.plural = {0}s
quest.breakBlock.default.plural = {0}s
quest.mineBlock.default.plural = {0}s
quest.harvestBlock.default.plural = {0}s
quest.chopWood.default.plural = {0}s
quest.enchantItem.default.plural = {0}s
```

3. Unique plural forms: In some languages certain items require completely unique plural forms. These values can be added with **OPTIONAL** properties of the following format:
```
quest.killEntity.<mob_key>.plural
quest.breakBlock.<item_key>.plural
quest.mineBlock.<item_key>.plural
quest.harvestBlock.<item_key>.plural
quest.chopWood.<item_key>.plural
quest.enchantItem.<item_key>.plural
```
The keys must be lowercase and snake_case.   
Hint: Use the same keys that can be found in the [generation files](https://github.com/Tonnanto/BasicQuests/tree/basicQuestsPlugin/src/basicQuestsPlugin/resources/quest_generation) and make them lowercase.


All three approaches can be combined if necessary. The following example shows how all three approaches are used to pluralize kill entity quests in english:
```
# {0} - amount, {1} - entity
quest.killEntity.plural = Kill {0} {1}
# {0} - entity
quest.killEntity.singular = Kill 1 {0}

# {0} - singular entity
quest.killEntity.default.plural = {0}s
quest.killEntity.witch.plural = Witches
quest.killEntity.drowned.plural = Drowned
quest.killEntity.enderman.plural = Endermen
quest.killEntity.sheep.plural = Sheep
```

---
### Contact me if there are any questions.
### Create an issue or a pull request to submit your file.  
