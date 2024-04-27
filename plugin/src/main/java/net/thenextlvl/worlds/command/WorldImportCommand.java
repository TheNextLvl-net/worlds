package net.thenextlvl.worlds.command;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.Worlds;
import net.thenextlvl.worlds.image.WorldImage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.incendo.cloud.Command;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

@RequiredArgsConstructor
class WorldImportCommand {
    private final Worlds plugin;
    private final Command.Builder<CommandSender> builder;

    Command.Builder<CommandSender> create() {
        return builder.literal("import")
                .permission("worlds.command.world.import")
                .required("image", StringParser.greedyStringParser(),
                        SuggestionProvider.blocking((context, input) -> plugin.imageProvider().findImages().stream()
                                .map(WorldImage::name)
                                .filter(name -> Bukkit.getWorld(name) == null)
                                .map(Suggestion::simple)
                                .toList()))
                .handler(this::execute);
    }

    private void execute(CommandContext<CommandSender> context) {
        var imageName = context.<String>get("image");
        var image = plugin.imageProvider().findImageFiles().stream()
                .filter(file -> {
                    var worldImage = plugin.imageProvider().of(file);
                    return worldImage != null && worldImage.name().equals(imageName);
                })
                .findFirst()
                .map(plugin.imageProvider()::of)
                .orElse(null);

        if (image == null) {
            plugin.bundle().sendMessage(context.sender(), "image.exists.not", Placeholder.parsed("image", imageName));
            return;
        }

        var world = Bukkit.getWorld(image.name());
        var placeholder = Placeholder.parsed("world", world != null ? world.getName() : image.name());
        if (world != null) {
            plugin.bundle().sendMessage(context.sender(), "world.known", placeholder);
            return;
        }

        var result = plugin.imageProvider().load(image);
        var message = result != null ? "world.import.success" : "world.import.failed";
        plugin.bundle().sendMessage(context.sender(), message, placeholder);
        if (result == null || !(context.sender() instanceof Player player)) return;
        player.teleportAsync(result.getWorld().getSpawnLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
    }
}
