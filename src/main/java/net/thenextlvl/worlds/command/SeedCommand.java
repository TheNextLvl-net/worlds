package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

import static net.thenextlvl.worlds.command.WorldCommand.worldArgument;

@NullMarked
public class SeedCommand {
    public static LiteralCommandNode<CommandSourceStack> create(WorldsPlugin plugin) {
        return Commands.literal("seed")
                .requires(source -> source.getSender().hasPermission("minecraft.command.seed"))
                .executes(context -> seed(context, context.getSource().getLocation().getWorld(), plugin))
                .then(worldArgument(plugin).executes(context -> {
                    var world = context.getArgument("world", World.class);
                    return seed(context, world, plugin);
                })).build();
    }

    private static int seed(CommandContext<CommandSourceStack> context, World world, WorldsPlugin plugin) {
        plugin.bundle().sendMessage(context.getSource().getSender(), "world.info.seed",
                Placeholder.parsed("seed", String.valueOf(world.getSeed())));
        return Command.SINGLE_SUCCESS;
    }
}
