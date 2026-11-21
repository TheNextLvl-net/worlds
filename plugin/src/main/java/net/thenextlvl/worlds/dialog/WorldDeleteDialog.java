package net.thenextlvl.worlds.dialog;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.OperationScheduler;
import net.thenextlvl.worlds.WorldOperationException;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.CommandFailureHandler;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;

import static net.thenextlvl.worlds.dialog.DialogSupport.WIDTH;
import static net.thenextlvl.worlds.dialog.DialogSupport.button;
import static net.thenextlvl.worlds.dialog.DialogSupport.text;
import static net.thenextlvl.worlds.event.WorldActionScheduledEvent.ActionType.DELETE;

@NullMarked
final class WorldDeleteDialog {
    private WorldDeleteDialog() {
    }

    static Dialog create(final WorldsPlugin plugin, final Player player, final World world) {
        final var placeholder = Placeholder.parsed("world", world.key().asString());
        final var scheduled = plugin.getScheduler().isScheduled(world.key(), DELETE);

        final var delete = button(plugin, player, "dialog.button.delete", (viewer, response) -> {
            if (!viewer.hasPermission("worlds.command.delete")) return;
            plugin.bundle().sendMessage(viewer, "world.delete", placeholder);
            plugin.delete(world).thenAccept(success -> {
                if (success) plugin.bundle().sendMessage(viewer, "world.delete.success", placeholder);
                else CommandFailureHandler.handle(plugin, viewer, new WorldOperationException(
                        WorldOperationException.Reason.EVENT_CANCELLED
                ));
            }).exceptionally(throwable -> {
                CommandFailureHandler.handle(plugin, viewer, throwable, placeholder);
                return null;
            });
        });
        final var schedule = scheduled ? button(plugin, player, "dialog.button.delete.unschedule", (viewer, response) -> {
            if (!viewer.hasPermission("worlds.command.delete")) return;
            if (plugin.getScheduler().cancel(world.key(), DELETE))
                plugin.bundle().sendMessage(viewer, "world.delete.schedule-cancelled", placeholder);
        }) : button(plugin, player, "dialog.button.delete.schedule", (viewer, response) -> {
            if (!viewer.hasPermission("worlds.command.delete")) return;
            if (plugin.getScheduler().schedule(new OperationScheduler.DeleteOperation(world.key())))
                plugin.bundle().sendMessage(viewer, "world.delete.scheduled", placeholder);
            else CommandFailureHandler.handle(plugin, viewer, new WorldOperationException(
                    WorldOperationException.Reason.EVENT_CANCELLED
            ));
        });
        final var cancel = button(plugin, player, "dialog.button.cancel",
                (viewer, response) -> viewer.showDialog(WorldEditDialog.create(plugin, viewer, world)));

        final var body = new ArrayList<DialogBody>();
        body.add(DialogBody.plainMessage(text(plugin, player, "dialog.delete.warning", placeholder), WIDTH));
        if (scheduled) body.add(DialogBody.plainMessage(text(plugin, player, "dialog.delete.scheduled", placeholder), WIDTH));
        return Dialog.create(builder -> builder.empty()
                .type(DialogType.multiAction(List.<ActionButton>of(delete, schedule)).exitAction(cancel).columns(2).build())
                .base(DialogBase.builder(text(plugin, player, "dialog.delete.title", placeholder))
                        .canCloseWithEscape(true).body(body).build()));
    }
}
