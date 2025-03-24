package net.thenextlvl.perworlds.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.thenextlvl.perworlds.PerWorldsPlugin;

public class WorldCommand {
    public static LiteralCommandNode<CommandSourceStack> create(PerWorldsPlugin plugin) {
        return Commands.literal("world")
                .requires(source -> source.getSender().hasPermission("perworlds.command"))
                .then(GroupCommand.create(plugin.commons()))
                .build();
    }
}
