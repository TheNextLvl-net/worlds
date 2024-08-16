# Worlds

Simple, modern, efficient and blazingly fast world management system
with support for linking worlds to properly use portals with multiple worlds<br/>
bStat Metrics can be found [here](https://bstats.org/plugin/bukkit/TheNextLvl%20Worlds/19652)

## Versions

> [!IMPORTANT]
> The latest major version (2.0.0) changed the way how worlds and links are loaded<br/>
> When updating from any (1.x.x) version you'll have to import your worlds using the command
> `/world import <world> [<key>] [<dimension>] [<generator>]`<br/>
> To link your worlds again you have to use `/world link create <source> <destination>`

> [!NOTE]
> World images (`*.image`) do no longer exist, and you can safely delete all remanence of them<br/>
> The files `links.json` and `config.json` in `plugins/Worlds` also no longer exist and can be safely removed<br/>
> If you didn't change anything about the translations or presets, it is recommended to regenerate them by deleting the
> folders (`Worlds/translations` and `Worlds/presets`)
>
> **As with all major updates that introduce breaking changes, make sure to create backups before updating**<br/>
> If you encounter any issues after updating please report
> them [to us](https://github.com/TheNextLvl-net/worlds/issues/new/choose)

[Latest version supporting 1.20.6 (Java 21)](https://github.com/TheNextLvl-net/worlds/releases/tag/v1.2.5)<br>
[Latest version supporting 1.19/1.20.4 (Java 19)](https://github.com/TheNextLvl-net/worlds/releases/tag/v1.1.6)<br>
[Latest version supporting 1.19/1.20.4 (Java 17)](https://github.com/TheNextLvl-net/worlds/releases/tag/v1.1.3)<br>

## Commands

| Command                                                                         | Description                                                   | Permission                 |
|---------------------------------------------------------------------------------|---------------------------------------------------------------|----------------------------|
| /world clone <world> <key> [template]                                           | clone a world                                                 | worlds.command.clone       |
| /world create <key> generator <generator> [<dimension>] [<structures>] [<seed>] | create a world using a custom world generator plugin          | worlds.command.create      |
| /world create <key> preset <preset> [<dimension>] [<structures>] [<seed>]       | create a world using a preset                                 | worlds.command.create      |
| /world create <key> type <type> [<dimension>] [<structures>] [<seed>]           | create a world with a specific type                           | worlds.command.create      |
| /world delete <world> [<flags>]                                                 | immediately delete or schedule a world for deletion           | worlds.command.delete      |
| /world import <world> [<key>] [<dimension>] [<generator>]                       | import a world from files                                     | worlds.command.import      |
| /world info [<world>]                                                           | display info about a certain world                            | worlds.command.info        |
| /world link create <source> <destination>                                       | link to worlds                                                | worlds.command.link.create |
| /world link list                                                                | display all links                                             | worlds.command.link.list   |
| /world link remove <world> <relative>                                           | unlink two worlds                                             | worlds.command.link.remove |
| /world list                                                                     | list all loaded worlds                                        | worlds.command.list        |
| /world load <world>                                                             | load a previously imported world                              | worlds.command.load        |
| /world regenerate <world> [<flags>]                                             | immediately regenerate or schedule a world for regeneration   | worlds.command.regenerate  |
| /world save <world> [flush]                                                     | save a world to disk                                          | worlds.command.save        |
| /world save-all [flush]                                                         | save all worlds to disk                                       | worlds.command.save-all    |
| /world save-off [<world>]                                                       | temporarily disable saving in a specific world                | worlds.command.save-off    |
| /world save-on [<world>]                                                        | enable saving in a specific world if previously turned off    | worlds.command.save-on     |
| /world setspawn [<position>] [<angle>]                                          | set the spawn of the world you are in                         | worlds.command.setspawn    |
| /world spawn                                                                    | teleport yourself to the spawn of your current world          | worlds.command.spawn       |
| /world teleport <world> [<entities>] [<position>]                               | teleport something to a different position in a certain world | worlds.command.teleport    |
| /world unload <world> [<fallback>]                                              | unload a world                                                | worlds.command.unload      |

## World Presets

## API

https://repo.thenextlvl.net/#/releases/net/thenextlvl/worlds/api
