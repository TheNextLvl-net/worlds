package net.thenextlvl.worlds.command.link;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.link.LinkTree;
import net.thenextlvl.worlds.command.brigadier.SimpleCommand;
import org.jspecify.annotations.NullMarked;

@NullMarked
final class WorldLinkListCommand extends SimpleCommand {
    private WorldLinkListCommand(final WorldsPlugin plugin) {
        super(plugin, "list", "worlds.command.link.list");
    }

    public static ArgumentBuilder<CommandSourceStack, ?> create(final WorldsPlugin plugin) {
        final var command = new WorldLinkListCommand(plugin);
        return command.create().executes(command);
    }

    @Override
    public int run(final CommandContext<CommandSourceStack> context) {
        final var sender = context.getSource().getSender();
        final var links = plugin.linkProvider().getLinkTrees()
                .filter(tree -> !tree.isEmpty())
                .map(LinkTree::toString)
                .map(Component::text)
                .toList();
        if (links.isEmpty()) plugin.bundle().sendMessage(sender, "world.link.list.empty");
        else plugin.bundle().sendMessage(sender, "world.link.list",
                Formatter.joining("links", links),
                Formatter.number("amount", links.size()));
        return links.isEmpty() ? 0 : SINGLE_SUCCESS;
    }
}
