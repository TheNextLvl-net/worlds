package net.thenextlvl.worlds.command.argument;

import core.paper.command.WrappedArgumentType;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.key.Key;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.generator.GeneratorType;
import net.thenextlvl.worlds.command.suggestion.DimensionSuggestionProvider;
import org.jspecify.annotations.NullMarked;

import java.util.Map;

@NullMarked
public class GeneratorTypeArgument extends WrappedArgumentType<Key, GeneratorType> {
    public GeneratorTypeArgument(WorldsPlugin plugin) {
        super(ArgumentTypes.key(), (reader, type) -> GeneratorType.getByKey(type)
                        .orElseThrow(() -> new IllegalArgumentException("Unknown dimension type")),
                new DimensionSuggestionProvider(plugin, Map.of(
                        GeneratorType.AMPLIFIED.key().asString(), "world.type.amplified",
                        GeneratorType.DEBUG.key().asString(), "world.type.debug",
                        GeneratorType.FLAT.key().asString(), "world.type.flat",
                        GeneratorType.LARGE_BIOMES.key().asString(), "world.type.large_biomes",
                        GeneratorType.NORMAL.key().asString(), "world.type.normal",
                        GeneratorType.SINGLE_BIOME.key().asString(), "world.type.fixed"
                )));
    }
}
