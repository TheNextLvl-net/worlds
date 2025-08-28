package net.thenextlvl.worlds.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.generator.GeneratorType;
import net.thenextlvl.worlds.command.suggestion.TooltipSuggestionProvider;
import org.jspecify.annotations.NullMarked;

import java.util.Map;

@NullMarked
public final class GeneratorTypeArgument extends TooltipSuggestionProvider implements SimpleArgumentType<GeneratorType, String> {
    public GeneratorTypeArgument(WorldsPlugin plugin) {
        super(plugin, Map.of(
                GeneratorType.AMPLIFIED.name(), "world.type.amplified",
                GeneratorType.DEBUG.name(), "world.type.debug",
                GeneratorType.FLAT.name(), "world.type.flat",
                GeneratorType.LARGE_BIOMES.name(), "world.type.large_biomes",
                GeneratorType.NORMAL.name(), "world.type.normal",
                GeneratorType.SINGLE_BIOME.name(), "world.type.fixed"
        ));
    }

    @Override
    public GeneratorType convert(StringReader reader, String type) {
        return GeneratorType.getByName(type).orElseThrow(() ->
                new IllegalArgumentException("Unknown dimension type"));
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.string();
    }
}
