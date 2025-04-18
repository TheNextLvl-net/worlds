package net.thenextlvl.perworlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.perworlds.SharedWorlds;
import net.thenextlvl.perworlds.WorldGroup;
import net.thenextlvl.perworlds.command.suggestion.GroupMemberSuggestionProvider;
import org.bukkit.World;

import static net.thenextlvl.perworlds.command.GroupCommand.groupArgument;

class GroupRemoveCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> create(SharedWorlds commons) {
        return Commands.literal("remove")
                .requires(source -> source.getSender().hasPermission("perworlds.command.group.remove"))
                .then(remove(commons));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> remove(SharedWorlds commons) {
        return groupArgument(commons).then(Commands.argument("world", ArgumentTypes.world())
                .suggests(new GroupMemberSuggestionProvider<>())
                .executes(context -> remove(context, commons)));
    }

    private static int remove(CommandContext<CommandSourceStack> context, SharedWorlds commons) {
        var group = context.getArgument("group", WorldGroup.class);
        var world = context.getArgument("world", World.class);
        var success = group.removeWorld(world);
        var message = success ? "group.world.removed" : "group.world.remove.failed";
        commons.bundle().sendMessage(context.getSource().getSender(), message,
                Placeholder.unparsed("group", group.getName()),
                Placeholder.unparsed("world", world.getName()));
        return success ? Command.SINGLE_SUCCESS : 0;
    }
}
