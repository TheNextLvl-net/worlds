package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.level.Level;
import net.thenextlvl.worlds.command.suggestion.LevelSuggestionProvider;
import org.bukkit.entity.Entity;
import org.jspecify.annotations.NullMarked;

import java.nio.file.Path;

import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND;

@NullMarked
class WorldLoadCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> create(WorldsPlugin plugin) {
        return Commands.literal("load")
                .requires(source -> source.getSender().hasPermission("worlds.command.load"))
                .then(load(plugin));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, String> load(WorldsPlugin plugin) {
        return Commands.argument("world", StringArgumentType.string())
                .suggests(new LevelSuggestionProvider<>(plugin, false))
                .executes(context -> load(context, plugin));
    }

    private static int load(CommandContext<CommandSourceStack> context, WorldsPlugin plugin) {
        var name = context.getArgument("world", String.class);
        var build = plugin.levelView().read(Path.of(name)).map(Level.Builder::build);
        var future = build.filter(Level::isWorldKnown).map(Level::createAsync).orElse(null);

        if (future == null) {
            plugin.bundle().sendMessage(context.getSource().getSender(), "world.load.failed",
                    Placeholder.parsed("world", name));
            return 0;
        }

        future.thenAccept(level -> {
            plugin.bundle().sendMessage(context.getSource().getSender(), "world.load.success",
                    Placeholder.parsed("world", level.getName()));

            plugin.levelView().persistStatus(level, true, true);
            plugin.levelView().saveLevelData(level, true);

            if (!(context.getSource().getSender() instanceof Entity entity)) return;
            entity.teleportAsync(level.getSpawnLocation(), COMMAND);
        }).exceptionally(throwable -> {
            plugin.getComponentLogger().warn("Failed to load world {}", name, throwable);
            plugin.bundle().sendMessage(context.getSource().getSender(), "world.load.failed",
                    Placeholder.parsed("world", name));
            return null;
        });
        return Command.SINGLE_SUCCESS;
    }
}
