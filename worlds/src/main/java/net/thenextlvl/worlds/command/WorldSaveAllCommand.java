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
    private final WorldsPlugin plugin;

    WorldSaveAllCommand(WorldsPlugin plugin) {
        this.plugin = plugin;
    }

    ArgumentBuilder<CommandSourceStack, ?> create() {
        return Commands.literal("save-all")
                .requires(source -> source.getSender().hasPermission("worlds.command.save-all"))
                .then(Commands.literal("flush").executes(context -> saveAll(context.getSource(), true)))
                .executes(context -> saveAll(context.getSource(), false));
    }

    private int saveAll(CommandSourceStack source, boolean flush) {
        plugin.bundle().sendMessage(source.getSender(), "world.save.saving");
        var server = ((CraftServer) plugin.getServer()).getServer();
        var saved = server.saveEverything(!(source.getSender() instanceof ConsoleCommandSender), flush, true);
        var message = saved ? "world.save.success" : "world.save.failed";
        plugin.bundle().sendMessage(source.getSender(), message);
        return saved ? Command.SINGLE_SUCCESS : 0;
    }
}
