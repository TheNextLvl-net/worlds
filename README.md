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

### /world

| Argument | Description                                     | Usage                              | Permission                    |
|----------|-------------------------------------------------|------------------------------------|-------------------------------|
| create   | build an image and create a world based on that | /world create [name] (flags)       | worlds.command.world.create   |
| delete   | permanently delete a certain world              | /world delete [world] (flags)      | worlds.command.world.delete   |
| export   | save a certain world to the disk                | /world export (world)              | worlds.command.world.export   |
| import   | import a world from an existing image           | /world import [image]              | worlds.command.world.import   |
| info     | receive all important information about a world | /world info (world)                | worlds.command.world.info     |
| list     | receive a list of all existing worlds           | /world list                        | worlds.command.world.list     |
| setspawn | change the spawn point of a world               | /world setspawn (position) (angle) | worlds.command.world.setspawn |
| teleport | teleport you or someone else to a certain world | /world teleport [world] (player)   | worlds.command.world.teleport |

The perm-pack to grant all permissions: `worlds.commands.world`

### /world link

| Argument | Description                       | Usage                                                   | Permission                 |
|----------|-----------------------------------|---------------------------------------------------------|----------------------------|
| create   | create a new world link           | /world link create [source] [destination] (portal-type) | worlds.command.link.create |
| delete   | delete an existing world link     | /world link delete [link]                               | worlds.command.link.delete |
| list     | receive a list of all world links | /world link list                                        | worlds.command.link.list   |

The perm-pack to grant all permissions: `worlds.commands.link`

## Flags

_Command flags are used by adding a double dash (--) in front of the name (example: `--generator`)_<br/>
_the aliases can be used by adding a single dash (-) in front of the alias (example: `-g`)_

### /world create

| Flag        | Alias | Values                                        | Description                                                                                                            |
|-------------|-------|-----------------------------------------------|------------------------------------------------------------------------------------------------------------------------|
| auto-save   |       | `true`, `false`                               | whether the world will automatically save                                                                              |
| base        | b     | world                                         | creates a copy of the given world                                                                                      |
| deletion    |       | `world`, `world-and-image`                    | automatically deletes the world (_and its image_) on shutdown                                                          |
| environment | e     | `nether`, `normal`, `the-end`                 | changes the [environment](https://minecraft.fandom.com/wiki/Dimension) of the world                                    |
| generator   | g     | world generator plugin                        | uses a custom generator and biome provider for the world creation                                                      |
| hardcore    |       |                                               | sets the world in [hardcore](https://minecraft.fandom.com/wiki/Hardcore) mode                                          |
| identifier  | i     | world generator identifier                    | defines which generator/biome-provider was requested for the generator                                                 |
| load-manual |       |                                               | prevents the world from getting automatically loaded on startup                                                        |
| preset      |       | world preset                                  | uses a custom [world preset](https://minecraft.fandom.com/wiki/Custom_world_preset) for the world creation             |
| seed        | s     | seed (Long/String)                            | defines the [seed](https://minecraft.fandom.com/wiki/Seed_(level_generation)) which will be used to generate the world |
| structures  |       | `true`, `false`                               | whether [structures](https://minecraft.fandom.com/wiki/Structure) should generate in the world                         |
| type        | t     | `amplified`, `flat`, `large-biomes`, `normal` | changes the [type](https://minecraft.fandom.com/wiki/Category:World_types) of the world                                |

### /world delete

| Flag       | Description                                    |
|------------|------------------------------------------------|
| confirm    | confirms you action                            |
| schedule   | schedules the world deletion to shutdown       |
| keep-image | deletes the world but not its image            |
| keep-world | unloads the world _(no files will be deleted)_ |

_The general spawn position will be overridden and only the world will be used_

## World Presets

## API

https://repo.thenextlvl.net/#/releases/net/thenextlvl/worlds/api
