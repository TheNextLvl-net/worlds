package net.thenextlvl.worlds.command.link;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.Worlds;
import net.thenextlvl.worlds.link.Link;
import net.thenextlvl.worlds.link.PortalType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

class LinkCreateCommand {
    private static final Worlds plugin = JavaPlugin.getPlugin(Worlds.class);

    static Command.Builder<CommandSender> create(Command.Builder<CommandSender> manager) {
        return manager.literal("create")
                .permission("worlds.command.link.create")
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
            plugin.bundle().sendMessage(context.getSender(), "command.argument");
        }
    }

    private static void handleCreate(CommandContext<CommandSender> context) {
        var sender = context.getSender();
        var source = context.<String>get("source");
        var destination = context.<String>get("destination");
        var portalType = PortalType.valueOf(context.<String>get("portal-type").toUpperCase().replace("-", "_"));
        var link = new Link(portalType, source, destination);
        if (!plugin.linkFile().links().contains(link)) {
            plugin.bundle().sendMessage(sender, "link.created",
                    Placeholder.parsed("type", link.portalType().toString()),
                    Placeholder.parsed("first", String.valueOf(link.first())),
                    Placeholder.parsed("second", String.valueOf(link.second())));
            plugin.linkFile().links().add(link);
        } else plugin.bundle().sendMessage(sender, "link.exists",
                Placeholder.parsed("type", link.portalType().toString()),
                Placeholder.parsed("first", String.valueOf(link.first())),
                Placeholder.parsed("second", String.valueOf(link.second())));
    }
}
