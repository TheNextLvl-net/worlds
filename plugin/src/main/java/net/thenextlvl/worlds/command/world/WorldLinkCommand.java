package net.thenextlvl.worlds.command.world;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.paper.PaperCommandManager;
import net.thenextlvl.worlds.Worlds;
import net.thenextlvl.worlds.link.Link;
import net.thenextlvl.worlds.link.PortalType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

class WorldLinkCommand {
    private static final Worlds plugin = JavaPlugin.getPlugin(Worlds.class);

    static void register(PaperCommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        manager.command(remove(builder));
        manager.command(create(builder));
        manager.command(list(builder));
    }

    private static Command.Builder<CommandSender> remove(Command.Builder<CommandSender> manager) {
        return manager
                .literal("remove")
                .argument(StringArgument.<CommandSender>builder("link")
                        .withSuggestionsProvider((context, token) -> plugin.linkFile().links().keySet().stream()
                                .filter(s -> s.startsWith(token))
                                .toList())
                        .build())
                .handler(WorldLinkCommand::executeRemove);
    }

    private static Command.Builder<CommandSender> create(Command.Builder<CommandSender> manager) {
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
                .argument(StringArgument.of("identifier"))
                .handler(WorldLinkCommand::executeCreate);
    }

    private static Command.Builder<CommandSender> list(Command.Builder<CommandSender> manager) {
        return manager
                .literal("list")
                .handler(WorldLinkCommand::executeList);
    }

    private static Command.Builder<CommandSender> list(Command.Builder<CommandSender> manager) {
        return manager.literal("list").handler(WorldLinkCommand::executeList);
    }

    private static void executeRemove(CommandContext<CommandSender> context) {
    }

    private static void executeCreate(CommandContext<CommandSender> context) {
    }

    private static void executeList(CommandContext<CommandSender> context) {
        var links = plugin.linkFile().links().keySet();
        var sender = context.getSender();
        var locale = sender instanceof Player player ? player.locale() : Messages.ENGLISH;
        if (links.isEmpty()) sender.sendRichMessage(Messages.WORLD_NO_LINKS.message(locale, sender));
        else sender.sendRichMessage(Messages.WORLD_LINK_LIST.message(locale, sender,
                Placeholder.of("links", String.join(", ", links)),
                Placeholder.of("amount", links.size())));
    }
}
