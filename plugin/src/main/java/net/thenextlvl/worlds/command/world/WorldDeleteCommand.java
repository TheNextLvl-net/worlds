package net.thenextlvl.worlds.command.world;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.flags.CommandFlag;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.Worlds;
import net.thenextlvl.worlds.image.Image;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.java.JavaPlugin;

class WorldDeleteCommand {
    private static final Worlds plugin = JavaPlugin.getPlugin(Worlds.class);

    static Command.Builder<CommandSender> create(Command.Builder<CommandSender> builder) {
        return builder.literal("delete")
                .permission("worlds.command.world.delete")
                .argument(StringArgument.<CommandSender>builder("world")
                        .withSuggestionsProvider((context, token) -> Bukkit.getWorlds().stream()
                                .filter(world -> !world.getKey().toString().equals("minecraft:overworld"))
                                .map(WorldInfo::getName)
                                .filter(s -> s.startsWith(token))
                                .toList())
                        .build())
                .flag(CommandFlag.builder("keep-image"))
                .flag(CommandFlag.builder("keep-world"))
                .handler(WorldDeleteCommand::execute);
    }

    private static void execute(CommandContext<CommandSender> context) {
        var name = context.<String>get("world");
        var world = Bukkit.getWorld(name);
        var sender = context.getSender();
        if (world == null) {
            plugin.bundle().sendMessage(sender, "world.exists.not", Placeholder.parsed("world", name));
            return;
        }
        var keepImage = context.flags().contains("keep-image");
        var keepWorld = context.flags().contains("keep-world");
        var image = Image.getOrDefault(world);
        var result = image.delete(keepImage, keepWorld);
        plugin.bundle().sendMessage(sender, result.getMessage(),
                Placeholder.parsed("world", world.getName()),
                Placeholder.parsed("image", image.getWorldImage().name()));
        if (keepImage) image.save();
    }
}
