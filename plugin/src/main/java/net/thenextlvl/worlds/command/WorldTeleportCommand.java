package net.thenextlvl.worlds.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.Worlds;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.incendo.cloud.Command;
import org.incendo.cloud.bukkit.parser.PlayerParser;
import org.incendo.cloud.bukkit.parser.WorldParser;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.exception.InvalidSyntaxException;

import java.util.List;

@RequiredArgsConstructor
@SuppressWarnings("UnstableApiUsage")
class WorldTeleportCommand {
    private final Worlds plugin;
    private final Command.Builder<CommandSourceStack> builder;

    Command.Builder<CommandSourceStack> create() {
        return builder.literal("teleport", "tp")
                .permission("worlds.command.world.teleport")
                .required("world", WorldParser.worldParser())
                .optional("player", PlayerParser.playerParser())
                .handler(this::execute);
    }

    private void execute(CommandContext<CommandSourceStack> context) {
        var sender = context.sender().getSender();
        var world = context.<World>get("world");
        var player = context.<Player>optional("player").orElse(sender instanceof Player self ? self : null);
        if (player == null) throw new InvalidSyntaxException("world teleport [world] [player]", sender, List.of());
        player.teleportAsync(world.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
        var message = player.equals(sender) ? "world.teleport.player.self" : "world.teleport.player.other";
        plugin.bundle().sendMessage(sender, message, Placeholder.parsed("world", world.getName()),
                Placeholder.parsed("player", player.getName()));
    }
}
