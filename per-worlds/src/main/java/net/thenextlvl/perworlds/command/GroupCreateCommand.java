package net.thenextlvl.perworlds.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.thenextlvl.perworlds.SharedWorlds;

class GroupCreateCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> create(SharedWorlds commons) {
        return Commands.literal("create");
    }
}
