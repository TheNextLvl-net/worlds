package net.thenextlvl.worlds.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import core.paper.command.SuggestionProvider;
import net.thenextlvl.worlds.link.Relative;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class RelativeSuggestionProvider implements SuggestionProvider {
    @Override
    public CompletableFuture<Suggestions> suggest(CommandContext<?> context, SuggestionsBuilder builder) {
        Arrays.stream(Relative.values())
                .map(relative -> relative.key().asString())
                .filter(s -> s.contains(builder.getRemaining()))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }
}
