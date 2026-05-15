package net.thenextlvl.worlds.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.key.Key;
import net.thenextlvl.worlds.WorldsPlugin;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@NullMarked
public final class WorldKeyImportSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
    private final WorldsPlugin plugin;

    public WorldKeyImportSuggestionProvider(final WorldsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) {
        return CompletableFuture.runAsync(() -> {
            final var loaded = plugin.getServer().getWorlds().stream()
                    .map(World::getWorldPath)
                    .collect(Collectors.toSet());
            final var managed = plugin.listLevels().toList();
            plugin.levelView().listLevelFolders()
                    .filter(path -> !loaded.contains(path))
                    .filter(path -> !managed.contains(path))
                    .map(path -> plugin.levelView().key(path).orElse(null))
                    .filter(Objects::nonNull)
                    .filter(key -> !plugin.getWorldRegistry().isRegistered(key))
                    .map(Key::asString)
                    .filter(s -> s.contains(builder.getRemaining()))
                    .forEach(builder::suggest);
        }).thenApply(ignored -> builder.build());
    }
}
