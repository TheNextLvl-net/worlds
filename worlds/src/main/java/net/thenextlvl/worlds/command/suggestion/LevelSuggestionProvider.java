package net.thenextlvl.worlds.command.suggestion;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.kyori.adventure.util.TriState;
import net.thenextlvl.worlds.WorldsPlugin;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@NullMarked
public class LevelSuggestionProvider<S> implements SuggestionProvider<S> {
    private final WorldsPlugin plugin;
    private final boolean unknownLevels;

    public LevelSuggestionProvider(WorldsPlugin plugin, boolean unknownLevels) {
        this.unknownLevels = unknownLevels;
        this.plugin = plugin;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CompletableFuture.runAsync(() -> plugin.levelView().listLevels().stream()
                .filter(plugin.levelView()::canLoad)
                .map(plugin.levelView()::read)
                .map(level -> level.orElse(null))
                .filter(Objects::nonNull)
                .filter(level -> unknownLevels ? !level.isWorldKnown() : level.isWorldKnown() && level.isEnabled().equals(TriState.FALSE))
                .forEach(level -> {
                    var name = level.getFile().getFileName().toString();
                    var escaped = StringArgumentType.escapeIfRequired(name);
                    if (!escaped.contains(builder.getRemaining())) return;
                    builder.suggest(escaped, () -> level.key().asString());
                })
        ).thenApply(unused -> builder.build());
    }
}
