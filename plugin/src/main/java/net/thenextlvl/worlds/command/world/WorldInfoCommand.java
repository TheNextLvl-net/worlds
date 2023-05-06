package net.thenextlvl.worlds.command.world;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import core.api.placeholder.Placeholder;
import net.kyori.adventure.audience.Audience;
import net.thenextlvl.worlds.util.Messages;
import net.thenextlvl.worlds.volume.Volume;
import org.bukkit.Bukkit;
import org.bukkit.WorldType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;

import java.util.Objects;

class WorldInfoCommand {

    static Command.Builder<CommandSender> create(Command.Builder<CommandSender> builder) {
        return builder.literal("info")
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
        var locale = sender instanceof Player player ? player.locale() : Messages.ENGLISH;
        if (world == null && target.isEmpty()) {
            sender.sendRichMessage(Messages.ENTER_WORLD_NAME.message(locale, sender));
            return;
        }
        var name = Placeholder.<Audience>of("world", world != null ? world.getName() : target.get());
        if (world != null) {
            var volume = Volume.getOrCreate(world);
            var environment = Placeholder.<Audience>of("environment", () -> switch (world.getEnvironment()) {
                case THE_END -> "The End";
                case NETHER -> "Nether";
                case NORMAL -> "Normal";
                case CUSTOM -> "Custom";
            });
            var type = Placeholder.<Audience>of("type", () -> switch (Objects.requireNonNullElse(world.getWorldType(), WorldType.NORMAL)) {
                case LARGE_BIOMES -> "Large Biomes";
                case AMPLIFIED -> "Amplified";
                case NORMAL -> "Normal";
                case FLAT -> "Flat";
            });
            var players = Placeholder.<Audience>of("players", () -> world.getPlayers().size());
            var generator = Placeholder.<Audience>of("generator", () ->
                    Objects.requireNonNullElse(volume.getWorldImage().generator(), "Vanilla"));
            sender.sendRichMessage(Messages.WORLD_INFO_NAME.message(locale, sender, name));
            sender.sendRichMessage(Messages.WORLD_INFO_PLAYERS.message(locale, sender, players));
            sender.sendRichMessage(Messages.WORLD_INFO_TYPE.message(locale, sender, type));
            sender.sendRichMessage(Messages.WORLD_INFO_ENVIRONMENT.message(locale, sender, environment));
            sender.sendRichMessage(Messages.WORLD_INFO_GENERATOR.message(locale, sender, generator));
        } else sender.sendRichMessage(Messages.WORLD_NOT_FOUND.message(locale, sender, name));
    }
}
