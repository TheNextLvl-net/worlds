package net.thenextlvl.worlds.dialog;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.CommandFailureHandler;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.List;

import static net.thenextlvl.worlds.dialog.DialogSupport.WIDTH;
import static net.thenextlvl.worlds.dialog.DialogSupport.button;
import static net.thenextlvl.worlds.dialog.DialogSupport.text;

@NullMarked
final class WorldBackupDialog {
    private WorldBackupDialog() {
    }

    static Dialog create(final WorldsPlugin plugin, final Player player, final World world) {
        final var placeholder = Placeholder.parsed("world", world.key().asString());
        final var name = DialogInput.text("name", text(plugin, player, "dialog.backup.name"))
                .width(WIDTH).initial("").maxLength(64).build();

        final var backup = button(plugin, player, "dialog.button.backup", (viewer, response) -> {
            if (!viewer.hasPermission("worlds.command.backup.create")) return;
            final var text = response.getText("name");
            final var backupName = text != null && !text.isBlank() ? text.trim() : null;
            plugin.bundle().sendMessage(viewer, "world.backup", placeholder);
            plugin.createBackup(world, backupName).thenAccept(created -> {
                final var bytes = created.size();
                final var kb = bytes / 1024d;
                final var mb = kb / 1024d;
                final var gb = mb / 1024d;
                plugin.bundle().sendMessage(viewer, "world.backup.success", placeholder,
                        Formatter.number("size", gb >= 1 ? gb : mb >= 1 ? mb : kb >= 1 ? kb : bytes),
                        Formatter.choice("unit", gb >= 1 ? 0 : mb >= 1 ? 1 : kb >= 1 ? 2 : 3));
            }).exceptionally(throwable -> {
                CommandFailureHandler.handle(plugin, viewer, throwable, placeholder,
                        Placeholder.parsed("backup", backupName != null ? backupName : ""));
                return null;
            });
        });
        final var back = button(plugin, player, "dialog.button.back",
                (viewer, response) -> viewer.showDialog(WorldEditDialog.create(plugin, viewer, world)));

        return Dialog.create(builder -> builder.empty()
                .type(DialogType.confirmation(backup, back))
                .base(DialogBase.builder(text(plugin, player, "dialog.backup.title", placeholder))
                        .canCloseWithEscape(true).inputs(List.of(name)).build()));
    }
}
