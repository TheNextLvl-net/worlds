package net.thenextlvl.worlds.dialog;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.CommandFailureHandler;
import net.thenextlvl.worlds.command.UnimportedWorlds;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static net.thenextlvl.worlds.dialog.DialogSupport.WIDTH;
import static net.thenextlvl.worlds.dialog.DialogSupport.button;
import static net.thenextlvl.worlds.dialog.DialogSupport.confirmation;
import static net.thenextlvl.worlds.dialog.DialogSupport.text;
import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND;

@NullMarked
public final class WorldListDialog {
    private WorldListDialog() {
    }

    public static Dialog create(final WorldsPlugin plugin, final Player player) {
        final var loaded = plugin.getServer().getWorlds().stream()
                .sorted(Comparator.comparing(world -> world.key().asString()))
                .toList();
        final var unloaded = plugin.getWorldRegistry().worlds()
                .filter(key -> plugin.getServer().getWorld(key) == null)
                .sorted(Comparator.comparing(Key::asString))
                .toList();

        final var actions = new ArrayList<ActionButton>();
        if (player.hasPermission("worlds.command.create")) actions.add(button(plugin, player, "dialog.list.create",
                (viewer, response) -> viewer.showDialog(WorldSetupDialog.create(plugin, viewer))));
        loaded.forEach(world -> actions.add(loaded(plugin, player, world)));
        if (player.hasPermission("worlds.command.load"))
            unloaded.forEach(key -> actions.add(unloaded(plugin, player, key)));
        final var unimported = player.hasPermission("worlds.command.import") ? UnimportedWorlds.list(plugin).stream()
                .sorted(Comparator.comparing(entry -> entry.key().asString()))
                .toList() : List.<UnimportedWorlds.Entry>of();
        unimported.forEach(entry -> actions.add(unimported(plugin, player, entry)));

        final var body = DialogBody.plainMessage(text(plugin, player, "dialog.list.summary",
                Formatter.number("worlds", loaded.size()),
                Formatter.number("unloaded", unloaded.size()),
                Formatter.number("unimported", unimported.size())), WIDTH);
        final var done = button(plugin, player, "dialog.button.done", (viewer, response) -> {
        });
        return Dialog.create(builder -> builder.empty()
                .type(DialogType.multiAction(actions).exitAction(done).columns(2).build())
                .base(DialogBase.builder(text(plugin, player, "dialog.list.title"))
                        .canCloseWithEscape(true).body(List.of(body)).build()));
    }

    private static ActionButton loaded(final WorldsPlugin plugin, final Player player, final World world) {
        return button(plugin, player, "dialog.list.loaded",
                (viewer, response) -> viewer.showDialog(WorldEditDialog.create(plugin, viewer, world)),
                Placeholder.parsed("world", world.key().asString()));
    }

    private static ActionButton unloaded(final WorldsPlugin plugin, final Player player, final Key key) {
        final var placeholder = Placeholder.parsed("world", key.asString());
        return button(plugin, player, "dialog.list.unloaded", (viewer, response) -> viewer.showDialog(confirmation(plugin, viewer,
                text(plugin, viewer, "dialog.load.title", placeholder),
                text(plugin, viewer, "dialog.load.message", placeholder),
                "dialog.button.load", target -> {
                    if (!target.hasPermission("worlds.command.load")) return;
                    plugin.bundle().sendMessage(target, "world.load", placeholder);
                    plugin.load(key).thenAccept(world -> {
                        plugin.getWorldRegistry().setEnabled(world.key(), true);
                        plugin.bundle().sendMessage(target, "world.load.success", placeholder);
                        target.teleportAsync(world.getSpawnLocation(), COMMAND);
                    }).exceptionally(throwable -> {
                        CommandFailureHandler.handle(plugin, target, throwable, placeholder);
                        return null;
                    });
                }, target -> target.showDialog(create(plugin, target)))), placeholder);
    }

    private static ActionButton unimported(final WorldsPlugin plugin, final Player player, final UnimportedWorlds.Entry entry) {
        return button(plugin, player, "dialog.list.unimported",
                (viewer, response) -> viewer.showDialog(WorldImportDialog.create(plugin, viewer, entry)),
                Placeholder.parsed("world", entry.key().asString()));
    }
}
