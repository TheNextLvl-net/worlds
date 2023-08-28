package net.thenextlvl.worlds.command.world;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import net.thenextlvl.worlds.util.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;

import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND;

class WorldTeleportCommand {

    static Command.Builder<CommandSender> create(Command.Builder<CommandSender> builder) {
        return builder
                .literal("teleport", "tp")
                .argument(StringArgument.<CommandSender>builder("world")
                        .withSuggestionsProvider((context, token) -> Bukkit.getWorlds().stream()
                                .map(WorldInfo::getName)
                                .filter(s -> s.startsWith(token))
                                .toList())
                        .build())
                .argument(StringArgument.<CommandSender>builder("player")
                        .withSuggestionsProvider((context, token) -> Bukkit.getOnlinePlayers().stream()
                                .map(Player::getName)
                                .filter(s -> s.startsWith(token))
                                .toList())
                        .asOptional().build())
                .handler(WorldTeleportCommand::execute);
    }

    private static void execute(CommandContext<CommandSender> context) {
        var sender = context.getSender();
        var locale = sender instanceof Player player ? player.locale() : Messages.ENGLISH;
        var world = Bukkit.getWorld(context.<String>get("world"));
        var player = context.contains("player") ? Bukkit.getPlayer(context.<String>get("player")) :
                sender instanceof Player self ? self : null;
        if (player == null) sender.sendRichMessage(Messages.PLAYER_NOT_ONLINE.message(locale, sender));
        else if (world == null) sender.sendRichMessage(Messages.ENTER_WORLD_NAME.message(locale, sender));
        else player.teleportAsync(world.getSpawnLocation().add(0.5, 0, 0.5), COMMAND);
    }
}
