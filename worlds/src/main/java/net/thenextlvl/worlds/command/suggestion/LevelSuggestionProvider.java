package net.thenextlvl.worlds.command.suggestion;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.thenextlvl.worlds.WorldsPlugin;
import org.jspecify.annotations.NullMarked;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

@NullMarked
public class LevelSuggestionProvider<S> implements SuggestionProvider<S> {
    private final WorldsPlugin plugin;

    public LevelSuggestionProvider(WorldsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        plugin.levelView().listLevels().stream()
                .filter(plugin.levelView()::canLoad)
                .map(Path::getFileName)
                .map(Path::toString)
                .map(StringArgumentType::escapeIfRequired)
                .filter(s -> s.contains(builder.getRemaining()))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }
}
