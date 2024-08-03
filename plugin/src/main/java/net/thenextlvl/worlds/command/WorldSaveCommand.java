package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import lombok.RequiredArgsConstructor;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.suggestion.WorldSuggestionProvider;
import org.bukkit.World;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
@SuppressWarnings("UnstableApiUsage")
class WorldSaveCommand {
    private final WorldsPlugin plugin;

    ArgumentBuilder<CommandSourceStack, ?> create() {
        return Commands.literal("save")
                .requires(source -> source.getSender().hasPermission("worlds.command.save"))
                .then(Commands.argument("world", ArgumentTypes.world())
                        .suggests(new WorldSuggestionProvider<>(plugin))
                        .executes(context -> {
                            var world = context.getArgument("world", World.class);
                            world.save();
                            // todo: add message
                            return Command.SINGLE_SUCCESS;
                        }))
                .executes(context -> {
                    if (!(context.getSource().getSender() instanceof Player player)) {
                        plugin.bundle().sendMessage(context.getSource().getSender(), "command.sender");
                    } else player.getWorld().save();
                    // todo: add message
                    return Command.SINGLE_SUCCESS;
                });
    }
}
