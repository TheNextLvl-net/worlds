package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

@RequiredArgsConstructor
@SuppressWarnings("UnstableApiUsage")
class WorldSetSpawnCommand {
    private final WorldsPlugin plugin;

    ArgumentBuilder<CommandSourceStack, ?> create() {
        return Commands.literal("setspawn")
                .requires(source -> source.getSender().hasPermission("worlds.command.setspawn"))
                .then(Commands.argument("position", ArgumentTypes.blockPosition())
                        .then(Commands.argument("angle", FloatArgumentType.floatArg(-180, 180))
                                .executes(context -> {
                                    var angle = context.getArgument("angle", float.class);
                                    var resolver = context.getArgument("position", BlockPositionResolver.class);
                                    var position = resolver.resolve(context.getSource());
                                    return setSpawn(context.getSource().getSender(),
                                            context.getSource().getLocation().getWorld(),
                                            position.blockX(), position.blockY(), position.blockZ(), angle
                                    );
                                }))
                        .executes(context -> {
                            var resolver = context.getArgument("position", BlockPositionResolver.class);
                            var position = resolver.resolve(context.getSource());
                            return setSpawn(context.getSource().getSender(),
                                    context.getSource().getLocation().getWorld(),
                                    position.blockX(), position.blockY(), position.blockZ(), 0
                            );
                        }))
                .executes(context -> {
                    var location = context.getSource().getLocation();
                    return setSpawn(context.getSource().getSender(),
                            location.getWorld(),
                            location.blockX(),
                            location.blockY(),
                            location.blockZ(), 0
                    );
                });
    }

    private int setSpawn(CommandSender sender, World world, int x, int y, int z, float angle) {
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
