
# Adding a new language

In order for BasicQuest to support a new language, the `messages_en.yml` file needs to be translated.
The new file should be named `messages_<language code>.yml`. The file can be found [here](https://github.com/Tonnanto/BasicQuests/blob/main/src/main/resources/lang/messages_en.yml).

I recommend copy and pasting the contents of the `messages_en.yml` file into the new file.

### Keep in mind:
- Placeholders `{0}`, `{1}`, ... should not be changed! Ask if it is unclear which placeholder contains which value.
- ColorCodes `&a`, `&f`, `&7`, ... should not be changed. If a certain word has been highlighted, try to highlight the same word in your language.
- MineDown Syntax `[...](hover=... run_command=...)` should not be changed.
- Please always use quotation marks `""` for consistency.
- Everything within the `ìtem-plural` sections is optional! This can be used to add unique plural forms of minecraft items names. Feel free to ask if unclear how to use this section.
- You can test your language file by adding its contents to the `custom_messages.yml` file at `plugins/BasicQuests/`. 
  To include minecraft item names in your language set the `locale` option in the `config.yml` to a locale from this [list](https://minecraft.fandom.com/wiki/Language) (`In-game` Column)

---
Contact me if there are any questions.  
Create an issue or a pull request to submit your file.  
