package net.thenextlvl.worlds.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.kyori.adventure.key.Key;
import org.bukkit.Keyed;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiPredicate;

@NullMarked
public final class WorldSuggestionProvider<S> implements SuggestionProvider<S> {
    private final Plugin plugin;
    private final BiPredicate<CommandContext<S>, World> filter;

    public WorldSuggestionProvider(final Plugin plugin) {
        this(plugin, (context, world) -> true);
    }

    public WorldSuggestionProvider(final Plugin plugin, final BiPredicate<CommandContext<S>, World> filter) {
        this.plugin = plugin;
        this.filter = filter;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        plugin.getServer().getWorlds().stream()
                .filter(world -> filter.test(context, world))
                .map(Keyed::key)
                .map(Key::asString)
                .filter(s -> s.contains(builder.getRemaining()))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }
}
