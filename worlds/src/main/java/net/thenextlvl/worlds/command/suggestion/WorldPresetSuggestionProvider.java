package net.thenextlvl.worlds.command.suggestion;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import core.paper.command.SuggestionProvider;
import net.thenextlvl.worlds.WorldsPlugin;
import org.jspecify.annotations.NullMarked;

import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

@NullMarked
public class WorldPresetSuggestionProvider implements SuggestionProvider {
    private final WorldsPlugin plugin;

    public WorldPresetSuggestionProvider(WorldsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CompletableFuture<Suggestions> suggest(CommandContext<?> context, SuggestionsBuilder builder) {
        return CompletableFuture.runAsync(() -> {
            try (var directoryStream = Files.newDirectoryStream(plugin.presetsFolder(), "*.json")) {
                for (var path : directoryStream) {
                    var fileName = path.getFileName().toString();
                    var suggestion = fileName.substring(0, fileName.length() - 5);
                    var escapedSuggestion = StringArgumentType.escapeIfRequired(suggestion);
                    if (escapedSuggestion.contains(builder.getRemaining())) builder.suggest(escapedSuggestion);
                }
            } catch (Exception e) {
                plugin.getComponentLogger().warn("Failed to list presets", e);
            }
        }).thenApply(unused -> builder.build());
    }
}
