package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.model.Generator;
import net.thenextlvl.worlds.api.model.WorldPreset;
import net.thenextlvl.worlds.api.preset.Preset;
import net.thenextlvl.worlds.command.argument.*;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.concurrent.ThreadLocalRandom;

import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND;

@RequiredArgsConstructor
@SuppressWarnings("UnstableApiUsage")
class WorldCreateCommand {
    private final WorldsPlugin plugin;

    ArgumentBuilder<CommandSourceStack, ?> create() {
        return Commands.literal("create")
                .requires(source -> source.getSender().hasPermission("worlds.command.create"))
                .then(Commands.argument("key", ArgumentTypes.namespacedKey())
                        .then(Commands.literal("generator")
                                .then(Commands.argument("generator", new GeneratorArgument(plugin))
                                        .executes(context -> createGenerator(context, World.Environment.NORMAL,
                                                true, ThreadLocalRandom.current().nextLong()))
                                        .then(tree(this::createGenerator))))
                        .then(Commands.literal("preset")
                                .then(Commands.argument("preset", new WorldPresetArgument(plugin))
                                        .executes(context -> createPreset(context, World.Environment.NORMAL,
                                                true, ThreadLocalRandom.current().nextLong()))
                                        .then(tree(this::createPreset))))
                        .then(Commands.literal("type")
                                .then(Commands.argument("type", new WorldTypeArgument(plugin))
                                        .executes(context -> createType(context, World.Environment.NORMAL,
                                                true, ThreadLocalRandom.current().nextLong()))
                                        .then(tree(this::createType))))
                        .executes(context -> create(context, World.Environment.NORMAL, true,
                                ThreadLocalRandom.current().nextLong(), WorldPreset.NORMAL, null, null)));
    }

    private RequiredArgumentBuilder<CommandSourceStack, World.Environment> tree(Creator<CommandSourceStack> creator) {
        return Commands.argument("dimension", new DimensionArgument(plugin))
                .then(Commands.argument("structures", BoolArgumentType.bool())
                        .then(Commands.argument("seed", new SeedArgument())
                                .executes(context -> {
                                    var environment = context.getArgument("dimension", World.Environment.class);
                                    var structures = context.getArgument("structures", boolean.class);
                                    var seed = context.getArgument("seed", long.class);
                                    return creator.create(context, environment, structures, seed);
                                }))
                        .executes(context -> {
                            var environment = context.getArgument("dimension", World.Environment.class);
                            var structures = context.getArgument("structures", boolean.class);
                            return creator.create(context, environment, structures,
                                    ThreadLocalRandom.current().nextLong());
                        }))
                .executes(context -> {
                    var environment = context.getArgument("dimension", World.Environment.class);
                    return creator.create(context, environment, true, ThreadLocalRandom.current().nextLong());
                });
    }

    private int create(CommandContext<CommandSourceStack> context, World.Environment environment, boolean structures,
                       long seed, WorldPreset type, @Nullable Preset preset, @Nullable Generator generator) {
        var key = context.getArgument("key", NamespacedKey.class);
        var name = key.getKey();

        var levelFolder = new File(plugin.getServer().getWorldContainer(), name);

        var level = plugin.levelBuilder(levelFolder)
                .environment(environment)
                .generator(generator)
                .preset(preset)
                .seed(seed)
                .name(name)
                .structures(structures)
                .type(type)
                .build();

        var world = plugin.getServer().getWorld(level.key()) == null
                    && plugin.getServer().getWorld(level.name()) == null
                ? level.create().orElse(null) : null;

        var message = world != null ? "world.create.success" : "world.create.failed";
        plugin.bundle().sendMessage(context.getSource().getSender(), message,
                Placeholder.parsed("world", world != null ? world.getName() : key.asString()));

        if (world != null && context.getSource().getSender() instanceof Entity entity)
            entity.teleportAsync(world.getSpawnLocation(), COMMAND);

        if (world != null) {
            plugin.persistWorld(world, true);
            if (generator != null) plugin.persistGenerator(world, generator);
            plugin.levelView().saveLevelData(world, true);
        }

        return world != null ? Command.SINGLE_SUCCESS : 0;
    }

    private int createGenerator(CommandContext<CommandSourceStack> context, World.Environment environment, boolean structures, long seed) {
        var generator = context.getArgument("generator", Generator.class);
        return create(context, environment, structures, seed, WorldPreset.NORMAL, null, generator);
    }

    private int createPreset(CommandContext<CommandSourceStack> context, World.Environment environment, boolean structures, long seed) {
        var preset = context.getArgument("preset", Preset.class);
        return create(context, environment, structures, seed, WorldPreset.FLAT, preset, null);
    }

    private int createType(CommandContext<CommandSourceStack> context, World.Environment environment, boolean structures, long seed) {
        var type = context.getArgument("type", WorldPreset.class);
        return create(context, environment, structures, seed, type, null, null);
    }

    private interface Creator<S> {
        int create(CommandContext<S> context, World.Environment environment, boolean structures, long seed);
    }
}
