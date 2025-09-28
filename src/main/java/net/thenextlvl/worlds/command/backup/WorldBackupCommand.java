package net.thenextlvl.worlds.command.backup;

import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.brigadier.BrigadierCommand;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class WorldBackupCommand extends BrigadierCommand {
    private WorldBackupCommand(WorldsPlugin plugin) {
        super(plugin, "backup", "worlds.command.backup");
    }

    public static ArgumentBuilder<CommandSourceStack, ?> create(WorldsPlugin plugin) {
        var command = new WorldBackupCommand(plugin);
        return command.create()
                .then(WorldBackupCreateCommand.create(plugin))
                .then(WorldBackupListCommand.create(plugin))
                .then(WorldBackupRestoreCommand.create(plugin));
    }
}
