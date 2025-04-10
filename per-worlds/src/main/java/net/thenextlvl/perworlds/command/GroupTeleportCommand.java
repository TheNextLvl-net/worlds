package net.thenextlvl.perworlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.perworlds.SharedWorlds;
import net.thenextlvl.perworlds.WorldGroup;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.List;

import static net.thenextlvl.perworlds.command.GroupCommand.groupArgument;

@NullMarked
class GroupTeleportCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> create(SharedWorlds commons) {
        return Commands.literal("teleport")
                .requires(source -> source.getSender().hasPermission("perworlds.command.group.teleport"))
                .then(groupArgument(commons)
                        .then(Commands.argument("players", ArgumentTypes.players())
                                .executes(context -> teleportPlayers(context, commons)))
                        .executes(context -> teleport(context, commons)));
    }

    private static int teleportPlayers(CommandContext<CommandSourceStack> context, SharedWorlds commons) throws CommandSyntaxException {
        var players = context.getArgument("players", PlayerSelectorArgumentResolver.class);
        var resolved = players.resolve(context.getSource());
        return teleport(context, resolved, commons);
    }

    private static int teleport(CommandContext<CommandSourceStack> context, SharedWorlds commons) {
        if (context.getSource().getSender() instanceof Player player)
            return teleport(context, List.of(player), commons);
        commons.bundle().sendMessage(context.getSource().getSender(), "command.sender");
        return 0;
    }

    private static int teleport(CommandContext<CommandSourceStack> context, List<Player> players, SharedWorlds commons) {
        var group = context.getArgument("group", WorldGroup.class);
        return teleport(context.getSource().getSender(), group, players.stream()
                .filter(player -> !group.containsWorld(player.getWorld()))
                .filter(group::hasPlayerData)
                .toList(), commons);
    }

    private static int teleport(CommandSender sender, WorldGroup group, List<Player> players, SharedWorlds commons) {
        var message = players.size() == 1 ? "group.teleport.other"
                : players.isEmpty() ? "group.teleport.none" : "group.teleport.others";
        players.forEach(player -> {
            group.loadPlayerData(player, true);
            commons.bundle().sendMessage(player, "group.teleport.self",
                    Placeholder.parsed("group", group.getName()));
        });
        if (players.size() == 1 && players.getFirst().equals(sender)) return Command.SINGLE_SUCCESS;
        commons.bundle().sendMessage(sender, message,
                Placeholder.component("player", players.isEmpty() ? Component.empty() : players.getFirst().name()),
                Placeholder.parsed("players", String.valueOf(players.size())),
                Placeholder.parsed("group", group.getName()));
        return players.isEmpty() ? 0 : Command.SINGLE_SUCCESS;
    }
}
