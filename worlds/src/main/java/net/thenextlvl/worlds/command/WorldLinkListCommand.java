package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.link.LinkTree;
import org.jspecify.annotations.NullMarked;

@NullMarked
class WorldLinkListCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> create(WorldsPlugin plugin) {
        return Commands.literal("list")
                .requires(source -> source.getSender().hasPermission("worlds.command.link.list"))
                .executes(context -> list(context, plugin));
    }

    private static int list(CommandContext<CommandSourceStack> context, WorldsPlugin plugin) {
        var sender = context.getSource().getSender();
        var links = plugin.linkProvider().getLinkTrees()
                .filter(tree -> !tree.isEmpty())
                .map(LinkTree::toString)
                .map(Component::text)
                .toList();
        if (links.isEmpty()) plugin.bundle().sendMessage(sender, "world.link.list.empty");
        else plugin.bundle().sendMessage(sender, "world.link.list",
                Formatter.joining("links", links),
                Formatter.number("amount", links.size()));
        return links.isEmpty() ? 0 : Command.SINGLE_SUCCESS;
    }
}
