package net.thenextlvl.worlds.command;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.RequiredArgsConstructor;
import net.thenextlvl.worlds.WorldsPlugin;

@RequiredArgsConstructor
@SuppressWarnings("UnstableApiUsage")
public class WorldCommand {
    private final WorldsPlugin plugin;

    public void register() {
        var command = Commands.literal("world")
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
                .then(new WorldUnloadCommand(plugin).create())
                .build();
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event ->
                event.registrar().register(command)));
    }
}
