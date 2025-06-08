package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jspecify.annotations.NullMarked;

import static net.thenextlvl.worlds.command.WorldCommand.keyArgument;
import static net.thenextlvl.worlds.command.WorldCommand.worldArgument;

@NullMarked
class WorldCloneCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> create(WorldsPlugin plugin) {
        return Commands.literal("clone")
                .requires(source -> source.getSender().hasPermission("worlds.command.clone"))
                .then(clone(plugin));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, World> clone(WorldsPlugin plugin) {
        return worldArgument(plugin).then(keyArgument().then(Commands.literal("template")
                        .executes(context -> clone(context, false, plugin)))
                .executes(context -> clone(context, true, plugin)));
    }

    private static int clone(CommandContext<CommandSourceStack> context, boolean full, WorldsPlugin plugin) {
        try {
            var world = context.getArgument("world", World.class);
            var key = context.getArgument("key", Key.class);
            var clone = plugin.levelView().clone(world, builder -> builder.key(key), full).orElse(null);

            if (clone != null) plugin.levelView().persistWorld(clone, true);

            var placeholder = Placeholder.parsed("world", world.getName());
            var message = clone != null ? "world.clone.success" : "world.clone.failed";

            if (clone != null && context.getSource().getSender() instanceof Player player)
                player.teleportAsync(clone.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);

            plugin.bundle().sendMessage(context.getSource().getSender(), message, placeholder);
            return clone != null ? Command.SINGLE_SUCCESS : 0;
        } catch (Exception e) {
            // todo: send error message
            return 0;
        }
    }
}
