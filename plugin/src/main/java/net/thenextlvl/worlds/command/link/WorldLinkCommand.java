package net.thenextlvl.worlds.command.link;

import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.brigadier.BrigadierCommand;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class WorldLinkCommand extends BrigadierCommand {
    WorldLinkCommand(final WorldsPlugin plugin) {
        super(plugin, "link", "worlds.command.link");
    }

    public static ArgumentBuilder<CommandSourceStack, ?> create(final WorldsPlugin plugin) {
        return new WorldLinkCommand(plugin).create()
                .then(WorldLinkCreateCommand.create(plugin))
                .then(WorldLinkListCommand.create(plugin))
                .then(WorldLinkRemoveCommand.create(plugin));
    }
}
