package net.thenextlvl.perworlds.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.thenextlvl.perworlds.SharedWorlds;
import net.thenextlvl.perworlds.WorldGroup;
import net.thenextlvl.perworlds.command.argument.GroupArgument;

public class GroupCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> create(SharedWorlds commons) {
        return Commands.literal("group")
                .requires(source -> source.getSender().hasPermission("perworlds.command.group"))
                .then(GroupAddCommand.create(commons))
                .then(GroupCreateCommand.create(commons))
                .then(GroupDeleteCommand.create(commons))
                .then(GroupInfoCommand.create(commons))
                .then(GroupListCommand.create(commons))
                .then(GroupOptionCommand.create(commons))
                .then(GroupRemoveCommand.create(commons));
    }

    static RequiredArgumentBuilder<CommandSourceStack, WorldGroup> groupArgument(SharedWorlds commons) {
        return Commands.argument("group", new GroupArgument(commons));
    }
}
