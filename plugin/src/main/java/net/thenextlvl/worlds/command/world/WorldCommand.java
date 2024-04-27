package net.thenextlvl.worlds.command.world;

import com.google.gson.JsonParseException;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.Worlds;
import net.thenextlvl.worlds.command.CustomSyntaxFormatter;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.bukkit.parser.PlayerParser;
import org.incendo.cloud.bukkit.parser.WorldParser;
import org.incendo.cloud.exception.ArgumentParseException;
import org.incendo.cloud.exception.InvalidCommandSenderException;
import org.incendo.cloud.exception.InvalidSyntaxException;
import org.incendo.cloud.exception.NoPermissionException;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.minecraft.extras.MinecraftExceptionHandler;
import org.incendo.cloud.paper.PaperCommandManager;

public class WorldCommand extends PaperCommandManager<CommandSender> {
    private final Worlds plugin;

    public WorldCommand(Worlds plugin) {
        super(plugin, ExecutionCoordinator.simpleCoordinator(), SenderMapper.identity());
        MinecraftExceptionHandler.<CommandSender>createNative()
                .handler(InvalidSyntaxException.class, (formatter, context) -> {
                    var syntax = context.exception().correctSyntax()
                            .replace("[", "<dark_gray>[<gold>").replace("]", "<dark_gray>]")
                            .replace("(", "<dark_gray>(<gold>").replace(")", "<dark_gray>)")
                            .replace("|", "<dark_gray>|<red>").replace("--", "<red>--");
                    return plugin.bundle().deserialize("<prefix> <red>/" + syntax);
                })
                .handler(InvalidCommandSenderException.class, (formatter, context) ->
                        plugin.bundle().component(context.context().sender(), "command.sender"))
                .handler(NoPermissionException.class, (formatter, context) ->
                        plugin.bundle().component(context.context().sender(), "command.permission",
                                Placeholder.parsed("permission", context.exception().missingPermission().permissionString())))
                .handler(ArgumentParseException.class, (formatter, context) -> {
                    context.exception().printStackTrace();
                    return plugin.bundle().component(context.context().sender(), "command.argument");
                })
                .handler(PlayerParser.PlayerParseException.class, (formatter, context) ->
                        plugin.bundle().component(context.context().sender(), "player.unknown",
                                Placeholder.parsed("player", context.exception().input())))
                .handler(WorldParser.WorldParseException.class, (formatter, context) ->
                        plugin.bundle().component(context.context().sender(), "world.unknown",
                                Placeholder.parsed("world", context.exception().input())))
                .handler(JsonParseException.class, (formatter, context) ->
                        plugin.bundle().component(context.context().sender(), "world.preset.invalid"))
                .defaultCommandExecutionHandler()
                .registerTo(this);
        commandSyntaxFormatter(new CustomSyntaxFormatter<>(this));
        registerAsynchronousCompletions();
        registerBrigadier();
        this.plugin = plugin;
    }

    public void register() {
        var world = commandBuilder("world");
        command(new WorldCreateCommand(plugin, world).create());
        command(new WorldDeleteCommand(plugin, world).create());
        command(new WorldExportCommand(plugin, world).create());
        command(new WorldImportCommand(plugin, world).create());
        command(new WorldInfoCommand(plugin, world).create());
        command(new WorldLinkCommand.Create(plugin, world).create());
        command(new WorldLinkCommand.Delete(plugin, world).create());
        command(new WorldLinkCommand.List(plugin, world).create());
        command(new WorldListCommand(plugin, world).create());
        command(new WorldSetSpawnCommand(plugin, world).create());
        command(new WorldTeleportCommand(plugin, world).create());
    }
}
