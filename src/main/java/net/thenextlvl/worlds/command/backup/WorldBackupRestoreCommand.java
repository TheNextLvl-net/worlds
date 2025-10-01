package net.thenextlvl.worlds.command.backup;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.argument.CommandFlagsArgument;
import net.thenextlvl.worlds.command.brigadier.SimpleCommand;
import net.thenextlvl.worlds.command.suggestion.BackupSuggestionProvider;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;

import static net.thenextlvl.worlds.command.WorldCommand.worldArgument;

@NullMarked
final class WorldBackupRestoreCommand extends SimpleCommand {
    private WorldBackupRestoreCommand(WorldsPlugin plugin) {
        super(plugin, "restore", "worlds.command.backup.restore");
    }

    public static ArgumentBuilder<CommandSourceStack, ?> create(WorldsPlugin plugin) {
        var command = new WorldBackupRestoreCommand(plugin);
        return command.create().then(command.restore());
    }

    private RequiredArgumentBuilder<CommandSourceStack, World> restore() {
        return worldArgument(plugin)
                .then(x(Commands.argument("backup", StringArgumentType.string())
                        .suggests(new BackupSuggestionProvider(plugin))))
                .then(x(Commands.literal("latest")));
    }

    private ArgumentBuilder<CommandSourceStack, ?> x(ArgumentBuilder<CommandSourceStack, ?> command) {
        return command.then(Commands.argument("flags", new CommandFlagsArgument(
                        Set.of("--confirm", "--schedule")
                )).executes(this))
                .executes(this::confirmationNeeded);
    }

    private int confirmationNeeded(CommandContext<CommandSourceStack> context) {
        var sender = context.getSource().getSender();
        plugin.bundle().sendMessage(sender, "command.confirmation",
                Placeholder.parsed("action", "/" + context.getInput()),
                Placeholder.parsed("confirmation", "/" + context.getInput() + " --confirm"));
        return 0;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        var flags = context.getArgument("flags", CommandFlagsArgument.Flags.class);
        if (!flags.contains("--confirm")) return confirmationNeeded(context);
        var world = context.getArgument("world", World.class);
        var backup = tryGetArgument(context, "backup", String.class);
        var schedule = flags.contains("--schedule");
        if (!schedule) plugin.bundle().sendMessage(context.getSource().getSender(), "world.backup.restore",
                Placeholder.parsed("world", world.getName()));
        
        var path = backup.map(name -> plugin.levelView().getBackupFolder(world).resolve(name + ".zip")).or(() -> {
            return plugin.levelView().listBackups(world).min((first, second) -> {
                try {
                    var time1 = Files.readAttributes(first, BasicFileAttributes.class).creationTime();
                    var time2 = Files.readAttributes(second, BasicFileAttributes.class).creationTime();
                    return time2.compareTo(time1);
                } catch (IOException e) {
                    return 0;
                }
            });
        }).orElse(null);

        if (path == null) {
            plugin.bundle().sendMessage(context.getSource().getSender(), "world.backup.list.empty",
                    Placeholder.parsed("world", world.getName()));
            return 0;
        }

        var string = path.getFileName().toString();
        var backupName = backup.orElse(string.substring(0, string.lastIndexOf('.')));
        
        plugin.levelView().restoreBackupAsync(world, path, schedule).thenAccept(result -> {
            var message = switch (result.result()) {
                case SUCCESS -> "world.backup.restore.success";
                case SCHEDULED -> "world.backup.restore.scheduled";
                case REQUIRES_SCHEDULING -> "world.backup.restore.disallowed";
                case UNLOAD_FAILED -> "world.unload.failed";
                case FAILED -> "world.backup.restore.failed";
            };
            plugin.bundle().sendMessage(context.getSource().getSender(), message,
                    Placeholder.parsed("world", result.world() != null ? result.world().getName() : world.getName()),
                    Placeholder.parsed("identifier", backupName));
        }).exceptionally(throwable -> {
            plugin.bundle().sendMessage(context.getSource().getSender(), "world.backup.restore.failed",
                    Placeholder.parsed("world", world.getName()),
                    Placeholder.parsed("identifier", backupName));
            plugin.getComponentLogger().warn("Failed to restore backup of world {} from {}", world.getName(), backupName, throwable);
            return null;
        });
        return SINGLE_SUCCESS;
    }
}
