package net.thenextlvl.worlds.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.argument.CommandFlagsArgument;
import net.thenextlvl.worlds.command.brigadier.SimpleCommand;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

import java.util.Set;

import static net.thenextlvl.worlds.command.WorldCommand.worldArgument;

@NullMarked
final class WorldDeleteCommand extends SimpleCommand {
    private WorldDeleteCommand(WorldsPlugin plugin) {
        super(plugin, "delete", "worlds.command.delete");
    }

    public static ArgumentBuilder<CommandSourceStack, ?> create(WorldsPlugin plugin) {
        var command = new WorldDeleteCommand(plugin);
        return command.create().then(command.delete());
    }

    private RequiredArgumentBuilder<CommandSourceStack, World> delete() {
        return worldArgument(plugin)
                .then(Commands.argument("flags", new CommandFlagsArgument(
                        Set.of("--confirm", "--schedule")
                )).executes(this))
                .executes(this::confirmationNeeded);
    }

    private int confirmationNeeded(CommandContext<CommandSourceStack> context) {
        var sender = context.getSource().getSender();
        plugin.bundle().sendMessage(sender, "command.confirmation",
                Placeholder.parsed("action", "/" + context.getInput()),
                Placeholder.parsed("confirmation", "/" + context.getInput() + " --confirm"));
        return 0;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        var flags = context.getArgument("flags", CommandFlagsArgument.Flags.class);
        if (!flags.contains("--confirm")) return confirmationNeeded(context);
        var world = context.getArgument("world", World.class);
        var schedule = flags.contains("--schedule");
        if (!schedule) plugin.bundle().sendMessage(context.getSource().getSender(), "world.delete",
                Placeholder.parsed("world", world.getName()));
        plugin.levelView().deleteAsync(world, schedule).thenAccept(result -> {
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
        return SINGLE_SUCCESS;
    }
}
