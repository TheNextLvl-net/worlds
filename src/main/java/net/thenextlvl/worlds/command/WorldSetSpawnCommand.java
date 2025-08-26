package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class WorldSetSpawnCommand {
    static LiteralCommandNode<CommandSourceStack> create(WorldsPlugin plugin) {
        return create(plugin, "setspawn");
    }

    public static LiteralCommandNode<CommandSourceStack> create(WorldsPlugin plugin, String label) {
        return Commands.literal(label)
                .requires(source -> source.getSender().hasPermission("minecraft.command.setworldspawn"))
                .then(setSpawn(plugin))
                .executes(context -> setSpawn(plugin, context))
                .build();
    }

    private static int setSpawn(WorldsPlugin plugin, CommandContext<CommandSourceStack> context) {
        var location = context.getSource().getLocation();
        return setSpawn(context.getSource().getSender(),
                location.getWorld(),
                location.blockX(),
                location.blockY(),
                location.blockZ(), 0,
                plugin);
    }

    private static RequiredArgumentBuilder<CommandSourceStack, BlockPositionResolver> setSpawn(WorldsPlugin plugin) {
        return Commands.argument("position", ArgumentTypes.blockPosition())
                .then(Commands.argument("angle", FloatArgumentType.floatArg(-180, 180))
                        .executes(context -> setSpawnWithAngle(plugin, context)))
                .executes(context -> setSpawnPosition(plugin, context));
    }

    private static int setSpawnPosition(WorldsPlugin plugin, CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var resolver = context.getArgument("position", BlockPositionResolver.class);
        var position = resolver.resolve(context.getSource());
        return setSpawn(context.getSource().getSender(),
                context.getSource().getLocation().getWorld(),
                position.blockX(), position.blockY(), position.blockZ(), 0,
                plugin);
    }

    private static int setSpawnWithAngle(WorldsPlugin plugin, CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var angle = context.getArgument("angle", float.class);
        var resolver = context.getArgument("position", BlockPositionResolver.class);
        var position = resolver.resolve(context.getSource());
        return setSpawn(context.getSource().getSender(),
                context.getSource().getLocation().getWorld(),
                position.blockX(), position.blockY(), position.blockZ(), angle,
                plugin
        );
    }

    private static int setSpawn(CommandSender sender, World world, int x, int y, int z, float angle, WorldsPlugin plugin) {
        var success = world.setSpawnLocation(x, y, z, angle);
        var message = success ? "world.spawn.set.success" : "world.spawn.set.failed";
        plugin.bundle().sendMessage(sender, message,
                Placeholder.parsed("x", String.valueOf(x)),
                Placeholder.parsed("y", String.valueOf(y)),
                Placeholder.parsed("z", String.valueOf(z)),
                Placeholder.parsed("angle", String.valueOf(angle)));
        return success ? Command.SINGLE_SUCCESS : 0;
    }
}
