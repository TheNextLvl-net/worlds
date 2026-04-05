package net.thenextlvl.worlds.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.brigadier.SimpleCommand;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND;

@NullMarked
final class WorldSpawnCommand extends SimpleCommand {
    private WorldSpawnCommand(final WorldsPlugin plugin) {
        super(plugin, "spawn", "worlds.command.spawn");
    }

    public static ArgumentBuilder<CommandSourceStack, ?> create(final WorldsPlugin plugin) {
        final var command = new WorldSpawnCommand(plugin);
        return command.create()
                .requires(source -> source.getSender() instanceof Player && command.canUse(source))
                .executes(command);
    }

    @Override
    public int run(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final var player = (Player) context.getSource().getSender();
        player.teleportAsync(player.getWorld().getSpawnLocation(), COMMAND).thenAccept(success -> {
            final var message = success ? "world.teleport.self" : "world.teleport.failed";
            plugin.bundle().sendMessage(player, message, Placeholder.parsed("world", player.getWorld().getName()));
        });
        return SINGLE_SUCCESS;
    }
}
