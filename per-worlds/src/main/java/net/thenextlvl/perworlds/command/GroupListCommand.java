package net.thenextlvl.perworlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.perworlds.SharedWorlds;
import org.jspecify.annotations.NullMarked;

@NullMarked
class GroupListCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> create(SharedWorlds commons) {
        return Commands.literal("list")
                .requires(source -> source.getSender().hasPermission("perworlds.command.group.list"))
                .executes(context -> list(context, commons));
    }

    private static int list(CommandContext<CommandSourceStack> context, SharedWorlds commons) {
        var sender = context.getSource().getSender();
        var groups = commons.groupProvider().getGroups().stream().map(group ->
                commons.bundle().component("group.list.component", sender,
                        Placeholder.parsed("group", group.getName()))
        ).toList();
        commons.bundle().sendMessage(sender, "group.list",
                Formatter.number("amount", groups.size()),
                Formatter.joining("groups", groups));
        return Command.SINGLE_SUCCESS;
    }
}
