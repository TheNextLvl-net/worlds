package net.thenextlvl.worlds.command.link;

import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import net.thenextlvl.worlds.Worlds;
import net.thenextlvl.worlds.command.CustomExceptionHandler;
import net.thenextlvl.worlds.command.CustomSyntaxFormatter;

import java.util.function.Function;

public class LinkCommand {

    public static void register(Worlds plugin) throws Exception {
        var manager = new PaperCommandManager<>(plugin, CommandExecutionCoordinator.simpleCoordinator(), Function.identity(), Function.identity());
        CustomExceptionHandler.INSTANCE.apply(manager, sender -> sender);
        manager.commandSyntaxFormatter(new CustomSyntaxFormatter<>());
        manager.registerAsynchronousCompletions();
        manager.registerBrigadier();
        var builder = manager.commandBuilder("link");
        manager.command(LinkCreateCommand.create(builder));
        manager.command(LinkDeleteCommand.create(builder));
        manager.command(LinkListCommand.create(builder));
    }
}
