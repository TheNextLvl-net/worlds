package net.thenextlvl.worlds.command;

import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import core.annotation.FieldsAreNotNullByDefault;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.Worlds;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import static cloud.commandframework.minecraft.extras.MinecraftExceptionHandler.ExceptionType.*;

@FieldsAreNotNullByDefault
public class CustomExceptionHandler {
    private static final Worlds plugin = JavaPlugin.getPlugin(Worlds.class);

    public static final MinecraftExceptionHandler<CommandSender> INSTANCE = new MinecraftExceptionHandler<CommandSender>()
            .withHandler(INVALID_SYNTAX, e -> {
                var syntax = ((InvalidSyntaxException) e).getCorrectSyntax()
                        .replace("[", "<dark_gray>[<gold>").replace("]", "<dark_gray>]")
                        .replace("(", "<dark_gray>(<gold>").replace(")", "<dark_gray>)")
                        .replace("|", "<dark_gray>|<red>").replace("--", "<red>--");
                return plugin.bundle().deserialize("<prefix> <red>/" + syntax);
            })
            .withHandler(INVALID_SENDER, (sender, exception) ->
                    plugin.bundle().component(sender, "command.sender"))
            .withHandler(NO_PERMISSION, (sender, exception) ->
                    plugin.bundle().component(sender, "command.permission", Placeholder.parsed("permission",
                            ((NoPermissionException) exception).getMissingPermission())))
            .withHandler(ARGUMENT_PARSING, (sender, exception) ->
                    plugin.bundle().component(sender, "command.argument"))
            .withCommandExecutionHandler();
}
