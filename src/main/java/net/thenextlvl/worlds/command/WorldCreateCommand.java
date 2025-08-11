package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.generator.Generator;
import net.thenextlvl.worlds.api.generator.GeneratorType;
import net.thenextlvl.worlds.api.generator.LevelStem;
import net.thenextlvl.worlds.api.preset.Preset;
import net.thenextlvl.worlds.command.argument.GeneratorArgument;
import net.thenextlvl.worlds.command.argument.GeneratorTypeArgument;
import net.thenextlvl.worlds.command.argument.LevelStemArgument;
import net.thenextlvl.worlds.command.argument.SeedArgument;
import net.thenextlvl.worlds.command.argument.WorldPresetArgument;
import org.bukkit.entity.Entity;
import org.jspecify.annotations.NullMarked;

import java.util.Set;

import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND;

@NullMarked
class WorldCreateCommand extends OptionCommand {

    private WorldCreateCommand(WorldsPlugin plugin) {
        super(plugin);
    }

    public static ArgumentBuilder<CommandSourceStack, ?> create(WorldsPlugin plugin) {
        var command = new WorldCreateCommand(plugin);
        return Commands.literal("create")
                .requires(source -> source.getSender().hasPermission("worlds.command.create"))
                .then(command.createCommand());
    }

    private RequiredArgumentBuilder<CommandSourceStack, ?> createCommand() {
        var name = Commands.argument("name", StringArgumentType.string());

        addOptions(name, true, Set.of(
                new Option("generator", new GeneratorArgument(plugin)),
                new Option("preset", new WorldPresetArgument(plugin)),
                new Option("type", new GeneratorTypeArgument(plugin))
        ), builder -> addOptions(builder, false, Set.of(
                new Option("dimension", new LevelStemArgument(plugin)),
                new Option("key", ArgumentTypes.key()),
                new Option("seed", new SeedArgument()),
                new Option("structures", BoolArgumentType.bool())
        ), null));

        return name;
    }

    @Override
    protected int execute(CommandContext<CommandSourceStack> context) {
        // todo: custom parser
        // var keyInput = context.getNodes().stream()
        //         .filter(node -> node.getNode().getName().equals("key"))
        //         .map(ParsedCommandNode::getRange)
        //         .map(range -> range.get(context.getInput()))
        //         .findAny();
        // var key = keyInput.map(Key::key).orElseGet(() -> tryGetArgument(context, "key", Key.class));

        var generator = tryGetArgument(context, "generator", Generator.class);
        var key = tryGetArgument(context, "key", Key.class);
        var dimension = tryGetArgument(context, "dimension", LevelStem.class);
        var preset = tryGetArgument(context, "preset", Preset.class);
        var seed = tryGetArgument(context, "seed", Long.class);
        var structures = tryGetArgument(context, "structures", Boolean.class);
        var type = tryGetArgument(context, "type", GeneratorType.class);

        var name = context.getArgument("name", String.class);
        var level = plugin.levelBuilder(plugin.levelView().findFreePath(name))
                .name(name)
                .levelStem(dimension)
                .generator(generator)
                .key(key)
                .preset(preset)
                .seed(seed)
                .structures(structures)
                .generatorType(type)
                .build();

        plugin.bundle().sendMessage(context.getSource().getSender(), "world.create",
                Placeholder.parsed("world", name));
        level.createAsync().thenAccept(world -> {
            plugin.bundle().sendMessage(context.getSource().getSender(), "world.create.success",
                    Placeholder.parsed("world", world.getName()));
            if (!(context.getSource().getSender() instanceof Entity entity)) return;
            entity.teleportAsync(world.getSpawnLocation(), COMMAND);
        }).exceptionally(throwable -> {
            plugin.getComponentLogger().warn("Failed to create world {} ({})", key, name, throwable);
            plugin.bundle().sendMessage(context.getSource().getSender(), "world.create.failed",
                    Placeholder.parsed("world", name));
            return null;
        });
        return Command.SINGLE_SUCCESS;

    }
}
