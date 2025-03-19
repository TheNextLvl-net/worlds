package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.suggestion.WorldSuggestionProvider;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NullMarked;

@NullMarked
class WorldSaveOffCommand {
    private final WorldsPlugin plugin;

    WorldSaveOffCommand(WorldsPlugin plugin) {
        this.plugin = plugin;
    }

    ArgumentBuilder<CommandSourceStack, ?> create() {
        return Commands.literal("save-off")
                .requires(source -> source.getSender().hasPermission("worlds.command.save-off"))
                .then(Commands.argument("world", ArgumentTypes.world())
                        .suggests(new WorldSuggestionProvider<>(plugin))
                        .executes(context -> saveOff(context.getSource().getSender(),
                                context.getArgument("world", World.class))))
                .executes(context -> saveOff(context.getSource().getSender(),
                        context.getSource().getLocation().getWorld()));
    }

    private int saveOff(CommandSender sender, World world) {
        var message = world.isAutoSave() ? "world.save.off" : "world.save.already-off";
        world.setAutoSave(false);
        plugin.bundle().sendMessage(sender, message);
        return Command.SINGLE_SUCCESS;
    }
}
