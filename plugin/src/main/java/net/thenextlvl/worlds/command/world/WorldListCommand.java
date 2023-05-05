package net.thenextlvl.worlds.command.world;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import core.api.placeholder.Placeholder;
import net.thenextlvl.worlds.util.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;

class WorldListCommand {

    static Command.Builder<CommandSender> create(Command.Builder<CommandSender> builder) {
        return builder.literal("list").handler(WorldListCommand::execute);
    }

    private static void execute(CommandContext<CommandSender> context) {
        var towns = Bukkit.getWorlds().stream().map(WorldInfo::getName).toList();
        var sender = context.getSender();
        var locale = sender instanceof Player player ? player.locale() : Messages.ENGLISH;
        sender.sendRichMessage(Messages.WORLD_LIST.message(locale, sender,
                Placeholder.of("amount", towns.size()), Placeholder.of("towns", String.join(", ", towns))));
    }
}
