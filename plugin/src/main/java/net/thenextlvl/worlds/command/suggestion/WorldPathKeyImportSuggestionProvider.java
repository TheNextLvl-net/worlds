package net.thenextlvl.worlds.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.key.Key;
import net.thenextlvl.worlds.LegacyWorldRegistry;
import net.thenextlvl.worlds.ModernWorldRegistry;
import net.thenextlvl.worlds.WorldsPlugin;
import org.jspecify.annotations.NullMarked;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@NullMarked
public final class WorldPathKeyImportSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
    private final WorldsPlugin plugin;

    public WorldPathKeyImportSuggestionProvider(final WorldsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) {
        suggestedKey(context)
                .map(plugin.levelView()::findFreeKey)
                .map(Key::asString)
                .filter(s -> s.contains(builder.getRemaining()))
                .ifPresent(builder::suggest);
        return builder.buildFuture();
    }

    private Optional<Key> suggestedKey(final CommandContext<?> context) {
        final var input = Path.of(context.getArgument("path", String.class));
        final var source = (input.isAbsolute() ? input
                : plugin.getServer().getWorldContainer().toPath().resolve(input)
        ).toAbsolutePath().normalize();
        return plugin.legacyWorldRegistry().read(source).map(LegacyWorldRegistry.LegacyWorldData::key)
                .or(() -> plugin.modernWorldRegistry().read(source).map(ModernWorldRegistry.ModernWorldData::key))
                .or(() -> plugin.levelView().lenientKey(source));
    }
}
