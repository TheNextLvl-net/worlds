package net.thenextlvl.worlds.command.suggestion;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.util.TriState;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.level.Level;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@NullMarked
public final class LevelSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
    private final WorldsPlugin plugin;
    private final boolean unknownLevels;

    public LevelSuggestionProvider(final WorldsPlugin plugin, final boolean unknownLevels) {
        this.unknownLevels = unknownLevels;
        this.plugin = plugin;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) {
        return CompletableFuture.runAsync(() -> plugin.levelView().listLevels().stream()
                .filter(plugin.levelView()::canLoad)
                .map(plugin.levelView()::read)
                .map(level -> level.map(Level.Builder::build).orElse(null))
                .filter(Objects::nonNull)
                .filter(level -> unknownLevels ? !level.isWorldKnown() : level.isWorldKnown() && level.isEnabled().equals(TriState.FALSE))
                .forEach(level -> {
                    final var name = level.getDirectory().getFileName().toString();
                    final var escaped = StringArgumentType.escapeIfRequired(name);
                    if (!escaped.contains(builder.getRemaining())) return;
                    builder.suggest(escaped, () -> level.key().asString());
                })
        ).thenApply(ignored -> builder.build());
    }

    @SuppressWarnings("unchecked")
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        return getSuggestions((CommandContext<CommandSourceStack>) context, builder);
    }
}
