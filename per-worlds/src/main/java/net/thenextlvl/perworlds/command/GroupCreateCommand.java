package net.thenextlvl.perworlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.perworlds.SharedWorlds;

class GroupCreateCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> create(SharedWorlds commons) {
        return Commands.literal("create")
                .requires(source -> source.getSender().hasPermission("perworlds.command.group.create"))
                .then(Commands.argument("name", StringArgumentType.string())
                        .executes(context -> create(context, commons)));
    }

    private static int create(CommandContext<CommandSourceStack> context, SharedWorlds commons) {
        var sender = context.getSource().getSender();
        var name = context.getArgument("name", String.class);
        var success = !commons.groupProvider().hasGroup(name);
        if (success) commons.groupProvider().createGroup(name, settings -> {
        });
        var message = success ? "group.created" : "group.exists";
        commons.bundle().sendMessage(sender, message, Placeholder.unparsed("name", name));
        return success ? Command.SINGLE_SUCCESS : 0;
    }
}
