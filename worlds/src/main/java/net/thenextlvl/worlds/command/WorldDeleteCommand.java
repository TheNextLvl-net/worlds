package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.argument.CommandFlagsArgument;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

import java.io.File;
import java.util.Set;

import static net.thenextlvl.worlds.command.WorldCommand.worldArgument;

@NullMarked
class WorldDeleteCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> create(WorldsPlugin plugin) {
        return Commands.literal("delete")
                .requires(source -> source.getSender().hasPermission("worlds.command.delete"))
                .then(delete(plugin));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, World> delete(WorldsPlugin plugin) {
        return worldArgument(plugin)
                .then(Commands.argument("flags", new CommandFlagsArgument(
                        Set.of("--confirm", "--schedule")
                )).executes(context -> delete(context, plugin)))
                .executes(context -> confirmationNeeded(context, plugin));
    }

    private static int confirmationNeeded(CommandContext<CommandSourceStack> context, WorldsPlugin plugin) {
        var sender = context.getSource().getSender();
        plugin.bundle().sendMessage(sender, "command.confirmation",
                Placeholder.parsed("action", "/" + context.getInput()),
                Placeholder.parsed("confirmation", "/" + context.getInput() + " --confirm"));
        return Command.SINGLE_SUCCESS;
    }

    private static int delete(CommandContext<CommandSourceStack> context, WorldsPlugin plugin) {
        var flags = context.getArgument("flags", CommandFlagsArgument.Flags.class);
        if (!flags.contains("--confirm")) return confirmationNeeded(context, plugin);
        var world = context.getArgument("world", World.class);
        var result = delete(world, flags.contains("--schedule"), plugin);
        plugin.bundle().sendMessage(context.getSource().getSender(), result,
                Placeholder.parsed("world", world.getName()));
        return Command.SINGLE_SUCCESS;
    }

    // todo: create public api
    private static String delete(World world, boolean schedule, WorldsPlugin plugin) {
        return schedule ? scheduleDeletion(world, plugin) : deleteNow(world, plugin);
    }

    private static String deleteNow(World world, WorldsPlugin plugin) {
        if (plugin.isRunningFolia())
            return "world.delete.disallowed.folia";
        if (world.getKey().toString().equals("minecraft:overworld"))
            return "world.delete.disallowed";

        var fallback = plugin.getServer().getWorlds().getFirst().getSpawnLocation();
        world.getPlayers().forEach(player -> player.teleport(fallback));

        if (!plugin.levelView().unload(world, false))
            return "world.unload.failed";

        return delete(world.getWorldFolder()) ? "world.delete.success" : "world.delete.failed";
    }

    private static String scheduleDeletion(World world, WorldsPlugin plugin) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (delete(world.getWorldFolder())) return;
            plugin.getComponentLogger().error("Failed to delete world {}", world.getName());
        }));
        return "world.delete.scheduled";
    }

    private static boolean delete(File file) {
        if (file.isFile()) return file.delete();
        var files = file.listFiles();
        if (files == null) return false;
        for (var file1 : files) delete(file1);
        return file.delete();
    }
}
