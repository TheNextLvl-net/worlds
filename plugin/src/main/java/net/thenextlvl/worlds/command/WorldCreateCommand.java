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
class WorldCreateCommand {
    private final WorldsPlugin plugin;

    ArgumentBuilder<CommandSourceStack, ?> create() {
        return Commands.literal("create")
                .requires(source -> source.getSender().hasPermission("worlds.command.create"))
                .then(Commands.argument("key", ArgumentTypes.key())
                        .executes(context -> {
                            return Command.SINGLE_SUCCESS;
                        }));
    }
}
