package net.thenextlvl.worlds.command.suggestion;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import lombok.RequiredArgsConstructor;
import net.thenextlvl.worlds.WorldsPlugin;

import java.io.File;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class LevelSuggestionProvider<S> implements SuggestionProvider<S> {
    private final WorldsPlugin plugin;

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        plugin.levelView().listLevels()
                .filter(plugin.levelView()::canLoad)
                .map(File::getName)
                .map(StringArgumentType::escapeIfRequired)
                .filter(s -> s.contains(builder.getRemaining()))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }
}
