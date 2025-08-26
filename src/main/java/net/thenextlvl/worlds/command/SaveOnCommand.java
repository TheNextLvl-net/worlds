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
public final class SaveOnCommand extends SimpleCommand {
    private SaveOnCommand(WorldsPlugin plugin) {
        super(plugin, "save-on", "worlds.command.save-on");
    }

    public static LiteralCommandNode<CommandSourceStack> create(WorldsPlugin plugin) {
        var command = new SaveOnCommand(plugin);
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
        var message = autoSave ? "world.save.already-on" : "world.save.on";
        if (!autoSave) world.setAutoSave(true);
        plugin.bundle().sendMessage(context.getSource().getSender(), message);
        return autoSave ? 0 : SINGLE_SUCCESS;
    }
}
