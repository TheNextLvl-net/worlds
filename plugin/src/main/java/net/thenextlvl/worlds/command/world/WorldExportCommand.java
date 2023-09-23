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

class WorldExportCommand {
    private static final Worlds plugin = JavaPlugin.getPlugin(Worlds.class);

    static Command.Builder<CommandSender> create(Command.Builder<CommandSender> builder) {
        return builder.literal("export")
                .permission("worlds.command.world.export")
                .argument(StringArgument.<CommandSender>builder("world")
                        .withSuggestionsProvider((context, token) -> Bukkit.getWorlds().stream()
                                .map(WorldInfo::getName)
                                .filter(name -> name.startsWith(token))
                                .toList())
                        .asOptional())
                .handler(WorldExportCommand::execute);
    }

    @SuppressWarnings("CallToPrintStackTrace")
    private static void execute(CommandContext<CommandSender> context) {
        var sender = context.getSender();
        var input = context.<String>getOptional("world");
        var world = input.map(Bukkit::getWorld).orElse(sender instanceof Player player ? player.getWorld() : null);
        if (world != null) {
            var placeholder = Placeholder.parsed("world", world.getName());
            try {
                world.save();
                plugin.bundle().sendMessage(sender, "world.save.success", placeholder);
            } catch (Exception e) {
                plugin.bundle().sendMessage(sender, "world.save.failed", placeholder);
                e.printStackTrace();
            }
        } else input.ifPresentOrElse(name -> plugin.bundle().sendMessage(sender, "world.exists.not",
                        Placeholder.parsed("world", name)),
                () -> plugin.bundle().sendMessage(sender, "world.name.absent"));
    }
}
