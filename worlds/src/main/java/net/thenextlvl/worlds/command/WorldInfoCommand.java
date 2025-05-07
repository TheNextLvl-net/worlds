package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.thenextlvl.worlds.WorldsPlugin;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import static net.thenextlvl.worlds.command.WorldCommand.worldArgument;

@NullMarked
class WorldInfoCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> create(WorldsPlugin plugin) {
        return Commands.literal("info")
                .requires(source -> source.getSender().hasPermission("worlds.command.info"))
                .then(info(plugin))
                .executes(context -> info(plugin, context));
    }

    private static int info(WorldsPlugin plugin, CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getSender() instanceof Player player)) {
            plugin.bundle().sendMessage(context.getSource().getSender(), "command.sender");
            return 0;
        } else return list(context.getSource().getSender(), player.getWorld(), plugin);
    }

    private static RequiredArgumentBuilder<CommandSourceStack, World> info(WorldsPlugin plugin) {
        return worldArgument(plugin).executes(context -> {
            var world = context.getArgument("world", World.class);
            return list(context.getSource().getSender(), world, plugin);
        });
    }

    @SuppressWarnings("deprecation")
    private static int list(CommandSender sender, World world, WorldsPlugin plugin) {
        // var root = plugin.levelView().getLevelDataFile(world.getWorldFolder().toPath()).getRoot();
        // var data = root.<CompoundTag>optional("Data");
        // var settings = data.flatMap(tag -> tag.<CompoundTag>optional("WorldGenSettings"));
        // var dimensions = settings.flatMap(tag -> tag.<CompoundTag>optional("dimensions"));
        // var dimension = dimensions.flatMap(tag -> tag.<CompoundTag>optional(
        //         plugin.levelView().getDimension(tag, world.getEnvironment())));
        // var generator = dimension.flatMap(tag -> tag.<CompoundTag>optional("generator"));
//
        // var environment = dimensions.map(tag -> plugin.levelView().getDimension(tag, world.getEnvironment()));
        // var worldPreset = generator.flatMap(tag -> plugin.levelView().getWorldPreset(tag));
//
        // plugin.bundle().sendMessage(sender, "world.info.name",
        //         Placeholder.parsed("world", world.key().asString()),
        //         Placeholder.parsed("name", world.getName()));
        // plugin.bundle().sendMessage(sender, "world.info.players",
        //         Placeholder.parsed("players", String.valueOf(world.getPlayers().size())));
        // plugin.bundle().sendMessage(sender, "world.info.type",
        //         Placeholder.parsed("type", worldPreset.map(GeneratorType::key)
        //                 .map(Key::asString).orElse("unknown")),
        //         Placeholder.parsed("old", Optional.ofNullable(world.getWorldType())
        //                 .orElse(WorldType.NORMAL).getName().toLowerCase()));
        // plugin.bundle().sendMessage(sender, "world.info.dimension",
        //         Placeholder.parsed("dimension", environment.orElse("unknown")));
        // plugin.levelView().getGenerator(world).ifPresent(gen -> plugin.bundle().sendMessage(sender,
        //         "world.info.generator", Placeholder.parsed("generator", gen)));
        // plugin.bundle().sendMessage(sender, "world.info.seed",
        //         Placeholder.parsed("seed", String.valueOf(world.getSeed())));
//
        // // todo: send total world size
        return Command.SINGLE_SUCCESS;
    }
}
