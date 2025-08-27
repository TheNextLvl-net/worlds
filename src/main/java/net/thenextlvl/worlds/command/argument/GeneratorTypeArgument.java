package net.thenextlvl.worlds.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.generator.GeneratorType;
import net.thenextlvl.worlds.command.suggestion.DimensionSuggestionProvider;
import org.jspecify.annotations.NullMarked;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@NullMarked
public final class GeneratorTypeArgument implements SimpleArgumentType<GeneratorType, String> {
    private final WorldsPlugin plugin;

    public GeneratorTypeArgument(WorldsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public GeneratorType convert(StringReader reader, String type) {
        return GeneratorType.getByName(type).orElseThrow(() ->
                new IllegalArgumentException("Unknown dimension type"));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return new DimensionSuggestionProvider<S>(plugin, Map.of(
                GeneratorType.AMPLIFIED.name(), "world.type.amplified",
                GeneratorType.DEBUG.name(), "world.type.debug",
                GeneratorType.FLAT.name(), "world.type.flat",
                GeneratorType.LARGE_BIOMES.name(), "world.type.large_biomes",
                GeneratorType.NORMAL.name(), "world.type.normal",
                GeneratorType.SINGLE_BIOME.name(), "world.type.fixed"
        )).getSuggestions(context, builder);
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.string();
    }
}
