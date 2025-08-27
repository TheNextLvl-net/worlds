package net.thenextlvl.worlds.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.level.Level;
import net.thenextlvl.worlds.command.brigadier.SimpleCommand;
import net.thenextlvl.worlds.command.suggestion.LevelSuggestionProvider;
import org.bukkit.entity.Entity;
import org.jspecify.annotations.NullMarked;

import java.nio.file.Path;

import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND;

@NullMarked
final class WorldLoadCommand extends SimpleCommand {
    private WorldLoadCommand(WorldsPlugin plugin) {
        super(plugin, "load", "worlds.command.load");
    }

    public static ArgumentBuilder<CommandSourceStack, ?> create(WorldsPlugin plugin) {
        var command = new WorldLoadCommand(plugin);
        return command.create().then(command.load());
    }

    private RequiredArgumentBuilder<CommandSourceStack, String> load() {
        return Commands.argument("path", StringArgumentType.string())
                .suggests(new LevelSuggestionProvider<>(plugin, false))
                .executes(this);
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        var path = context.getArgument("path", String.class);

        plugin.bundle().sendMessage(context.getSource().getSender(), "world.load",
                Placeholder.parsed("world", path));

        var build = plugin.levelView().read(Path.of(path)).map(Level.Builder::build);
        var future = build.filter(Level::isWorldKnown).map(Level::createAsync).orElse(null);

        if (future == null) {
            plugin.bundle().sendMessage(context.getSource().getSender(), "world.load.failed",
                    Placeholder.parsed("world", path));
            return 0;
        }

        future.thenAccept(level -> {
            plugin.levelView().setEnabled(level, true);
            plugin.bundle().sendMessage(context.getSource().getSender(), "world.load.success",
                    Placeholder.parsed("world", level.getName()));
            if (!(context.getSource().getSender() instanceof Entity entity)) return;
            entity.teleportAsync(level.getSpawnLocation(), COMMAND);
        }).exceptionally(throwable -> {
            plugin.getComponentLogger().warn("Failed to load world {}", path, throwable);
            plugin.bundle().sendMessage(context.getSource().getSender(), "world.load.failed",
                    Placeholder.parsed("world", path));
            return null;
        });
        return SINGLE_SUCCESS;
    }
}
