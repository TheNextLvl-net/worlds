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
                .filter(this::hasGenerator)
                .map(Plugin::getName)
                .filter(s -> s.contains(builder.getRemaining()))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }

    private boolean hasGenerator(Plugin plugin) {
        return hasChunkGenerator(plugin.getClass()) || hasBiomeProvider(plugin.getClass());
    }

    private boolean hasChunkGenerator(Class<? extends Plugin> clazz) {
        try {
            return clazz.getMethod("getDefaultWorldGenerator", String.class, String.class).getDeclaringClass().equals(clazz);
        } catch (NoSuchMethodException e) {
            e.printStackTrace(); // todo remove
            return false;
        }
    }

    private boolean hasBiomeProvider(Class<? extends Plugin> clazz) {
        try {
            return clazz.getMethod("getDefaultBiomeProvider", String.class, String.class).getDeclaringClass().equals(clazz);
        } catch (NoSuchMethodException e) {
            e.printStackTrace(); // todo remove
            return false;
        }
    }
}
