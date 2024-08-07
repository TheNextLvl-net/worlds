package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.suggestion.WorldSuggestionProvider;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftWorld;

@RequiredArgsConstructor
@SuppressWarnings("UnstableApiUsage")
class WorldSaveCommand {
    private final WorldsPlugin plugin;

    ArgumentBuilder<CommandSourceStack, ?> create() {
        return Commands.literal("save")
                .requires(source -> source.getSender().hasPermission("worlds.command.save"))
                .then(Commands.argument("world", ArgumentTypes.world())
                        .suggests(new WorldSuggestionProvider<>(plugin))
                        .then(Commands.literal("flush")
                                .executes(context -> {
                                    var world = context.getArgument("world", World.class);
                                    save(context.getSource().getSender(), world, true);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .executes(context -> {
                            var world = context.getArgument("world", World.class);
                            save(context.getSource().getSender(), world, false);
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(Commands.literal("flush")
                        .executes(context -> save(context.getSource().getSender(),
                                context.getSource().getLocation().getWorld(), true)))
                .executes(context -> save(context.getSource().getSender(),
                        context.getSource().getLocation().getWorld(), false));
    }

    private int save(CommandSender sender, World world, boolean flush) {
        var placeholder = Placeholder.parsed("world", world.key().asString());
        plugin.bundle().sendMessage(sender, "world.save", placeholder);

        var level = ((CraftWorld) world).getHandle();
        var oldSave = level.noSave;
        level.noSave = false;
        level.save(null, flush, false);
        level.noSave = oldSave;

        plugin.bundle().sendMessage(sender, "world.save.success", placeholder);
        return Command.SINGLE_SUCCESS;
    }
}
