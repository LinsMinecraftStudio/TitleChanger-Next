# TitleChanger Next
TitleChanger Next is the next generation of [TitleChanger](https://modrinth.com/mod/mctitlechanger).
It can change the game title, and you can use placeholders to customize your title.

# Why TitleChanger Next?
Due to well-known performance issues in the old version, I have decided to abandon it and instead develop a new version.

**New features compared to old version**
1. ModMenu hook
2. Config screen
3. Better performance (maybe?)

# Placeholder
## Format
* placeholder with no args
```
%placeholder%
```

* placeholder with args
```
%placeholder:arg1,arg2%
```

* placeholder with header (for other mods)
```
%header_placeholder%
```

* placeholder with header and args (for other mods)
```
%header_placeholder:arg1,arg2%
```

**Note: Built-in placeholders don't have a header!**
## Bulitin Placeholders
### Constant Placeholders
1. %mcver% — get the version of Minecraft
2. %hitokoto% — get a sentence from Hitokoto
4. %playername% — get the name of the player
5. %playeruuid% — get the uuid of the player
6. %modver:\<modid\>% return the version of a mod

### Auto updated Placeholders
1. %syncedtime% — get the time
2. %fps% — get fps
3. %ping% — get ping (returns 0 if in singleplayer or not playing)
4. %playtime% — get played time
5. %playingmode% — get the mode that you are playing (empty if not playing)
6. %x% — return the x-axis coordinate of the player's location
7. %y% — return the y-axis coordinate of the player's location
8. %z% — return the z-axis coordinate of the player's location
