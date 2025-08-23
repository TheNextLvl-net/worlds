package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
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
import net.thenextlvl.worlds.api.preset.Preset;
import net.thenextlvl.worlds.command.argument.GeneratorArgument;
import net.thenextlvl.worlds.command.argument.GeneratorTypeArgument;
import net.thenextlvl.worlds.command.argument.KeyArgument;
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
                new Option("bonus-chest", BoolArgumentType.bool()),
                new Option("hardcore", BoolArgumentType.bool()),
                new Option("dimension", new LevelStemArgument(plugin)),
                new Option("key", new KeyArgument()),
                new Option("seed", new SeedArgument()),
                new Option("structures", BoolArgumentType.bool())
        ), null));

        return name.executes(this::execute);
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    protected int execute(CommandContext<CommandSourceStack> context) {
        var name = context.getArgument("name", String.class);
        var level = plugin.levelBuilder(plugin.levelView().findFreePath(name))
                .levelStem(tryGetArgument(context, "dimension", LevelStem.class))
                .generator(tryGetArgument(context, "generator", Generator.class))
                .key(tryGetArgument(context, "key", Key.class))
                .preset(tryGetArgument(context, "preset", Preset.class))
                .seed(tryGetArgument(context, "seed", Long.class))
                .structures(tryGetArgument(context, "structures", Boolean.class))
                .generatorType(tryGetArgument(context, "type", GeneratorType.class))
                .bonusChest(tryGetArgument(context, "bonus-chest", Boolean.class))
                .hardcore(tryGetArgument(context, "hardcore", Boolean.class))
                .name(name)
                .build();

        var sender = context.getSource().getSender();
        var placeholder = Placeholder.parsed("world", level.getName());
        
        plugin.bundle().sendMessage(sender, "world.create", placeholder);
        level.createAsync().thenAccept(world -> {
            plugin.bundle().sendMessage(sender, "world.create.success", placeholder);
            if (!(sender instanceof Entity entity)) return;
            entity.teleportAsync(world.getSpawnLocation(), COMMAND);
        }).exceptionally(throwable -> {
            plugin.getComponentLogger().warn("Failed to create world {} ({})", level.key(), name, throwable);
            plugin.bundle().sendMessage(sender, "world.create.failed", placeholder);
            return null;
        });
        return Command.SINGLE_SUCCESS;

    }
}
