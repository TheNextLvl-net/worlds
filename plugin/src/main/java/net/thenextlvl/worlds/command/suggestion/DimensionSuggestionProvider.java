package net.thenextlvl.worlds.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import core.paper.command.SuggestionProvider;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.RequiredArgsConstructor;
import net.thenextlvl.worlds.WorldsPlugin;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@SuppressWarnings("UnstableApiUsage")
public class DimensionSuggestionProvider implements SuggestionProvider {
    private final WorldsPlugin plugin;

    private final Map<String, String> dimensions = Map.of(
            "minecraft:overworld", "environment.normal",
            "minecraft:the_end", "environment.end",
            "minecraft:the_nether", "environment.nether"
    );

    @Override
    public CompletableFuture<Suggestions> suggest(CommandContext<?> context, SuggestionsBuilder builder) {
        var sender = ((CommandSourceStack) context.getSource()).getSender();
        dimensions.entrySet().stream()
                .filter(entry -> entry.getKey().contains(builder.getRemaining()))
                .forEach(entry -> builder.suggest(entry.getKey(), () ->
                        plugin.bundle().format(sender, entry.getValue())));
        return builder.buildFuture();
    }
}