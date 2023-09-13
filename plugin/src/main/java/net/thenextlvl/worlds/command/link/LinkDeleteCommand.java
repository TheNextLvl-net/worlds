package net.thenextlvl.worlds.command.link;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import core.api.placeholder.Placeholder;
import net.thenextlvl.worlds.Worlds;
import net.thenextlvl.worlds.link.Link;
import net.thenextlvl.worlds.util.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

class LinkDeleteCommand {
    private static final Worlds plugin = JavaPlugin.getPlugin(Worlds.class);

    static Command.Builder<CommandSender> create(Command.Builder<CommandSender> manager) {
        return manager.literal("delete")
                .permission("worlds.command.link.delete")
                .argument(StringArgument.<CommandSender>builder("link")
                        .withSuggestionsProvider((context, token) -> plugin.linkFile().links().stream()
                                .map(Link::toString)
                                .filter(s -> s.startsWith(token))
                                .toList())
                        .greedy().build())
                .handler(LinkDeleteCommand::execute);
    }

    private static void execute(CommandContext<CommandSender> context) {
        var sender = context.getSender();
        var linkName = context.<String>get("link");
        var link = plugin.linkFile().links().stream()
                .filter(link1 -> link1.toString().equals(linkName))
                .findFirst()
                .orElse(null);
        var locale = sender instanceof Player player ? player.locale() : Messages.ENGLISH;
        if (link != null) {
            plugin.linkFile().links().remove(link);
            sender.sendRichMessage(Messages.LINK_DELETED.message(locale, sender,
                    Placeholder.of("link", link)));
        } else sender.sendRichMessage(Messages.LINK_NOT_FOUND.message(locale, sender,
                Placeholder.of("link", linkName)));
    }
}
