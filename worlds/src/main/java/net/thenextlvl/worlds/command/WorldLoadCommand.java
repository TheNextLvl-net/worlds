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
        var world = build.filter(Level::isWorldKnown).flatMap(Level::create).orElse(null);

        var message = world != null ? "world.load.success" : "world.load.failed";
        plugin.bundle().sendMessage(context.getSource().getSender(), message,
                Placeholder.parsed("world", world != null ? world.getName() : name));

        // todo: extract duplicate, make it look less sketchy
        if (world != null && context.getSource().getSender() instanceof Entity entity) {
            if (plugin.isRunningFolia()) {
                plugin.getServer().getRegionScheduler().run(plugin, world, 0, 0, scheduledTask -> {
                    entity.teleportAsync(world.getSpawnLocation(), COMMAND);
                });
            } else entity.teleportAsync(world.getSpawnLocation(), COMMAND);
        }

        if (world != null) {
            plugin.levelView().persistStatus(world, true, true);
            plugin.levelView().saveLevelData(world, true);
        }

        return world != null ? Command.SINGLE_SUCCESS : 0;
    }
}
