package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
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
import net.thenextlvl.worlds.command.argument.LevelStemArgument;
import net.thenextlvl.worlds.command.suggestion.LevelSuggestionProvider;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;

import static net.thenextlvl.worlds.command.WorldCommand.generatorArgument;
import static net.thenextlvl.worlds.command.WorldCommand.keyArgument;
import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND;

@NullMarked
class WorldImportCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> create(WorldsPlugin plugin) {
        return Commands.literal("import")
                .requires(source -> source.getSender().hasPermission("worlds.command.import"))
                .then(importWorld(plugin));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, String> importWorld(WorldsPlugin plugin) {
        return Commands.argument("world", StringArgumentType.string())
                .suggests(new LevelSuggestionProvider<>(plugin, true))
                .then(importKeyed(plugin))
                .executes(context -> execute(context, null, null, null, plugin));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, Key> importKeyed(WorldsPlugin plugin) {
        return keyArgument().then(importDimension(plugin)).executes(context -> {
            var key = context.getArgument("key", NamespacedKey.class);
            return execute(context, key, null, null, plugin);
        });
    }

    private static RequiredArgumentBuilder<CommandSourceStack, LevelStem> importDimension(WorldsPlugin plugin) {
        return Commands.argument("level-type", new LevelStemArgument(plugin))
                .then(importGenerator(plugin))
                .executes(context -> importWorld(plugin, context));
    }

    private static int importWorld(WorldsPlugin plugin, CommandContext<CommandSourceStack> context) {
        var levelStem = context.getArgument("level-type", LevelStem.class);
        var key = context.getArgument("key", NamespacedKey.class);
        return execute(context, key, levelStem, null, plugin);
    }

    private static RequiredArgumentBuilder<CommandSourceStack, Generator> importGenerator(WorldsPlugin plugin) {
        return generatorArgument(plugin).executes(context -> {
            var levelStem = context.getArgument("level-type", LevelStem.class);
            var generator = context.getArgument("generator", Generator.class);
            var key = context.getArgument("key", NamespacedKey.class);
            return execute(context, key, levelStem, generator, plugin);
        });
    }

    private static int execute(CommandContext<CommandSourceStack> context, @Nullable NamespacedKey key,
                               @Nullable LevelStem levelStem, @Nullable Generator generator, WorldsPlugin plugin) {
        var name = context.getArgument("world", String.class);
        var build = plugin.levelView().read(Path.of(name))
                .map(level -> level.levelStem(levelStem).generator(generator).key(key).build());
        var world = build.filter(level -> !level.isWorldKnown()).map(Level::createAsync).orElse(null);

        if (world == null) {
            plugin.bundle().sendMessage(context.getSource().getSender(), "world.import.failed",
                    Placeholder.parsed("world", name));
            return 0;
        }

        plugin.bundle().sendMessage(context.getSource().getSender(), "world.import",
                Placeholder.parsed("world", name));
        world.thenAccept(level -> {
            plugin.bundle().sendMessage(context.getSource().getSender(), "world.import.success",
                    Placeholder.parsed("world", level.getName()));

            plugin.levelView().persistWorld(level, true);
            if (generator != null) plugin.levelView().persistGenerator(level, generator);
            plugin.levelView().saveLevelDataAsync(level);

            if (!(context.getSource().getSender() instanceof Entity entity)) return;
            entity.teleportAsync(level.getSpawnLocation(), COMMAND);
        }).exceptionally(throwable -> {
            plugin.bundle().sendMessage(context.getSource().getSender(), "world.import.failed",
                    Placeholder.parsed("world", name));
            plugin.getComponentLogger().warn("Failed to import world {}", name, throwable);
            return null;
        });

        return Command.SINGLE_SUCCESS;
    }
}
