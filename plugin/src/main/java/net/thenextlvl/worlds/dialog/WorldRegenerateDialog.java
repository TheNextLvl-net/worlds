package net.thenextlvl.worlds.dialog;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
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
import java.util.concurrent.ThreadLocalRandom;

import static net.thenextlvl.worlds.dialog.DialogSupport.WIDTH;
import static net.thenextlvl.worlds.dialog.DialogSupport.button;
import static net.thenextlvl.worlds.dialog.DialogSupport.getBoolean;
import static net.thenextlvl.worlds.dialog.DialogSupport.text;
import static net.thenextlvl.worlds.event.WorldActionScheduledEvent.ActionType.REGENERATE;

@NullMarked
final class WorldRegenerateDialog {
    private WorldRegenerateDialog() {
    }

    static Dialog create(final WorldsPlugin plugin, final Player player, final World world) {
        final var placeholder = Placeholder.parsed("world", world.key().asString());
        final var scheduled = plugin.getScheduler().isScheduled(world.key(), REGENERATE);

        final var regenerate = button(plugin, player, "dialog.button.regenerate", (viewer, response) -> {
            if (!viewer.hasPermission("worlds.command.regenerate")) return;
            final var newSeed = getBoolean(response, "new_seed", false);
            final var seed = newSeed ? ThreadLocalRandom.current().nextLong() : world.getSeed();
            plugin.bundle().sendMessage(viewer, "world.regenerate", placeholder);
            plugin.regenerate(world, builder -> {
                if (newSeed) builder.seed(seed);
            }).thenAccept(ignored -> plugin.bundle().sendMessage(viewer, "world.regenerate.success", placeholder)
            ).exceptionally(throwable -> {
                CommandFailureHandler.handle(plugin, viewer, throwable, placeholder);
                return null;
            });
        });
        final var schedule = scheduled ? button(plugin, player, "dialog.button.regenerate.unschedule", (viewer, response) -> {
            if (!viewer.hasPermission("worlds.command.regenerate")) return;
            if (plugin.getScheduler().cancel(world.key(), REGENERATE))
                plugin.bundle().sendMessage(viewer, "world.regenerate.schedule-cancelled", placeholder);
        }) : button(plugin, player, "dialog.button.regenerate.schedule", (viewer, response) -> {
            if (!viewer.hasPermission("worlds.command.regenerate")) return;
            final var seed = getBoolean(response, "new_seed", false) ? ThreadLocalRandom.current().nextLong() : world.getSeed();
            if (plugin.getScheduler().schedule(new OperationScheduler.RegenerateOperation(world.key(), seed)))
                plugin.bundle().sendMessage(viewer, "world.regenerate.scheduled", placeholder);
            else CommandFailureHandler.handle(plugin, viewer, new WorldOperationException(
                    WorldOperationException.Reason.EVENT_CANCELLED
            ));
        });
        final var cancel = button(plugin, player, "dialog.button.cancel",
                (viewer, response) -> viewer.showDialog(WorldEditDialog.create(plugin, viewer, world)));

        final var body = new ArrayList<DialogBody>();
        body.add(DialogBody.plainMessage(text(plugin, player, "dialog.regenerate.warning", placeholder), WIDTH));
        if (scheduled) body.add(DialogBody.plainMessage(text(plugin, player, "dialog.regenerate.scheduled", placeholder), WIDTH));
        final var newSeed = DialogInput.bool("new_seed", text(plugin, player, "dialog.regenerate.new-seed")).initial(false).build();
        return Dialog.create(builder -> builder.empty()
                .type(DialogType.multiAction(List.of(regenerate, schedule)).exitAction(cancel).columns(2).build())
                .base(DialogBase.builder(text(plugin, player, "dialog.regenerate.title", placeholder))
                        .canCloseWithEscape(true).body(body).inputs(List.of(newSeed)).build()));
    }
}
