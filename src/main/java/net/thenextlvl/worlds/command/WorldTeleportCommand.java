package net.thenextlvl.worlds.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.FinePositionResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.brigadier.SimpleCommand;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.List;

import static net.thenextlvl.worlds.command.WorldCommand.worldArgument;
import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND;

@NullMarked
final class WorldTeleportCommand extends SimpleCommand {
    WorldTeleportCommand(WorldsPlugin plugin) {
        super(plugin, "teleport", "worlds.command.teleport");
    }

    public static ArgumentBuilder<CommandSourceStack, ?> create(WorldsPlugin plugin) {
        var command = new WorldTeleportCommand(plugin);
        return command.create().then(worldArgument(plugin)
                .then(command.teleportEntity())
                .executes(command));
    }

    private RequiredArgumentBuilder<CommandSourceStack, EntitySelectorArgumentResolver> teleportEntity() {
        return Commands.argument("entities", ArgumentTypes.entities())
                .then(Commands.argument("position", ArgumentTypes.finePosition(true)).executes(this))
                .executes(this);
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var sender = context.getSource().getSender();
        var entityResolver = tryGetArgument(context, "entities", EntitySelectorArgumentResolver.class).orElse(null);

        if (entityResolver == null && !(sender instanceof Player player)) {
            plugin.bundle().sendMessage(sender, "command.sender");
            return 0;
        }

        var position = tryGetArgument(context, "position", FinePositionResolver.class).orElse(null);
        var world = context.getArgument("world", World.class);

        var entities = entityResolver != null ? entityResolver.resolve(context.getSource()) : List.of((Player) sender);
        var location = position != null ? position.resolve(context.getSource()).toLocation(world) : world.getSpawnLocation();

        entities.forEach(entity -> entity.teleportAsync(location, COMMAND).thenRun(() -> {
            plugin.bundle().sendMessage(entity, "world.teleport.self",
                    Placeholder.parsed("world", location.getWorld().getName()));
        }));

        if (entities.size() == 1 && entities.getFirst().equals(sender)) return SINGLE_SUCCESS;

        var message = entities.size() == 1
                ? "world.teleport.other" : entities.isEmpty()
                ? "world.teleport.none" : "world.teleport.others";
        var entity = entities.isEmpty() ? null : entities.getFirst();


        Runnable runnable = () -> plugin.bundle().sendMessage(sender, message,
                Placeholder.component("entity", entity != null ? entity.name() : Component.empty()),
                Placeholder.parsed("entities", String.valueOf(entities.size())),
                Placeholder.parsed("world", location.getWorld().getName()));

        if (entity != null) entity.getScheduler().run(plugin, task -> runnable.run(), null);
        else runnable.run();

        return entities.isEmpty() ? 0 : SINGLE_SUCCESS;
    }
}
