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
import net.thenextlvl.worlds.api.level.Level;
import net.thenextlvl.worlds.api.preset.Preset;
import net.thenextlvl.worlds.command.argument.GeneratorArgument;
import net.thenextlvl.worlds.command.argument.GeneratorTypeArgument;
import net.thenextlvl.worlds.command.argument.KeyArgument;
import net.thenextlvl.worlds.command.argument.LevelStemArgument;
import net.thenextlvl.worlds.command.argument.SeedArgument;
import net.thenextlvl.worlds.command.argument.WorldPresetArgument;
import net.thenextlvl.worlds.command.brigadier.OptionCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Set;

import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND;

@NullMarked
final class WorldCreateCommand extends OptionCommand {
    private WorldCreateCommand(WorldsPlugin plugin) {
        super(plugin, "create", "worlds.command.create");
    }

    public static ArgumentBuilder<CommandSourceStack, ?> create(WorldsPlugin plugin) {
        var command = new WorldCreateCommand(plugin);
        return command.create().then(command.createCommand());
    }

    @Override
    protected RequiredArgumentBuilder<CommandSourceStack, ?> createCommand() {
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

        return name.executes(this);
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        var sender = context.getSource().getSender();
        var level = buildLevel(context, sender);
        if (level == null) return 0;

        var placeholder = Placeholder.parsed("world", level.getName());
        if (plugin.getServer().getWorld(level.getName()) != null) {
            plugin.bundle().sendMessage(sender, "world.name.taken", placeholder);
            return 0;
        } else if (plugin.getServer().getWorld(level.key()) != null) {
            plugin.bundle().sendMessage(sender, "world.key.taken", Placeholder.parsed("key", level.key().asString()));
            return 0;
        }

        plugin.bundle().sendMessage(sender, "world.create", placeholder);
        level.createAsync().thenAccept(world -> {
            plugin.bundle().sendMessage(sender, "world.create.success", placeholder);
            if (!(sender instanceof Entity entity)) return;
            entity.teleportAsync(world.getSpawnLocation(), COMMAND);
        }).exceptionally(throwable -> {
            plugin.getComponentLogger().warn("Failed to create world {} ({})", level.key(), level.getName(), throwable);
            plugin.bundle().sendMessage(sender, "world.create.failed", placeholder);
            return null;
        });
        return SINGLE_SUCCESS;
    }

    private @Nullable Level buildLevel(CommandContext<CommandSourceStack> context, CommandSender sender) {
        var name = context.getArgument("name", String.class);
        try {
            return plugin.levelBuilder(plugin.levelView().findFreePath(name).getFileName())
                    .levelStem(tryGetArgument(context, "dimension", LevelStem.class).orElse(null))
                    .generator(tryGetArgument(context, "generator", Generator.class).orElse(null))
                    .key(tryGetArgument(context, "key", Key.class).orElse(null))
                    .preset(tryGetArgument(context, "preset", Preset.class).orElse(null))
                    .seed(tryGetArgument(context, "seed", Long.class).orElse(null))
                    .structures(tryGetArgument(context, "structures", Boolean.class).orElse(null))
                    .generatorType(tryGetArgument(context, "type", GeneratorType.class).orElse(null))
                    .bonusChest(tryGetArgument(context, "bonus-chest", Boolean.class).orElse(null))
                    .hardcore(tryGetArgument(context, "hardcore", Boolean.class).orElse(null))
                    .name(name)
                    .build();
        } catch (Exception e) {
            plugin.getComponentLogger().warn("Failed to create world {}", name, e);
            plugin.bundle().sendMessage(sender, "world.create.failed", Placeholder.parsed("world", name));
            return null;
        }
    }
}
