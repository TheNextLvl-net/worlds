package net.thenextlvl.worlds.command.link;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.argument.KeyArgument;
import net.thenextlvl.worlds.command.brigadier.SimpleCommand;
import net.thenextlvl.worlds.command.suggestion.LinkSuggestionProvider;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

import static net.thenextlvl.worlds.command.WorldCommand.worldArgument;

@NullMarked
final class WorldLinkRemoveCommand extends SimpleCommand {
    private WorldLinkRemoveCommand(WorldsPlugin plugin) {
        super(plugin, "remove", "worlds.command.link.remove");
    }

    public static ArgumentBuilder<CommandSourceStack, ?> create(WorldsPlugin plugin) {
        var command = new WorldLinkRemoveCommand(plugin);
        return command.create().then(command.remove());
    }

    private RequiredArgumentBuilder<CommandSourceStack, World> remove() {
        return worldArgument(plugin).suggests(new LinkSuggestionProvider<>(plugin, true)).then(Commands.argument("destination", new KeyArgument()).suggests(new LinkSuggestionProvider.Linked<>(plugin)).executes(this));
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        var world = context.getArgument("world", World.class);
        var destination = context.getArgument("destination", Key.class);
        var removed = plugin.linkProvider().unlink(world.key(), destination);
        var message = removed ? "world.unlink.success" : "world.unlink.failed";
        plugin.bundle().sendMessage(context.getSource().getSender(), message, Placeholder.parsed("relative", destination.key().asString()), Placeholder.parsed("world", world.getName()));
        return removed ? SINGLE_SUCCESS : 0;
    }
}
