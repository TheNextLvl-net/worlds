package net.thenextlvl.perworlds;

import net.thenextlvl.perworlds.group.PaperGroupSettings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class GroupSettingsTest {
    @Test
    @DisplayName("settings providers count")
    public void testSettingsProvidersCount() {
        var count = Arrays.stream(GroupSettings.class.getMethods())
                .filter(method -> method.getReturnType() != Void.class)
                .filter(method -> method.getParameterCount() == 1)
                .filter(method -> method.getParameterTypes()[0] == boolean.class)
                .count();
        var expected = settingsProvider().count();
        Assertions.assertEquals(expected, count, "Found more settings than expected");
    }

    @DisplayName("setters")
    @ParameterizedTest
    @MethodSource("settingsProvider")
    public void testSettings(BiConsumer<GroupSettings, Boolean> setter, Function<GroupSettings, Boolean> getter) {
        var settings = new PaperGroupSettings();
        setter.accept(settings, true);
        Assertions.assertTrue(getter.apply(settings), "Setter did not properly update its value");
        setter.accept(settings, false);
        Assertions.assertFalse(getter.apply(settings), "Setter did not properly update its value");
    }

    private static Stream<Arguments> settingsProvider() {
        return Stream.of(
                arguments("absorption", GroupSettings::absorption, GroupSettings::absorption),
                arguments("advancements", GroupSettings::advancements, GroupSettings::advancements),
                arguments("arrowsInBody", GroupSettings::arrowsInBody, GroupSettings::arrowsInBody),
                arguments("attributes", GroupSettings::attributes, GroupSettings::attributes),
                arguments("beeStingersInBody", GroupSettings::beeStingersInBody, GroupSettings::beeStingersInBody),
                arguments("chat", GroupSettings::chat, GroupSettings::chat),
                arguments("enabled", GroupSettings::enabled, GroupSettings::enabled),
                arguments("endCredits", GroupSettings::endCredits, GroupSettings::endCredits),
                arguments("enderChest", GroupSettings::enderChest, GroupSettings::enderChest),
                arguments("exhaustion", GroupSettings::exhaustion, GroupSettings::exhaustion),
                arguments("experience", GroupSettings::experience, GroupSettings::experience),
                arguments("fallDistance", GroupSettings::fallDistance, GroupSettings::fallDistance),
                arguments("fireTicks", GroupSettings::fireTicks, GroupSettings::fireTicks),
                arguments("flyState", GroupSettings::flyState, GroupSettings::flyState),
                arguments("foodLevel", GroupSettings::foodLevel, GroupSettings::foodLevel),
                arguments("freezeTicks", GroupSettings::freezeTicks, GroupSettings::freezeTicks),
                arguments("gameMode", GroupSettings::gameMode, GroupSettings::gameMode),
                arguments("gameRules", GroupSettings::gameRules, GroupSettings::gameRules),
                arguments("gliding", GroupSettings::gliding, GroupSettings::gliding),
                arguments("health", GroupSettings::health, GroupSettings::health),
                arguments("hotbarSlot", GroupSettings::hotbarSlot, GroupSettings::hotbarSlot),
                arguments("inventory", GroupSettings::inventory, GroupSettings::inventory),
                arguments("invulnerable", GroupSettings::invulnerable, GroupSettings::invulnerable),
                arguments("lastDeathLocation", GroupSettings::lastDeathLocation, GroupSettings::lastDeathLocation),
                arguments("lastLocation", GroupSettings::lastLocation, GroupSettings::lastLocation),
                arguments("lockFreezeTicks", GroupSettings::lockFreezeTicks, GroupSettings::lockFreezeTicks),
                arguments("portalCooldown", GroupSettings::portalCooldown, GroupSettings::portalCooldown),
                arguments("potionEffects", GroupSettings::potionEffects, GroupSettings::potionEffects),
                arguments("recipes", GroupSettings::recipes, GroupSettings::recipes),
                arguments("remainingAir", GroupSettings::remainingAir, GroupSettings::remainingAir),
                arguments("respawnLocation", GroupSettings::respawnLocation, GroupSettings::respawnLocation),
                arguments("saturation", GroupSettings::saturation, GroupSettings::saturation),
                arguments("score", GroupSettings::score, GroupSettings::score),
                arguments("statistics", GroupSettings::statistics, GroupSettings::statistics),
                arguments("tabList", GroupSettings::tabList, GroupSettings::tabList),
                arguments("time", GroupSettings::time, GroupSettings::time),
                arguments("velocity", GroupSettings::velocity, GroupSettings::velocity),
                arguments("visualFire", GroupSettings::visualFire, GroupSettings::visualFire),
                arguments("wardenSpawnTracker", GroupSettings::wardenSpawnTracker, GroupSettings::wardenSpawnTracker),
                arguments("weather", GroupSettings::weather, GroupSettings::weather)
        );
    }

    private static Arguments arguments(String name, BiConsumer<GroupSettings, Boolean> setter, Function<GroupSettings, Boolean> getter) {
        return Arguments.argumentSet(name, setter, getter);
    }
}