package net.thenextlvl.worlds.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.thenextlvl.worlds.WorldsPlugin;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

@NullMarked
public final class WorldPathImportSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
    private final WorldsPlugin plugin;

    public WorldPathImportSuggestionProvider(final WorldsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) {
        try (final var paths = plugin.legacyWorldRegistry().listPaths(plugin.getServer().getWorldContainer().toPath())) {
            paths.map(path -> path.toAbsolutePath().normalize())
                    .map(this::suggestion)
                    .map(s -> "\"" + s + "\"")
                    .filter(s -> s.contains(builder.getRemaining()))
                    .forEach(builder::suggest);
        } catch (final IOException ignored) {
        }
        return builder.buildFuture();
    }

    private String suggestion(final Path path) {
        final var root = plugin.getServer().getWorldContainer().toPath().toAbsolutePath().normalize();
        return path.equals(root) ? root.toString() : root.relativize(path).toString();
    }
}
