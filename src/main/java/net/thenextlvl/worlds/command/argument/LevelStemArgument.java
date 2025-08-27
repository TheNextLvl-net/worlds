package net.thenextlvl.worlds.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.generator.LevelStem;
import net.thenextlvl.worlds.command.suggestion.DimensionSuggestionProvider;
import org.jspecify.annotations.NullMarked;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@NullMarked
public final class LevelStemArgument implements SimpleArgumentType<LevelStem, String> {
    private final WorldsPlugin plugin;

    public LevelStemArgument(WorldsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public LevelStem convert(StringReader reader, String type) {
        return switch (type) {
            case "normal", "overworld" -> LevelStem.OVERWORLD;
            case "nether" -> LevelStem.NETHER;
            case "end" -> LevelStem.END;
            default -> throw new IllegalArgumentException("Custom dimensions are not yet supported");
        };
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return new DimensionSuggestionProvider<S>(plugin, Map.of(
                "normal", "environment.normal",
                "nether", "environment.nether",
                "end", "environment.end"
        )).getSuggestions(context, builder);
    }
}
