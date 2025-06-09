package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.model.Generator;
import net.thenextlvl.worlds.api.model.WorldPreset;
import net.thenextlvl.worlds.api.preset.Preset;
import net.thenextlvl.worlds.command.argument.DimensionArgument;
import net.thenextlvl.worlds.command.argument.SeedArgument;
import net.thenextlvl.worlds.command.argument.WorldPresetArgument;
import net.thenextlvl.worlds.command.argument.WorldTypeArgument;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.concurrent.ThreadLocalRandom;

import static net.thenextlvl.worlds.command.WorldCommand.generatorArgument;
import static net.thenextlvl.worlds.command.WorldCommand.keyArgument;
import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND;

@NullMarked
class WorldCreateCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> create(WorldsPlugin plugin) {
        return Commands.literal("create")
                .requires(source -> source.getSender().hasPermission("worlds.command.create"))
                .then(createCommand(plugin));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, Key> createCommand(WorldsPlugin plugin) {
        return keyArgument()
                .then(generator(plugin))
                .then(preset(plugin))
                .then(type(plugin))
                .executes(context -> create(plugin, context));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> type(WorldsPlugin plugin) {
        return Commands.literal("type").then(Commands.argument("type", new WorldTypeArgument(plugin))
                .executes(context -> createType(context, World.Environment.NORMAL,
                        true, ThreadLocalRandom.current().nextLong(), plugin))
                .then(tree(WorldCreateCommand::createType, plugin)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> preset(WorldsPlugin plugin) {
        return Commands.literal("preset").then(Commands.argument("preset", new WorldPresetArgument(plugin))
                .executes(context -> createPreset(context, World.Environment.NORMAL,
                        true, ThreadLocalRandom.current().nextLong(), plugin))
                .then(tree(WorldCreateCommand::createPreset, plugin)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> generator(WorldsPlugin plugin) {
        return Commands.literal("generator").then(generatorArgument(plugin)
                .executes(context -> createGenerator(context, World.Environment.NORMAL,
                        true, ThreadLocalRandom.current().nextLong(), plugin))
                .then(tree(WorldCreateCommand::createGenerator, plugin)));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, World.Environment> tree(Creator<CommandSourceStack> creator, WorldsPlugin plugin) {
        return Commands.argument("dimension", new DimensionArgument(plugin))
                .then(structures(creator, plugin))
                .executes(context -> createDimension(creator, plugin, context));
    }

    private static int createDimension(Creator<CommandSourceStack> creator, WorldsPlugin plugin, CommandContext<CommandSourceStack> context) {
        var environment = context.getArgument("dimension", World.Environment.class);
        return creator.create(context, environment, true, ThreadLocalRandom.current().nextLong(), plugin);
    }

    private static RequiredArgumentBuilder<CommandSourceStack, Boolean> structures(Creator<CommandSourceStack> creator, WorldsPlugin plugin) {
        return Commands.argument("structures", BoolArgumentType.bool())
                .then(seed(creator, plugin))
                .executes(context -> createStructures(creator, plugin, context));
    }

    private static int createStructures(Creator<CommandSourceStack> creator, WorldsPlugin plugin, CommandContext<CommandSourceStack> context) {
        var environment = context.getArgument("dimension", World.Environment.class);
        var structures = context.getArgument("structures", boolean.class);
        return creator.create(context, environment, structures, ThreadLocalRandom.current().nextLong(), plugin);
    }

    private static RequiredArgumentBuilder<CommandSourceStack, Long> seed(Creator<CommandSourceStack> creator, WorldsPlugin plugin) {
        return Commands.argument("seed", new SeedArgument()).executes(context -> {
            var environment = context.getArgument("dimension", World.Environment.class);
            var structures = context.getArgument("structures", boolean.class);
            var seed = context.getArgument("seed", long.class);
            return creator.create(context, environment, structures, seed, plugin);
        });
    }

    private static int create(WorldsPlugin plugin, CommandContext<CommandSourceStack> context) {
        return create(context, World.Environment.NORMAL, true,
                ThreadLocalRandom.current().nextLong(), WorldPreset.NORMAL, null, null, plugin);
    }

    private static int create(CommandContext<CommandSourceStack> context, World.Environment environment, boolean structures,
                              long seed, WorldPreset type, @Nullable Preset preset, @Nullable Generator generator, WorldsPlugin plugin) {
        var keyInput = context.getNodes().stream()
                .filter(node -> node.getNode().getName().equals("key"))
                .map(ParsedCommandNode::getRange)
                .map(range -> range.get(context.getInput()))
                .findAny();

        var key = keyInput.map(string -> {
            var split = string.split(":", 2);
            if (split.length == 1) return new NamespacedKey("worlds", split[0]);
            return new NamespacedKey(split[0], split[1]);
        }).orElseGet(() -> context.getArgument("key", NamespacedKey.class));

        var name = key.getKey();

        var levelFolder = new File(plugin.getServer().getWorldContainer(), name);

        var level = plugin.levelBuilder(levelFolder)
                .environment(environment)
                .generator(generator)
                .key(key)
                .name(name)
                .preset(preset)
                .seed(seed)
                .structures(structures)
                .type(type)
                .build();

        var world = plugin.getServer().getWorld(level.key()) == null
                    && plugin.getServer().getWorld(level.name()) == null
                ? level.create().orElse(null) : null;

        var message = world != null ? "world.create.success" : "world.create.failed";
        plugin.bundle().sendMessage(context.getSource().getSender(), message,
                Placeholder.parsed("world", world != null ? world.getName() : key.asString()));

        // todo: extract duplicate, make it look less sketchy
        if (world != null && context.getSource().getSender() instanceof Entity entity) {
            if (plugin.isRunningFolia()) {
                plugin.getServer().getRegionScheduler().run(plugin, world, 0, 0, scheduledTask -> {
                    entity.teleportAsync(world.getSpawnLocation(), COMMAND);
                });
            } else entity.teleportAsync(world.getSpawnLocation(), COMMAND);
        }

        if (world != null) {
            plugin.persistWorld(world, true);
            if (generator != null) plugin.persistGenerator(world, generator);
            plugin.levelView().saveLevelData(world, true);
        }

        return world != null ? Command.SINGLE_SUCCESS : 0;
    }

    private static int createGenerator(CommandContext<CommandSourceStack> context, World.Environment environment, boolean structures, long seed, WorldsPlugin plugin) {
        var generator = context.getArgument("generator", Generator.class);
        return create(context, environment, structures, seed, WorldPreset.NORMAL, null, generator, plugin);
    }

    private static int createPreset(CommandContext<CommandSourceStack> context, World.Environment environment, boolean structures, long seed, WorldsPlugin plugin) {
        var preset = context.getArgument("preset", Preset.class);
        return create(context, environment, structures, seed, WorldPreset.FLAT, preset, null, plugin);
    }

    private static int createType(CommandContext<CommandSourceStack> context, World.Environment environment, boolean structures, long seed, WorldsPlugin plugin) {
        var type = context.getArgument("type", WorldPreset.class);
        return create(context, environment, structures, seed, type, null, null, plugin);
    }

    private interface Creator<S> {
        int create(CommandContext<S> context, World.Environment environment, boolean structures, long seed, WorldsPlugin plugin);
    }
}
