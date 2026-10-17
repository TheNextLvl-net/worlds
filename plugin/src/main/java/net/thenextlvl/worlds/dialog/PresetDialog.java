package net.thenextlvl.worlds.dialog;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.preset.Preset;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static net.thenextlvl.worlds.dialog.DialogSupport.BUTTON_WIDTH;
import static net.thenextlvl.worlds.dialog.DialogSupport.WIDTH;
import static net.thenextlvl.worlds.dialog.DialogSupport.button;
import static net.thenextlvl.worlds.dialog.DialogSupport.text;

@NullMarked
final class PresetDialog {
    private static final int COLUMNS = 2;

    private PresetDialog() {
    }

    static Dialog create(final WorldsPlugin plugin, final Player player, final Map<String, Preset> presets,
                         final String selected, final BiConsumer<Player, String> onSelect, final Consumer<Player> onBack) {
        final var actions = presets.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    final var label = name(entry.getValue());
                    return button(entry.getKey().equals(selected) ? label.color(NamedTextColor.GREEN) : label, BUTTON_WIDTH,
                            (viewer, response) -> onSelect.accept(viewer, entry.getKey()));
                }).toList();

        final var current = presets.get(selected);
        final var body = DialogBody.plainMessage(text(plugin, player, "dialog.preset.selected",
                Placeholder.component("preset", current != null ? name(current) : Component.text(selected))), WIDTH);
        final var back = button(plugin, player, "dialog.button.back", (viewer, response) -> onBack.accept(viewer));
        return Dialog.create(builder -> builder.empty()
                .type(DialogType.multiAction(actions).exitAction(back).columns(COLUMNS).build())
                .base(DialogBase.builder(text(plugin, player, "dialog.preset.title"))
                        .canCloseWithEscape(true).body(List.of(body)).build()));
    }

    static Component name(final Preset preset) {
        return Component.text(preset.name().orElse("?"));
    }
}
