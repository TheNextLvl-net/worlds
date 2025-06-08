package net.thenextlvl.worlds.api.preset;

import org.bukkit.Material;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

import java.util.Set;

@NullMarked
public class Presets {
    public static final Preset BOTTOMLESS_PIT = new Preset("Bottomless Pit")
            .addLayer(new Layer(Material.COBBLESTONE, 2))
            .addLayer(new Layer(Material.DIRT, 3))
            .addLayer(new Layer(Material.GRASS_BLOCK, 1))
            .addStructure(Structure.literal("villages"));

    public static final Preset CLASSIC_FLAT = new Preset("Classic Flat")
            .addLayer(new Layer(Material.BEDROCK, 1))
            .addLayer(new Layer(Material.DIRT, 2))
            .addLayer(new Layer(Material.GRASS_BLOCK, 1))
            .addStructure(Structure.literal("villages"));

    public static final Preset DESERT = new Preset("Desert")
            .biome(Biome.literal("desert"))
            .features(true)
            .addLayer(new Layer(Material.BEDROCK, 1))
            .addLayer(new Layer(Material.STONE, 3))
            .addLayer(new Layer(Material.SANDSTONE, 52))
            .addLayer(new Layer(Material.SAND, 8))
            .addStructure(Structure.literal("desert_pyramids"))
            .addStructure(Structure.literal("mineshafts"))
            .addStructure(Structure.literal("strongholds"))
            .addStructure(Structure.literal("villages"));

    public static final Preset OVERWORLD = new Preset("Overworld")
            .lakes(true)
            .features(true)
            .addLayer(new Layer(Material.BEDROCK, 1))
            .addLayer(new Layer(Material.STONE, 59))
            .addLayer(new Layer(Material.DIRT, 3))
            .addLayer(new Layer(Material.GRASS_BLOCK, 1))
            .addStructure(Structure.literal("mineshafts"))
            .addStructure(Structure.literal("pillager_outposts"))
            .addStructure(Structure.literal("ruined_portals"))
            .addStructure(Structure.literal("strongholds"))
            .addStructure(Structure.literal("villages"));

    public static final Preset REDSTONE_READY = new Preset("Redstone Ready")
            .biome(Biome.literal("desert"))
            .addLayer(new Layer(Material.BEDROCK, 1))
            .addLayer(new Layer(Material.STONE, 3))
            .addLayer(new Layer(Material.SANDSTONE, 116));

    public static final Preset SNOWY_KINGDOM = new Preset("Snowy Kingdom")
            .biome(Biome.literal("snowy_plains"))
            .addLayer(new Layer(Material.BEDROCK, 1))
            .addLayer(new Layer(Material.STONE, 59))
            .addLayer(new Layer(Material.DIRT, 3))
            .addLayer(new Layer(Material.GRASS_BLOCK, 1))
            .addLayer(new Layer(Material.SNOW, 1))
            .addStructure(Structure.literal("igloos"))
            .addStructure(Structure.literal("villages"));

    public static final Preset THE_VOID = new Preset("The Void")
            .features(true)
            .biome(Biome.literal("the_void"))
            .addLayer(new Layer(Material.AIR, 1));

    public static final Preset TUNNELERS_DREAM = new Preset("Tunnelers' Dream")
            .features(true)
            .biome(Biome.literal("windswept_hills"))
            .addLayer(new Layer(Material.BEDROCK, 1))
            .addLayer(new Layer(Material.STONE, 230))
            .addLayer(new Layer(Material.DIRT, 5))
            .addLayer(new Layer(Material.GRASS_BLOCK, 1))
            .addStructure(Structure.literal("mineshafts"))
            .addStructure(Structure.literal("strongholds"));

    public static final Preset WATER_WORLD = new Preset("Water World")
            .biome(Biome.literal("deep_ocean"))
            .addLayer(new Layer(Material.BEDROCK, 1))
            .addLayer(new Layer(Material.DEEPSLATE, 64))
            .addLayer(new Layer(Material.STONE, 5))
            .addLayer(new Layer(Material.DIRT, 5))
            .addLayer(new Layer(Material.GRAVEL, 5))
            .addLayer(new Layer(Material.WATER, 90))
            .addStructure(Structure.literal("ocean_monuments"))
            .addStructure(Structure.literal("ocean_ruins"))
            .addStructure(Structure.literal("shipwrecks"));

    private static final Set<Preset> presets = Set.of(
            BOTTOMLESS_PIT,
            CLASSIC_FLAT,
            DESERT,
            OVERWORLD,
            REDSTONE_READY,
            SNOWY_KINGDOM,
            THE_VOID,
            TUNNELERS_DREAM,
            WATER_WORLD
    );

    public static @Unmodifiable Set<Preset> presets() {
        return presets;
    }
}
