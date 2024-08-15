package net.thenextlvl.worlds.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import core.paper.command.SuggestionProvider;
import lombok.RequiredArgsConstructor;
import net.thenextlvl.worlds.WorldsPlugin;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class GeneratorSuggestionProvider implements SuggestionProvider {
    private final WorldsPlugin plugin;

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
