package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import core.nbt.tag.CompoundTag;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.model.WorldPreset;
import net.thenextlvl.worlds.command.suggestion.WorldSuggestionProvider;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.Optional;

@NullMarked
class WorldInfoCommand {
    private final WorldsPlugin plugin;

    WorldInfoCommand(WorldsPlugin plugin) {
        this.plugin = plugin;
    }

    ArgumentBuilder<CommandSourceStack, ?> create() {
        return Commands.literal("info")
                .requires(source -> source.getSender().hasPermission("worlds.command.info"))
                .then(Commands.argument("world", ArgumentTypes.world())
                        .suggests(new WorldSuggestionProvider<>(plugin))
                        .executes(context -> {
                            var world = context.getArgument("world", World.class);
                            return list(context.getSource().getSender(), world);
                        }))
                .executes(context -> {
                    if (!(context.getSource().getSender() instanceof Player player)) {
                        plugin.bundle().sendMessage(context.getSource().getSender(), "command.sender");
                        return 0;
                    } else return list(context.getSource().getSender(), player.getWorld());
                });
    }

    @SuppressWarnings("deprecation")
    private int list(CommandSender sender, World world) {
        var root = plugin.levelView().getLevelDataFile(world.getWorldFolder()).getRoot();
        var data = root.<CompoundTag>optional("Data");
        var settings = data.flatMap(tag -> tag.<CompoundTag>optional("WorldGenSettings"));
        var dimensions = settings.flatMap(tag -> tag.<CompoundTag>optional("dimensions"));
        var dimension = dimensions.flatMap(tag -> tag.<CompoundTag>optional(
                plugin.levelView().getDimension(tag, world.getEnvironment())));
        var generator = dimension.flatMap(tag -> tag.<CompoundTag>optional("generator"));

        var environment = dimensions.map(tag -> plugin.levelView().getDimension(tag, world.getEnvironment()));
        var worldPreset = generator.flatMap(tag -> plugin.levelView().getWorldPreset(tag));

        plugin.bundle().sendMessage(sender, "world.info.name",
                Placeholder.parsed("world", world.key().asString()),
                Placeholder.parsed("name", world.getName()));
        plugin.bundle().sendMessage(sender, "world.info.players",
                Placeholder.parsed("players", String.valueOf(world.getPlayers().size())));
        plugin.bundle().sendMessage(sender, "world.info.type",
                Placeholder.parsed("type", worldPreset.map(WorldPreset::key)
                        .map(Key::asString).orElse("unknown")),
                Placeholder.parsed("old", Optional.ofNullable(world.getWorldType())
                        .orElse(WorldType.NORMAL).getName().toLowerCase()));
        plugin.bundle().sendMessage(sender, "world.info.dimension",
                Placeholder.parsed("dimension", environment.orElse("unknown")));
        plugin.levelView().getGenerator(world).ifPresent(gen -> plugin.bundle().sendMessage(sender,
                "world.info.generator", Placeholder.parsed("generator", gen)));
        plugin.bundle().sendMessage(sender, "world.info.seed",
                Placeholder.parsed("seed", String.valueOf(world.getSeed())));
        return Command.SINGLE_SUCCESS;
    }
}
