package net.thenextlvl.worlds.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.thenextlvl.worlds.Dimension;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.brigadier.SimpleCommand;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@NullMarked
final class WorldListCommand extends SimpleCommand {
    private WorldListCommand(final WorldsPlugin plugin) {
        super(plugin, "list", "worlds.command.list");
    }

    public static ArgumentBuilder<CommandSourceStack, ?> create(final WorldsPlugin plugin) {
        final var command = new WorldListCommand(plugin);
        return command.create().executes(command);
    }

    @Override
    public int run(final CommandContext<CommandSourceStack> context) {
        final var sender = context.getSource().getSender();

        final var entries = collectEntries();
        plugin.bundle().sendMessage(sender, "world.list.header",
                Placeholder.parsed("worlds", String.valueOf(count(entries, State.LOADED))),
                Placeholder.parsed("unloaded", String.valueOf(count(entries, State.UNLOADED))),
                Placeholder.parsed("unimported", String.valueOf(count(entries, State.UNIMPORTED))));
        groupedByNamespace(entries).forEach((namespace, worlds) -> sendNamespace(sender, namespace, worlds));
        return SINGLE_SUCCESS;
    }

    private List<WorldListEntry> collectEntries() {
        return Stream.concat(Stream.concat(loadedEntries(), unloadedEntries()), unimportedEntries().stream())
                .toList();
    }

    private Stream<WorldListEntry> loadedEntries() {
        return plugin.getServer().getWorlds().stream()
                .map(world -> new WorldListEntry(
                        world.key(),
                        plugin.handler().getDimension(world),
                        State.LOADED,
                        null
                ));
    }

    private Stream<WorldListEntry> unloadedEntries() {
        return plugin.getWorldRegistry().entrySet()
                .filter(entry -> plugin.getServer().getWorld(entry.getKey()) == null)
                .map(entry -> new WorldListEntry(
                        entry.getKey(),
                        entry.getValue().dimension(),
                        State.UNLOADED,
                        null
                ));
    }

    private List<WorldListEntry> unimportedEntries() {
        try (final var unimported = listUnimported()) {
            return unimported.toList();
        } catch (final IOException ignored) {
            return List.of();
        }
    }

    private Map<String, List<WorldListEntry>> groupedByNamespace(final List<WorldListEntry> entries) {
        return entries.stream()
                .sorted()
                .collect(Collectors.groupingBy(this::namespace, TreeMap::new, Collectors.toList()));
    }

    private void sendNamespace(final CommandSender sender, final String namespace, final List<WorldListEntry> worlds) {
        plugin.bundle().sendMessage(sender, "world.list.namespace",
                Placeholder.parsed("namespace", namespace),
                Placeholder.parsed("amount", String.valueOf(worlds.size())));
        IntStream.range(0, worlds.size())
                .mapToObj(index -> worlds.get(index).component(plugin, sender, index == worlds.size() - 1))
                .forEach(sender::sendMessage);
    }

    private String namespace(final WorldListEntry entry) {
        if (!entry.state().equals(State.UNIMPORTED) || entry.importPath() == null
                || plugin.modernWorldRegistry().read(entry.importPath()).isPresent()
                || plugin.legacyWorldRegistry().read(entry.importPath()).isPresent())
            return entry.key().namespace();
        final var root = plugin.getServer().getWorldContainer().toPath().toAbsolutePath().normalize();
        final var parent = entry.importPath().toAbsolutePath().normalize().getParent();
        if (parent == null || parent.equals(root)) return ".";
        return root.relativize(parent).toString();
    }

    private Stream<WorldListEntry> listUnimported() throws IOException {
        final var root = plugin.getServer().getWorldContainer().toPath();
        final var legacy = plugin.legacyWorldRegistry().listEntries(root)
                .map(entry -> new WorldListEntry(entry.getValue().key(), entry.getValue().dimension(), State.UNIMPORTED, entry.getKey()));
        final var modern = plugin.modernWorldRegistry().listEntries(root)
                .map(entry -> new WorldListEntry(entry.getValue().key(), null, State.UNIMPORTED, entry.getKey()));
        return Stream.concat(legacy, modern);
    }

