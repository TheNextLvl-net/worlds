package net.thenextlvl.worlds.command.world;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import core.api.placeholder.Placeholder;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import worlds.util.Messages;

class WorldCreateCommand {

    static Command.Builder<CommandSender> create(Command.Builder<CommandSender> builder) {
        return builder
                .literal("create")
                .argument(StringArgument.of("name"))
                .handler(WorldCreateCommand::execute);
    }

    private static void execute(CommandContext<CommandSender> context) {
        var name = context.<String>get("name");
        var sender = context.getSender();
        var placeholder = Placeholder.<Audience>of("world", name);
        var locale = sender instanceof Player player ? player.locale() : Messages.ENGLISH;
        // TODO: 04.05.23 do stuff
    }
}
