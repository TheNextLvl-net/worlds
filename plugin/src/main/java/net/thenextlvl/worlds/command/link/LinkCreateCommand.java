package net.thenextlvl.worlds.command.link;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import core.api.placeholder.Placeholder;
import net.thenextlvl.worlds.Worlds;
import net.thenextlvl.worlds.link.Link;
import net.thenextlvl.worlds.link.PortalType;
import net.thenextlvl.worlds.util.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

class LinkCreateCommand {
    private static final Worlds plugin = JavaPlugin.getPlugin(Worlds.class);

    static Command.Builder<CommandSender> create(Command.Builder<CommandSender> manager) {
        return manager
                .literal("create")
                .argument(StringArgument.<CommandSender>builder("source")
                        .withSuggestionsProvider((context, token) -> Bukkit.getWorlds().stream()
                                .map(WorldInfo::getName)
                                .filter(s -> s.startsWith(token))
                                .toList())
                        .build())
                .argument(StringArgument.<CommandSender>builder("destination")
                        .withSuggestionsProvider((context, token) -> Bukkit.getWorlds().stream()
                                .map(WorldInfo::getName)
                                .filter(s -> s.startsWith(token))
                                .filter(s -> !s.equals(context.get("source")))
                                .toList())
                        .build())
                .argument(StringArgument.<CommandSender>builder("portal-type")
                        .withSuggestionsProvider((context, token) -> Arrays.stream(PortalType.values())
                                .map(type -> type.name().toLowerCase().replace("_", "-"))
                                .filter(s -> s.startsWith(token))
                                .toList())
                        .build())
                .handler(LinkCreateCommand::execute);
    }

    private static void execute(CommandContext<CommandSender> context) {
        try {
            handleCreate(context);
        } catch (Exception e) {
            var sender = context.getSender();
            var locale = sender instanceof Player player ? player.locale() : Messages.ENGLISH;
            sender.sendRichMessage(Messages.INVALID_ARGUMENT.message(locale, sender));
        }
    }

    private static void handleCreate(CommandContext<CommandSender> context) {
        var sender = context.getSender();
        var source = context.<String>get("source");
        var destination = context.<String>get("destination");
        var portalType = PortalType.valueOf(context.<String>get("portal-type").toUpperCase().replace("-", "_"));
        var link = new Link(portalType, source, destination);
        var locale = sender instanceof Player player ? player.locale() : Messages.ENGLISH;
        if (plugin.linkFile().links().contains(link)) {
            sender.sendRichMessage(Messages.LINK_EXISTS.message(locale, sender,
                    Placeholder.of("link", () -> link.portalType().name().toLowerCase().replace("_", "-")
                            + ": " + link.first() + " -> " + link.second())));
        } else {
            sender.sendRichMessage(Messages.LINK_CREATED.message(locale, sender,
                    Placeholder.of("link", link)));
            plugin.linkFile().links().add(link);
        }
    }
}
