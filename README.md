# Worlds

Simple, modern, efficient and blazingly fast world management system
with support for linking worlds to properly use portals with multiple worlds<br/>
bStat Metrics can be found [here](https://bstats.org/plugin/bukkit/TheNextLvl%20Worlds/19652)

## Commands

### /world

| Argument | Description                                     | Usage                              |
|----------|-------------------------------------------------|------------------------------------|
| create   | build an image and create a world based on that | /world create [name] (flags)       |
| delete   | permanently delete a certain world              | /world delete [world] (flags)      |
| export   | save a certain world to the disk                | /world export (world)              |
| import   | import a world from an existing image           | /world import [image]              |
| info     | receive all important information about a world | /world info (world)                |
| list     | receive a list of all existing worlds           | /world list                        |
| setspawn | change the spawn point of a world               | /world setspawn (position) (angle) |
| teleport | teleport you or someone else to a certain world | /world teleport [world] (player)   |

### /link

| Argument | Description                       | Usage                                             |
|----------|-----------------------------------|---------------------------------------------------|
| create   | create a new world link           | /link create [source] [destination] [portal-type] |
| delete   | delete an existing world link     | /link delete [link]                               |
| list     | receive a list of all world links | /link list                                        |

## Flags

_Command flags are used by adding a double dash (--) in front of the name (example: `--generator`)_<br/>
_the aliases can be used by adding a single dash (-) in front of the alias (example: `-g`)_

### /world create

| Flag        | Alias | Values                                        | Description                                                                                                            |
|-------------|-------|-----------------------------------------------|------------------------------------------------------------------------------------------------------------------------|
| base        | b     | world                                         | creates a world based on another world                                                                                 |
| deletion    |       | `world`, `world-and-image`                    | automatically deletes the world (_and its image_) on shutdown                                                          |
| environment | e     | `nether`, `normal`, `the-end`                 | changes the [environment](https://minecraft.fandom.com/wiki/Dimension) of the world                                    |
| generator   | g     | world generator plugin                        | uses a custom generator and biome provider for the world creation                                                      |
| hardcore    |       |                                               | sets the world in [hardcore](https://minecraft.fandom.com/wiki/Hardcore) mode                                          |
| identifier  | i     | world generator identifier                    | defines which generator/biome-provider was requested for the generator                                                 |
| load-manual |       |                                               | prevents the world from getting automatically loaded on startup                                                        |
| preset      |       | world preset                                  | uses a custom [world preset](https://minecraft.fandom.com/wiki/Custom_world_preset) for the world creation             |
| seed        | s     | seed (Long/String)                            | defines the [seed](https://minecraft.fandom.com/wiki/Seed_(level_generation)) which will be used to generate the world |
| structures  |       |                                               | whether [structures](https://minecraft.fandom.com/wiki/Structure) should generate in the world                         |
| type        | t     | `amplified`, `flat`, `large-biomes`, `normal` | changes the [type](https://minecraft.fandom.com/wiki/Category:World_types) of the world                                |

### /world delete

| Flag       | Description                                                            |
|------------|------------------------------------------------------------------------|
| keep-image | deletes the world but not its image and marks the image as load-manual |
| keep-world | unloads the world _(no files will be deleted)_                         |

## World Images

A world image file has the extension `.image` and contains a json object with the following entries

| Key                | Values                                        | Description                                                                   | Optional |
|--------------------|-----------------------------------------------|-------------------------------------------------------------------------------|----------|
| name               | String                                        | the name of the world                                                         | No       |
| settings           | World Preset                                  | the world settings<br/>_(only for flat maps)_                                 | Yes      |
| generator          | World Generator                               | defines the world generator<br/>_(not combinable with settings)_              | Yes      |
| deletion           | `WORLD`, `WORLD_AND_IMAGE`                    | what to delete on shutdown                                                    | Yes      |
| environment        | `NORMAL`, `NETHER`, `THE_END`                 | the environment of the world                                                  | No       |
| type               | `NORMAL`, `FLAT`, `LARGE_BIOMES`, `AMPLIFIED` | the type of the world                                                         | No       |
| generateStructures | boolean                                       | whether to generate structures                                                | Yes      |
| hardcore           | boolean                                       | whether the world should be in hardcore mode<br/>_(not properly working yet)_ | Yes      |
| loadOnStart        | boolean                                       | whether the world should be loaded on startup                                 | Yes      |
| seed               | double                                        | the seed of the world                                                         | No       |

### Example

```json
{
  "name": "example",
  "settings": "{\"biome\":\"minecraft:the_void\",\"layers\":[]}",
  "deletion": "WORLD",
  "environment": "NORMAL",
  "type": "FLAT",
  "generateStructures": true,
  "hardcore": false,
  "loadOnStart": true,
  "seed": -7920583562141293424
}
```

## World Generator

A world generator consists out of two parts: The **plugin** and the **identifier**<br/>
The plugin is just the name of the plugin itself.<br/>
In most cases the identifier can be null, but in case a plugin provides multiple world generators,<br/>
you have to define which one should be used.

### Example

```json
{
  "plugin": "example-plugin",
  "identifier": "example-generator"
}
```

## World Presets

## API

https://repo.thenextlvl.net/#/releases/net/thenextlvl/worlds/api
