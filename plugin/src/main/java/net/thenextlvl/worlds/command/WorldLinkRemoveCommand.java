package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import lombok.RequiredArgsConstructor;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.suggestion.WorldSuggestionProvider;

@RequiredArgsConstructor
@SuppressWarnings("UnstableApiUsage")
public class WorldLinkRemoveCommand {
    private final WorldsPlugin plugin;

    ArgumentBuilder<CommandSourceStack, ?> create() {
        return Commands.literal("remove")
                .requires(source -> source.getSender().hasPermission("worlds.command.link.remove"))
                .then(Commands.argument("source", ArgumentTypes.world())
                        .suggests(new WorldSuggestionProvider<>(plugin))
                        .then(Commands.argument("destination", ArgumentTypes.world())
                                .executes(context -> {
                                    return Command.SINGLE_SUCCESS;
                                })));
    }
}
