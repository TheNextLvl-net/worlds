# Worlds

Simple, modern, efficient and blazingly fast world management system

## Commands

### /world

| Argument | Description                                     | Usage                            |
|----------|-------------------------------------------------|----------------------------------|
| create   | build an image and create a world based on that | /world create [name] (flags)     |
| delete   | permanently delete a certain world              | /world delete [world] (flags)    |
| import   | import a world from an existing image           | /world import [image]            |
| info     | receive all important information about a world | /world info (world)              |
| list     | receive a list of all existing worlds           | /world list                      |
| teleport | teleport you or someone else to a certain world | /world teleport [world] (player) |

### Flags

_Command flags are used by adding a double dash (--) in front of the name (example: `--generator`)_<br/>
_the aliases can be used by adding a single dash (-) in front of the alias (example: `-g`)_

#### /world create

| Flag        | Alias | Values                                        | Description                                                                                                            |
|-------------|-------|-----------------------------------------------|------------------------------------------------------------------------------------------------------------------------|
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

#### /world delete

| Flag       | Alias | Description                                                            |
|------------|-------|------------------------------------------------------------------------|
| keep-image | k     | deletes the world but not its image and marks the image as load-manual |

## API

https://repo.thenextlvl.net/#/releases/net/thenextlvl/worlds/api
