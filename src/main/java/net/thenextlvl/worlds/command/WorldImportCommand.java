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
import net.thenextlvl.worlds.api.model.Generator;
import net.thenextlvl.worlds.api.model.Level;
import net.thenextlvl.worlds.command.argument.DimensionArgument;
import net.thenextlvl.worlds.command.argument.GeneratorArgument;
import net.thenextlvl.worlds.command.suggestion.LevelSuggestionProvider;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.Optional;

import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND;

@NullMarked
@RequiredArgsConstructor
class WorldImportCommand {
    private final WorldsPlugin plugin;

    ArgumentBuilder<CommandSourceStack, ?> create() {
        return Commands.literal("import")
                .requires(source -> source.getSender().hasPermission("worlds.command.import"))
                .then(Commands.argument("world", StringArgumentType.string())
                        .suggests(new LevelSuggestionProvider<>(plugin))
                        .then(Commands.argument("key", ArgumentTypes.namespacedKey())
                                .then(Commands.argument("dimension", new DimensionArgument(plugin))
                                        .then(Commands.argument("generator", new GeneratorArgument(plugin))
                                                .executes(context -> {
                                                    var environment = context.getArgument("dimension", World.Environment.class);
                                                    var generator = context.getArgument("generator", Generator.class);
                                                    var key = context.getArgument("key", NamespacedKey.class);
                                                    return execute(context, key, environment, generator);
                                                }))
                                        .executes(context -> {
                                            var environment = context.getArgument("dimension", World.Environment.class);
                                            var key = context.getArgument("key", NamespacedKey.class);
                                            return execute(context, key, environment, null);
                                        }))
                                .executes(context -> {
                                    var key = context.getArgument("key", NamespacedKey.class);
                                    return execute(context, key, null, null);
                                }))
                        .executes(context -> execute(context, null, null, null)));
    }

    private int execute(CommandContext<CommandSourceStack> context, @Nullable NamespacedKey key,
                        World.@Nullable Environment environment, @Nullable Generator generator) {
        var name = context.getArgument("world", String.class);
        var levelFolder = new File(plugin.getServer().getWorldContainer(), name);

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
