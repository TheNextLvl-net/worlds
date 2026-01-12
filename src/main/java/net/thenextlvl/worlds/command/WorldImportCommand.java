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
import net.thenextlvl.worlds.api.generator.GeneratorType;
import net.thenextlvl.worlds.api.generator.LevelStem;
import net.thenextlvl.worlds.api.level.Level;
import net.thenextlvl.worlds.api.preset.Preset;
import net.thenextlvl.worlds.command.argument.GeneratorArgument;
import net.thenextlvl.worlds.command.argument.KeyArgument;
import net.thenextlvl.worlds.command.argument.LevelPathArgument;
import net.thenextlvl.worlds.command.argument.LevelStemArgument;
import net.thenextlvl.worlds.command.argument.WorldPresetArgument;
import net.thenextlvl.worlds.command.brigadier.OptionCommand;
import net.thenextlvl.worlds.command.suggestion.LevelSuggestionProvider;
import org.bukkit.entity.Entity;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;
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
        var command = Commands.argument("path", new LevelPathArgument(plugin))
                .suggests(new LevelSuggestionProvider(plugin, true)).executes(this);

        addOptions(command, false, Set.of(
                new Option("dimension", new LevelStemArgument(plugin)),
                new Option("directory", new LevelPathArgument(plugin)),
                new Option("generator", new GeneratorArgument(plugin), "preset"),
                new Option("key", new KeyArgument()),
                new Option("name", StringArgumentType.string()),
                new Option("preset", new WorldPresetArgument(plugin), "generator")
        ), null);

        return command;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        var sender = context.getSource().getSender();
        var path = context.getArgument("path", Path.class);
        var container = plugin.levelView().getWorldContainer();

        if (!path.startsWith(container) || path.getNameCount() != container.getNameCount() + 1) {
            plugin.bundle().sendMessage(sender, "world.container.load");
            return 0;
        }

        var preset = tryGetArgument(context, "preset", Preset.class).orElse(null);
        var dimension = tryGetArgument(context, "dimension", LevelStem.class).orElse(null);
        var directory = tryGetArgument(context, "directory", Path.class).filter(dir -> !dir.equals(path)).orElse(null);
        var displayName = tryGetArgument(context, "name", String.class).orElse(null);
        var generator = tryGetArgument(context, "generator", Generator.class).orElse(null);
        var key = tryGetArgument(context, "key", Key.class).orElse(null);

        var name = displayName != null ? displayName : path.getFileName().toString();

        plugin.bundle().sendMessage(sender, "world.import", Placeholder.parsed("world", name));

        if (directory != null) {
            try {
                plugin.levelView().copyDirectory(path, directory, null);
            } catch (IOException e) {
                plugin.getComponentLogger().warn("Failed to copy world {} to {}", path, directory, e);
                plugin.bundle().sendMessage(sender, "world.import.failed", Placeholder.parsed("world", name));
                return 0;
            }
        }

        var build = plugin.levelView().read(path).map(level -> {
            if (directory != null) level
                    .directory(directory)
                    .worldKnown(false);
            if (preset != null) level
                    .generatorType(GeneratorType.FLAT)
                    .ignoreLevelData(true)
                    .preset(preset);
            return level.key(key)
                    .generator(generator)
                    .levelStem(dimension)
                    .name(displayName)
                    .build();
        });
        var world = build.filter(level -> !level.isWorldKnown()).map(Level::createAsync).orElse(null);

        if (world == null) {
            plugin.bundle().sendMessage(sender, "world.import.failed", Placeholder.parsed("world", name));
            return 0;
        }

        world.thenAccept(level -> {
            plugin.bundle().sendMessage(sender, "world.import.success",
                    Placeholder.parsed("world", level.getName()));
            if (!(sender instanceof Entity entity)) return;
            entity.teleportAsync(level.getSpawnLocation(), COMMAND);
        }).exceptionally(throwable -> {
            plugin.bundle().sendMessage(sender, "world.import.failed", Placeholder.parsed("world", name));
            var t = throwable.getCause() != null ? throwable.getCause() : throwable;
            plugin.getComponentLogger().warn("Failed to import world {}", name, t);
            return null;
        });

        return SINGLE_SUCCESS;
    }
}
