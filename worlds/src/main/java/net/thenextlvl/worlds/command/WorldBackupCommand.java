package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;

import static net.thenextlvl.worlds.command.WorldCommand.worldArgument;

@NullMarked
public class WorldBackupCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> create(WorldsPlugin plugin) {
        return Commands.literal("backup")
                .requires(source -> source.getSender().hasPermission("worlds.command.backup"))
                .then(Commands.literal("*").executes(context -> backupAll(context, plugin)))
                .then(worldArgument(plugin).executes(context -> backup(context, plugin)))
                .executes(context -> backup(context, context.getSource().getLocation().getWorld(), plugin));
    }

    private static int backupAll(CommandContext<CommandSourceStack> context, WorldsPlugin plugin) {
        return plugin.getServer().getWorlds().stream().mapToInt(world -> backup(context, world, plugin)).sum();
    }

    private static int backup(CommandContext<CommandSourceStack> context, WorldsPlugin plugin) {
        return backup(context, context.getArgument("world", World.class), plugin);
    }

    private static int backup(CommandContext<CommandSourceStack> context, World world, WorldsPlugin plugin) {
        var sender = context.getSource().getSender();
        var placeholder = Placeholder.parsed("world", world.getName());
        try {
            plugin.bundle().sendMessage(sender, "world.backup", placeholder);
            var bytes = plugin.levelView().backup(world);
            var kb = bytes / 1024d;
            var mb = kb / 1024d;
            var gb = mb / 1024d;
            plugin.bundle().sendMessage(sender, "world.backup.success", placeholder,
                    Formatter.number("size", gb >= 1 ? gb : mb >= 1 ? mb : kb >= 1 ? kb : bytes),
                    Formatter.choice("unit", gb >= 1 ? 0 : mb >= 1 ? 1 : kb >= 1 ? 2 : 3));
            return Command.SINGLE_SUCCESS;
        } catch (IOException e) {
            plugin.getComponentLogger().warn("Failed to backup world {}", world.getName(), e);
            plugin.bundle().sendMessage(sender, "world.backup.failed", placeholder);
            return 0;
        }
    }
}
