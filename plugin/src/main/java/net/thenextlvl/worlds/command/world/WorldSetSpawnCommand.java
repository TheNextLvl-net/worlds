package net.thenextlvl.worlds.command.world;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.flags.CommandFlag;
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
                .flag(CommandFlag.builder("first-join"))
                .flag(CommandFlag.builder("on-join"))
                .handler(WorldSetSpawnCommand::execute);
    }

    private static void execute(CommandContext<CommandSender> context) {
        var player = (Player) context.getSender();
        var location = context.contains("position") ? context.<Location>get("position") : player.getLocation();
        var angle = context.contains("angle") ? context.<Float>get("angle") : player.getLocation().getYaw();
        player.getWorld().setSpawnLocation(location.getBlockX(), location.getBlockY(), location.getBlockZ(), angle);
        var firstJoinWorld = context.flags().contains("first-join");
        var joinWorld = context.flags().contains("on-join");
        assert plugin.configFile() != null;
        if (firstJoinWorld) plugin.configFile().getRoot().setFirstJoinLocation(location);
        if (joinWorld) plugin.configFile().getRoot().setJoinLocation(location);
        if (firstJoinWorld || joinWorld) plugin.configFile().save();
        setSpawn(player, location, angle);
        var message = joinWorld ? "world.spawn.set.join"
                : firstJoinWorld ? "world.spawn.set.first"
                : "world.spawn.set";
        plugin.bundle().sendMessage(player, message,
                Placeholder.parsed("x", String.valueOf(location.getBlockX())),
                Placeholder.parsed("y", String.valueOf(location.getBlockY())),
                Placeholder.parsed("z", String.valueOf(location.getBlockZ())),
                Placeholder.parsed("angle", String.valueOf(angle)));
    }

    private static void setSpawn(Player player, Location location, float angle) {
        player.teleportAsync(player.getWorld().getSpawnLocation().add(0.5, 0, 0.5), COMMAND);
    }
}
