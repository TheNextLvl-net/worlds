package net.thenextlvl.worlds.command.link;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.Worlds;
import net.thenextlvl.worlds.link.Link;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

class LinkListCommand {
    private static final Worlds plugin = JavaPlugin.getPlugin(Worlds.class);

    static Command.Builder<CommandSender> create(Command.Builder<CommandSender> manager) {
        return manager.literal("list")
                .permission("worlds.command.link.list")
                .handler(LinkListCommand::execute);
    }

    private static void execute(CommandContext<CommandSender> context) {
        var links = plugin.linkFile().links().stream().map(Link::toString).toList();
        if (links.isEmpty()) plugin.bundle().sendMessage(context.getSender(), "link.list.empty");
        else plugin.bundle().sendMessage(context.getSender(), "link.list",
                Placeholder.parsed("links", String.join(", ", links)),
                Placeholder.parsed("amount", String.valueOf(links.size())));
    }
}
