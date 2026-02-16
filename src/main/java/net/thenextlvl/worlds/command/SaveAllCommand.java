package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.brigadier.BrigadierCommand;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.CompletableFuture;

@NullMarked
public final class SaveAllCommand extends BrigadierCommand {
    private SaveAllCommand(final WorldsPlugin plugin) {
        super(plugin, "save-all", "worlds.command.save-all");
    }

    public static LiteralCommandNode<CommandSourceStack> create(final WorldsPlugin plugin) {
        final var command = new SaveAllCommand(plugin);
        return command.create()
                .then(Commands.literal("flush").executes(command::flush))
                .executes(command::saveAll)
                .build();
    }

    private int flush(final CommandContext<CommandSourceStack> context) {
        return saveAll(context.getSource(), true);
    }

    private int saveAll(final CommandContext<CommandSourceStack> context) {
        return saveAll(context.getSource(), false);
    }

    private int saveAll(final CommandSourceStack source, final boolean flush) {
        plugin.bundle().sendMessage(source.getSender(), "world.save.all");
        CompletableFuture.allOf(plugin.getServer().getWorlds().stream().map(world -> {
            return plugin.levelView().saveAsync(world, flush);
        }).toArray(CompletableFuture[]::new)).thenAccept(ignored -> {
            plugin.bundle().sendMessage(source.getSender(), "world.save.all.success");
        }).exceptionally(throwable -> {
            plugin.bundle().sendMessage(source.getSender(), "world.save.all.failed");
            plugin.getComponentLogger().warn("Failed to save all worlds", throwable);
            return null;
        });
        return Command.SINGLE_SUCCESS;
    }
}
