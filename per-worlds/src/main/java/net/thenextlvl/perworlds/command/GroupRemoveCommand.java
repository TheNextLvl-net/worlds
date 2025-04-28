package net.thenextlvl.perworlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.perworlds.SharedWorlds;
import net.thenextlvl.perworlds.WorldGroup;
import net.thenextlvl.perworlds.command.suggestion.GroupMemberSuggestionProvider;
import org.jspecify.annotations.NullMarked;

import static net.thenextlvl.perworlds.command.GroupCommand.groupArgument;

@NullMarked
class GroupRemoveCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> create(SharedWorlds commons) {
        return Commands.literal("remove")
                .requires(source -> source.getSender().hasPermission("perworlds.command.group.remove"))
                .then(remove(commons));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> remove(SharedWorlds commons) {
        return groupArgument(commons).then(Commands.argument("world", ArgumentTypes.key())
                .suggests(new GroupMemberSuggestionProvider<>())
                .executes(context -> remove(context, commons)));
    }

    private static int remove(CommandContext<CommandSourceStack> context, SharedWorlds commons) {
        var group = context.getArgument("group", WorldGroup.class);
        var key = context.getArgument("world", Key.class);
        var world = commons.getServer().getWorld(key);
        var success = world != null ? group.removeWorld(world) : group.removeWorld(key);
        var message = success ? "group.world.removed" : "group.world.remove.failed";
        commons.bundle().sendMessage(context.getSource().getSender(), message,
                Placeholder.unparsed("group", group.getName()),
                Placeholder.unparsed("world", world != null ? world.getName() : key.asString()));
        return success ? Command.SINGLE_SUCCESS : 0;
    }
}
