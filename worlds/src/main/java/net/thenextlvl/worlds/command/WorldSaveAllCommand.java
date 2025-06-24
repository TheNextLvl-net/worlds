package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.thenextlvl.worlds.WorldsPlugin;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.CompletableFuture;

@NullMarked
class WorldSaveAllCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> create(WorldsPlugin plugin) {
        return Commands.literal("save-all")
                .requires(source -> source.getSender().hasPermission("worlds.command.save-all"))
                .then(Commands.literal("flush").executes(context -> saveAll(context.getSource(), true, plugin)))
                .executes(context -> saveAll(context.getSource(), false, plugin));
    }

    private static int saveAll(CommandSourceStack source, boolean flush, WorldsPlugin plugin) {
        plugin.bundle().sendMessage(source.getSender(), "world.save.all");
        CompletableFuture.allOf(plugin.getServer().getWorlds().stream().map(world -> {
            return plugin.levelView().saveAsync(world, flush);
        }).toList().toArray(new CompletableFuture[]{})).thenAccept(ignored -> {
            plugin.bundle().sendMessage(source.getSender(), "world.save.all.success");
        }).exceptionally(throwable -> {
            plugin.bundle().sendMessage(source.getSender(), "world.save.all.failed");
            plugin.getComponentLogger().warn("Failed to save all worlds", throwable);
            return null;
        });
        return Command.SINGLE_SUCCESS;
    }
}
