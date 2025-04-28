package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.suggestion.WorldSuggestionProvider;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

@NullMarked
class WorldSaveCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> create(WorldsPlugin plugin) {
        return Commands.literal("save")
                .requires(source -> source.getSender().hasPermission("worlds.command.save"))
                .then(Commands.argument("world", ArgumentTypes.world())
                        .suggests(new WorldSuggestionProvider<>(plugin))
                        .then(Commands.literal("flush")
                                .executes(context -> save(context, true, plugin)))
                        .executes(context -> save(context, false, plugin)));
    }

    private static int save(CommandContext<CommandSourceStack> context, boolean flush, WorldsPlugin plugin) {
        var world = context.getArgument("world", World.class);
        var placeholder = Placeholder.parsed("world", world.getName());
        plugin.bundle().sendMessage(context.getSource().getSender(), "world.save", placeholder);
        plugin.levelView().saveLevel(world, flush);
        plugin.bundle().sendMessage(context.getSource().getSender(), "world.save.success", placeholder);
        return Command.SINGLE_SUCCESS;
    }
}
