package net.thenextlvl.worlds.command;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.thenextlvl.perworlds.command.GroupCommand;
import net.thenextlvl.worlds.WorldsPlugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class WorldCommand {
    private final WorldsPlugin plugin;

    public WorldCommand(WorldsPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        var command = Commands.literal("world")
                .requires(source -> source.getSender().hasPermission("worlds.command"))
                .then(new WorldCloneCommand(plugin).create())
                .then(new WorldCreateCommand(plugin).create())
                .then(new WorldDeleteCommand(plugin).create())
                .then(new WorldImportCommand(plugin).create())
                .then(new WorldInfoCommand(plugin).create())
                .then(new WorldLinkCommand(plugin).create())
                .then(new WorldListCommand(plugin).create())
                .then(new WorldLoadCommand(plugin).create())
                .then(new WorldRegenerateCommand(plugin).create())
                .then(new WorldSaveAllCommand(plugin).create())
                .then(new WorldSaveCommand(plugin).create())
                .then(new WorldSaveOffCommand(plugin).create())
                .then(new WorldSaveOnCommand(plugin).create())
                .then(new WorldSetSpawnCommand(plugin).create())
                .then(new WorldSpawnCommand(plugin).create())
                .then(new WorldTeleportCommand(plugin).create())
                .then(new WorldUnloadCommand(plugin).create());
        var commons = plugin.commons();
        if (commons != null) command.then(GroupCommand.create(commons));
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event ->
                event.registrar().register(command.build())));
    }
}
