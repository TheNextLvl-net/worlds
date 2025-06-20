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
        var world = build.filter(level -> !level.isWorldKnown()).flatMap(Level::create).orElse(null);

        var message = world != null ? "world.import.success" : "world.import.failed";
        plugin.bundle().sendMessage(context.getSource().getSender(), message,
                Placeholder.parsed("world", world != null ? world.getName() : name));

        // todo: extract duplicate, make it look less sketchy
        if (world != null && context.getSource().getSender() instanceof Entity entity) {
            if (WorldsPlugin.RUNNING_FOLIA) {
                plugin.getServer().getRegionScheduler().run(plugin, world, 0, 0, scheduledTask -> {
                    entity.teleportAsync(world.getSpawnLocation(), COMMAND);
                });
            } else entity.teleportAsync(world.getSpawnLocation(), COMMAND);
        }

        if (world != null) {
            plugin.levelView().persistWorld(world, true);
            if (generator != null) plugin.levelView().persistGenerator(world, generator);
            plugin.levelView().saveLevelData(world, true);
        }

        return world != null ? Command.SINGLE_SUCCESS : 0;
    }
}
