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
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

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
                        !world.key().asString().equals("minecraft:overworld")))
                .then(unloadFallback(plugin))
                .executes(context -> unload(plugin, context));
    }

    private static int unload(WorldsPlugin plugin, CommandContext<CommandSourceStack> context) {
        var world = context.getArgument("world", World.class);
        var message = unload(world, null, plugin);
        plugin.bundle().sendMessage(context.getSource().getSender(), message,
                Placeholder.parsed("world", world.getName()));
        return Command.SINGLE_SUCCESS;
    }

    private static RequiredArgumentBuilder<CommandSourceStack, World> unloadFallback(WorldsPlugin plugin) {
        return Commands.argument("fallback", ArgumentTypes.world())
                .suggests(new WorldSuggestionProvider<>(plugin, (context, world) ->
                        !world.equals(context.getLastChild().getArgument("world", World.class))))
                .executes(context -> unloadFallback(plugin, context));
    }

    private static int unloadFallback(WorldsPlugin plugin, CommandContext<CommandSourceStack> context) {
        var world = context.getArgument("world", World.class);
        var fallback = context.getArgument("fallback", World.class);
        var message = unload(world, fallback, plugin);
        plugin.bundle().sendMessage(context.getSource().getSender(), message,
                Placeholder.parsed("world", world.getName()));
        return Command.SINGLE_SUCCESS;
    }

    private static String unload(World world, @Nullable World fallback, WorldsPlugin plugin) {
        if (plugin.isRunningFolia())
            return "world.unload.disallowed.folia";
        if (world.getKey().toString().equals("minecraft:overworld"))
            return "world.unload.disallowed";
        if (world.equals(fallback)) return "world.unload.fallback";

        var fallbackSpawn = fallback != null ? fallback.getSpawnLocation()
                : plugin.getServer().getWorlds().getFirst().getSpawnLocation();
        world.getPlayers().forEach(player -> player.teleport(fallbackSpawn));

        plugin.persistStatus(world, false, false);
        if (!world.isAutoSave()) plugin.levelView().saveLevelData(world, false);

        return plugin.levelView().unload(world, world.isAutoSave())
                ? "world.unload.success"
                : "world.unload.failed";
    }
}
