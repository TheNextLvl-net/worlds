package net.thenextlvl.worlds.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.key.Key;
import net.thenextlvl.worlds.WorldsPlugin;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

@NullMarked
// todo: make this more readable
public final class WorldKeyImportSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
    private final WorldsPlugin plugin;

    public WorldKeyImportSuggestionProvider(final WorldsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) {
        suggestModern(builder);
        suggestLegacyPaths(builder);
        return builder.buildFuture();
    }

    private void suggestModern(final SuggestionsBuilder builder) {
        try (final var entries = plugin.modernWorldRegistry().listEntries(plugin.getServer().getWorldContainer().toPath())) {
            entries.forEach(entry -> {
                if (entry.getValue().keyImportable()) suggest(builder, entry.getValue().key());
                else suggest(builder, entry.getKey());
            });
        } catch (final IOException ignored) {
        }
    }

    private void suggestLegacyPaths(final SuggestionsBuilder builder) {
        try (final var paths = plugin.legacyWorldRegistry().listPaths(plugin.getServer().getWorldContainer().toPath())) {
            paths.forEach(path -> suggest(builder, path));
        } catch (final IOException ignored) {
        }
    }

    private void suggest(final SuggestionsBuilder builder, final Key key) {
        if (plugin.getWorldRegistry().isRegistered(key)) return;
        final var suggestion = key.asString();
        if (suggestion.contains(builder.getRemaining())) builder.suggest(suggestion);
    }

    private void suggest(final SuggestionsBuilder builder, final Path path) {
        final var suggestion = escape(suggestion(path.toAbsolutePath().normalize()));
        if (suggestion.contains(builder.getRemaining())) builder.suggest(suggestion);
    }

    private String escape(final String string) {
        return "\"" + string + "\"";
    }

    private String suggestion(final Path path) {
        final var directory = Path.of("").toAbsolutePath().normalize();
        return path.startsWith(directory) ? directory.relativize(path).toString() : path.toString();
    }
}
