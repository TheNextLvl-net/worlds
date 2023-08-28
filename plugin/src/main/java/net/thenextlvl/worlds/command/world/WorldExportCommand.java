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
                                .filter(name -> name.startsWith(token))
                                .toList())
                        .asOptional())
                .handler(WorldExportCommand::execute);
    }

    @SuppressWarnings("CallToPrintStackTrace")
    private static void execute(CommandContext<CommandSender> context) {
        var sender = context.getSender();
        var input = context.<String>getOptional("world");
        var world = input.map(Bukkit::getWorld).orElse(sender instanceof Player player ? player.getWorld() : null);
        var locale = sender instanceof Player player ? player.locale() : Messages.ENGLISH;
        if (world != null) {
            var placeholder = Placeholder.<Audience>of("world", world.getName());
            try {
                world.save();
                sender.sendRichMessage(Messages.WORLD_SAVE_SUCCEEDED.message(locale, sender, placeholder));
            } catch (Exception e) {
                sender.sendRichMessage(Messages.WORLD_SAVE_FAILED.message(locale, sender, placeholder));
                e.printStackTrace();
            }
        } else input.ifPresentOrElse(name -> sender.sendRichMessage(Messages.WORLD_NOT_FOUND
                        .message(locale, sender, Placeholder.of("world", name))),
                () -> sender.sendRichMessage(Messages.ENTER_WORLD_NAME.message(locale, sender)));
    }
}
