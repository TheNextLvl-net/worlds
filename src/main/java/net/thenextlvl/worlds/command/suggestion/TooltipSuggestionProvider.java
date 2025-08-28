package net.thenextlvl.worlds.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.thenextlvl.worlds.WorldsPlugin;
import org.jspecify.annotations.NullMarked;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@NullMarked
public class TooltipSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
    private final Map<String, String> tooltips;
    protected final WorldsPlugin plugin;

    protected TooltipSuggestionProvider(WorldsPlugin plugin, Map<String, String> tooltips) {
        this.tooltips = tooltips;
        this.plugin = plugin;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        var sender = context.getSource().getSender();
        tooltips.entrySet().stream()
                .filter(entry -> entry.getKey().contains(builder.getRemaining()))
                .forEach(entry -> {
                    var component = plugin.bundle().component(entry.getValue(), sender);
                    builder.suggest(entry.getKey(), MessageComponentSerializer.message().serialize(component));
                });
        return builder.buildFuture();
    }

    @SuppressWarnings("unchecked")
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return getSuggestions((CommandContext<CommandSourceStack>) context, builder);
    }
}
