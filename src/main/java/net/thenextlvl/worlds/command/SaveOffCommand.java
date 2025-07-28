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
public class SaveOffCommand {
    public static LiteralCommandNode<CommandSourceStack> create(WorldsPlugin plugin) {
        return Commands.literal("save-off")
                .requires(source -> source.getSender().hasPermission("minecraft.command.save-off"))
                .then(saveOff(plugin))
                .executes(context -> saveOff(plugin, context))
                .build();
    }

    private static int saveOff(WorldsPlugin plugin, CommandContext<CommandSourceStack> context) {
        return saveOff(context.getSource().getSender(),
                context.getSource().getLocation().getWorld(), plugin);
    }

    private static RequiredArgumentBuilder<CommandSourceStack, World> saveOff(WorldsPlugin plugin) {
        return worldArgument(plugin)
                .executes(context -> saveOff(context.getSource().getSender(),
                        context.getArgument("world", World.class), plugin));
    }

    private static int saveOff(CommandSender sender, World world, WorldsPlugin plugin) {
        var message = world.isAutoSave() ? "world.save.off" : "world.save.already-off";
        world.setAutoSave(false);
        plugin.bundle().sendMessage(sender, message);
        return Command.SINGLE_SUCCESS;
    }
}
