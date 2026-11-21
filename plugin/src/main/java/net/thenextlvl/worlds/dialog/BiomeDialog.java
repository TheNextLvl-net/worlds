package net.thenextlvl.worlds.dialog;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static net.thenextlvl.worlds.dialog.DialogSupport.WIDTH;
import static net.thenextlvl.worlds.dialog.DialogSupport.button;
import static net.thenextlvl.worlds.dialog.DialogSupport.text;

@NullMarked
final class BiomeDialog {
    private static final int COLUMNS = 4;
    private static final int BIOME_WIDTH = 100;

    private BiomeDialog() {
    }

    static Dialog create(final WorldsPlugin plugin, final Player player, final Key selected,
                         final BiConsumer<Player, Key> onSelect, final Consumer<Player> onBack) {
        final var actions = RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME).stream()
                .sorted(Comparator.comparing(biome -> biome.key().asString()))
                .map(biome -> {
                    final var label = Component.translatable(biome);
                    return button(biome.key().equals(selected) ? label.color(NamedTextColor.GREEN) : label, BIOME_WIDTH,
                            Component.text(biome.key().asString()), (viewer, response) -> onSelect.accept(viewer, biome.key()));
                }).toList();

        final var body = DialogBody.plainMessage(text(plugin, player, "dialog.biome.selected",
                Placeholder.component("biome", name(selected))), WIDTH);
        final var back = button(plugin, player, "dialog.button.back", (viewer, response) -> onBack.accept(viewer));
        return Dialog.create(builder -> builder.empty()
                .type(DialogType.multiAction(actions).exitAction(back).columns(COLUMNS).build())
                .base(DialogBase.builder(text(plugin, player, "dialog.biome.title"))
                        .canCloseWithEscape(true).body(List.of(body)).build()));
    }

    static Component name(final Key key) {
        final var biome = RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME).get(key);
        return biome != null ? Component.translatable(biome) : Component.text(key.asString());
    }
}
