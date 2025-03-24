package net.thenextlvl.worlds.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import core.paper.command.SuggestionProvider;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.thenextlvl.worlds.WorldsPlugin;
import org.jspecify.annotations.NullMarked;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@NullMarked
public class WorldTypeSuggestionProvider implements SuggestionProvider {
    private final WorldsPlugin plugin;
    private final Map<String, String> dimensions = Map.of(
            "minecraft:amplified", "world.type.amplified",
            "minecraft:checkerboard", "world.type.checkerboard",
            "minecraft:debug", "world.type.debug",
            "minecraft:flat", "world.type.flat",
            "minecraft:large_biomes", "world.type.large_biomes",
            "minecraft:normal", "world.type.normal",
            "minecraft:single_biome", "world.type.single_biome"
    );

    public WorldTypeSuggestionProvider(WorldsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CompletableFuture<Suggestions> suggest(CommandContext<?> context, SuggestionsBuilder builder) {
        var sender = ((CommandSourceStack) context.getSource()).getSender();
        dimensions.entrySet().stream()
                .filter(entry -> entry.getKey().contains(builder.getRemaining()))
                .forEach(entry -> builder.suggest(entry.getKey(), () ->
                        plugin.bundle().format(sender, entry.getValue())));
        return builder.buildFuture();
    }
}
