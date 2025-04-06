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
import net.thenextlvl.perworlds.command.suggestion.UnassignedWorldsSuggestionProvider;
import org.bukkit.World;

import static net.thenextlvl.perworlds.command.GroupCommand.groupArgument;

class GroupAddCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> create(SharedWorlds commons) {
        return Commands.literal("add")
                .requires(source -> source.getSender().hasPermission("perworlds.command.group.add"))
                .then(add(commons));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> add(SharedWorlds commons) {
        return Commands.argument("world", ArgumentTypes.world())
                .suggests(new UnassignedWorldsSuggestionProvider<>(commons))
                .then(groupArgument(commons).executes(context -> add(context, commons)));
    }

    private static int add(CommandContext<CommandSourceStack> context, SharedWorlds commons) {
        var group = context.getArgument("group", WorldGroup.class);
        var world = context.getArgument("world", World.class);
        var success = group.addWorld(world);
        var message = success ? "group.world.added" : "group.world.add.failed";
        commons.bundle().sendMessage(context.getSource().getSender(), message,
                Placeholder.unparsed("group", group.getName()),
                Placeholder.unparsed("world", world.getName()));
        return success ? Command.SINGLE_SUCCESS : 0;
    }
}
