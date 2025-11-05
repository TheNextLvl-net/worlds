package net.thenextlvl.worlds.command;

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
import net.thenextlvl.worlds.command.argument.KeyArgument;
import net.thenextlvl.worlds.command.argument.LevelStemArgument;
import net.thenextlvl.worlds.command.argument.SeedArgument;
import net.thenextlvl.worlds.command.brigadier.OptionCommand;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jspecify.annotations.NullMarked;

import java.nio.file.Paths;
import java.util.Set;

import static net.thenextlvl.worlds.command.WorldCommand.worldArgument;
import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND;

@NullMarked
final class WorldRecreateCommand extends OptionCommand {
    private WorldRecreateCommand(WorldsPlugin plugin) {
        super(plugin, "recreate", "worlds.command.recreate");
    }

    public static ArgumentBuilder<CommandSourceStack, ?> create(WorldsPlugin plugin) {
        var command = new WorldRecreateCommand(plugin);
        return command.create().then(command.createCommand());
    }

    @Override
    protected RequiredArgumentBuilder<CommandSourceStack, ?> createCommand() {
        var name = Commands.argument("name", StringArgumentType.string());

        addOptions(name, false, Set.of(
                new Option("bonus-chest", BoolArgumentType.bool()),
                new Option("hardcore", BoolArgumentType.bool()),
                new Option("dimension", new LevelStemArgument(plugin)),
                new Option("key", new KeyArgument()),
                new Option("seed", new SeedArgument()),
                new Option("structures", BoolArgumentType.bool())
        ), null);

        return worldArgument(plugin).then(name.executes(this));
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        var sender = context.getSource().getSender();
        var world = context.getArgument("world", World.class);
        var name = context.getArgument("name", String.class);

        if (Paths.get(name).getNameCount() != 1) {
            plugin.bundle().sendMessage(sender, "world.subfolders.create");
            return 0;
        }

        var builder = plugin.levelBuilder(world).directory(plugin.levelView().findFreePath(name));

        tryGetArgument(context, "bonus-chest", Boolean.class).ifPresent(builder::bonusChest);
        tryGetArgument(context, "dimension", LevelStem.class).ifPresent(builder::levelStem);
        tryGetArgument(context, "generator", Generator.class).ifPresent(builder::generator);
        tryGetArgument(context, "hardcore", Boolean.class).ifPresent(builder::hardcore);
        tryGetArgument(context, "key", Key.class).ifPresentOrElse(builder::key, () ->
                builder.key(plugin.levelView().findFreeKey(world.key())));
        tryGetArgument(context, "preset", Preset.class).ifPresent(builder::preset);
        tryGetArgument(context, "seed", Long.class).ifPresent(builder::seed);
        tryGetArgument(context, "structures", Boolean.class).ifPresent(builder::structures);
        tryGetArgument(context, "type", GeneratorType.class).ifPresent(builder::generatorType);

        var level = builder.name(name).build();

        var placeholder = Placeholder.parsed("world", world.getName());

        plugin.bundle().sendMessage(sender, "world.recreate", placeholder);
        level.createAsync().thenAccept(recreated -> {
            plugin.bundle().sendMessage(sender, "world.recreate.success", placeholder);
            if (!(sender instanceof Entity entity)) return;
            entity.teleportAsync(recreated.getSpawnLocation(), COMMAND);
        }).exceptionally(throwable -> {
            plugin.getComponentLogger().warn("Failed to recreate world {}", world.getName(), throwable);
            plugin.bundle().sendMessage(sender, "world.recreate.failed", placeholder);
            return null;
        });
        return SINGLE_SUCCESS;
    }
}
