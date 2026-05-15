package net.thenextlvl.worlds.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.key.Key;
import net.thenextlvl.worlds.WorldsPlugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@NullMarked
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
            entries.map(entry -> entry.getValue().keyImportable()
                            ? suggestion(entry.getValue().key())
                            : suggestion(entry.getKey()))
                    .filter(Objects::nonNull)
                    .filter(suggestion -> suggestion.contains(builder.getRemaining()))
                    .forEach(builder::suggest);
        } catch (final IOException ignored) {
        }
    }

    private void suggestLegacyPaths(final SuggestionsBuilder builder) {
        try (final var paths = plugin.legacyWorldRegistry().listPaths(plugin.getServer().getWorldContainer().toPath())) {
            paths.map(this::suggestion)
                    .filter(suggestion -> suggestion.contains(builder.getRemaining()))
                    .forEach(builder::suggest);
        } catch (final IOException ignored) {
        }
    }

    private @Nullable String suggestion(final Key key) {
        return plugin.getWorldRegistry().isRegistered(key) ? null : key.asString();
    }

    private String suggestion(final Path path) {
        return "\"" + relativeSuggestion(path.toAbsolutePath().normalize()) + "\"";
    }

    private String relativeSuggestion(final Path path) {
        final var directory = Path.of("").toAbsolutePath().normalize();
        return path.startsWith(directory) ? directory.relativize(path).toString() : path.toString();
    }
}
