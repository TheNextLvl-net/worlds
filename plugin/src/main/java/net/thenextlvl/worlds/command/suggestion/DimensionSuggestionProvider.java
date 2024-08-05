package net.thenextlvl.worlds.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import core.paper.command.SuggestionProvider;

import java.util.concurrent.CompletableFuture;

public class DimensionSuggestionProvider implements SuggestionProvider {

    @Override
    public CompletableFuture<Suggestions> suggest(CommandContext<?> context, SuggestionsBuilder builder) {
        builder.suggest("minecraft:overworld");
        builder.suggest("minecraft:the_end");
        builder.suggest("minecraft:the_nether");
        return builder.buildFuture();
    }
}
