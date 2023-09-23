package net.thenextlvl.worlds.command.world;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.Worlds;
import net.thenextlvl.worlds.image.Image;
import org.bukkit.Bukkit;
import org.bukkit.WorldType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

class WorldInfoCommand {
    private static final Worlds plugin = JavaPlugin.getPlugin(Worlds.class);

    static Command.Builder<CommandSender> create(Command.Builder<CommandSender> builder) {
        return builder.literal("info")
                .permission("worlds.command.world.info")
                .argument(StringArgument.<CommandSender>builder("world")
                        .withSuggestionsProvider((context, token) -> Bukkit.getWorlds().stream()
                                .map(WorldInfo::getName)
                                .filter(s -> s.startsWith(token))
                                .toList())
                        .asOptional().build())
                .handler(WorldInfoCommand::execute);
    }

    @SuppressWarnings("deprecation")
    private static void execute(CommandContext<CommandSender> context) {
        var sender = context.getSender();
        var target = context.<String>getOptional("world");
        var world = target.isPresent() ? Bukkit.getWorld(target.get()) :
                sender instanceof Entity entity ? entity.getWorld() : null;

        if (world == null && target.isEmpty()) {
            plugin.bundle().sendMessage(sender, "world.name.absent");
            return;
        }

        if (world == null) {
            plugin.bundle().sendMessage(sender, "world.exists.not",
                    Placeholder.parsed("world", target.get()));
            return;
        }

        var volume = Image.getOrDefault(world);
        plugin.bundle().sendMessage(sender, "world.info.name",
                Placeholder.parsed("world", world.getName()));
        plugin.bundle().sendMessage(sender, "world.info.players",
                Placeholder.parsed("players", String.valueOf(world.getPlayers().size())));
        plugin.bundle().sendMessage(sender, "world.info.type",
                Placeholder.parsed("type", switch (notnull(world.getWorldType(), WorldType.NORMAL)) {
                    case LARGE_BIOMES -> "Large Biomes";
                    case AMPLIFIED -> "Amplified";
                    case NORMAL -> "Normal";
                    case FLAT -> "Flat";
                }));
        plugin.bundle().sendMessage(sender, "world.info.environment",
                Placeholder.parsed("environment", switch (world.getEnvironment()) {
                    case THE_END -> "The End";
                    case NETHER -> "Nether";
                    case NORMAL -> "Normal";
                    case CUSTOM -> "Custom";
                }));
        plugin.bundle().sendMessage(sender, "world.info.generator",
                Placeholder.parsed("generator", String.valueOf(notnull(
                        volume.getWorldImage().generator(), "Vanilla"))));
        plugin.bundle().sendMessage(sender, "world.info.seed",
                Placeholder.parsed("seed", String.valueOf(world.getSeed())));
    }

    private static <V> V notnull(@Nullable V value, V defaultValue) {
        return value != null ? value : defaultValue;
    }
}
