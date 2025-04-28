package net.thenextlvl.perworlds;

import net.thenextlvl.perworlds.group.PaperGroupSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GroupSettingsTest {
    @Test
    @DisplayName("settings providers count")
    public void testSettingsProvidersCount() {
        var count = Arrays.stream(GroupSettings.class.getMethods())
                .filter(method -> method.getReturnType() != Void.class)
                .filter(method -> method.getParameterCount() == 1)
                .filter(method -> method.getParameterTypes()[0] == boolean.class)
                .count();
        assertEquals(settings.size(), count, "Found more settings than expected");
    }

    @ParameterizedTest
    @MethodSource("settingsProvider")
    @DisplayName("Getters and Setters")
    public void testSettings(BiConsumer<GroupSettings, Boolean> setter, Function<GroupSettings, Boolean> getter) {
        var settings = new PaperGroupSettings();
        setter.accept(settings, true);
        assertTrue(getter.apply(settings), "Setter did not properly update its value");
        setter.accept(settings, false);
        assertFalse(getter.apply(settings), "Setter did not properly update its value");
    }

    @Test
    @DisplayName("group option command")
    public void testAllOptionsRegistered() throws Exception {
        var path = Path.of("src/main/java/net/thenextlvl/perworlds/command/GroupOptionCommand.java");
        var content = Files.readAllLines(path, StandardCharsets.UTF_8).stream()
                .map(String::strip)
                .filter(s -> !s.isEmpty() && !s.startsWith("//"))
                .collect(Collectors.joining("\n"));

        var pattern = Pattern.compile("option\\(\"([^\"]+)\"");
        var matcher = pattern.matcher(content);
        var options = new HashSet<>(settings.keySet());

        while (matcher.find()) {
            var option = matcher.group(1);
            assertTrue(options.remove(option), "Unknown option '" + option + "'");
        }

        if (options.isEmpty()) return;
        throw new IllegalStateException("Missing options: " + String.join(", ", options));
    }

    @Test
    @DisplayName("option name matches method references")
    public void testOptionNameMatchesMethodReferences() throws Exception {
        var path = Path.of("src/main/java/net/thenextlvl/perworlds/command/GroupOptionCommand.java");
        var content = Files.readAllLines(path, StandardCharsets.UTF_8).stream()
                .map(String::strip)
                .filter(s -> !s.isEmpty() && !s.startsWith("//"))
                .collect(Collectors.joining("\n"));

        var pattern = Pattern.compile("option\\(\"([^\"]+)\"\\s*,\\s*GroupSettings::([a-zA-Z0-9_]+)\\s*,\\s*GroupSettings::([a-zA-Z0-9_]+)");
        var matcher = pattern.matcher(content);

        while (matcher.find()) {
            var optionName = matcher.group(1);
            var getter = matcher.group(2);
            var setter = matcher.group(3);
            assertEquals(optionName, getter, "Option name and getter should match: \"" + optionName + "\" != \"" + getter + "\"");
            assertEquals(optionName, setter, "Option name and setter should match: \"" + optionName + "\" != \"" + setter + "\"");
        }
    }

    private static final Map<String, Setting> settings = Map.ofEntries(
            Map.entry("absorption", new Setting(GroupSettings::absorption, GroupSettings::absorption)),
            Map.entry("advancementMessages", new Setting(GroupSettings::advancementMessages, GroupSettings::advancementMessages)),
            Map.entry("advancements", new Setting(GroupSettings::advancements, GroupSettings::advancements)),
            Map.entry("arrowsInBody", new Setting(GroupSettings::arrowsInBody, GroupSettings::arrowsInBody)),
            Map.entry("attributes", new Setting(GroupSettings::attributes, GroupSettings::attributes)),
            Map.entry("beeStingersInBody", new Setting(GroupSettings::beeStingersInBody, GroupSettings::beeStingersInBody)),
            Map.entry("chat", new Setting(GroupSettings::chat, GroupSettings::chat)),
            Map.entry("deathMessages", new Setting(GroupSettings::deathMessages, GroupSettings::deathMessages)),
            Map.entry("difficulty", new Setting(GroupSettings::difficulty, GroupSettings::difficulty)),
            Map.entry("enabled", new Setting(GroupSettings::enabled, GroupSettings::enabled)),
            Map.entry("endCredits", new Setting(GroupSettings::endCredits, GroupSettings::endCredits)),
            Map.entry("enderChest", new Setting(GroupSettings::enderChest, GroupSettings::enderChest)),
            Map.entry("exhaustion", new Setting(GroupSettings::exhaustion, GroupSettings::exhaustion)),
            Map.entry("experience", new Setting(GroupSettings::experience, GroupSettings::experience)),
            Map.entry("fallDistance", new Setting(GroupSettings::fallDistance, GroupSettings::fallDistance)),
            Map.entry("fireTicks", new Setting(GroupSettings::fireTicks, GroupSettings::fireTicks)),
            Map.entry("flySpeed", new Setting(GroupSettings::flySpeed, GroupSettings::flySpeed)),
            Map.entry("flyState", new Setting(GroupSettings::flyState, GroupSettings::flyState)),
            Map.entry("foodLevel", new Setting(GroupSettings::foodLevel, GroupSettings::foodLevel)),
            Map.entry("freezeTicks", new Setting(GroupSettings::freezeTicks, GroupSettings::freezeTicks)),
            Map.entry("gameMode", new Setting(GroupSettings::gameMode, GroupSettings::gameMode)),
            Map.entry("gameRules", new Setting(GroupSettings::gameRules, GroupSettings::gameRules)),
            Map.entry("gliding", new Setting(GroupSettings::gliding, GroupSettings::gliding)),
            Map.entry("health", new Setting(GroupSettings::health, GroupSettings::health)),
            Map.entry("hotbarSlot", new Setting(GroupSettings::hotbarSlot, GroupSettings::hotbarSlot)),
            Map.entry("inventory", new Setting(GroupSettings::inventory, GroupSettings::inventory)),
            Map.entry("invulnerable", new Setting(GroupSettings::invulnerable, GroupSettings::invulnerable)),
            Map.entry("joinMessages", new Setting(GroupSettings::joinMessages, GroupSettings::joinMessages)),
            Map.entry("lastDeathLocation", new Setting(GroupSettings::lastDeathLocation, GroupSettings::lastDeathLocation)),
            Map.entry("lastLocation", new Setting(GroupSettings::lastLocation, GroupSettings::lastLocation)),
            Map.entry("lockFreezeTicks", new Setting(GroupSettings::lockFreezeTicks, GroupSettings::lockFreezeTicks)),
            Map.entry("portalCooldown", new Setting(GroupSettings::portalCooldown, GroupSettings::portalCooldown)),
            Map.entry("potionEffects", new Setting(GroupSettings::potionEffects, GroupSettings::potionEffects)),
            Map.entry("quitMessages", new Setting(GroupSettings::quitMessages, GroupSettings::quitMessages)),
            Map.entry("recipes", new Setting(GroupSettings::recipes, GroupSettings::recipes)),
            Map.entry("remainingAir", new Setting(GroupSettings::remainingAir, GroupSettings::remainingAir)),
            Map.entry("respawnLocation", new Setting(GroupSettings::respawnLocation, GroupSettings::respawnLocation)),
            Map.entry("saturation", new Setting(GroupSettings::saturation, GroupSettings::saturation)),
            Map.entry("score", new Setting(GroupSettings::score, GroupSettings::score)),
            Map.entry("statistics", new Setting(GroupSettings::statistics, GroupSettings::statistics)),
            Map.entry("tabList", new Setting(GroupSettings::tabList, GroupSettings::tabList)),
            Map.entry("time", new Setting(GroupSettings::time, GroupSettings::time)),
            Map.entry("velocity", new Setting(GroupSettings::velocity, GroupSettings::velocity)),
            Map.entry("visualFire", new Setting(GroupSettings::visualFire, GroupSettings::visualFire)),
            Map.entry("walkSpeed", new Setting(GroupSettings::walkSpeed, GroupSettings::walkSpeed)),
            Map.entry("wardenSpawnTracker", new Setting(GroupSettings::wardenSpawnTracker, GroupSettings::wardenSpawnTracker)),
            Map.entry("weather", new Setting(GroupSettings::weather, GroupSettings::weather)),
            Map.entry("worldBorder", new Setting(GroupSettings::worldBorder, GroupSettings::worldBorder))
    );

    private record Setting(BiConsumer<GroupSettings, Boolean> setter, Function<GroupSettings, Boolean> getter) {
    }

    private static Stream<Arguments> settingsProvider() {
        return settings.entrySet().stream().map(entry -> arguments(
                entry.getKey(),
                entry.getValue().setter(),
                entry.getValue().getter()
        ));
    }

    private static Arguments arguments(String name, BiConsumer<GroupSettings, Boolean> setter, Function<GroupSettings, Boolean> getter) {
        return Arguments.argumentSet(name, setter, getter);
    }
}