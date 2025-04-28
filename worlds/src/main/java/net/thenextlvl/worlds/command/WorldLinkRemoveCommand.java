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
import net.thenextlvl.worlds.api.link.Relative;
import net.thenextlvl.worlds.command.argument.RelativeArgument;
import net.thenextlvl.worlds.command.suggestion.WorldSuggestionProvider;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

@NullMarked
class WorldLinkRemoveCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> create(WorldsPlugin plugin) {
        return Commands.literal("remove")
                .requires(source -> source.getSender().hasPermission("worlds.command.link.remove"))
                .then(remove(plugin));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, World> remove(WorldsPlugin plugin) {
        return Commands.argument("world", ArgumentTypes.world())
                .suggests(new WorldSuggestionProvider<>(plugin, (context, world) ->
                        world.getEnvironment().equals(World.Environment.NORMAL)))
                .then(Commands.argument("relative", new RelativeArgument(relative ->
                                !relative.equals(Relative.OVERWORLD)))
                        .executes(context -> remove(plugin, context)));
    }

    private static int remove(WorldsPlugin plugin, CommandContext<CommandSourceStack> context) {
        var world = context.getArgument("world", World.class);
        var relative = context.getArgument("relative", Relative.class);
        var unlink = plugin.linkController().unlink(world, relative);
        var message = unlink ? "world.unlink.success" : "world.unlink.failed";
        plugin.bundle().sendMessage(context.getSource().getSender(), message,
                Placeholder.parsed("relative", relative.key().asString()),
                Placeholder.parsed("world", world.getName()));
        return unlink ? Command.SINGLE_SUCCESS : 0;
    }
}
