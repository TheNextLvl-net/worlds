package net.thenextlvl.worlds.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.thenextlvl.worlds.WorldsPlugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
class WorldLinkCommand {
    private final WorldsPlugin plugin;

    WorldLinkCommand(WorldsPlugin plugin) {
        this.plugin = plugin;
    }

    ArgumentBuilder<CommandSourceStack, ?> create() {
        return Commands.literal("link")
                .requires(source -> source.getSender().hasPermission("worlds.command.link"))
                .then(new WorldLinkCreateCommand(plugin).create())
                .then(new WorldLinkListCommand(plugin).create())
                .then(new WorldLinkRemoveCommand(plugin).create());
    }
}
