package net.thenextlvl.perworlds.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.thenextlvl.perworlds.SharedWorlds;

class GroupOptionCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> create(SharedWorlds commons) {
        return Commands.literal("option");
    }
}
