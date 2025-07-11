package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.suggestion.LinkSuggestionProvider;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

@NullMarked
class WorldLinkCreateCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> create(WorldsPlugin plugin) {
        return Commands.literal("create")
                .requires(source -> source.getSender().hasPermission("worlds.command.link.create"))
                .then(createArgument(plugin));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, World> createArgument(WorldsPlugin plugin) {
        return Commands.argument("world", ArgumentTypes.world())
                .suggests(new LinkSuggestionProvider<>(plugin, false))
                .then(Commands.argument("destination", ArgumentTypes.world())
                        .suggests(new LinkSuggestionProvider.Unlinked<>(plugin))
                        .executes(context -> create(plugin, context)));
    }

    private static int create(WorldsPlugin plugin, CommandContext<CommandSourceStack> context) {
        var world = context.getArgument("world", World.class);
        var destination = context.getArgument("destination", World.class);
        var link = plugin.linkProvider().link(world, destination);
        var message = link ? "world.link.success" : "world.link.failed";
        plugin.bundle().sendMessage(context.getSource().getSender(), message,
                Placeholder.parsed("source", world.key().asString()),
                Placeholder.parsed("destination", destination.key().asString()));
        return link ? Command.SINGLE_SUCCESS : 0;
    }
}
