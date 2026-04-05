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
    private WorldBackupCreateCommand(final WorldsPlugin plugin) {
        super(plugin, "create", "worlds.command.backup.create");
    }

    public static ArgumentBuilder<CommandSourceStack, ?> create(final WorldsPlugin plugin) {
        final var command = new WorldBackupCreateCommand(plugin);
        final var all = Commands.literal("*").executes(command::backupAll);
        final var world = worldArgument(plugin).executes(command::backup);
        final var named = Commands.argument("name", StringArgumentType.string()).executes(command::backup);
        return command.create().then(all).then(world.then(named)).executes(command::backup);
    }

    private int backupAll(final CommandContext<CommandSourceStack> context) {
        return plugin.getServer().getWorlds().stream().mapToInt(world -> backup(context, world, null)).sum();
    }

    private int backup(final CommandContext<CommandSourceStack> context) {
        final var world = tryGetArgument(context, "world", World.class).orElseGet(() ->
                context.getSource().getLocation().getWorld());
        final var name = tryGetArgument(context, "name", String.class).orElse(null);
        return backup(context, world, name);
    }

    private int backup(final CommandContext<CommandSourceStack> context, final World world, @Nullable final String name) {
        final var sender = context.getSource().getSender();
        final var placeholder = Placeholder.parsed("world", world.getName());
        plugin.bundle().sendMessage(sender, "world.backup", placeholder);
        plugin.levelView().createBackupAsync(world, name).thenApply(path -> {
            try {
                return Files.size(path);
            } catch (final IOException e) {
                throw new RuntimeException("Failed to calculate backup size for " + path, e);
            }
        }).thenAccept(bytes -> {
            final var kb = bytes / 1024d;
            final var mb = kb / 1024d;
            final var gb = mb / 1024d;
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
