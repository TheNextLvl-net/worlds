package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.argument.WorldArgument;
import net.thenextlvl.worlds.command.brigadier.SimpleCommand;
import net.thenextlvl.worlds.command.suggestion.WorldSuggestionProvider;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.CompletableFuture;

import static net.thenextlvl.worlds.command.WorldCommand.worldArgument;

@NullMarked
final class WorldUnloadCommand extends SimpleCommand {
    private WorldUnloadCommand(WorldsPlugin plugin) {
        super(plugin, "unload", "worlds.command.unload");
    }

    public static ArgumentBuilder<CommandSourceStack, ?> create(WorldsPlugin plugin) {
        var command = new WorldUnloadCommand(plugin);
        return command.create().then(worldArgument(plugin)
                .suggests(new WorldSuggestionProvider<>(plugin, (context, world) ->
                        !plugin.levelView().isOverworld(world)))
                .then(command.unloadFallback())
                .executes(command));
    }

    private RequiredArgumentBuilder<CommandSourceStack, World> unloadFallback() {
        return Commands.argument("fallback", new WorldArgument(plugin))
                .suggests(new WorldSuggestionProvider<>(plugin, (context, world) ->
                        !world.equals(context.getLastChild().getArgument("world", World.class))))
                .executes(this);
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var world = context.getArgument("world", World.class);
        var fallback = tryGetArgument(context, "fallback", World.class).orElse(null);

        if (plugin.levelView().isOverworld(world)) {
            plugin.bundle().sendMessage(context.getSource().getSender(), "world.unload.disallowed");
            return 0;
        } else if (world.equals(fallback)) {
            plugin.bundle().sendMessage(context.getSource().getSender(), "world.unload.fallback");
            return 0;
        }

        var fallbackSpawn = fallback != null ? fallback.getSpawnLocation()
                : plugin.levelView().getOverworld().getSpawnLocation();

        plugin.bundle().sendMessage(context.getSource().getSender(), "world.unload",
                Placeholder.parsed("world", world.getName()));

        CompletableFuture.allOf(world.getPlayers().stream()
                .map(player -> player.teleportAsync(fallbackSpawn))
                .toArray(CompletableFuture[]::new)
        ).thenCompose(ignored -> {
            plugin.levelView().setEnabled(world, false);
            return plugin.levelView().unloadAsync(world, true);
        }).thenAccept(success -> {
            var message = success ? "world.unload.success" : "world.unload.failed";
            plugin.bundle().sendMessage(context.getSource().getSender(), message,
                    Placeholder.parsed("world", world.getName()));
        });
        return Command.SINGLE_SUCCESS;
    }
}
