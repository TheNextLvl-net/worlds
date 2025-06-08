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
class WorldRegenerateCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> create(WorldsPlugin plugin) {
        return Commands.literal("regenerate")
                .requires(source -> source.getSender().hasPermission("worlds.command.regenerate"))
                .then(regenerate(plugin));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, World> regenerate(WorldsPlugin plugin) {
        return worldArgument(plugin)
                .then(Commands.argument("flags", new CommandFlagsArgument(
                        Set.of("--confirm", "--schedule")
                )).executes(context -> regenerate(context, plugin)))
                .executes(context -> confirmationNeeded(context, plugin));
    }

    private static int confirmationNeeded(CommandContext<CommandSourceStack> context, WorldsPlugin plugin) {
        var sender = context.getSource().getSender();
        plugin.bundle().sendMessage(sender, "command.confirmation",
                Placeholder.parsed("action", "/" + context.getInput()),
                Placeholder.parsed("confirmation", "/" + context.getInput() + " --confirm"));
        return Command.SINGLE_SUCCESS;
    }

    private static int regenerate(CommandContext<CommandSourceStack> context, WorldsPlugin plugin) {
        var flags = context.getArgument("flags", CommandFlagsArgument.Flags.class);
        if (!flags.contains("--confirm")) return confirmationNeeded(context, plugin);
        var world = context.getArgument("world", World.class);
        var result = plugin.levelView().regenerate(world, flags.contains("--schedule"));
        var message = switch (result) {
            case SUCCESS -> "world.regenerate.success";
            case SCHEDULED -> "world.regenerate.scheduled";
            case REQUIRES_SCHEDULING -> WorldsPlugin.RUNNING_FOLIA
                    ? "world.regenerate.disallowed.folia"
                    : "world.regenerate.disallowed";
            case UNLOAD_FAILED -> "world.unload.failed";
            case FAILED -> "world.regenerate.failed";
        };
        plugin.bundle().sendMessage(context.getSource().getSender(), message,
                Placeholder.parsed("world", world.getName()));
        return result.isSuccess() ? Command.SINGLE_SUCCESS : 0;
    }
}
