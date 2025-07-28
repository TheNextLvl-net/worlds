package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.suggestion.WorldSuggestionProvider;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import static net.thenextlvl.worlds.command.WorldCommand.worldArgument;

@NullMarked
class WorldUnloadCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> create(WorldsPlugin plugin) {
        return Commands.literal("unload")
                .requires(source -> source.getSender().hasPermission("worlds.command.unload"))
                .then(unload(plugin));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, World> unload(WorldsPlugin plugin) {
        return worldArgument(plugin)
                .suggests(new WorldSuggestionProvider<>(plugin, (context, world) ->
                        !plugin.levelView().isOverworld(world)))
                .then(unloadFallback(plugin))
                .executes(context -> unload(plugin, null, context));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, World> unloadFallback(WorldsPlugin plugin) {
        return Commands.argument("fallback", ArgumentTypes.world())
                .suggests(new WorldSuggestionProvider<>(plugin, (context, world) ->
                        !world.equals(context.getLastChild().getArgument("world", World.class))))
                .executes(context -> unload(plugin, context.getArgument("fallback", World.class), context));
    }

    private static int unload(WorldsPlugin plugin, @Nullable World fallback, CommandContext<CommandSourceStack> context) {
        var world = context.getArgument("world", World.class);
        unload(context.getSource().getSender(), world, fallback, plugin).thenAccept(message -> {
            plugin.bundle().sendMessage(context.getSource().getSender(), message,
                    Placeholder.parsed("world", world.getName()));
        });
        return Command.SINGLE_SUCCESS;
    }

    private static CompletableFuture<String> unload(CommandSender sender, World world, @Nullable World fallback, WorldsPlugin plugin) {
        if (plugin.levelView().isOverworld(world))
            return CompletableFuture.completedFuture("world.unload.disallowed");
        if (world.equals(fallback))
            return CompletableFuture.completedFuture("world.unload.fallback");

        var fallbackSpawn = fallback != null ? fallback.getSpawnLocation()
                : plugin.levelView().getOverworld().getSpawnLocation();

        plugin.bundle().sendMessage(sender, "world.unload", Placeholder.parsed("world", world.getName()));
        return CompletableFuture.allOf(world.getPlayers().stream()
                .map(player -> player.teleportAsync(fallbackSpawn))
                .toList().toArray(new CompletableFuture[0])
        ).thenCompose(ignored -> {
            plugin.levelView().setEnabled(world, false);
            return plugin.levelView().unloadAsync(world, true);
        }).thenApply(success -> {
            return success ? "world.unload.success" : "world.unload.failed";
        });
    }
}
