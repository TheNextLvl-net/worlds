package net.thenextlvl.worlds.command.argument;

import core.paper.command.WrappedArgumentType;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.key.Key;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.model.WorldPreset;
import net.thenextlvl.worlds.command.suggestion.DimensionSuggestionProvider;
import org.jspecify.annotations.NullMarked;

import java.util.Map;

@NullMarked
public class WorldTypeArgument extends WrappedArgumentType<Key, WorldPreset> {
    public WorldTypeArgument(WorldsPlugin plugin) {
        super(ArgumentTypes.key(), (reader, type) -> switch (type.asMinimalString()) {
            case "amplified" -> WorldPreset.AMPLIFIED;
            case "checkerboard" -> WorldPreset.CHECKERBOARD;
            case "debug_all_block_states", "debug_world", "debug" -> WorldPreset.DEBUG;
            case "fixed", "single_biome" -> WorldPreset.SINGLE_BIOME;
            case "flat" -> WorldPreset.FLAT;
            case "large_biomes" -> WorldPreset.LARGE_BIOMES;
            case "noise", "normal", "default" -> WorldPreset.NORMAL;
            default -> throw new IllegalArgumentException("Custom dimensions are not yet supported");
        }, new DimensionSuggestionProvider(plugin, Map.of(
                "minecraft:amplified", "world.type.amplified",
                "minecraft:checkerboard", "world.type.checkerboard",
                "minecraft:debug", "world.type.debug",
                "minecraft:flat", "world.type.flat",
                "minecraft:large_biomes", "world.type.large_biomes",
                "minecraft:normal", "world.type.normal",
                "minecraft:single_biome", "world.type.single_biome"
        )));
    }
}
