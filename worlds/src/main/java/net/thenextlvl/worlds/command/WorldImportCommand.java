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
import net.thenextlvl.worlds.api.model.Generator;
import net.thenextlvl.worlds.api.model.Level;
import net.thenextlvl.worlds.command.argument.DimensionArgument;
import net.thenextlvl.worlds.command.suggestion.LevelSuggestionProvider;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

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
                .suggests(new LevelSuggestionProvider<>(plugin))
                .then(importKeyed(plugin))
                .executes(context -> execute(context, null, null, null, plugin));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, Key> importKeyed(WorldsPlugin plugin) {
        return keyArgument().then(importDimension(plugin)).executes(context -> {
            var key = context.getArgument("key", NamespacedKey.class);
            return execute(context, key, null, null, plugin);
        });
    }

    private static RequiredArgumentBuilder<CommandSourceStack, World.Environment> importDimension(WorldsPlugin plugin) {
        return Commands.argument("dimension", new DimensionArgument(plugin))
                .then(importGenerator(plugin))
                .executes(context -> importWorld(plugin, context));
    }

    private static int importWorld(WorldsPlugin plugin, CommandContext<CommandSourceStack> context) {
        var environment = context.getArgument("dimension", World.Environment.class);
        var key = context.getArgument("key", NamespacedKey.class);
        return execute(context, key, environment, null, plugin);
    }

    private static RequiredArgumentBuilder<CommandSourceStack, Generator> importGenerator(WorldsPlugin plugin) {
        return generatorArgument(plugin).executes(context -> {
            var environment = context.getArgument("dimension", World.Environment.class);
            var generator = context.getArgument("generator", Generator.class);
            var key = context.getArgument("key", NamespacedKey.class);
            return execute(context, key, environment, generator, plugin);
        });
    }

    private static int execute(CommandContext<CommandSourceStack> context, @Nullable NamespacedKey key,
                               World.@Nullable Environment environment, @Nullable Generator generator, WorldsPlugin plugin) {
        var name = context.getArgument("world", String.class);
        var levelFolder = plugin.getServer().getWorldContainer().toPath().resolve(name);

        var build = plugin.levelView().isLevel(levelFolder)
                ? plugin.levelBuilder(levelFolder).environment(environment)
                .generator(generator).key(key).build() : null;

        var world = Optional.ofNullable(build)
                .filter(level -> !level.importedBefore())
                .flatMap(Level::create)
                .orElse(null);

        var message = world != null ? "world.import.success" : "world.import.failed";
        plugin.bundle().sendMessage(context.getSource().getSender(), message,
                Placeholder.parsed("world", world != null ? world.getName() : name));

        if (world != null && context.getSource().getSender() instanceof Entity entity)
            entity.teleportAsync(world.getSpawnLocation(), COMMAND);

        if (world != null) {
            plugin.persistWorld(world, true);
            if (generator != null) plugin.persistGenerator(world, generator);
            plugin.levelView().saveLevelData(world, true);
        }

        return world != null ? Command.SINGLE_SUCCESS : 0;
    }
}
