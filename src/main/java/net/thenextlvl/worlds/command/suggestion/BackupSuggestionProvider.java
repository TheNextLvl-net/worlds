package net.thenextlvl.worlds.command.suggestion;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.thenextlvl.worlds.WorldsPlugin;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.CompletableFuture;

@NullMarked
public final class BackupSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
    private final WorldsPlugin plugin;

    public BackupSuggestionProvider(WorldsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        var world = context.getArgument("world", World.class);
        return CompletableFuture.runAsync(() -> plugin.levelView().listBackups(world)
                        .map(backup -> {
                            var string = backup.getFileName().toString();
                            return string.substring(0, string.lastIndexOf('.'));
                        })
                        .map(StringArgumentType::escapeIfRequired)
                        .filter(s -> s.contains(builder.getRemaining()))
                        .forEach(builder::suggest))
                .thenApply(ignored -> builder.build());
    }
}
