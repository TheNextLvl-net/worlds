package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import lombok.RequiredArgsConstructor;
import net.thenextlvl.worlds.WorldsPlugin;

@RequiredArgsConstructor
@SuppressWarnings("UnstableApiUsage")
class WorldDeleteCommand {
    private final WorldsPlugin plugin;

    ArgumentBuilder<CommandSourceStack, ?> create() {
        return Commands.literal("delete")
                .requires(source -> source.getSender().hasPermission("worlds.command.delete"))
                .then(Commands.argument("world", ArgumentTypes.world())
                        .executes(context -> {
                            return Command.SINGLE_SUCCESS;
                        }));
    }
}
