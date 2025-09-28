package net.thenextlvl.worlds.command.backup;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.brigadier.BrigadierCommand;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;

import static net.thenextlvl.worlds.command.WorldCommand.worldArgument;

@NullMarked
final class WorldBackupCreateCommand extends BrigadierCommand {
    private WorldBackupCreateCommand(WorldsPlugin plugin) {
        super(plugin, "create", "worlds.command.backup.create");
    }

    public static ArgumentBuilder<CommandSourceStack, ?> create(WorldsPlugin plugin) {
        var command = new WorldBackupCreateCommand(plugin);
        var all = Commands.literal("*").executes(command::backupAll);
        var world = worldArgument(plugin).executes(command::backup);
        var named = Commands.argument("name", StringArgumentType.string()).executes(command::backup);
        return command.create().then(all).then(world.then(named)).executes(command::backup);
    }

    private int backupAll(CommandContext<CommandSourceStack> context) {
        return plugin.getServer().getWorlds().stream().mapToInt(world -> backup(context, world, null)).sum();
    }

    private int backup(CommandContext<CommandSourceStack> context) {
        var world = tryGetArgument(context, "world", World.class).orElseGet(() -> 
                context.getSource().getLocation().getWorld());
        var name = tryGetArgument(context, "name", String.class).orElse(null);
        return backup(context, world, name);
    }

    private int backup(CommandContext<CommandSourceStack> context, World world, @Nullable String name) {
        var sender = context.getSource().getSender();
        var placeholder = Placeholder.parsed("world", world.getName());
        plugin.bundle().sendMessage(sender, "world.backup", placeholder);
        plugin.levelView().createBackupAsync(world, name).thenApply(path -> {
            try {
                return Files.size(path);
            } catch (IOException e) {
                throw new RuntimeException("Failed to calculate backup size for " + path, e);
            }
        }).thenAccept(bytes -> {
            var kb = bytes / 1024d;
            var mb = kb / 1024d;
            var gb = mb / 1024d;
            plugin.bundle().sendMessage(sender, "world.backup.success", placeholder,
                    Formatter.number("size", gb >= 1 ? gb : mb >= 1 ? mb : kb >= 1 ? kb : bytes),
                    Formatter.choice("unit", gb >= 1 ? 0 : mb >= 1 ? 1 : kb >= 1 ? 2 : 3));
        }).exceptionally(throwable -> {
            plugin.getComponentLogger().warn("Failed to backup world {}", world.getName(), throwable);
            plugin.bundle().sendMessage(sender, "world.backup.failed", placeholder);
            return null;
        });
        return Command.SINGLE_SUCCESS;
    }
}
