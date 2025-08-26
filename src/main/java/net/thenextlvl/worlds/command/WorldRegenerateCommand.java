package net.thenextlvl.worlds.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
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
final class WorldRegenerateCommand extends SimpleCommand {
    private WorldRegenerateCommand(WorldsPlugin plugin) {
        super(plugin, "regenerate", "worlds.command.regenerate");
    }

    public static ArgumentBuilder<CommandSourceStack, ?> create(WorldsPlugin plugin) {
        var command = new WorldRegenerateCommand(plugin);
        return command.create().then(command.regenerate());
    }

    private RequiredArgumentBuilder<CommandSourceStack, World> regenerate() {
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
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var flags = context.getArgument("flags", CommandFlagsArgument.Flags.class);
        if (!flags.contains("--confirm")) return confirmationNeeded(context);
        var world = context.getArgument("world", World.class);
        var schedule = flags.contains("--schedule");
        if (!schedule) plugin.bundle().sendMessage(context.getSource().getSender(), "world.regenerate",
                Placeholder.parsed("world", world.getName()));
        plugin.levelView().regenerateAsync(world, schedule).thenAccept(result -> {
            var message = switch (result) {
                case SUCCESS -> "world.regenerate.success";
                case SCHEDULED -> "world.regenerate.scheduled";
                case REQUIRES_SCHEDULING -> "world.regenerate.disallowed";
                case UNLOAD_FAILED -> "world.unload.failed";
                case FAILED -> "world.regenerate.failed";
            };
            plugin.bundle().sendMessage(context.getSource().getSender(), message,
                    Placeholder.parsed("world", world.getName()));
        }).exceptionally(throwable -> {
            plugin.bundle().sendMessage(context.getSource().getSender(), "world.regenerate.failed",
                    Placeholder.parsed("world", world.getName()));
            plugin.getComponentLogger().warn("Failed to regenerate world {}", world.getName(), throwable);
            return null;
        });
        return SINGLE_SUCCESS;
    }
}
