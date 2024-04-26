package net.thenextlvl.worlds.command.world;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.Worlds;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.incendo.cloud.Command;
import org.incendo.cloud.bukkit.parser.PlayerParser;
import org.incendo.cloud.bukkit.parser.WorldParser;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.exception.InvalidSyntaxException;

import java.util.List;

@RequiredArgsConstructor
class WorldTeleportCommand {
    private final Worlds plugin;
    private final Command.Builder<CommandSender> builder;

    Command.Builder<CommandSender> create() {
        return builder.literal("teleport", "tp")
                .permission("worlds.command.world.teleport")
                .required("world", WorldParser.worldParser())
                .optional("player", PlayerParser.playerParser())
                .handler(this::execute);
    }

    private void execute(CommandContext<CommandSender> context) {
        var sender = context.sender();
        var world = context.<World>get("world");
        var player = context.<Player>optional("player").orElse(sender instanceof Player self ? self : null);
        if (player == null) throw new InvalidSyntaxException("world teleport [world] [player]", sender, List.of());
        player.teleportAsync(world.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
        var placeholder = Placeholder.parsed("world", world.getName());
        if (!player.equals(sender)) plugin.bundle().sendMessage(sender, "world.teleport.player.other", placeholder);
        plugin.bundle().sendMessage(player, "world.teleport.player.self", placeholder);
    }
}
