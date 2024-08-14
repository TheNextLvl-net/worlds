package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.argument.DimensionArgument;
import net.thenextlvl.worlds.command.suggestion.LevelSuggestionProvider;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Optional;

import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND;

@RequiredArgsConstructor
@SuppressWarnings("UnstableApiUsage")
class WorldImportCommand {
    private final WorldsPlugin plugin;

    ArgumentBuilder<CommandSourceStack, ?> create() {
        return Commands.literal("import")
                .requires(source -> source.getSender().hasPermission("worlds.command.import"))
                .then(Commands.argument("world", StringArgumentType.string())
                        .suggests(new LevelSuggestionProvider<>(plugin))
                        .then(Commands.argument("dimension", new DimensionArgument(plugin))
                                .then(Commands.argument("key", ArgumentTypes.namespacedKey())
                                        .executes(context -> {
                                            var environment = context.getArgument("dimension", World.Environment.class);
                                            var key = context.getArgument("key", NamespacedKey.class);
                                            return execute(context, environment, key);
                                        }))
                                .executes(context -> {
                                    var environment = context.getArgument("dimension", World.Environment.class);
                                    return execute(context, environment, null);
                                }))
                        .executes(context -> execute(context, null, null)));
    }

    private int execute(CommandContext<CommandSourceStack> context, @Nullable World.Environment environment, @Nullable NamespacedKey key) {
        var name = context.getArgument("world", String.class);
        var level = new File(plugin.getServer().getWorldContainer(), name);
        // todo: define key
        var world = plugin.levelView().isLevel(level) ? environment != null
                ? plugin.levelView().loadLevel(level, environment, Optional::isEmpty)
                : plugin.levelView().loadLevel(level, Optional::isEmpty) : null;

        var message = world != null ? "world.import.success" : "world.import.failed";
        plugin.bundle().sendMessage(context.getSource().getSender(), message,
                Placeholder.parsed("world", world != null ? world.key().asString()
                        : key != null ? key.asString() : name));

        if (world != null && context.getSource().getSender() instanceof Entity entity)
            entity.teleportAsync(world.getSpawnLocation(), COMMAND);

        if (world != null) {
            plugin.persistWorld(world, true);
            plugin.levelView().saveLevelData(world, true);
        }

        return world != null ? Command.SINGLE_SUCCESS : 0;
    }
}
