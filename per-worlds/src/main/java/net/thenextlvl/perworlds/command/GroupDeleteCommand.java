package net.thenextlvl.perworlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.perworlds.SharedWorlds;
import net.thenextlvl.perworlds.WorldGroup;
import net.thenextlvl.perworlds.command.argument.GroupArgument;

class GroupDeleteCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> create(SharedWorlds commons) {
        return Commands.literal("delete")
                .requires(source -> source.getSender().hasPermission("perworlds.command.group.delete"))
                .then(Commands.argument("group", new GroupArgument(commons))
                        .executes(context -> delete(context, commons)));
    }

    private static int delete(CommandContext<CommandSourceStack> context, SharedWorlds commons) {
        var sender = context.getSource().getSender();
        var group = context.getArgument("group", WorldGroup.class);
        var success = commons.groupProvider().removeGroup(group) | group.delete();
        var message = success ? "group.created" : "group.exists";
        commons.bundle().sendMessage(sender, message, Placeholder.unparsed("name", group.getName()));
        return success ? Command.SINGLE_SUCCESS : 0;
    }
}
