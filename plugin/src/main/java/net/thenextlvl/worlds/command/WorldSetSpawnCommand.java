package net.thenextlvl.worlds.command;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.Worlds;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.bukkit.parser.location.LocationParser;
import org.incendo.cloud.component.DefaultValue;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.FloatParser;

import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND;

@RequiredArgsConstructor
class WorldSetSpawnCommand {
    private final Worlds plugin;
    private final Command.Builder<CommandSender> builder;

    Command.Builder<Player> create() {
        return builder.literal("setspawn")
                .permission("worlds.command.world.setspawn")
                .senderType(Player.class)
                .optional("position", LocationParser.locationParser(),
                        DefaultValue.dynamic(context -> context.sender().getLocation()))
                .optional("angle", FloatParser.floatParser(-360, 360),
                        DefaultValue.dynamic(context -> context.sender().getYaw()))
                .handler(this::execute);
    }

    private void execute(CommandContext<Player> context) {
        var location = context.<Location>get("position");
        float angle = context.<Float>get("angle");

        var success = context.sender().getWorld().setSpawnLocation(
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(),
                angle
        );
        if (success) context.sender().teleportAsync(context.sender().getWorld().getSpawnLocation(), COMMAND);

        var message = success ? "world.spawn.set.success" : "world.spawn.set.failed";
        plugin.bundle().sendMessage(context.sender(), message,
                Placeholder.parsed("x", String.valueOf(location.getBlockX())),
                Placeholder.parsed("y", String.valueOf(location.getBlockY())),
                Placeholder.parsed("z", String.valueOf(location.getBlockZ())),
                Placeholder.parsed("angle", String.valueOf(angle)));
    }
}
