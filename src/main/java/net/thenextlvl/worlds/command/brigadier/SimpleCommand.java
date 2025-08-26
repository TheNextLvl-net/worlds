package net.thenextlvl.worlds.command.brigadier;

import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.thenextlvl.worlds.WorldsPlugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class SimpleCommand extends BrigadierCommand implements Command<CommandSourceStack> {
    protected SimpleCommand(WorldsPlugin plugin, String name, String permission) {
        super(plugin, name, permission);
    }
}
