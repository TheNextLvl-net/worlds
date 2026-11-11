package net.thenextlvl.worlds.dialog;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.Dimension;
import net.thenextlvl.worlds.LegacyWorldRegistry;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.UnimportedWorlds;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static net.thenextlvl.worlds.dialog.DialogSupport.WIDTH;
import static net.thenextlvl.worlds.dialog.DialogSupport.button;
import static net.thenextlvl.worlds.dialog.DialogSupport.getBoolean;
import static net.thenextlvl.worlds.dialog.DialogSupport.getText;
import static net.thenextlvl.worlds.dialog.DialogSupport.option;
import static net.thenextlvl.worlds.dialog.DialogSupport.parseKey;
import static net.thenextlvl.worlds.dialog.DialogSupport.text;

@NullMarked
final class WorldImportDialog {
    private static final String DETECT = "detect";
    private static final String NONE = "none";

    private WorldImportDialog() {
    }

    static Dialog create(final WorldsPlugin plugin, final Player player, final UnimportedWorlds.Entry entry) {
        final var data = plugin.legacyWorldRegistry().read(entry.path());
        return create(plugin, player, entry, new Settings(
                entry.key().asString(),
                data.map(LegacyWorldRegistry.LegacyWorldData::dimension).map(dimension -> dimension.key().asString()).orElse(DETECT),
                data.map(LegacyWorldRegistry.LegacyWorldData::generator).orElse(NONE),
                false
        ));
    }

    private static Dialog create(final WorldsPlugin plugin, final Player player,
                                 final UnimportedWorlds.Entry entry, final Settings settings) {
        final var root = plugin.getServer().getWorldContainer().toPath().toAbsolutePath().normalize();
        final var path = root.relativize(entry.path().toAbsolutePath().normalize()).toString();
        final var placeholder = Placeholder.parsed("world", entry.key().asString());

        final var inputs = new ArrayList<DialogInput>();
        inputs.add(DialogInput.text("key", text(plugin, player, "dialog.create.key"))
                .width(WIDTH).initial(settings.key()).maxLength(128).build());
        inputs.add(DialogInput.singleOption("dimension", text(plugin, player, "dialog.create.dimension"), Stream.concat(
                Stream.of(option(DETECT, text(plugin, player, "dialog.import.dimension.detect"), settings.dimension())),
                Stream.concat(Dimension.dimensions(), plugin.customDimensions()).distinct().map(dimension ->
                        option(dimension.key().asString(), Component.text(dimension.key().asString()), settings.dimension()))
        ).toList()).width(WIDTH).build());
        final var generators = Stream.concat(Arrays.stream(plugin.getServer().getPluginManager().getPlugins())
                        .filter(Plugin::isEnabled)
                        .filter(plugin.generatorView()::hasGenerator)
                        .map(Plugin::getName), Stream.of(settings.generator()).filter(generator -> !generator.equals(NONE)))
                .distinct().sorted().toList();
        inputs.add(DialogInput.singleOption("generator", text(plugin, player, "dialog.create.generator"), Stream.concat(
                Stream.of(option(NONE, text(plugin, player, "dialog.create.generator.none"), settings.generator())),
                generators.stream().map(generator -> option(generator, Component.text(generator), settings.generator()))
        ).toList()).width(WIDTH).build());
        inputs.add(DialogInput.bool("void_world", text(plugin, player, "dialog.import.void-world"))
                .initial(settings.voidWorld()).build());

        final var confirm = button(plugin, player, "dialog.button.import", (viewer, response) -> {
            if (!viewer.hasPermission("worlds.command.import")) return;
            final var updated = settings.update(response);
            final var key = parseKey(updated.key()).orElse(null);
            if (key == null) {
                plugin.bundle().sendMessage(viewer, "dialog.create.key.invalid", Placeholder.unparsed("key", updated.key()));
                viewer.showDialog(create(plugin, viewer, entry, updated));
                return;
            }
            @SuppressWarnings("PatternValidation") final var dimension = updated.dimension().equals(DETECT)
                    ? null : new Dimension(Key.key(updated.dimension()));
            final var generator = updated.generator().equals(NONE) ? null : updated.generator();
            viewer.performCommand(UnimportedWorlds.importCommand(plugin, entry.path(), key,
                    dimension, generator, updated.voidWorld()));
        });
        final var cancel = button(plugin, player, "dialog.button.cancel",
                (viewer, response) -> viewer.showDialog(WorldListDialog.create(plugin, viewer)));

        final var body = DialogBody.plainMessage(text(plugin, player, "dialog.import.message",
                placeholder, Placeholder.unparsed("path", path)), WIDTH);
        return Dialog.create(builder -> builder.empty()
                .type(DialogType.confirmation(confirm, cancel))
                .base(DialogBase.builder(text(plugin, player, "dialog.import.title", placeholder))
                        .canCloseWithEscape(true).body(List.of(body)).inputs(inputs).build()));
    }

    private record Settings(String key, String dimension, String generator, boolean voidWorld) {
        Settings update(final DialogResponseView response) {
            final var key = getText(response, "key", this.key).trim();
            return new Settings(
                    key.isBlank() ? this.key : key,
                    getText(response, "dimension", dimension),
                    getText(response, "generator", generator),
                    getBoolean(response, "void_world", voidWorld)
            );
        }
    }
}
