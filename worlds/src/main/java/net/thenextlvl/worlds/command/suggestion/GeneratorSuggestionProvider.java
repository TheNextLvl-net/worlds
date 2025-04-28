package net.thenextlvl.worlds.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import core.paper.command.SuggestionProvider;
import net.thenextlvl.worlds.WorldsPlugin;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NullMarked;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

@NullMarked
public class GeneratorSuggestionProvider implements SuggestionProvider {
    private final WorldsPlugin plugin;

    public GeneratorSuggestionProvider(WorldsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CompletableFuture<Suggestions> suggest(CommandContext<?> context, SuggestionsBuilder builder) {
        Arrays.stream(plugin.getServer().getPluginManager().getPlugins())
                .filter(Plugin::isEnabled)
                .filter(plugin.generatorView()::hasGenerator)
                .map(Plugin::getName)
                .filter(s -> s.contains(builder.getRemaining()))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }
}
