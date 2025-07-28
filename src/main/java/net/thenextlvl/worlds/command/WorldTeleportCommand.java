package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.FinePositionResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.List;

import static net.thenextlvl.worlds.command.WorldCommand.worldArgument;
import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND;

@NullMarked
class WorldTeleportCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> create(WorldsPlugin plugin) {
        return Commands.literal("teleport")
                .requires(source -> source.getSender().hasPermission("worlds.command.teleport"))
                .then(teleport(plugin));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, World> teleport(WorldsPlugin plugin) {
        return worldArgument(plugin)
                .then(teleportEntity(plugin))
                .executes(context -> teleport(context, plugin));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, EntitySelectorArgumentResolver> teleportEntity(WorldsPlugin plugin) {
        return Commands.argument("entities", ArgumentTypes.entities())
                .then(Commands.argument("position", ArgumentTypes.finePosition(true))
                        .executes(context -> teleportEntitiesPosition(context, plugin)))
                .executes(context -> teleportEntities(context, plugin));
    }

    private static int teleportEntitiesPosition(CommandContext<CommandSourceStack> context, WorldsPlugin plugin) throws CommandSyntaxException {
        var entities = context.getArgument("entities", EntitySelectorArgumentResolver.class);
        var position = context.getArgument("position", FinePositionResolver.class);
        var world = context.getArgument("world", World.class);
        var location = position.resolve(context.getSource()).toLocation(world);
        var resolved = entities.resolve(context.getSource());
        return teleport(context.getSource().getSender(), resolved, location, plugin);
    }

    private static int teleportEntities(CommandContext<CommandSourceStack> context, WorldsPlugin plugin) throws CommandSyntaxException {
        var entities = context.getArgument("entities", EntitySelectorArgumentResolver.class);
        var world = context.getArgument("world", World.class);
        var resolved = entities.resolve(context.getSource());
        return teleport(context.getSource().getSender(), resolved, world.getSpawnLocation(), plugin);
    }

    private static int teleport(CommandContext<CommandSourceStack> context, WorldsPlugin plugin) {
        if (!(context.getSource().getSender() instanceof Player player)) {
            plugin.bundle().sendMessage(context.getSource().getSender(), "command.sender");
            return 0;
        }
        var world = context.getArgument("world", World.class);
        return teleport(player, List.of(player), world.getSpawnLocation(), plugin);
    }

    private static int teleport(CommandSender sender, List<Entity> entities, Location location, WorldsPlugin plugin) {
        var message = entities.size() == 1 ? "world.teleport.other"
                : entities.isEmpty() ? "world.teleport.none" : "world.teleport.others";
        entities.forEach(entity -> {
            entity.teleportAsync(location, COMMAND);
            plugin.bundle().sendMessage(entity, "world.teleport.self",
                    Placeholder.parsed("world", location.getWorld().getName()));
        });
        if (entities.size() == 1 && entities.getFirst().equals(sender)) return Command.SINGLE_SUCCESS;
        plugin.bundle().sendMessage(sender, message,
                Placeholder.component("entity", entities.isEmpty() ? Component.empty() : entities.getFirst().teamDisplayName()),
                Placeholder.parsed("entities", String.valueOf(entities.size())),
                Placeholder.parsed("world", location.getWorld().getName()));
        return entities.isEmpty() ? 0 : Command.SINGLE_SUCCESS;
    }
}
