package net.thenextlvl.worlds.command.suggestion;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import core.paper.command.SuggestionProvider;
import lombok.RequiredArgsConstructor;
import net.thenextlvl.worlds.WorldsPlugin;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class WorldPresetSuggestionProvider implements SuggestionProvider {
    private final WorldsPlugin plugin;

    @Override
    public CompletableFuture<Suggestions> suggest(CommandContext<?> context, SuggestionsBuilder builder) {
        var files = plugin.presetsFolder().listFiles((file, name) ->
                name.endsWith(".json"));
        if (files != null) Arrays.stream(files)
                .map(File::getName)
                .map(name -> name.substring(0, name.length() - 5))
                .map(StringArgumentType::escapeIfRequired)
                .filter(s -> s.contains(builder.getRemaining()))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }
}
