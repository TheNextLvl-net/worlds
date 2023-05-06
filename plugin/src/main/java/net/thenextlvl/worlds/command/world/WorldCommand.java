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
        manager.command(WorldTeleportCommand.create(builder));
        manager.command(WorldCreateCommand.create(builder));
        manager.command(WorldImportCommand.create(builder));
        manager.command(WorldDeleteCommand.create(builder));
        manager.command(WorldInfoCommand.create(builder));
        manager.command(WorldListCommand.create(builder));

        // TODO: 03.05.23 commands
        // /world unlink [world] [world]
        // /world link [world] [world]
        // /world export [world]
    }
}
