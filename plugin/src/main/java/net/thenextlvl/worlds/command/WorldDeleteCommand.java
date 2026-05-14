package net.thenextlvl.worlds.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.OperationScheduler;
import net.thenextlvl.worlds.WorldOperationException;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.argument.CommandOptionsArgument;
import net.thenextlvl.worlds.command.brigadier.SimpleCommand;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static net.thenextlvl.worlds.command.WorldCommand.worldArgument;
import static net.thenextlvl.worlds.event.WorldActionScheduledEvent.ActionType.DELETE;

@NullMarked
final class WorldDeleteCommand extends SimpleCommand {
    private WorldDeleteCommand(final WorldsPlugin plugin) {
        super(plugin, "delete", "worlds.command.delete");
    }

    public static ArgumentBuilder<CommandSourceStack, ?> create(final WorldsPlugin plugin) {
        final var command = new WorldDeleteCommand(plugin);
        return command.create().then(command.delete());
    }

    private RequiredArgumentBuilder<CommandSourceStack, World> delete() {
        return worldArgument(plugin)
                .then(Commands.argument("options", new CommandOptionsArgument(
                        Set.of("--confirm", "--schedule")
                )).executes(this))
                .executes(this::confirmationNeeded);
    }

    private int confirmationNeeded(final CommandContext<CommandSourceStack> context) {
        final var sender = context.getSource().getSender();
        plugin.bundle().sendMessage(sender, "command.confirmation",
                Placeholder.parsed("action", "/" + context.getInput()),
                Placeholder.parsed("confirmation", "/" + context.getInput() + " --confirm"));
        return 0;
    }

    @Override
    public int run(final CommandContext<CommandSourceStack> context) {
        final var options = context.getArgument("options", CommandOptionsArgument.Options.class);
        if (!options.contains("--confirm") && !options.contains("--schedule")) return confirmationNeeded(context);
        final var world = context.getArgument("world", World.class);
        final var schedule = options.contains("--schedule");

        if (schedule && plugin.getScheduler().cancel(world.key(), DELETE)) {
            plugin.bundle().sendMessage(context.getSource().getSender(), "world.delete.schedule-cancelled",
                    Placeholder.parsed("world", world.key().asString()));
            return SINGLE_SUCCESS;
        }

        if (!schedule) plugin.bundle().sendMessage(context.getSource().getSender(), "world.delete",
                Placeholder.parsed("world", world.key().asString()));
        final var future = !schedule ? plugin.delete(world)
                : CompletableFuture.completedFuture(plugin.getScheduler().schedule(
                new OperationScheduler.DeleteOperation(world.key())
        ));
        future.thenAccept(success -> {
            if (success) {
                final var message = schedule ? "world.delete.scheduled" : "world.delete.success";
                plugin.bundle().sendMessage(context.getSource().getSender(), message,
                        Placeholder.parsed("world", world.key().asString()));
            } else CommandFailureHandler.handle(plugin, context.getSource().getSender(), new WorldOperationException(
                    WorldOperationException.Reason.EVENT_CANCELLED
            ));
        }).exceptionally(throwable -> {
            CommandFailureHandler.handle(plugin, context.getSource().getSender(), throwable,
                    Placeholder.parsed("world", world.key().asString()));
            return null;
        });
        return SINGLE_SUCCESS;
    }
}
