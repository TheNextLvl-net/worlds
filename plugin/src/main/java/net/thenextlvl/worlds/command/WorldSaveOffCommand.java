package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.RequiredArgsConstructor;
import net.thenextlvl.worlds.WorldsPlugin;

@RequiredArgsConstructor
@SuppressWarnings("UnstableApiUsage")
class WorldSaveOffCommand {
    private final WorldsPlugin plugin;

    ArgumentBuilder<CommandSourceStack, ?> create() {
        return Commands.literal("save-off")
                .requires(source -> source.getSender().hasPermission("worlds.command.save-off"))
                .executes(context -> {
                    // todo: save-off
                    return Command.SINGLE_SUCCESS;
                });
    }
}
