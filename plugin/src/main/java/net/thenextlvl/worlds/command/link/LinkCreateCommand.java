package net.thenextlvl.worlds.command.link;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import net.thenextlvl.worlds.Worlds;
import net.thenextlvl.worlds.link.PortalType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
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
                .argument(StringArgument.of("identifier"))
                .handler(LinkCreateCommand::execute);
    }

    private static void execute(CommandContext<CommandSender> context) {
    }
}
