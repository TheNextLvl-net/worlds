package net.thenextlvl.worlds.command.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import core.paper.command.WrappedArgumentType;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.generator.GeneratorType;
import net.thenextlvl.worlds.command.suggestion.DimensionSuggestionProvider;
import org.jspecify.annotations.NullMarked;

import java.util.Map;

@NullMarked
public class GeneratorTypeArgument extends WrappedArgumentType<String, GeneratorType> {
    public GeneratorTypeArgument(WorldsPlugin plugin) {
        super(StringArgumentType.word(), (reader, type) -> GeneratorType.getByName(type)
                        .orElseThrow(() -> new IllegalArgumentException("Unknown dimension type")),
                new DimensionSuggestionProvider(plugin, Map.of(
                        GeneratorType.AMPLIFIED.name(), "world.type.amplified",
                        GeneratorType.DEBUG.name(), "world.type.debug",
                        GeneratorType.FLAT.name(), "world.type.flat",
                        GeneratorType.LARGE_BIOMES.name(), "world.type.large_biomes",
                        GeneratorType.NORMAL.name(), "world.type.normal",
                        GeneratorType.SINGLE_BIOME.name(), "world.type.fixed"
                )));
    }
}
