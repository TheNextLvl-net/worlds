package net.thenextlvl.worlds.command.world;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.Worlds;
import net.thenextlvl.worlds.image.Image;
import net.thenextlvl.worlds.image.WorldImage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND;

class WorldImportCommand {
    private static final Worlds plugin = JavaPlugin.getPlugin(Worlds.class);

    static Command.Builder<CommandSender> create(Command.Builder<CommandSender> builder) {
        return builder.literal("import")
                .permission("worlds.command.world.import")
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
            plugin.bundle().sendMessage(sender, "image.exists.not", Placeholder.parsed("image", imageName));
            return;
        }
        var world = Bukkit.getWorld(image.name());
        var placeholder = Placeholder.parsed("world", world != null ? world.getName() : image.name());
        if (world != null) {
            plugin.bundle().sendMessage(sender, "world.exists", placeholder);
            return;
        }
        var result = Image.load(image);
        var message = result != null ? "world.import.success" : "world.import.failed";
        plugin.bundle().sendMessage(sender, message, placeholder);
        if (result == null || !(sender instanceof Player player)) return;
        player.teleportAsync(result.getWorld().getSpawnLocation().add(0.5, 0, 0.5), COMMAND);
    }
}
