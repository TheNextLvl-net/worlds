package net.thenextlvl.perworlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.perworlds.SharedWorlds;
import net.thenextlvl.perworlds.WorldGroup;
import org.jspecify.annotations.NullMarked;

import static net.thenextlvl.perworlds.command.GroupCommand.groupArgument;

@NullMarked
class GroupInfoCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> create(SharedWorlds commons) {
        return Commands.literal("info")
                .requires(source -> source.getSender().hasPermission("perworlds.command.group.info"))
                .then(groupArgument(commons).executes(context -> info(context, commons)));
    }

    private static int info(CommandContext<CommandSourceStack> context, SharedWorlds commons) {
        var sender = context.getSource().getSender();
        var group = context.getArgument("group", WorldGroup.class);
        var worlds = group.getPersistedWorlds().stream().map(key -> {
            var world = commons.getServer().getWorld(key);
            return world != null ? world.getName() : key.asString();
        }).map(Component::text).toList();
        commons.bundle().sendMessage(sender, "group.info",
                Formatter.booleanChoice("single", worlds.size() == 1),
                Formatter.joining("worlds", worlds),
                Formatter.number("amount", worlds.size()),
                Placeholder.unparsed("group", group.getName()));
        return Command.SINGLE_SUCCESS;
    }
}
