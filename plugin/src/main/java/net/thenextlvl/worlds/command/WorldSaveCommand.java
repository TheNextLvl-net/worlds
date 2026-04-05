package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.brigadier.BrigadierCommand;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

import static net.thenextlvl.worlds.command.WorldCommand.worldArgument;

@NullMarked
final class WorldSaveCommand extends BrigadierCommand {
    private WorldSaveCommand(final WorldsPlugin plugin) {
        super(plugin, "save", "worlds.command.save");
    }

    public static ArgumentBuilder<CommandSourceStack, ?> create(final WorldsPlugin plugin) {
        final var command = new WorldSaveCommand(plugin);
        return command.create().then(worldArgument(plugin)
                .then(Commands.literal("flush").executes(command::flush))
                .executes(command::save));
    }

    public int flush(final CommandContext<CommandSourceStack> context) {
        return save(context, true);
    }

    private int save(final CommandContext<CommandSourceStack> context) {
        return save(context, false);
    }

    private int save(final CommandContext<CommandSourceStack> context, final boolean flush) {
        final var world = context.getArgument("world", World.class);
        final var placeholder = Placeholder.parsed("world", world.getName());
        plugin.bundle().sendMessage(context.getSource().getSender(), "world.save", placeholder);
        plugin.levelView().saveAsync(world, flush).thenAccept(ignored -> {
            plugin.bundle().sendMessage(context.getSource().getSender(), "world.save.success", placeholder);
        }).exceptionally(throwable -> {
            plugin.bundle().sendMessage(context.getSource().getSender(), "world.save.failed", placeholder);
            plugin.getComponentLogger().warn("Failed to save world {}", world.getName(), throwable);
            return null;
        });
        return Command.SINGLE_SUCCESS;
    }
}
