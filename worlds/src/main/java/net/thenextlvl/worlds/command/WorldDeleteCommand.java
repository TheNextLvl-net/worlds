package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.argument.CommandFlagsArgument;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

import java.util.Set;

import static net.thenextlvl.worlds.command.WorldCommand.worldArgument;

@NullMarked
class WorldDeleteCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> create(WorldsPlugin plugin) {
        return Commands.literal("delete")
                .requires(source -> source.getSender().hasPermission("worlds.command.delete"))
                .then(delete(plugin));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, World> delete(WorldsPlugin plugin) {
        return worldArgument(plugin)
                .then(Commands.argument("flags", new CommandFlagsArgument(
                        Set.of("--confirm", "--schedule")
                )).executes(context -> delete(context, plugin)))
                .executes(context -> confirmationNeeded(context, plugin));
    }

    private static int confirmationNeeded(CommandContext<CommandSourceStack> context, WorldsPlugin plugin) {
        var sender = context.getSource().getSender();
        plugin.bundle().sendMessage(sender, "command.confirmation",
                Placeholder.parsed("action", "/" + context.getInput()),
                Placeholder.parsed("confirmation", "/" + context.getInput() + " --confirm"));
        return Command.SINGLE_SUCCESS;
    }

    private static int delete(CommandContext<CommandSourceStack> context, WorldsPlugin plugin) {
        var flags = context.getArgument("flags", CommandFlagsArgument.Flags.class);
        if (!flags.contains("--confirm")) return confirmationNeeded(context, plugin);
        var world = context.getArgument("world", World.class);
        plugin.bundle().sendMessage(context.getSource().getSender(), "world.delete",
                Placeholder.parsed("world", world.getName()));
        plugin.levelView().deleteAsync(world, flags.contains("--schedule")).thenAccept(result -> {
            var message = switch (result) {
                case SUCCESS -> "world.delete.success";
                case SCHEDULED -> "world.delete.scheduled";
                case REQUIRES_SCHEDULING -> "world.delete.disallowed";
                case UNLOAD_FAILED -> "world.unload.failed";
                case FAILED -> "world.delete.failed";
            };
            plugin.bundle().sendMessage(context.getSource().getSender(), message,
                    Placeholder.parsed("world", world.getName()));
        });
        return Command.SINGLE_SUCCESS;
    }
}
