package net.thenextlvl.worlds.command.world;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import core.api.placeholder.Placeholder;
import net.kyori.adventure.audience.Audience;
import net.thenextlvl.worlds.util.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;

class WorldExportCommand {

    static Command.Builder<CommandSender> create(Command.Builder<CommandSender> builder) {
        return builder.literal("export")
                .argument(StringArgument.<CommandSender>builder("world")
                        .withSuggestionsProvider((context, token) -> Bukkit.getWorlds().stream()
                                .map(WorldInfo::getName)
                                .filter(s -> s.startsWith(token))
                                .toList())
                        .build())
                .handler(WorldExportCommand::execute);
    }

    private static void execute(CommandContext<CommandSender> context) {
        var name = context.<String>get("world");
        var world = Bukkit.getWorld(name);
        var sender = context.getSender();
        var placeholder = Placeholder.<Audience>of("world", world != null ? world.getName() : name);
        var locale = sender instanceof Player player ? player.locale() : Messages.ENGLISH;
        if (world == null) {
            sender.sendRichMessage(Messages.WORLD_NOT_FOUND.message(locale, sender, placeholder));
        } else try {
            world.save();
            sender.sendMessage(Messages.WORLD_SAVE_SUCCEEDED.message(locale, sender, placeholder));
        } catch (Exception e) {
            sender.sendMessage(Messages.WORLD_SAVE_FAILED.message(locale, sender, placeholder));
        }
    }
}
