package net.thenextlvl.worlds.command.world;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.Worlds;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.java.JavaPlugin;

import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND;

class WorldTeleportCommand {
    private static final Worlds plugin = JavaPlugin.getPlugin(Worlds.class);

    static Command.Builder<CommandSender> create(Command.Builder<CommandSender> builder) {
        return builder.literal("teleport", "tp")
                .permission("worlds.command.world.teleport")
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
        var world = Bukkit.getWorld(context.<String>get("world"));
        var target = context.<String>getOptional("player");
        var player = target.map(Bukkit::getPlayer).orElse(sender instanceof Player self ? self : null);
        if (player == null) plugin.bundle().sendMessage(sender, "player.not.online",
                Placeholder.parsed("player", target.orElse("null")));
        else if (world == null) plugin.bundle().sendMessage(sender, "world.name.absent");
        else player.teleportAsync(world.getSpawnLocation().add(0.5, 0, 0.5), COMMAND);
    }
}
