package net.thenextlvl.worlds.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.brigadier.SimpleCommand;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

import static net.thenextlvl.worlds.command.WorldCommand.worldArgument;

@NullMarked
public final class SeedCommand extends SimpleCommand {
    private SeedCommand(final WorldsPlugin plugin) {
        super(plugin, "seed", "worlds.command.seed");
    }

    public static LiteralCommandNode<CommandSourceStack> create(final WorldsPlugin plugin) {
        final var command = new SeedCommand(plugin);
        return command.create()
                .then(worldArgument(plugin).executes(command))
                .executes(command)
                .build();
    }

    @Override
    public int run(final CommandContext<CommandSourceStack> context) {
        final var world = tryGetArgument(context, "world", World.class)
                .orElseGet(() -> context.getSource().getLocation().getWorld());

        plugin.bundle().sendMessage(context.getSource().getSender(), "world.info.seed",
                Placeholder.parsed("seed", String.valueOf(world.getSeed())));
        return SINGLE_SUCCESS;
    }
}
