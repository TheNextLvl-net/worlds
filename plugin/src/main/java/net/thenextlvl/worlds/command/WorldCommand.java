package net.thenextlvl.worlds.command;

import com.google.gson.JsonParseException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.Worlds;
import org.incendo.cloud.bukkit.parser.PlayerParser;
import org.incendo.cloud.bukkit.parser.WorldParser;
import org.incendo.cloud.exception.ArgumentParseException;
import org.incendo.cloud.exception.InvalidCommandSenderException;
import org.incendo.cloud.exception.InvalidSyntaxException;
import org.incendo.cloud.exception.NoPermissionException;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.minecraft.extras.MinecraftExceptionHandler;
import org.incendo.cloud.paper.PaperCommandManager;

@SuppressWarnings("UnstableApiUsage")
public class WorldCommand {
    private final PaperCommandManager<CommandSourceStack> commandManager;
    private final Worlds plugin;

    public WorldCommand(Worlds plugin) {
        this.commandManager = PaperCommandManager.builder()
                .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
                .buildOnEnable(plugin);
        MinecraftExceptionHandler.create(CommandSourceStack::getSender)
                .handler(InvalidSyntaxException.class, (formatter, context) -> {
                    var syntax = context.exception().correctSyntax()
                            .replace("[", "<dark_gray>[<gold>").replace("]", "<dark_gray>]")
                            .replace("(", "<dark_gray>(<gold>").replace(")", "<dark_gray>)")
                            .replace("|", "<dark_gray>|<red>").replace("--", "<red>--");
                    return plugin.bundle().deserialize("<prefix> <red>/" + syntax);
                })
                .handler(InvalidCommandSenderException.class, (formatter, context) ->
                        plugin.bundle().component(context.context().sender().getSender(), "command.sender"))
                .handler(NoPermissionException.class, (formatter, context) ->
                        plugin.bundle().component(context.context().sender().getSender(), "command.permission",
                                Placeholder.parsed("permission", context.exception().missingPermission().permissionString())))
                .handler(ArgumentParseException.class, (formatter, context) ->
                        plugin.bundle().component(context.context().sender().getSender(), "command.argument"))
                .handler(PlayerParser.PlayerParseException.class, (formatter, context) ->
                        plugin.bundle().component(context.context().sender().getSender(), "player.unknown",
                                Placeholder.parsed("player", context.exception().input())))
                .handler(WorldParser.WorldParseException.class, (formatter, context) ->
                        plugin.bundle().component(context.context().sender().getSender(), "world.unknown",
                                Placeholder.parsed("world", context.exception().input())))
                .handler(JsonParseException.class, (formatter, context) ->
                        plugin.bundle().component(context.context().sender().getSender(), "world.preset.invalid"))
                .defaultCommandExecutionHandler()
                .registerTo(commandManager);
        commandManager.commandSyntaxFormatter(new CustomSyntaxFormatter<>(commandManager));
        this.plugin = plugin;
    }

    public void register() {
        var world = commandManager.commandBuilder("world");
        commandManager.command(new WorldCreateCommand(plugin, world).create());
        commandManager.command(new WorldDeleteCommand(plugin, world).create());
        commandManager.command(new WorldExportCommand(plugin, world).create());
        commandManager.command(new WorldImportCommand(plugin, world).create());
        commandManager.command(new WorldInfoCommand(plugin, world).create());
        commandManager.command(new WorldLinkCommand.Create(plugin, world).create());
        commandManager.command(new WorldLinkCommand.Delete(plugin, world).create());
        commandManager.command(new WorldLinkCommand.List(plugin, world).create());
        commandManager.command(new WorldListCommand(plugin, world).create());
        commandManager.command(new WorldSetSpawnCommand(plugin, world).create());
        commandManager.command(new WorldTeleportCommand(plugin, world).create());
    }
}
