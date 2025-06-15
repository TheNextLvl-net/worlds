package net.thenextlvl.perworlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.FinePositionResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.RotationResolver;
import io.papermc.paper.math.Rotation;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.perworlds.SharedWorlds;
import net.thenextlvl.perworlds.WorldGroup;
import net.thenextlvl.perworlds.command.suggestion.GroupMemberSuggestionProvider;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.CompletableFuture;

import static net.thenextlvl.perworlds.command.GroupCommand.groupArgument;

@NullMarked
class GroupSpawnCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> create(SharedWorlds commons) {
        return Commands.literal("spawn")
                .requires(source -> source.getSender().hasPermission("perworlds.command.group.spawn"))
                .then(setSpawn(commons))
                .then(unsetSpawn(commons))
                .executes(context -> spawn(context, commons));
    }

    private static int spawn(CommandContext<CommandSourceStack> context, SharedWorlds commons) {
        var sender = context.getSource().getSender();
        if (!(sender instanceof Player player)) {
            commons.bundle().sendMessage(sender, "command.sender");
            return 0;
        }
        var group = commons.groupProvider().getGroup(player.getWorld())
                .orElse(commons.groupProvider().getUnownedWorldGroup());
        group.getSpawnLocation().map(player::teleportAsync)
                .orElseGet(() -> CompletableFuture.completedFuture(false))
                .thenAccept(success -> {
                    var message = success ? "group.teleport.self" : "group.teleport.failed";
                    commons.bundle().sendMessage(player, message, Placeholder.parsed("group", group.getName()));
                });
        return Command.SINGLE_SUCCESS;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> setSpawn(SharedWorlds commons) {
        return Commands.literal("set")
                .requires(source -> source.getSender().hasPermission("perworlds.command.group.spawn.set"))
                .executes(context -> setSpawn(context, commons))
                .then(targetArgument(commons));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> unsetSpawn(SharedWorlds commons) {
        return Commands.literal("unset")
                .requires(source -> source.getSender().hasPermission("perworlds.command.group.spawn.unset"))
                .then(groupArgument(commons).executes(context -> {
                    var group = context.getArgument("group", WorldGroup.class);
                    return unsetSpawn(context, group, commons);
                })).executes(context -> {
                    var world = context.getSource().getLocation().getWorld();
                    var group = commons.groupProvider().getGroup(world)
                            .orElse(commons.groupProvider().getUnownedWorldGroup());
                    return unsetSpawn(context, group, commons);
                });
    }

    private static int unsetSpawn(CommandContext<CommandSourceStack> context, WorldGroup group, SharedWorlds commons) {
        var sender = context.getSource().getSender();
        group.getGroupData().setSpawnLocation(null);
        commons.bundle().sendMessage(sender, "group.spawn.unset",
                Placeholder.parsed("group", group.getName()));
        return Command.SINGLE_SUCCESS;
    }

    private static ArgumentBuilder<CommandSourceStack, ?> targetArgument(SharedWorlds commons) {
        return groupArgument(commons).then(worldArgument()
                .then(positionArgument().then(rotationArgument().executes(context -> {
                    var rotation = context.getArgument("rotation", RotationResolver.class);
                    return setTargetSpawn(context, commons, rotation.resolve(context.getSource()));
                })).executes(context -> setTargetSpawn(context, commons, Rotation.rotation(0, 0)))));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> positionArgument() {
        return Commands.argument("position", ArgumentTypes.finePosition());
    }

    private static ArgumentBuilder<CommandSourceStack, ?> rotationArgument() {
        return Commands.argument("rotation", ArgumentTypes.rotation());
    }

    private static ArgumentBuilder<CommandSourceStack, ?> worldArgument() {
        return Commands.argument("world", ArgumentTypes.world())
                .suggests(new GroupMemberSuggestionProvider<>());
    }

    private static int setTargetSpawn(CommandContext<CommandSourceStack> context, SharedWorlds commons, Rotation rotation) throws CommandSyntaxException {
        var position = context.getArgument("position", FinePositionResolver.class).resolve(context.getSource());
        var group = context.getArgument("group", WorldGroup.class);
        var world = context.getArgument("world", World.class);
        return setSpawn(context, commons, group, position.toLocation(world).setRotation(rotation));
    }

    private static int setSpawn(CommandContext<CommandSourceStack> context, SharedWorlds commons) {
        var location = context.getSource().getLocation();
        var group = commons.groupProvider().getGroup(location.getWorld())
                .orElse(commons.groupProvider().getUnownedWorldGroup());
        return setSpawn(context, commons, group, location);
    }

    private static int setSpawn(CommandContext<CommandSourceStack> context, SharedWorlds commons, WorldGroup group, Location location) {
        var sender = context.getSource().getSender();
        group.getGroupData().setSpawnLocation(location);
        commons.bundle().sendMessage(sender, "group.spawn.set",
                Placeholder.parsed("group", group.getName()),
                Placeholder.parsed("world", location.getWorld().getName()),
                Formatter.number("x", location.x()),
                Formatter.number("y", location.y()),
                Formatter.number("z", location.z()),
                Formatter.number("yaw", location.getYaw()),
                Formatter.number("pitch", location.getPitch()));
        return Command.SINGLE_SUCCESS;
    }
}
