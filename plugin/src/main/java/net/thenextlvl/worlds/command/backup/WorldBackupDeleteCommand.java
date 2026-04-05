package net.thenextlvl.worlds.command.backup;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.brigadier.SimpleCommand;
import net.thenextlvl.worlds.command.suggestion.BackupSuggestionProvider;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.thenextlvl.worlds.command.WorldCommand.worldArgument;

@NullMarked
final class WorldBackupDeleteCommand extends SimpleCommand {
    private WorldBackupDeleteCommand(final WorldsPlugin plugin) {
        super(plugin, "delete", "worlds.command.backup.delete");
    }

    public static ArgumentBuilder<CommandSourceStack, ?> create(final WorldsPlugin plugin) {
        final var command = new WorldBackupDeleteCommand(plugin);
        return command.create().then(worldArgument(plugin)
                .then(Commands.argument("backup", StringArgumentType.string())
                        .suggests(new BackupSuggestionProvider(plugin))
                        .executes(command)));
    }

    @Override
    public int run(final CommandContext<CommandSourceStack> context) {
        final var world = context.getArgument("world", World.class);
        final var backup = context.getArgument("backup", String.class);
        final var resolved = plugin.levelView().getBackupFolder(world).resolve(backup + ".zip");
        final var success = delete(world, resolved);
        final var message = success ? "world.backup.delete.success" : "world.backup.delete.failed";
        plugin.bundle().sendMessage(context.getSource().getSender(), message,
                Placeholder.parsed("world", world.getName()),
                Placeholder.parsed("identifier", backup));
        return success ? SINGLE_SUCCESS : 0;
    }

    private boolean delete(final World world, final Path backup) {
        try {
            if (!Files.isRegularFile(backup)) return false;
            Files.delete(backup);
            return true;
        } catch (final IOException e) {
            plugin.getComponentLogger().warn("Failed to delete backup of world {} from {}", world.getName(), backup, e);
            return false;
        }
    }
}
