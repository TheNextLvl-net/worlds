package net.thenextlvl.worlds.command.suggestion;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.kyori.adventure.util.TriState;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.exception.GeneratorException;
import net.thenextlvl.worlds.api.level.Level;
import org.jspecify.annotations.NullMarked;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@NullMarked
public final class LevelSuggestionProvider<S> implements SuggestionProvider<S> {
    private final WorldsPlugin plugin;
    private final boolean unknownLevels;

    public LevelSuggestionProvider(WorldsPlugin plugin, boolean unknownLevels) {
        this.unknownLevels = unknownLevels;
        this.plugin = plugin;
    }

    private Optional<Level.Builder> safeRead(Path path) {
        try {
            return plugin.levelView().read(path);
        } catch (GeneratorException e) {
            return Optional.empty();
        } catch (Exception e) {
            plugin.getComponentLogger().error("Failed to read level {}", path, e);
            return Optional.empty();
        }
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CompletableFuture.runAsync(() -> plugin.levelView().listLevels().stream()
                .filter(plugin.levelView()::canLoad)
                .map(this::safeRead)
                .map(level -> level.map(Level.Builder::build).orElse(null))
                .filter(Objects::nonNull)
                .filter(level -> unknownLevels ? !level.isWorldKnown() : level.isWorldKnown() && level.isEnabled().equals(TriState.FALSE))
                .forEach(level -> {
                    var name = level.getDirectory().getFileName().toString();
                    var escaped = StringArgumentType.escapeIfRequired(name);
                    if (!escaped.contains(builder.getRemaining())) return;
                    builder.suggest(escaped, () -> level.key().asString());
                })
        ).thenApply(ignored -> builder.build());
    }
}
