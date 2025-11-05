package net.thenextlvl.worlds.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.generator.Generator;
import net.thenextlvl.worlds.api.generator.LevelStem;
import net.thenextlvl.worlds.api.level.Level;
import net.thenextlvl.worlds.command.argument.GeneratorArgument;
import net.thenextlvl.worlds.command.argument.KeyArgument;
import net.thenextlvl.worlds.command.argument.LevelPathArgument;
import net.thenextlvl.worlds.command.argument.LevelStemArgument;
import net.thenextlvl.worlds.command.brigadier.OptionCommand;
import org.bukkit.entity.Entity;
import org.jspecify.annotations.NullMarked;

import java.nio.file.Path;
import java.util.Set;

import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND;

@NullMarked
final class WorldImportCommand extends OptionCommand {
    private WorldImportCommand(WorldsPlugin plugin) {
        super(plugin, "import", "worlds.command.import");
    }

    public static ArgumentBuilder<CommandSourceStack, ?> create(WorldsPlugin plugin) {
        var command = new WorldImportCommand(plugin);
        return command.create().then(command.createCommand());
    }

    @Override
    protected RequiredArgumentBuilder<CommandSourceStack, Path> createCommand() {
        var command = Commands.argument("path", new LevelPathArgument(plugin, true)).executes(this);

        addOptions(command, false, Set.of(
                new Option("key", new KeyArgument()),
                new Option("generator", new GeneratorArgument(plugin)),
                new Option("name", StringArgumentType.string()),
                new Option("dimension", new LevelStemArgument(plugin))
        ), null);

        return command;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        var sender = context.getSource().getSender();
        var path = context.getArgument("path", Path.class);
        var container = plugin.levelView().getWorldContainer();

            plugin.bundle().sendMessage(sender, "world.subfolders.load");
        if (!path.startsWith(container) || path.getNameCount() != container.getNameCount() + 1) {
            return 0;
        }

        var displayName = tryGetArgument(context, "name", String.class).orElse(null);
        var generator = tryGetArgument(context, "generator", Generator.class).orElse(null);
        var key = tryGetArgument(context, "key", Key.class).orElse(null);
        var dimension = tryGetArgument(context, "dimension", LevelStem.class).orElse(null);

        var build = plugin.levelView().read(path).map(level ->
                level.key(key).name(displayName).generator(generator).levelStem(dimension).build());
        var world = build.filter(level -> !level.isWorldKnown()).map(Level::createAsync).orElse(null);

        if (world == null) {
            plugin.bundle().sendMessage(context.getSource().getSender(), "world.import.failed",
                    Placeholder.parsed("world", path.toString()));
            return 0;
        }

        plugin.bundle().sendMessage(context.getSource().getSender(), "world.import",
                Placeholder.parsed("world", path.toString()));
        world.thenAccept(level -> {
            plugin.bundle().sendMessage(context.getSource().getSender(), "world.import.success",
                    Placeholder.parsed("world", level.getName()));
            if (!(context.getSource().getSender() instanceof Entity entity)) return;
            entity.teleportAsync(level.getSpawnLocation(), COMMAND);
        }).exceptionally(throwable -> {
            plugin.bundle().sendMessage(context.getSource().getSender(), "world.import.failed",
                    Placeholder.parsed("world", path.toString()));
            plugin.getComponentLogger().warn("Failed to import world {}", path, throwable);
            return null;
        });

        return SINGLE_SUCCESS;
    }
}
