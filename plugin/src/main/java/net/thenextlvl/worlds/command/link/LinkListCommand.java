package net.thenextlvl.worlds.command.link;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import core.api.placeholder.Placeholder;
import net.thenextlvl.worlds.Worlds;
import net.thenextlvl.worlds.util.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

class LinkListCommand {
    private static final Worlds plugin = JavaPlugin.getPlugin(Worlds.class);

    private static Command.Builder<CommandSender> list(Command.Builder<CommandSender> manager) {
        return manager.literal("list").handler(LinkListCommand::execute);
    }

    private static void execute(CommandContext<CommandSender> context) {
        var links = plugin.linkFile().links().keySet();
        var sender = context.getSender();
        var locale = sender instanceof Player player ? player.locale() : Messages.ENGLISH;
        if (links.isEmpty()) sender.sendRichMessage(Messages.WORLD_NO_LINKS.message(locale, sender));
        else sender.sendRichMessage(Messages.WORLD_LINK_LIST.message(locale, sender,
                Placeholder.of("links", String.join(", ", links)),
                Placeholder.of("amount", links.size())));
    }
}
