package net.thenextlvl.worlds.command;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.key.Key;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.generator.Generator;
import net.thenextlvl.worlds.command.argument.GeneratorArgument;
import net.thenextlvl.worlds.command.argument.KeyArgument;
import net.thenextlvl.worlds.command.suggestion.WorldSuggestionProvider;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class WorldCommand {
    public static LiteralCommandNode<CommandSourceStack> create(WorldsPlugin plugin) {
        return Commands.literal("world")
                .requires(source -> source.getSender().hasPermission("worlds.command"))
                .then(SaveAllCommand.create(plugin))
                .then(SaveOffCommand.create(plugin))
                .then(SaveOnCommand.create(plugin))
                .then(WorldBackupCommand.create(plugin))
                .then(WorldCloneCommand.create(plugin))
                .then(WorldCreateCommand.create(plugin))
                .then(WorldDeleteCommand.create(plugin))
                .then(WorldImportCommand.create(plugin))
                .then(WorldInfoCommand.create(plugin))
                .then(WorldLinkCommand.create(plugin))
                .then(WorldListCommand.create(plugin))
                .then(WorldLoadCommand.create(plugin))
                .then(WorldRecreateCommand.create(plugin))
                .then(WorldRegenerateCommand.create(plugin))
                .then(WorldSaveCommand.create(plugin))
                .then(WorldSetSpawnCommand.create(plugin))
                .then(WorldSpawnCommand.create(plugin))
                .then(WorldTeleportCommand.create(plugin))
                .then(WorldUnloadCommand.create(plugin))
                .build();
    }

    public static RequiredArgumentBuilder<CommandSourceStack, World> worldArgument(WorldsPlugin plugin) {
        return Commands.argument("world", ArgumentTypes.world())
                .suggests(new WorldSuggestionProvider<>(plugin));
    }

    public static RequiredArgumentBuilder<CommandSourceStack, Key> keyArgument() {
        return Commands.argument("key", new KeyArgument());
    }

    public static RequiredArgumentBuilder<CommandSourceStack, Generator> generatorArgument(WorldsPlugin plugin) {
        return Commands.argument("generator", new GeneratorArgument(plugin));
    }
}
