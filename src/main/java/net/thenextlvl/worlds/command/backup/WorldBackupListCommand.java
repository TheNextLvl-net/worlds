package net.thenextlvl.worlds.command.backup;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.brigadier.SimpleCommand;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

import static net.thenextlvl.worlds.command.WorldCommand.worldArgument;

@NullMarked
final class WorldBackupListCommand extends SimpleCommand {
    private WorldBackupListCommand(WorldsPlugin plugin) {
        super(plugin, "list", "worlds.command.backup.list");
    }

    public static ArgumentBuilder<CommandSourceStack, ?> create(WorldsPlugin plugin) {
        var command = new WorldBackupListCommand(plugin);
        return command.create().then(worldArgument(plugin).executes(command));
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        var world = context.getArgument("world", World.class);
        var backups = plugin.levelView().listBackups(world).sorted((first, second) -> {
            try {
                var time1 = Files.readAttributes(first, BasicFileAttributes.class).creationTime();
                var time2 = Files.readAttributes(second, BasicFileAttributes.class).creationTime();
                return time2.compareTo(time1);
            } catch (IOException e) {
                return 0;
            }
        }).map(path -> {
            var name = path.getFileName().toString();
            var trimmed = name.substring(0, name.lastIndexOf('.'));
            var bytes = 0L;
            try {
                bytes = Files.size(path);
            } catch (IOException e) {
                plugin.getComponentLogger().warn("Failed to calculate backup size for {}", path, e);
            }
            var kb = bytes / 1024d;
            var mb = kb / 1024d;
            var gb = mb / 1024d;

            var time = FileTime.fromMillis(0);
            try {
                time = Files.readAttributes(path, BasicFileAttributes.class).creationTime();
            } catch (IOException e) {
                plugin.getComponentLogger().warn("Failed to get creation time for {}", path, e);
            }
            var seconds = (System.currentTimeMillis() - time.toMillis()) / 1000;
            var minutes = seconds / 60;
            var hours = minutes / 60;
            var days = hours / 24;
            var weeks = days / 7;
            var months = weeks / 4;
            var years = months / 12;

            return plugin.bundle().component("world.backup.info", context.getSource().getSender(),
                    Placeholder.parsed("world", world.key().asString()),
                    Placeholder.parsed("identifier", trimmed),
                    Formatter.number("size", gb >= 1 ? gb : mb >= 1 ? mb : kb >= 1 ? kb : bytes),
                    Formatter.choice("unit", gb >= 1 ? 0 : mb >= 1 ? 1 : kb >= 1 ? 2 : 3),
                    Formatter.number("time", years >= 1 ? years : months >= 1 ? months : weeks >= 1 ? weeks : days >= 1 ? days : hours >= 1 ? hours : minutes >= 1 ? minutes : seconds),
                    Formatter.choice("timeunit", years >= 1 ? 0 : months >= 1 ? 1 : weeks >= 1 ? 2 : days >= 1 ? 3 : hours >= 1 ? 4 : minutes >= 1 ? 5 : 6));
        }).toList();
        var message = backups.isEmpty() ? "world.backup.list.empty" : "world.backup.list";
        plugin.bundle().sendMessage(context.getSource().getSender(), message,
                Placeholder.parsed("world", world.getName()),
                Formatter.number("amount", backups.size()),
                Formatter.booleanChoice("singular", backups.size() == 1),
                Formatter.joining("backups", backups));
        return SINGLE_SUCCESS;
    }
}
