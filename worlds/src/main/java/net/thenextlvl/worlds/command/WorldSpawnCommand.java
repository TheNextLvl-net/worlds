package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND;

@NullMarked
class WorldSpawnCommand {
    private final WorldsPlugin plugin;

    WorldSpawnCommand(WorldsPlugin plugin) {
        this.plugin = plugin;
    }

    ArgumentBuilder<CommandSourceStack, ?> create() {
        return Commands.literal("spawn")
                .requires(source -> source.getSender().hasPermission("worlds.command.spawn")
                                    && source.getSender() instanceof Player)
                .executes(context -> {
                    var player = (Player) context.getSource().getSender();
                    player.teleportAsync(player.getWorld().getSpawnLocation(), COMMAND);
                    plugin.bundle().sendMessage(player, "world.teleport.self",
                            Placeholder.parsed("world", player.getWorld().getName()));
                    return Command.SINGLE_SUCCESS;
                });
    }
}
