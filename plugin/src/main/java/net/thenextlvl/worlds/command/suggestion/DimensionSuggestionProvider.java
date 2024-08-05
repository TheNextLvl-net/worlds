package net.thenextlvl.worlds.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.concurrent.CompletableFuture;

public class DimensionSuggestionProvider<I> implements SuggestionProvider<I> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<I> context, SuggestionsBuilder builder) {
        builder.suggest("minecraft:overworld");
        builder.suggest("minecraft:the_end");
        builder.suggest("minecraft:the_nether");
        return builder.buildFuture();
    }
}
