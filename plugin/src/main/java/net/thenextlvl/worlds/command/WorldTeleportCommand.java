package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.FinePositionResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.suggestion.WorldSuggestionProvider;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND;

@RequiredArgsConstructor
@SuppressWarnings("UnstableApiUsage")
class WorldTeleportCommand {
    private final WorldsPlugin plugin;

    ArgumentBuilder<CommandSourceStack, ?> create() {
        return Commands.literal("teleport")
                .requires(source -> source.getSender().hasPermission("worlds.command.teleport"))
                .then(Commands.argument("world", ArgumentTypes.world())
                        .suggests(new WorldSuggestionProvider<>(plugin))
                        .then(Commands.argument("entities", ArgumentTypes.entities())
                                .then(Commands.argument("position", ArgumentTypes.finePosition(true))
                                        .executes(this::teleportEntitiesPosition))
                                .executes(this::teleportEntities))
                        .executes(this::teleport));
    }

    private int teleportEntitiesPosition(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var entities = context.getArgument("entities", EntitySelectorArgumentResolver.class);
        var position = context.getArgument("position", FinePositionResolver.class);
        var world = context.getArgument("world", World.class);
        var location = position.resolve(context.getSource()).toLocation(world);
        var resolved = entities.resolve(context.getSource());
        return teleport(context.getSource().getSender(), resolved, location);
    }

    private int teleportEntities(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var entities = context.getArgument("entities", EntitySelectorArgumentResolver.class);
        var world = context.getArgument("world", World.class);
        var resolved = entities.resolve(context.getSource());
        return teleport(context.getSource().getSender(), resolved, world.getSpawnLocation());
    }

    private int teleport(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getSender() instanceof Player player)) {
            plugin.bundle().sendMessage(context.getSource().getSender(), "command.sender");
            return 0;
        }
        var world = context.getArgument("world", World.class);
        return teleport(player, List.of(player), world.getSpawnLocation());
    }

    private int teleport(CommandSender sender, List<Entity> entities, Location location) {
        var message = entities.size() == 1 ? "world.teleport.other"
                : entities.isEmpty() ? "world.teleport.none" : "world.teleport.others";
        entities.forEach(entity -> {
            entity.teleportAsync(location, COMMAND);
            plugin.bundle().sendMessage(entity, "world.teleport.self",
                    Placeholder.parsed("world", location.getWorld().key().asString()));
        });
        if (entities.size() == 1 && entities.getFirst().equals(sender)) return Command.SINGLE_SUCCESS;
        plugin.bundle().sendMessage(sender, message,
                Placeholder.parsed("entities", String.valueOf(entities.size())),
                Placeholder.parsed("entity", entities.getFirst().getName()),
                Placeholder.parsed("world", location.getWorld().key().asString()));
        return entities.isEmpty() ? 0 : Command.SINGLE_SUCCESS;
    }
}
