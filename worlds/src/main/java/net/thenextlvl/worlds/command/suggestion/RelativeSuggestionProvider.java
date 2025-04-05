package net.thenextlvl.worlds.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import core.paper.command.SuggestionProvider;
import net.thenextlvl.worlds.api.link.Relative;
import org.jspecify.annotations.NullMarked;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

@NullMarked
public class RelativeSuggestionProvider implements SuggestionProvider {
    private final Predicate<Relative> filter;

    public RelativeSuggestionProvider(Predicate<Relative> filter) {
        this.filter = filter;
    }

    @Override
    public CompletableFuture<Suggestions> suggest(CommandContext<?> context, SuggestionsBuilder builder) {
        Arrays.stream(Relative.values())
                .filter(filter)
                .map(relative -> relative.key().asString())
                .filter(s -> s.contains(builder.getRemaining()))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }
}
