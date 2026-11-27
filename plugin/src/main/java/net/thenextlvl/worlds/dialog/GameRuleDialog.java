package net.thenextlvl.worlds.dialog;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.thenextlvl.worlds.dialog.DialogSupport.WIDTH;
import static net.thenextlvl.worlds.dialog.DialogSupport.button;
import static net.thenextlvl.worlds.dialog.DialogSupport.text;

@NullMarked
public final class GameRuleDialog {
    private GameRuleDialog() {
    }

    public static Dialog create(final WorldsPlugin plugin, final Player player, final World world) {
        final var placeholder = Placeholder.parsed("world", world.key().asString());
        return create(plugin, player, text(plugin, player, "dialog.gamerules.title.world", placeholder),
                world::getGameRuleValue, (viewer, changes) -> {
                    if (!viewer.hasPermission("worlds.command.gamerule")) return;
                    plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
                        final var changed = changes.entrySet().stream()
                                .filter(entry -> !entry.getValue().equals(world.getGameRuleValue(entry.getKey())))
                                .filter(entry -> apply(world, entry.getKey(), entry.getValue()))
                                .count();
                        plugin.bundle().sendMessage(viewer, "dialog.gamerules.saved", placeholder,
                                Formatter.number("amount", changed),
                                Formatter.booleanChoice("singular", changed == 1));
                        viewer.showDialog(WorldEditDialog.create(plugin, viewer, world));
                    });
                }, viewer -> viewer.showDialog(WorldEditDialog.create(plugin, viewer, world)));
    }

    static Dialog create(final WorldsPlugin plugin, final Player player, final Component title,
                         final Function<GameRule<?>, ?> values,
                         final BiConsumer<Player, Map<GameRule<?>, Object>> onSave,
                         final Consumer<Player> onBack) {
        final var rules = rules(plugin);
        final var inputs = new ArrayList<DialogInput>(rules.size());
        for (var index = 0; index < rules.size(); index++) {
            final var rule = rules.get(index);
            final var label = Component.translatable(rule);
            final var value = values.apply(rule);
            if (rule.getType() == Boolean.class) {
                inputs.add(DialogInput.bool(id(index), label).initial(Boolean.TRUE.equals(value)).build());
            } else inputs.add(DialogInput.text(id(index), label).width(WIDTH)
                    .initial(String.valueOf(value)).maxLength(11).build());
        }

        final var save = button(plugin, player, "dialog.button.save", (viewer, response) -> {
            final var result = new LinkedHashMap<GameRule<?>, Object>();
            for (var index = 0; index < rules.size(); index++) {
                final var rule = rules.get(index);
                if (rule.getType() == Boolean.class) {
                    final var value = response.getBoolean(id(index));
                    if (value != null) result.put(rule, value);
                } else try {
                    final var text = response.getText(id(index));
                    if (text != null) result.put(rule, Integer.parseInt(text.trim()));
                } catch (final NumberFormatException ignored) {
                }
            }
            onSave.accept(viewer, result);
        });
        final var back = button(plugin, player, "dialog.button.back", (viewer, response) -> onBack.accept(viewer));

        return Dialog.create(builder -> builder.empty()
                .type(DialogType.confirmation(save, back))
                .base(DialogBase.builder(title).canCloseWithEscape(true).inputs(inputs).build()));
    }

    static List<GameRule<?>> rules(final WorldsPlugin plugin) {
        final var features = plugin.handler().getEnabledFeatures();
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.GAME_RULE).stream()
                .filter(rule -> features.containsAll(rule.requiredFeatures()))
                .filter(rule -> rule.getType() == Boolean.class || rule.getType() == Integer.class)
                .sorted(Comparator.<GameRule<?>, Boolean>comparing(rule -> rule.getType() != Boolean.class)
                        .thenComparing(rule -> rule.key().asString()))
                .toList();
    }

    @SuppressWarnings("unchecked")
    static <T> boolean apply(final World world, final GameRule<T> rule, final Object value) {
        return world.setGameRule(rule, (T) value);
    }

    private static String id(final int index) {
        return "rule_" + index;
    }
}
