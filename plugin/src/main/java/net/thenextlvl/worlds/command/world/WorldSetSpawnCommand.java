package net.thenextlvl.worlds.command.world;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.FloatArgument;
import cloud.commandframework.bukkit.parsers.location.LocationArgument;
import cloud.commandframework.context.CommandContext;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.Worlds;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND;

class WorldSetSpawnCommand {
    private static final Worlds plugin = JavaPlugin.getPlugin(Worlds.class);

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
        plugin.bundle().sendMessage(player, "world.spawn.set",
                Placeholder.parsed("x", String.valueOf(location.getBlockX())),
                Placeholder.parsed("y", String.valueOf(location.getBlockY())),
                Placeholder.parsed("z", String.valueOf(location.getBlockZ())),
                Placeholder.parsed("angle", String.valueOf(angle)));
        player.teleportAsync(player.getWorld().getSpawnLocation().add(0.5, 0, 0.5), COMMAND);
    }
}
