package net.thenextlvl.worlds.command.world;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.FloatArgument;
import cloud.commandframework.bukkit.parsers.location.LocationArgument;
import cloud.commandframework.context.CommandContext;
import core.api.placeholder.Placeholder;
import net.thenextlvl.worlds.util.Messages;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND;

class WorldSetSpawnCommand {

    static Command.Builder<CommandSender> create(Command.Builder<CommandSender> builder) {
        return builder.literal("setspawn")
                .permission("worlds.command.world.setspawn")
                .senderType(Player.class)
                .argument(LocationArgument.optional("position"))
                .argument(FloatArgument.optional("angle"))
                .handler(WorldSetSpawnCommand::execute);
    }

    private static void execute(CommandContext<CommandSender> context) {
        var player = (Player) context.getSender();
        var location = context.contains("position") ? context.<Location>get("position") : player.getLocation();
        var angle = context.contains("angle") ? context.<Float>get("angle") : player.getLocation().getYaw();
        player.getWorld().setSpawnLocation(location.getBlockX(), location.getBlockY(), location.getBlockZ(), angle);
        player.sendRichMessage(Messages.WORLD_SPAWN_SET.message(player.locale(), player,
                Placeholder.of("x", location.getBlockX()), Placeholder.of("y", location.getBlockY()),
                Placeholder.of("z", location.getBlockZ()), Placeholder.of("angle", angle)));
        player.teleportAsync(player.getWorld().getSpawnLocation().add(0.5, 0, 0.5), COMMAND);
    }
}
