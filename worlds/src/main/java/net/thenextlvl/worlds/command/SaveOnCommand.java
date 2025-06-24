package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.thenextlvl.worlds.WorldsPlugin;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NullMarked;

import static net.thenextlvl.worlds.command.WorldCommand.worldArgument;

@NullMarked
public class SaveOnCommand {
    public static LiteralCommandNode<CommandSourceStack> create(WorldsPlugin plugin) {
        return Commands.literal("save-on")
                .requires(source -> source.getSender().hasPermission("minecraft.command.save-on"))
                .then(saveOn(plugin))
                .executes(context -> saveOn(plugin, context))
                .build();
    }

    private static int saveOn(WorldsPlugin plugin, CommandContext<CommandSourceStack> context) {
        return saveOn(context.getSource().getSender(),
                context.getSource().getLocation().getWorld(), plugin);
    }

    private static RequiredArgumentBuilder<CommandSourceStack, World> saveOn(WorldsPlugin plugin) {
        return worldArgument(plugin)
                .executes(context -> saveOn(context.getSource().getSender(),
                        context.getArgument("world", World.class), plugin));
    }

    private static int saveOn(CommandSender sender, World world, WorldsPlugin plugin) {
        var message = world.isAutoSave() ? "world.save.already-on" : "world.save.on";
        world.setAutoSave(true);
        plugin.bundle().sendMessage(sender, message);
        return Command.SINGLE_SUCCESS;
    }
}
