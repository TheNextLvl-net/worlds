package net.thenextlvl.worlds.command;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.RotationResolver;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.brigadier.SimpleCommand;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class WorldSetSpawnCommand extends SimpleCommand {
    private WorldSetSpawnCommand(final WorldsPlugin plugin, final String name) {
        super(plugin, name, "minecraft.command.setworldspawn");
    }

    static LiteralCommandNode<CommandSourceStack> create(final WorldsPlugin plugin) {
        return create(plugin, "setspawn");
    }

    public static LiteralCommandNode<CommandSourceStack> create(final WorldsPlugin plugin, final String name) {
        final var command = new WorldSetSpawnCommand(plugin, name);
        return command.create()
                .then(command.positioned())
                .executes(command)
                .build();
    }

    private RequiredArgumentBuilder<CommandSourceStack, BlockPositionResolver> positioned() {
        return Commands.argument("position", ArgumentTypes.blockPosition())
                .then(Commands.argument("rotation", ArgumentTypes.rotation()).executes(this))
                .executes(this);
    }

    @Override
    public int run(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final var location = context.getSource().getLocation();
        final var resolver = tryGetArgument(context, "position", BlockPositionResolver.class).orElse(null);
        final var position = resolver != null ? resolver.resolve(context.getSource()) : location;
        final var rotationResolver = tryGetArgument(context, "rotation", RotationResolver.class).orElse(null);
        final var rotation = rotationResolver != null ? rotationResolver.resolve(context.getSource()) : location.getRotation();

        final var success = location.getWorld().setSpawnLocation(position.toLocation(location.getWorld()).setRotation(rotation));
        final var message = success ? "world.spawn.set.success" : "world.spawn.set.failed";

        plugin.bundle().sendMessage(context.getSource().getSender(), message,
                Formatter.number("x", position.blockX()),
                Formatter.number("y", position.blockY()),
                Formatter.number("z", position.blockZ()),
                Formatter.number("yaw", rotation.yaw()),
                Formatter.number("pitch", rotation.pitch()));
        return success ? SINGLE_SUCCESS : 0;
    }
}
