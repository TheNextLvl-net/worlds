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
                arguments(GroupSettings::absorption, GroupSettings::absorption),
                arguments(GroupSettings::advancements, GroupSettings::advancements),
                arguments(GroupSettings::arrowsInBody, GroupSettings::arrowsInBody),
                arguments(GroupSettings::attributes, GroupSettings::attributes),
                arguments(GroupSettings::beeStingersInBody, GroupSettings::beeStingersInBody),
                arguments(GroupSettings::chat, GroupSettings::chat),
                arguments(GroupSettings::enabled, GroupSettings::enabled),
                arguments(GroupSettings::endCredits, GroupSettings::endCredits),
                arguments(GroupSettings::enderChest, GroupSettings::enderChest),
                arguments(GroupSettings::exhaustion, GroupSettings::exhaustion),
                arguments(GroupSettings::experience, GroupSettings::experience),
                arguments(GroupSettings::fallDistance, GroupSettings::fallDistance),
                arguments(GroupSettings::fireTicks, GroupSettings::fireTicks),
                arguments(GroupSettings::flyState, GroupSettings::flyState),
                arguments(GroupSettings::foodLevel, GroupSettings::foodLevel),
                arguments(GroupSettings::freezeTicks, GroupSettings::freezeTicks),
                arguments(GroupSettings::gameMode, GroupSettings::gameMode),
                arguments(GroupSettings::gameRules, GroupSettings::gameRules),
                arguments(GroupSettings::gliding, GroupSettings::gliding),
                arguments(GroupSettings::health, GroupSettings::health),
                arguments(GroupSettings::hotbarSlot, GroupSettings::hotbarSlot),
                arguments(GroupSettings::inventory, GroupSettings::inventory),
                arguments(GroupSettings::invulnerable, GroupSettings::invulnerable),
                arguments(GroupSettings::lastDeathLocation, GroupSettings::lastDeathLocation),
                arguments(GroupSettings::lockFreezeTicks, GroupSettings::lockFreezeTicks),
                arguments(GroupSettings::portalCooldown, GroupSettings::portalCooldown),
                arguments(GroupSettings::potionEffects, GroupSettings::potionEffects),
                arguments(GroupSettings::recipes, GroupSettings::recipes),
                arguments(GroupSettings::remainingAir, GroupSettings::remainingAir),
                arguments(GroupSettings::respawnLocation, GroupSettings::respawnLocation),
                arguments(GroupSettings::saturation, GroupSettings::saturation),
                arguments(GroupSettings::score, GroupSettings::score),
                arguments(GroupSettings::statistics, GroupSettings::statistics),
                arguments(GroupSettings::tabList, GroupSettings::tabList),
                arguments(GroupSettings::time, GroupSettings::time),
                arguments(GroupSettings::velocity, GroupSettings::velocity),
                arguments(GroupSettings::visualFire, GroupSettings::visualFire),
                arguments(GroupSettings::wardenSpawnTracker, GroupSettings::wardenSpawnTracker),
                arguments(GroupSettings::weather, GroupSettings::weather)
        );
    }

    private static Arguments arguments(BiConsumer<GroupSettings, Boolean> setter, Function<GroupSettings, Boolean> getter) {
        return Arguments.of(setter, getter);
    }
}