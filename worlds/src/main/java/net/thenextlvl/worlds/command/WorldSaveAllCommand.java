package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.thenextlvl.worlds.WorldsPlugin;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.jspecify.annotations.NullMarked;

@NullMarked
class WorldSaveAllCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> create(WorldsPlugin plugin) {
        return Commands.literal("save-all")
                .requires(source -> source.getSender().hasPermission("worlds.command.save-all"))
                .then(Commands.literal("flush").executes(context -> saveAll(context.getSource(), true, plugin)))
                .executes(context -> saveAll(context.getSource(), false, plugin));
    }

    private static int saveAll(CommandSourceStack source, boolean flush, WorldsPlugin plugin) {
        plugin.bundle().sendMessage(source.getSender(), "world.save.all");
        var server = ((CraftServer) plugin.getServer()).getServer();
        var saved = server.saveEverything(!(source.getSender() instanceof ConsoleCommandSender), flush, true);
        var message = saved ? "world.save.all.success" : "world.save.all.failed";
        plugin.bundle().sendMessage(source.getSender(), message);
        return saved ? Command.SINGLE_SUCCESS : 0;
    }
}
