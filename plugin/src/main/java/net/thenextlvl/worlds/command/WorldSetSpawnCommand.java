package net.thenextlvl.worlds.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.Worlds;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.bukkit.parser.location.LocationParser;
import org.incendo.cloud.component.DefaultValue;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.exception.InvalidCommandSenderException;
import org.incendo.cloud.parser.standard.FloatParser;

import java.util.List;

import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND;

@RequiredArgsConstructor
@SuppressWarnings("UnstableApiUsage")
class WorldSetSpawnCommand {
    private final Worlds plugin;
    private final Command.Builder<CommandSourceStack> builder;

    Command.Builder<CommandSourceStack> create() {
        return builder.literal("setspawn")
                .permission("worlds.command.world.setspawn")
                // .senderType(Player.class)
                .optional("position", LocationParser.locationParser(),
                        DefaultValue.dynamic(context -> context.sender().getLocation()))
                .optional("angle", FloatParser.floatParser(-360, 360),
                        DefaultValue.dynamic(context -> {
                            var executor = context.sender().getExecutor();
                            return executor != null ? executor.getYaw() : 0;
                        }))
                .handler(this::execute);
    }

    private void execute(CommandContext<CommandSourceStack> context) {
        if (!(context.sender().getSender() instanceof Player player))
            throw new InvalidCommandSenderException(context.sender(), Player.class, List.of(), context.command());

        var location = context.<Location>get("position");
        float angle = context.<Float>get("angle");

        var success = player.getWorld().setSpawnLocation(
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(),
                angle
        );
        if (success) player.teleportAsync(player.getWorld().getSpawnLocation(), COMMAND);

        var message = success ? "world.spawn.set.success" : "world.spawn.set.failed";
        plugin.bundle().sendMessage(player, message,
                Placeholder.parsed("x", String.valueOf(location.getBlockX())),
                Placeholder.parsed("y", String.valueOf(location.getBlockY())),
                Placeholder.parsed("z", String.valueOf(location.getBlockZ())),
                Placeholder.parsed("angle", String.valueOf(angle)));
    }
}
