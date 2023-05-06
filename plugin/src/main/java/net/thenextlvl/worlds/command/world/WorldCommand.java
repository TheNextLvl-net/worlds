package net.thenextlvl.worlds.command.world;

import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import net.thenextlvl.worlds.Worlds;
import net.thenextlvl.worlds.command.CustomSyntaxFormatter;
import net.thenextlvl.worlds.command.CustomExceptionHandler;

import java.util.function.Function;

public class WorldCommand {

    public static void register(Worlds plugin) throws Exception {
        var manager = new PaperCommandManager<>(plugin, CommandExecutionCoordinator.simpleCoordinator(), Function.identity(), Function.identity());
        CustomExceptionHandler.INSTANCE.apply(manager, sender -> sender);
        manager.commandSyntaxFormatter(new CustomSyntaxFormatter<>());
        manager.registerAsynchronousCompletions();
        manager.registerBrigadier();
        var builder = manager.commandBuilder("world").permission("worlds.command.world");
        manager.command(WorldCreateCommand.create(builder));
        manager.command(WorldDeleteCommand.create(builder));
        manager.command(WorldInfoCommand.create(builder));
        manager.command(WorldListCommand.create(builder));

        // TODO: 03.05.23 commands
        // /world import [file] (-t type) (-e environment) (-g generator)
        // /world tp [world] (player)
        // /world unload [world]
        // /world export [world]
        // /world load [world]
        // /world setspawn
        // /world spawn
    }
}
