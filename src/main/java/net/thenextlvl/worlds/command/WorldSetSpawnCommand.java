package net.thenextlvl.worlds.command;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.brigadier.SimpleCommand;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class WorldSetSpawnCommand extends SimpleCommand {
    private WorldSetSpawnCommand(WorldsPlugin plugin, String name) {
        super(plugin, name, "minecraft.command.setworldspawn");
    }

    static LiteralCommandNode<CommandSourceStack> create(WorldsPlugin plugin) {
        return create(plugin, "setspawn");
    }

    public static LiteralCommandNode<CommandSourceStack> create(WorldsPlugin plugin, String name) {
        var command = new WorldSetSpawnCommand(plugin, name);
        return command.create()
                .then(command.positioned())
                .executes(command)
                .build();
    }

    private RequiredArgumentBuilder<CommandSourceStack, BlockPositionResolver> positioned() {
        return Commands.argument("position", ArgumentTypes.blockPosition())
                .then(Commands.argument("angle", FloatArgumentType.floatArg(-180, 180)).executes(this))
                .executes(this);
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var location = context.getSource().getLocation();
        var resolver = tryGetArgument(context, "position", BlockPositionResolver.class).orElse(null);
        var position = resolver != null ? resolver.resolve(context.getSource()) : location;
        var angle = tryGetArgument(context, "angle", float.class).orElse(0f);

        var success = location.getWorld().setSpawnLocation(position.blockX(), position.blockY(), position.blockZ(), angle);
        var message = success ? "world.spawn.set.success" : "world.spawn.set.failed";
        
        plugin.bundle().sendMessage(context.getSource().getSender(), message,
                Formatter.number("x", position.blockX()),
                Formatter.number("y", position.blockY()),
                Formatter.number("z", position.blockZ()),
                Formatter.number("angle", angle));
        return success ? SINGLE_SUCCESS : 0;
    }
}
