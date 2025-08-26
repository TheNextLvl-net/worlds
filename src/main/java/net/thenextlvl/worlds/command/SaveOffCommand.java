package net.thenextlvl.worlds.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.brigadier.SimpleCommand;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

import static net.thenextlvl.worlds.command.WorldCommand.worldArgument;

@NullMarked
public final class SaveOffCommand extends SimpleCommand {
    private SaveOffCommand(WorldsPlugin plugin) {
        super(plugin, "save-off", "worlds.command.save-off");
    }

    public static LiteralCommandNode<CommandSourceStack> create(WorldsPlugin plugin) {
        var command = new SaveOffCommand(plugin);
        return command.create()
                .then(worldArgument(plugin).executes(command))
                .executes(command)
                .build();
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        var world = tryGetArgument(context, "world", World.class)
                .orElseGet(() -> context.getSource().getLocation().getWorld());
        var autoSave = world.isAutoSave();
        var message = autoSave ? "world.save.off" : "world.save.already-off";
        if (autoSave) world.setAutoSave(false);
        plugin.bundle().sendMessage(context.getSource().getSender(), message);
        return autoSave ? SINGLE_SUCCESS : 0;
    }
}
