package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.key.Key;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.argument.GeneratorArgument;
import net.thenextlvl.worlds.command.argument.KeyArgument;
import net.thenextlvl.worlds.command.argument.WorldArgument;
import net.thenextlvl.worlds.command.backup.WorldBackupCommand;
import net.thenextlvl.worlds.command.brigadier.BrigadierCommand;
import net.thenextlvl.worlds.command.suggestion.WorldSuggestionProvider;
import net.thenextlvl.worlds.dialog.WorldListDialog;
import net.thenextlvl.worlds.generator.Generator;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class WorldCommand extends BrigadierCommand {
    private WorldCommand(final WorldsPlugin plugin) {
        super(plugin, "world", "worlds.command");
    }

    public static LiteralCommandNode<CommandSourceStack> create(final WorldsPlugin plugin) {
        final var command = new WorldCommand(plugin);
        return command.create()
                .executes(command::showDialog)
                .then(SaveAllCommand.create(plugin))
                .then(SaveOffCommand.create(plugin))
                .then(SaveOnCommand.create(plugin))
                .then(WorldBackupCommand.create(plugin))
                .then(WorldCloneCommand.create(plugin))
                .then(WorldCreateCommand.create(plugin))
                .then(WorldDeleteCommand.create(plugin))
                .then(WorldGameRuleCommand.create(plugin))
                .then(WorldHelpCommand.create(plugin))
                .then(WorldImportCommand.create(plugin))
                .then(WorldInfoCommand.create(plugin))
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

    private int showDialog(final CommandContext<CommandSourceStack> context) {
        final var sender = context.getSource().getSender();
        if (!(sender instanceof final Player player) || !player.hasPermission("worlds.command.list")) {
            plugin.bundle().sendMessage(sender, "command.sender");
            return 0;
        }
        player.showDialog(WorldListDialog.create(plugin, player));
        return Command.SINGLE_SUCCESS;
    }

    public static RequiredArgumentBuilder<CommandSourceStack, World> worldArgument(final WorldsPlugin plugin) {
        return Commands.argument("world", new WorldArgument(plugin))
                .suggests(new WorldSuggestionProvider<>(plugin));
    }

    public static RequiredArgumentBuilder<CommandSourceStack, Key> keyArgument() {
        return Commands.argument("key", new KeyArgument());
    }

    public static RequiredArgumentBuilder<CommandSourceStack, Generator> generatorArgument(final WorldsPlugin plugin) {
        return Commands.argument("generator", new GeneratorArgument(plugin));
    }
}
