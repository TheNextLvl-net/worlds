package net.thenextlvl.worlds.dialog;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.thenextlvl.worlds.WorldsPlugin;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@NullMarked
final class DialogSupport {
    static final int WIDTH = 300;
    static final int BUTTON_WIDTH = 150;

    private static final ClickCallback.Options OPTIONS = ClickCallback.Options.builder()
            .uses(1)
            .lifetime(Duration.ofMinutes(30))
            .build();

    private DialogSupport() {
    }

    static Component text(final WorldsPlugin plugin, final Player player, final String key, final TagResolver... resolvers) {
        return plugin.bundle().component(key, player, resolvers);
    }

    static ActionButton button(final WorldsPlugin plugin, final Player player, final String key,
                               final BiConsumer<Player, DialogResponseView> callback, final TagResolver... resolvers) {
        return button(text(plugin, player, key, resolvers), callback);
    }

    static ActionButton button(final Component label, final BiConsumer<Player, DialogResponseView> callback) {
        return button(label, BUTTON_WIDTH, callback);
    }

    static ActionButton button(final Component label, final int width, final BiConsumer<Player, DialogResponseView> callback) {
        return button(label, width, null, callback);
    }

    static ActionButton button(final Component label, final int width, @Nullable final Component tooltip,
                               final BiConsumer<Player, DialogResponseView> callback) {
        return ActionButton.builder(label).width(width).tooltip(tooltip).action(DialogAction.customClick((response, audience) -> {
            if (audience instanceof final Player player) callback.accept(player, response);
        }, OPTIONS)).build();
    }

    static Dialog confirmation(final WorldsPlugin plugin, final Player player, final Component title,
                               final Component message, final String confirmKey,
                               final Consumer<Player> onConfirm, final Consumer<Player> onCancel) {
        final var confirm = button(plugin, player, confirmKey, (viewer, response) -> onConfirm.accept(viewer));
        final var cancel = button(plugin, player, "dialog.button.cancel", (viewer, response) -> onCancel.accept(viewer));
        return Dialog.create(builder -> builder.empty()
                .type(DialogType.confirmation(confirm, cancel))
                .base(DialogBase.builder(title).canCloseWithEscape(true)
                        .body(List.of(DialogBody.plainMessage(message, WIDTH))).build()));
    }

    static SingleOptionDialogInput.OptionEntry option(final String id, final Component display, final String selected) {
        return SingleOptionDialogInput.OptionEntry.create(id, display, id.equals(selected));
    }

    @SuppressWarnings("PatternValidation")
    static Optional<Key> parseKey(final String input) {
        try {
            return Optional.of(input.contains(":") ? Key.key(input) : Key.key("worlds", input));
        } catch (final InvalidKeyException e) {
            return Optional.empty();
        }
    }

    static String getText(final DialogResponseView response, final String key, final String fallback) {
        final var text = response.getText(key);
        return text != null ? text : fallback;
    }

    static boolean getBoolean(final DialogResponseView response, final String key, final boolean fallback) {
        final var value = response.getBoolean(key);
        return value != null ? value : fallback;
    }
}
