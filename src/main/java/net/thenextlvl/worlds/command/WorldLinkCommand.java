package net.thenextlvl.worlds.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.thenextlvl.worlds.WorldsPlugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
class WorldLinkCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> create(WorldsPlugin plugin) {
        return Commands.literal("link")
                .requires(source -> source.getSender().hasPermission("worlds.command.link"))
                .then(WorldLinkCreateCommand.create(plugin))
                .then(WorldLinkListCommand.create(plugin))
                .then(WorldLinkRemoveCommand.create(plugin));
    }
}
