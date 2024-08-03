package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.suggestion.WorldSuggestionProvider;
import org.bukkit.World;
import org.bukkit.entity.Player;

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
                                .executes(context -> {
                                    var entities = context.getArgument("entities", EntitySelectorArgumentResolver.class);
                                    var world = context.getArgument("world", World.class);
                                    var resolved = entities.resolve(context.getSource());
                                    // todo: add messages
                                    resolved.forEach(entity -> {
                                        entity.teleportAsync(world.getSpawnLocation(), COMMAND);
                                    });
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .executes(context -> {
                            if (!(context.getSource().getSender() instanceof Player player)) {
                                plugin.bundle().sendMessage(context.getSource().getSender(), "command.sender");
                                return 0;
                            }
                            var world = context.getArgument("world", World.class);
                            player.teleportAsync(world.getSpawnLocation(), COMMAND);
                            plugin.bundle().sendMessage(player, "world.teleport.player.self",
                                    Placeholder.parsed("world", world.key().asString()));
                            return Command.SINGLE_SUCCESS;
                        }));
    }
}
