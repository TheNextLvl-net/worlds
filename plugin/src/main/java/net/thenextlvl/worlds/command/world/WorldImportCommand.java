package net.thenextlvl.worlds.command.world;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import core.api.placeholder.Placeholder;
import net.kyori.adventure.audience.Audience;
import net.thenextlvl.worlds.util.Messages;
import net.thenextlvl.worlds.volume.Volume;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.io.File;

class WorldImportCommand {

    static Command.Builder<CommandSender> create(Command.Builder<CommandSender> builder) {
        return builder.literal("import")
                .argument(StringArgument.<CommandSender>builder("file")
                        .withSuggestionsProvider((context, token) -> Volume.findVolumes().stream()
                                .map(File::getParentFile)
                                .map(File::getName)
                                .filter(file -> Bukkit.getWorld(file) == null)
                                .filter(s -> s.startsWith(token))
                                .toList())
                        .build())
                .handler(WorldImportCommand::execute);
    }

    private static void execute(CommandContext<CommandSender> context) {
        var file = new File(Bukkit.getWorldContainer(), new File(context.<String>get("file")).getName());
        var sender = context.getSender();
        var locale = sender instanceof Player player ? player.locale() : Messages.ENGLISH;
        var world = Bukkit.getWorld(file.getName());
        var placeholder = Placeholder.<Audience>of("world", world != null ? world.getName() : file.getName());
        if (world != null) {
            sender.sendRichMessage(Messages.WORLD_EXISTS.message(locale, sender, placeholder));
            return;
        }
        var volume = new File(file, ".volume");
        if (!volume.isFile()) {
            sender.sendRichMessage(Messages.VOLUME_NOT_FOUND.message(locale, sender,
                    Placeholder.of("directory", file.getName())));
            return;
        }
        var result = Volume.load(volume);
        var message = result != null ? Messages.WORLD_IMPORT_SUCCEEDED : Messages.WORLD_IMPORT_FAILED;
        sender.sendRichMessage(message.message(locale, sender, placeholder));
        if (result == null || !(sender instanceof Player player)) return;
        player.teleportAsync(result.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
    }
}
