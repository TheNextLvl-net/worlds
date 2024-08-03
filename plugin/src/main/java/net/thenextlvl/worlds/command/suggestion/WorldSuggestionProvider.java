package net.thenextlvl.worlds.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.key.Key;
import org.bukkit.Keyed;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class WorldSuggestionProvider<S> implements SuggestionProvider<S> {
    private final Plugin plugin;

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        plugin.getServer().getWorlds().stream()
                .map(Keyed::key)
                .map(Key::asString)
                .filter(s -> s.contains(builder.getRemaining()))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }
}
