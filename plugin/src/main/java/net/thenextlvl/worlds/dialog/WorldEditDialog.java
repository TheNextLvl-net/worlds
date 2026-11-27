package net.thenextlvl.worlds.dialog;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.thenextlvl.worlds.Dimension;
import net.thenextlvl.worlds.WorldsPlugin;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;

import static net.thenextlvl.worlds.dialog.DialogSupport.WIDTH;
import static net.thenextlvl.worlds.dialog.DialogSupport.button;
import static net.thenextlvl.worlds.dialog.DialogSupport.text;
import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND;

@NullMarked
public final class WorldEditDialog {
    private WorldEditDialog() {
    }

    public static Dialog create(final WorldsPlugin plugin, final Player player, final World world) {
        final var placeholder = Placeholder.parsed("world", world.key().asString());

        final var actions = new ArrayList<ActionButton>();
        if (player.hasPermission("worlds.command.teleport")) actions.add(button(plugin, player, "dialog.button.teleport", (viewer, response) -> {
            if (!viewer.hasPermission("worlds.command.teleport")) return;
            viewer.teleportAsync(world.getSpawnLocation(), COMMAND).thenAccept(success -> {
                final var message = success ? "world.teleport.self" : "world.teleport.failed";
                plugin.bundle().sendMessage(viewer, message, placeholder);
            });
        }));
        if (player.hasPermission("worlds.command.gamerule")) actions.add(button(plugin, player, "dialog.button.gamerules",
                (viewer, response) -> viewer.showDialog(GameRuleDialog.create(plugin, viewer, world))));
        if (player.hasPermission("worlds.command.backup.create")) actions.add(button(plugin, player, "dialog.button.backup",
                (viewer, response) -> viewer.showDialog(WorldBackupDialog.create(plugin, viewer, world))));
        if (player.hasPermission("worlds.command.recreate")) actions.add(button(plugin, player, "dialog.button.recreate",
                (viewer, response) -> viewer.showDialog(WorldSetupDialog.recreate(plugin, viewer, world))));
        if (player.hasPermission("worlds.command.regenerate")) actions.add(button(plugin, player, "dialog.button.regenerate",
                (viewer, response) -> viewer.showDialog(WorldRegenerateDialog.create(plugin, viewer, world))));
        if (player.hasPermission("worlds.command.delete")) actions.add(button(plugin, player, "dialog.button.delete",
                (viewer, response) -> viewer.showDialog(WorldDeleteDialog.create(plugin, viewer, world))));

        final var back = button(plugin, player, "dialog.button.back",
                (viewer, response) -> viewer.showDialog(WorldListDialog.create(plugin, viewer)));

        final var title = text(plugin, player, "dialog.edit.title", placeholder);
        final var body = DialogBody.plainMessage(info(plugin, player, world), WIDTH);
        return Dialog.create(builder -> builder.empty()
                .type(actions.isEmpty() ? DialogType.notice(back)
                        : DialogType.multiAction(actions).exitAction(back).columns(2).build())
                .base(DialogBase.builder(title).canCloseWithEscape(true).body(List.of(body)).build()));
    }

    private static Component info(final WorldsPlugin plugin, final Player player, final World world) {
        final var rows = new ArrayList<Component>();
        rows.add(row(plugin, player, "world.info.type", Placeholder.parsed("type", plugin.handler().getGeneratorType(world).name())));
        rows.add(row(plugin, player, "world.info.dimension", Placeholder.parsed("dimension", display(plugin.getDimension(world)))));
        plugin.handler().getGenerator(world).ifPresent(generator -> rows.add(row(plugin, player,
                "world.info.generator", Placeholder.parsed("generator", generator.getName()))));
        rows.add(row(plugin, player, "world.info.seed", Placeholder.parsed("seed", String.valueOf(world.getSeed()))));
        rows.add(row(plugin, player, "world.info.players", Formatter.number("players", world.getPlayers().size())));
        return Component.join(JoinConfiguration.newlines(), rows);
    }

    private static Component row(final WorldsPlugin plugin, final Player player, final String key, final TagResolver resolver) {
        return text(plugin, player, key, Placeholder.parsed("tree", "•"), resolver);
    }

    private static String display(final Dimension dimension) {
        if (dimension.equals(Dimension.OVERWORLD)) return "normal";
        if (dimension.equals(Dimension.THE_NETHER)) return "nether";
        if (dimension.equals(Dimension.THE_END)) return "the_end";
        return dimension.key().asString();
    }
}
