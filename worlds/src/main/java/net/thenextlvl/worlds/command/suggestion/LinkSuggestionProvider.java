package net.thenextlvl.worlds.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.kyori.adventure.key.Key;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.link.LinkTree;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.CompletableFuture;

@NullMarked
public class LinkSuggestionProvider<T> implements SuggestionProvider<T> {
    private final WorldsPlugin plugin;
    private final boolean unlink;

    public LinkSuggestionProvider(WorldsPlugin plugin, boolean unlink) {
        this.plugin = plugin;
        this.unlink = unlink;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<T> context, SuggestionsBuilder builder) {
        plugin.linkProvider().getLinkTrees().stream()
                .filter(tree -> !unlink || !tree.isEmpty())
                .map(LinkTree::getPersistedOverworld)
                .map(Key::asString)
                .filter(s -> s.contains(builder.getRemaining()))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }

    public static class Unlinked<T> implements SuggestionProvider<T> {
        private final WorldsPlugin plugin;

        public Unlinked(WorldsPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<T> context, SuggestionsBuilder builder) {
            plugin.getServer().getWorlds().stream()
                    .filter(world -> !world.getEnvironment().equals(Environment.NORMAL))
                    .filter(world -> !plugin.linkProvider().hasLinkTree(world))
                    .map(World::key)
                    .map(Key::asString)
                    .filter(s -> s.contains(builder.getRemaining()))
                    .forEach(builder::suggest);
            return builder.buildFuture();
        }
    }

    public static class Linked<T> implements SuggestionProvider<T> {
        private final WorldsPlugin plugin;

        public Linked(WorldsPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<T> context, SuggestionsBuilder builder) {
            var world = context.getLastChild().getArgument("world", World.class);
            plugin.linkProvider().getLinkTree(world).ifPresent(tree -> {
                tree.getPersistedNether().map(Key::asString)
                        .filter(s -> s.contains(builder.getRemaining()))
                        .ifPresent(builder::suggest);
                tree.getPersistedEnd().map(Key::asString)
                        .filter(s -> s.contains(builder.getRemaining()))
                        .ifPresent(builder::suggest);
            });
            return builder.buildFuture();
        }
    }
}
