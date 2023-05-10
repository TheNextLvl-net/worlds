package net.thenextlvl.worlds.command.world;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import core.api.placeholder.Placeholder;
import net.kyori.adventure.audience.Audience;
import net.thenextlvl.worlds.util.Messages;
import net.thenextlvl.worlds.image.Image;
import net.thenextlvl.worlds.image.WorldImage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

class WorldImportCommand {

    static Command.Builder<CommandSender> create(Command.Builder<CommandSender> builder) {
        return builder.literal("import")
                .argument(StringArgument.<CommandSender>builder("image")
                        .withSuggestionsProvider((context, token) -> Image.findImages().stream()
                                .map(WorldImage::name)
                                .filter(name -> Bukkit.getWorld(name) == null)
                                .filter(s -> s.startsWith(token))
                                .toList())
                        .greedy().build())
                .handler(WorldImportCommand::execute);
    }

    private static void execute(CommandContext<CommandSender> context) {
        var sender = context.getSender();
        var locale = sender instanceof Player player ? player.locale() : Messages.ENGLISH;

        var imageName = context.<String>get("image");
        var image = Image.findImageFiles().stream()
                .filter(file -> {
                    var worldImage = WorldImage.of(file);
                    return worldImage != null && worldImage.name().equals(imageName);
                })
                .findFirst()
                .map(WorldImage::of)
                .orElse(null);

        if (image == null) {
            sender.sendRichMessage(Messages.IMAGE_NOT_FOUND.message(locale, sender, Placeholder.of("image", imageName)));
            return;
        }
        var world = Bukkit.getWorld(image.name());
        var placeholder = Placeholder.<Audience>of("world", world != null ? world.getName() : image.name());
        if (world != null) {
            sender.sendRichMessage(Messages.WORLD_EXISTS.message(locale, sender, placeholder));
            return;
        }
        var result = Image.load(image);
        var message = result != null ? Messages.WORLD_IMPORT_SUCCEEDED : Messages.WORLD_IMPORT_FAILED;
        sender.sendRichMessage(message.message(locale, sender, placeholder));
        if (result == null || !(sender instanceof Player player)) return;
        player.teleportAsync(result.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
    }
}