    private long count(final List<WorldListEntry> entries, final State state) {
        return entries.stream()
                .filter(entry -> entry.state() == state)
                .count();
    }

    static String displayDimension(final Dimension dimension) {
        final var key = dimension.key();
        if (key.equals(Dimension.OVERWORLD.key())) return "normal";
        if (key.equals(Dimension.THE_NETHER.key())) return "nether";
        if (key.equals(Dimension.THE_END.key())) return "the_end";
        return key.asString();
    }

    private enum State {
        LOADED("world.list.loaded", "world.list.hover", "/world teleport "),
        UNLOADED("world.list.unloaded", "world.list.load.hover", "/world load "),
        UNIMPORTED("world.list.unimported", "world.list.import.hover", "/world import ");

        private final String translationKey;
        private final String hoverKey;
        private final String command;

        State(final String translationKey, final String hoverKey, final String command) {
            this.translationKey = translationKey;
            this.hoverKey = hoverKey;
            this.command = command;
        }
    }

    private record WorldListEntry(Key key, @Nullable Dimension dimension,
                                  State state, @Nullable Path importPath) implements Comparable<WorldListEntry> {
        private static final Comparator<WorldListEntry> COMPARATOR = Comparator
                .comparing((WorldListEntry entry) -> entry.key.namespace())
                .thenComparing(entry -> entry.state)
                .thenComparingInt(WorldListEntry::dimensionOrder)
                .thenComparing(entry -> entry.dimension != null ? entry.dimension.key().asString() : "")
                .thenComparing(entry -> entry.key.value());

        private Component component(final WorldsPlugin plugin, final CommandSender sender, final boolean last) {
            final var key = key().asString();
            final var placeholders = new TagResolver[]{
                    Placeholder.parsed("tree", last ? "└" : "├"),
                    Placeholder.component("world", label()),
                    Placeholder.parsed("dimension", displayDimension()),
            };
            final var suffix = state.equals(State.UNIMPORTED) ? " " : "";
            return plugin.bundle().component(state.translationKey, sender, placeholders)
                    .hoverEvent(HoverEvent.showText(plugin.bundle().component(state.hoverKey, sender,
                            Placeholder.parsed("world", key))))
                    .clickEvent(ClickEvent.suggestCommand(command(plugin) + suffix));
        }

        private String command(final WorldsPlugin plugin) {
            if (!state.equals(State.UNIMPORTED) || importPath == null) return state.command + key().asString();
            final var root = plugin.getServer().getWorldContainer().toPath().toAbsolutePath().normalize();
            final var path = root.relativize(importPath.toAbsolutePath().normalize()).toString();
            final var command = new StringBuilder(state.command)
                    .append("\"")
                    .append(path.replace("\\", "\\\\").replace("\"", "\\\""))
                    .append("\" ")
                    .append(key().asString());
            plugin.legacyWorldRegistry().read(importPath).ifPresent(data -> {
                final var dimension = data.dimension();
                if (dimension != null) command.append(" dimension ").append(dimension.key().asString());
                final var generator = data.generator();
                if (generator != null) command.append(" generator ").append(generator);
            });
            return command.toString();
        }

        private Component label() {
            return Component.text(key.value());
        }

        private String displayDimension() {
            return dimension != null ? WorldListCommand.displayDimension(dimension) : "unknown";
        }

        @Override
        public int compareTo(final WorldListEntry other) {
            return COMPARATOR.compare(this, other);
        }

        private int dimensionOrder() {
            if (dimension == null) return Integer.MAX_VALUE;
            final var key = dimension.key();
            if (key.equals(Dimension.OVERWORLD.key())) return 0;
            if (key.equals(Dimension.THE_NETHER.key())) return 1;
            if (key.equals(Dimension.THE_END.key())) return 2;
            return 3;
        }
    }
}
