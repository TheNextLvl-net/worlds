package net.thenextlvl.worlds.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.Worlds;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.bukkit.parser.WorldParser;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.exception.InvalidSyntaxException;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@RequiredArgsConstructor
@SuppressWarnings("UnstableApiUsage")
class WorldInfoCommand {
    private final Worlds plugin;
    private final Command.Builder<CommandSourceStack> builder;

    Command.Builder<CommandSourceStack> create() {
        return builder.literal("info")
                .permission("worlds.command.world.info")
                .optional("world", WorldParser.worldParser())
                .handler(this::execute);
    }

    @SuppressWarnings("deprecation")
    private void execute(CommandContext<CommandSourceStack> context) {
        var sender = context.sender().getSender();
        var world = context.<World>optional("world").orElse(sender instanceof Player self ? self.getWorld() : null);
        if (world == null) throw new InvalidSyntaxException("world info [world]", sender, List.of());
        var volume = plugin.imageProvider().getOrDefault(world);
        plugin.bundle().sendMessage(sender, "world.info.name",
                Placeholder.parsed("world", world.getName()));
        plugin.bundle().sendMessage(sender, "world.info.players",
                Placeholder.parsed("players", String.valueOf(world.getPlayers().size())));
        plugin.bundle().sendMessage(sender, "world.info.type",
                Placeholder.parsed("type", getName(notnull(world.getWorldType(), WorldType.NORMAL))));
        plugin.bundle().sendMessage(sender, "world.info.environment",
                Placeholder.parsed("environment", getName(world.getEnvironment())));
        plugin.bundle().sendMessage(sender, "world.info.generator",
                Placeholder.parsed("generator", String.valueOf(notnull(
                        volume.getWorldImage().generator(), "Vanilla"))));
        plugin.bundle().sendMessage(sender, "world.info.seed",
                Placeholder.parsed("seed", String.valueOf(world.getSeed())));
    }

    private static String getName(World.Environment environment) {
        return switch (environment) {
            case THE_END -> "The End";
            case NETHER -> "Nether";
            case NORMAL -> "Normal";
            case CUSTOM -> "Custom";
        };
    }

    private String getName(WorldType type) {
        return switch (type) {
            case LARGE_BIOMES -> "Large Biomes";
            case AMPLIFIED -> "Amplified";
            case NORMAL -> "Normal";
            case FLAT -> "Flat";
        };
    }

    private <V> V notnull(@Nullable V value, V defaultValue) {
        return value != null ? value : defaultValue;
    }
}
