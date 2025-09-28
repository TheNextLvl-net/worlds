package net.thenextlvl.worlds.command.backup;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.brigadier.SimpleCommand;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NullMarked;

import static net.thenextlvl.worlds.command.WorldCommand.worldArgument;

@NullMarked
final class WorldBackupListCommand extends SimpleCommand {
    private WorldBackupListCommand(WorldsPlugin plugin) {
        super(plugin, "list", "worlds.command.backup.list");
    }

    public static ArgumentBuilder<CommandSourceStack, ?> create(WorldsPlugin plugin) {
        var command = new WorldBackupListCommand(plugin);
        return command.create()
                .then(worldArgument(plugin).executes(command))
                .executes(command);
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        tryGetArgument(context, "world", World.class).ifPresentOrElse(world -> {
            listBackups(context.getSource().getSender(), world);
        }, () -> plugin.getServer().getWorlds().forEach(world -> {
            listBackups(context.getSource().getSender(), world);
        }));
        return SINGLE_SUCCESS;
    }

    private void listBackups(CommandSender sender, World world) {
        var backups = plugin.levelView().listBackups(world)
                .map(path -> Component.text(path.getFileName().toString()))
                .toList();
        plugin.bundle().sendMessage(sender, "world.backup.list",
                Placeholder.parsed("world", world.getName()),
                Formatter.number("amount", backups.size()),
                Formatter.joining("backups", backups));
    }
}
