# Worlds

Simple, modern, efficient and blazingly fast world management system
with support for linking worlds to properly use portals with multiple worlds<br/>
bStat Metrics can be found [here](https://bstats.org/plugin/bukkit/TheNextLvl%20Worlds/19652)

## Versions

> [!IMPORTANT]
> Worlds only supports the latest version of Paper (1.20.6)<br>
> The latest version of Worlds requires Java 21

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

## World Images

> [!WARNING]
> In the near future World Images will be replaced or even removed
> In their current state they are not very safe or consistent in their behaviour
> The goal is to use as much information provided in the `level.dat` file

A world image file has the extension `.image` and contains a json object with the following entries

| Key         | Values                                        | Description                                                      | Optional |
|-------------|-----------------------------------------------|------------------------------------------------------------------|----------|
| name        | String                                        | the name of the world                                            | No       |
| key         | NamespacedKey                                 | the namespaced key of the world                                  | No       |
| settings    | World Preset                                  | the world settings<br/>_(only applies to world type flat)_       | Yes      |
| generator   | World Generator                               | defines the world generator<br/>_(not combinable with settings)_ | Yes      |
| deletion    | `WORLD`, `WORLD_AND_IMAGE`                    | what to delete on shutdown                                       | Yes      |
| environment | `NORMAL`, `NETHER`, `THE_END`                 | the environment of the world                                     | No       |
| type        | `NORMAL`, `FLAT`, `LARGE_BIOMES`, `AMPLIFIED` | the type of the world                                            | No       |
| loadOnStart | boolean                                       | whether the world should be loaded on startup                    | Yes      |

## World Generator

A world generator consists out of two parts: The **plugin** and the **identifier**<br/>
The plugin is the name of the plugin that provides a generator.<br/>
In most cases the identifier will not be required, but in case a plugin provides multiple world generators,<br/>
you have to define which one you have to use.

### Example

```json
{
  "name": "example",
  "generator": {
    "plugin": "example-plugin",
    "identifier": "example-generator"
  },
  "key": "worlds:example",
  "settings": {
    "biome": "minecraft:the_void",
    "layers": []
  },
  "deletion": "WORLD",
  "environment": "NORMAL",
  "type": "FLAT",
  "loadOnStart": true
}
```

## World Presets

## API

https://repo.thenextlvl.net/#/releases/net/thenextlvl/worlds/api
