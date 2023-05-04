package worlds.command.world;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import core.api.placeholder.Placeholder;
import net.kyori.adventure.audience.Audience;
import net.thenextlvl.town.property.Property;
import net.thenextlvl.town.town.Town;
import net.thenextlvl.town.util.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;

class WorldInfoCommand {

    static Command.Builder<CommandSender> create(Command.Builder<CommandSender> builder) {
        return builder.literal("info")
                .argument(StringArgument.<CommandSender>builder("world")
                        .withSuggestionsProvider((context, token) -> Bukkit.getWorlds().stream()
                                .map(WorldInfo::getName)
                                .filter(s -> s.startsWith(token))
                                .toList())
                        .build())
                .handler(WorldInfoCommand::execute);
    }

    private static void execute(CommandContext<CommandSender> context) {
        var sender = context.getSender();
        var town = Town.get(context.get("town"));
        var name = Placeholder.<Audience>of("town", town != null ? town.getName() : context.get("town"));
        var locale = sender instanceof Player player ? player.locale() : Messages.ENGLISH;
        if (town != null) {
            var price = Placeholder.<Audience>of("price", town.getSquareMeterPrice());
            var displayName = Placeholder.<Audience>of("displayName",
                    town.getDisplayName() != null ? town.getDisplayName() : "<gray>-<dark_gray>/<gray>-");
            var amount = Placeholder.<Audience>of("amount", () -> town.getProperties().count());
            var properties = Placeholder.<Audience>of("properties", () ->
                    String.join(", ", town.getProperties().map(Property::getCustomName).toList()));
            sender.sendRichMessage(Messages.TOWN_INFO_NAME.message(locale, sender, name));
            sender.sendRichMessage(Messages.TOWN_INFO_PRICE.message(locale, sender, price));
            sender.sendRichMessage(Messages.TOWN_INFO_DISPLAYNAME.message(locale, sender, displayName));
            sender.sendRichMessage(Messages.TOWN_INFO_PROPERTIES.message(locale, sender, amount, properties));
        } else sender.sendRichMessage(Messages.TOWN_NOT_FOUND.message(locale, sender, name));
    }
}
