package net.thenextlvl.worlds.command.link;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.argument.WorldArgument;
import net.thenextlvl.worlds.command.brigadier.SimpleCommand;
import net.thenextlvl.worlds.command.suggestion.LinkSuggestionProvider;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

import static net.thenextlvl.worlds.command.WorldCommand.worldArgument;

@NullMarked
final class WorldLinkCreateCommand extends SimpleCommand {
    private WorldLinkCreateCommand(WorldsPlugin plugin) {
        super(plugin, "create", "worlds.command.link.create");
    }

    public static ArgumentBuilder<CommandSourceStack, ?> create(WorldsPlugin plugin) {
        var command = new WorldLinkCreateCommand(plugin);
        return command.create().then(command.createArgument());
    }

    private RequiredArgumentBuilder<CommandSourceStack, World> createArgument() {
        return worldArgument(plugin)
                .suggests(new LinkSuggestionProvider<>(plugin, false))
                .then(Commands.argument("destination", new WorldArgument(plugin))
                        .suggests(new LinkSuggestionProvider.Unlinked<>(plugin))
                        .executes(this));
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        var world = context.getArgument("world", World.class);
        var destination = context.getArgument("destination", World.class);
        var link = plugin.linkProvider().link(world, destination);
        var message = link ? "world.link.success" : "world.link.failed";
        plugin.bundle().sendMessage(context.getSource().getSender(), message,
                Placeholder.parsed("source", world.key().asString()),
                Placeholder.parsed("destination", destination.key().asString()));
        return link ? SINGLE_SUCCESS : 0;
    }
}
