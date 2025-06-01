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
import net.thenextlvl.worlds.api.generator.Generator;
import net.thenextlvl.worlds.api.generator.GeneratorType;
import net.thenextlvl.worlds.api.generator.LevelStem;
import net.thenextlvl.worlds.api.preset.Preset;
import net.thenextlvl.worlds.command.argument.GeneratorTypeArgument;
import net.thenextlvl.worlds.command.argument.LevelStemArgument;
import net.thenextlvl.worlds.command.argument.SeedArgument;
import net.thenextlvl.worlds.command.argument.WorldPresetArgument;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;

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
        return Commands.literal("type").then(Commands.argument("type", new GeneratorTypeArgument(plugin))
                .executes(context -> createType(context, LevelStem.OVERWORLD, true, null, plugin))
                .then(tree(WorldCreateCommand::createType, plugin)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> preset(WorldsPlugin plugin) {
        return Commands.literal("preset").then(Commands.argument("preset", new WorldPresetArgument(plugin))
                .executes(context -> createPreset(context, LevelStem.OVERWORLD, true, null, plugin))
                .then(tree(WorldCreateCommand::createPreset, plugin)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> generator(WorldsPlugin plugin) {
        return Commands.literal("generator").then(generatorArgument(plugin)
                .executes(context -> createGenerator(context, LevelStem.OVERWORLD, true, null, plugin))
                .then(tree(WorldCreateCommand::createGenerator, plugin)));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, LevelStem> tree(Creator<CommandSourceStack> creator, WorldsPlugin plugin) {
        return Commands.argument("level-type", new LevelStemArgument(plugin))
                .then(structures(creator, plugin))
                .executes(context -> createDimension(creator, plugin, context));
    }

    private static int createDimension(Creator<CommandSourceStack> creator, WorldsPlugin plugin, CommandContext<CommandSourceStack> context) {
        var levelStem = context.getArgument("level-type", LevelStem.class);
        return creator.create(context, levelStem, true, null, plugin);
    }

    private static RequiredArgumentBuilder<CommandSourceStack, Boolean> structures(Creator<CommandSourceStack> creator, WorldsPlugin plugin) {
        return Commands.argument("structures", BoolArgumentType.bool())
                .then(seed(creator, plugin))
                .executes(context -> createStructures(creator, plugin, context));
    }

    private static int createStructures(Creator<CommandSourceStack> creator, WorldsPlugin plugin, CommandContext<CommandSourceStack> context) {
        var levelStem = context.getArgument("level-type", LevelStem.class);
        var structures = context.getArgument("structures", boolean.class);
        return creator.create(context, levelStem, structures, null, plugin);
    }

    private static RequiredArgumentBuilder<CommandSourceStack, Long> seed(Creator<CommandSourceStack> creator, WorldsPlugin plugin) {
        return Commands.argument("seed", new SeedArgument()).executes(context -> {
            var levelStem = context.getArgument("level-type", LevelStem.class);
            var structures = context.getArgument("structures", boolean.class);
            var seed = context.getArgument("seed", long.class);
            return creator.create(context, levelStem, structures, seed, plugin);
        });
    }

    private static int create(WorldsPlugin plugin, CommandContext<CommandSourceStack> context) {
        return create(context, LevelStem.OVERWORLD, true, null, GeneratorType.NORMAL, null, null, plugin);
    }

    private static int create(CommandContext<CommandSourceStack> context, LevelStem levelStem, boolean structures,
                              @Nullable Long seed, GeneratorType type, @Nullable Preset preset, @Nullable Generator generator, WorldsPlugin plugin) {
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

        var level = plugin.levelBuilder(Path.of(name))
                .levelStem(levelStem)
                .generator(generator)
                .key(key)
                .name(name)
                .preset(preset)
                .seed(seed)
                .structures(structures)
                .generatorType(type)
                .build();

        var world = plugin.getServer().getWorld(level.key()) == null
                    && plugin.getServer().getWorld(level.getName()) == null
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

    private static int createGenerator(CommandContext<CommandSourceStack> context, LevelStem levelStem, boolean structures, @Nullable Long seed, WorldsPlugin plugin) {
        var generator = context.getArgument("generator", Generator.class);
        return create(context, levelStem, structures, seed, GeneratorType.NORMAL, null, generator, plugin);
    }

    private static int createPreset(CommandContext<CommandSourceStack> context, LevelStem levelStem, boolean structures, @Nullable Long seed, WorldsPlugin plugin) {
        var preset = context.getArgument("preset", Preset.class);
        return create(context, levelStem, structures, seed, GeneratorType.FLAT, preset, null, plugin);
    }

    private static int createType(CommandContext<CommandSourceStack> context, LevelStem levelStem, boolean structures, @Nullable Long seed, WorldsPlugin plugin) {
        var type = context.getArgument("type", GeneratorType.class);
        return create(context, levelStem, structures, seed, type, null, null, plugin);
    }

    private interface Creator<S> {
        int create(CommandContext<S> context, LevelStem levelStem, boolean structures, @Nullable Long seed, WorldsPlugin plugin);
    }
}
