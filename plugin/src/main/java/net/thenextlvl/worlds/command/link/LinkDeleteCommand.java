package net.thenextlvl.worlds.command.link;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import net.thenextlvl.worlds.Worlds;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

class LinkDeleteCommand {
    private static final Worlds plugin = JavaPlugin.getPlugin(Worlds.class);

    private static Command.Builder<CommandSender> create(Command.Builder<CommandSender> manager) {
        return manager
                .literal("delete")
                .argument(StringArgument.<CommandSender>builder("link")
                        .withSuggestionsProvider((context, token) -> plugin.linkFile().links().keySet().stream()
                                .filter(s -> s.startsWith(token))
                                .toList())
                        .build())
                .handler(LinkDeleteCommand::execute);
    }

    private static void execute(CommandContext<CommandSender> context) {
    }
}
