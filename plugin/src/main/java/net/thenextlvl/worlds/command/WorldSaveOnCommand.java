package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import lombok.RequiredArgsConstructor;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.suggestion.WorldSuggestionProvider;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

@RequiredArgsConstructor
@SuppressWarnings("UnstableApiUsage")
class WorldSaveOnCommand {
    private final WorldsPlugin plugin;

    ArgumentBuilder<CommandSourceStack, ?> create() {
        return Commands.literal("save-on")
                .requires(source -> source.getSender().hasPermission("worlds.command.save-on"))
                .then(Commands.argument("world", ArgumentTypes.world())
                        .suggests(new WorldSuggestionProvider<>(plugin))
                        .executes(context -> saveOn(context.getSource().getSender(),
                                context.getArgument("world", World.class))))
                .executes(context -> saveOn(context.getSource().getSender(),
                        context.getSource().getLocation().getWorld()));
    }

    private int saveOn(CommandSender sender, World world) {
        var message = world.isAutoSave() ? "world.save.already-on" : "world.save.on";
        world.setAutoSave(true);
        plugin.bundle().sendMessage(sender, message);
        return Command.SINGLE_SUCCESS;
    }
}
