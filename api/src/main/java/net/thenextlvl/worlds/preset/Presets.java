package net.thenextlvl.worlds.preset;

import org.bukkit.Material;

public class Presets {
    public static final Preset BOTTOMLESS_PIT = new Preset()
            .addLayer(new Layer(Material.COBBLESTONE, 2))
            .addLayer(new Layer(Material.DIRT, 3))
            .addLayer(new Layer(Material.GRASS_BLOCK, 1))
            .addStructure(Structure.minecraft("villages"));

    public static final Preset CLASSIC_FLAT = new Preset()
            .addLayer(new Layer(Material.BEDROCK, 1))
            .addLayer(new Layer(Material.DIRT, 2))
            .addLayer(new Layer(Material.GRASS_BLOCK, 1))
            .addStructure(Structure.minecraft("villages"));

    public static final Preset DESERT = new Preset()
            .features(true)
            .addLayer(new Layer(Material.BEDROCK, 1))
            .addLayer(new Layer(Material.STONE, 3))
            .addLayer(new Layer(Material.SANDSTONE, 52))
            .addLayer(new Layer(Material.SAND, 8))
            .addStructure(Structure.minecraft("desert_pyramids"))
            .addStructure(Structure.minecraft("mineshafts"))
            .addStructure(Structure.minecraft("strongholds"))
            .addStructure(Structure.minecraft("villages"));

    public static final Preset OVERWORLD = new Preset()
            .lakes(true)
            .features(true)
            .addLayer(new Layer(Material.BEDROCK, 1))
            .addLayer(new Layer(Material.STONE, 59))
            .addLayer(new Layer(Material.DIRT, 3))
            .addLayer(new Layer(Material.GRASS_BLOCK, 1))
            .addStructure(Structure.minecraft("mineshafts"))
            .addStructure(Structure.minecraft("pillager_outposts"))
            .addStructure(Structure.minecraft("ruined_portals"))
            .addStructure(Structure.minecraft("strongholds"))
            .addStructure(Structure.minecraft("villages"));

    public static final Preset REDSTONE_READY = new Preset()
            .addLayer(new Layer(Material.BEDROCK, 1))
            .addLayer(new Layer(Material.STONE, 3))
            .addLayer(new Layer(Material.SANDSTONE, 116));

    public static final Preset SNOWY_KINGDOM = new Preset()
            .addLayer(new Layer(Material.BEDROCK, 1))
            .addLayer(new Layer(Material.STONE, 59))
            .addLayer(new Layer(Material.DIRT, 3))
            .addLayer(new Layer(Material.GRASS_BLOCK, 1))
            .addLayer(new Layer(Material.SNOW, 1))
            .addStructure(Structure.minecraft("igloos"))
            .addStructure(Structure.minecraft("villages"));

    public static final Preset THE_VOID = new Preset()
            .addLayer(new Layer(Material.AIR, 1));

    public static final Preset TUNNELERS_DREAM = new Preset()
            .features(true)
            .biome(Biome.minecraft("windswept_hills"))
            .addLayer(new Layer(Material.BEDROCK, 1))
            .addLayer(new Layer(Material.STONE, 230))
            .addLayer(new Layer(Material.DIRT, 5))
            .addLayer(new Layer(Material.GRASS_BLOCK, 1))
            .addStructure(Structure.minecraft("mineshafts"))
            .addStructure(Structure.minecraft("strongholds"));

    public static final Preset WATER_WORLD = new Preset()
            .addLayer(new Layer(Material.BEDROCK, 1))
            .addLayer(new Layer(Material.DEEPSLATE, 64))
            .addLayer(new Layer(Material.STONE, 5))
            .addLayer(new Layer(Material.DIRT, 5))
            .addLayer(new Layer(Material.GRAVEL, 5))
            .addLayer(new Layer(Material.WATER, 90))
            .addStructure(Structure.minecraft("ocean_monuments"))
            .addStructure(Structure.minecraft("ocean_ruins"))
            .addStructure(Structure.minecraft("shipwrecks"));
}
