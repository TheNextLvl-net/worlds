package net.thenextlvl.worlds.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.thenextlvl.worlds.WorldsPlugin;
import org.jspecify.annotations.NullMarked;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@NullMarked
public final class DimensionSuggestionProvider<S> implements SuggestionProvider<S> {
    private final Map<String, String> dimensions;
    private final WorldsPlugin plugin;

    public DimensionSuggestionProvider(WorldsPlugin plugin, Map<String, String> dimensions) {
        this.dimensions = dimensions;
        this.plugin = plugin;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        var sender = ((CommandSourceStack) context.getSource()).getSender();
        dimensions.entrySet().stream()
                .filter(entry -> entry.getKey().contains(builder.getRemaining()))
                .forEach(entry -> builder.suggest(entry.getKey(), () -> {
                    var component = plugin.bundle().component(entry.getValue(), sender);
                    return PlainTextComponentSerializer.plainText().serialize(component);
                }));
        return builder.buildFuture();
    }
}
