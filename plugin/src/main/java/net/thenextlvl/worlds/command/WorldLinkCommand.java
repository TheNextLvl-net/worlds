package net.thenextlvl.worlds.command;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.Worlds;
import net.thenextlvl.worlds.link.Link;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.Command;
import org.incendo.cloud.bukkit.parser.WorldParser;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.exception.InvalidSyntaxException;
import org.incendo.cloud.parser.standard.EnumParser;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

@RequiredArgsConstructor
abstract class WorldLinkCommand {
    protected final Worlds plugin;
    protected final Command.Builder<CommandSender> builder;

    protected final Command.Builder<CommandSender> linkCommand() {
        return builder.literal("link")
                .commandDescription(Description.description("link portals between dimensions"));
    }

    abstract Command.Builder<CommandSender> create();

    static class Create extends WorldLinkCommand {
        public Create(Worlds plugin, Command.Builder<CommandSender> builder) {
            super(plugin, builder);
        }

        @Override
        Command.Builder<CommandSender> create() {
            return linkCommand().literal("create")
                    .permission("worlds.command.link.create")
                    .required("source", WorldParser.worldParser())
                    .required("destination", WorldParser.worldParser())
                    .optional("portal-type", EnumParser.enumParser(PortalType.class))
                    .handler(this::execute);
        }

        private void execute(CommandContext<CommandSender> context) {
            handleCreate(context);
        }

        private void handleCreate(CommandContext<CommandSender> context) {
            var source = context.<World>get("source");
            var destination = context.<World>get("destination");
            var portalType = context.<PortalType>optional("portal-type")
                    .orElse(getPortalType(source.getEnvironment(), destination.getEnvironment()));

            if (portalType == null) throw new InvalidSyntaxException(
                    "world link create [source] [destination] [portal-type]",
                    context.sender(), java.util.List.of()
            );

            var link = new Link(portalType, source, destination);
            if (plugin.linkRegistry().register(link)) {
                plugin.bundle().sendMessage(context.sender(), "link.created",
                        Placeholder.parsed("type", link.portalType().name().toLowerCase()),
                        Placeholder.parsed("source", link.source().getName()),
                        Placeholder.parsed("destination", link.destination().getName()));
            } else plugin.bundle().sendMessage(context.sender(), "link.exists",
                    Placeholder.parsed("type", link.portalType().name().toLowerCase()),
                    Placeholder.parsed("source", link.source().getName()),
                    Placeholder.parsed("destination", link.destination().getName()));
        }

        private PortalType getPortalType(World.Environment source, World.Environment destination) {
            return switch (source) {
                case NORMAL -> switch (destination) {
                    case NETHER -> PortalType.NETHER;
                    case THE_END -> PortalType.ENDER;
                    default -> null;
                };
                case NETHER -> switch (destination) {
                    case THE_END -> PortalType.ENDER;
                    case NORMAL -> PortalType.NETHER;
                    default -> null;
                };
                case THE_END -> switch (destination) {
                    case NORMAL -> PortalType.ENDER;
                    case NETHER -> PortalType.NETHER;
                    default -> null;
                };
                default -> null;
            };
        }
    }

    static class Delete extends WorldLinkCommand {
        public Delete(Worlds plugin, Command.Builder<CommandSender> builder) {
            super(plugin, builder);
        }

        @Override
        Command.Builder<CommandSender> create() {
            return linkCommand().literal("delete")
                    .permission("worlds.command.link.delete")
                    .required("link", StringParser.greedyStringParser(),
                            SuggestionProvider.blocking((context, input) -> plugin.linkRegistry().getLinks()
                                    .map(Link::toString)
                                    .map(Suggestion::simple)
                                    .toList()))
                    .handler(this::execute);
        }

        private void execute(CommandContext<CommandSender> context) {
            var linkName = context.<String>get("link");
            var link = plugin.linkRegistry().getLinks()
                    .filter(link1 -> link1.toString().equals(linkName))
                    .findFirst()
                    .orElse(null);
            if (link != null && plugin.linkRegistry().unregister(link)) {
                plugin.bundle().sendMessage(context.sender(), "link.deleted",
                        Placeholder.parsed("type", link.portalType().name().toLowerCase()),
                        Placeholder.parsed("source", link.source().getName()),
                        Placeholder.parsed("destination", link.destination().getName()));
            } else plugin.bundle().sendMessage(context.sender(), "link.exists.not",
                    Placeholder.parsed("link", linkName));
        }
    }

    static class List extends WorldLinkCommand {
        public List(Worlds plugin, Command.Builder<CommandSender> builder) {
            super(plugin, builder);
        }

        @Override
        Command.Builder<CommandSender> create() {
            return linkCommand().literal("list")
                    .permission("worlds.command.link.list")
                    .handler(this::execute);
        }

        private void execute(CommandContext<CommandSender> context) {
            var links = plugin.linkRegistry().getLinks().map(Link::toString).toList();
            if (links.isEmpty()) plugin.bundle().sendMessage(context.sender(), "link.list.empty");
            else plugin.bundle().sendMessage(context.sender(), "link.list",
                    Placeholder.parsed("links", String.join(", ", links)),
                    Placeholder.parsed("amount", String.valueOf(links.size())));
        }
    }
}
