package net.thenextlvl.worlds.command.world;

import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.extra.confirmation.CommandConfirmationManager;
import cloud.commandframework.paper.PaperCommandManager;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.Worlds;
import net.thenextlvl.worlds.command.CustomExceptionHandler;
import net.thenextlvl.worlds.command.CustomSyntaxFormatter;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class WorldCommand {
    private static final Worlds plugin = JavaPlugin.getPlugin(Worlds.class);

    static final CommandConfirmationManager<CommandSender> confirmationManager = new CommandConfirmationManager<>(
            30, TimeUnit.SECONDS,
            context -> plugin.bundle().sendMessage(context.getCommandContext().getSender(), "command.confirmation",
                    Placeholder.parsed("action", "/" + context.getCommandContext().getRawInputJoined()),
                    Placeholder.parsed("confirmation", "/world confirm"),
                    Placeholder.parsed("time", String.valueOf(30))),
            sender -> plugin.bundle().sendMessage(sender, "command.confirmation.pending"));

    public static void register() throws Exception {
        var manager = new PaperCommandManager<>(plugin, CommandExecutionCoordinator.simpleCoordinator(), Function.identity(), Function.identity());
        CustomExceptionHandler.INSTANCE.apply(manager, sender -> sender);
        manager.commandSyntaxFormatter(new CustomSyntaxFormatter<>());
        confirmationManager.registerConfirmationProcessor(manager);
        manager.registerAsynchronousCompletions();
        manager.registerBrigadier();
        var builder = manager.commandBuilder("world");
        manager.command(WorldSetSpawnCommand.create(builder));
        manager.command(WorldTeleportCommand.create(builder));
        manager.command(WorldConfirmCommand.create(builder));
        manager.command(WorldCreateCommand.create(builder));
        manager.command(WorldImportCommand.create(builder));
        manager.command(WorldDeleteCommand.create(builder));
        manager.command(WorldExportCommand.create(builder));
        manager.command(WorldInfoCommand.create(builder));
        manager.command(WorldListCommand.create(builder));
    }
}
