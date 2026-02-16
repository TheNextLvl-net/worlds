package net.thenextlvl.worlds;

import net.thenextlvl.worlds.view.PaperLevelView;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

public class FreeNameTest {
    private static final Map<String, String> usedNames = new LinkedHashMap<>(Map.ofEntries(
            Map.entry("caves", "caves (3)"),
            Map.entry("caves (1)", "caves (3)"),
            Map.entry("caves (2)", "caves (3)"),
            Map.entry("checkerboard", "checkerboard (1)"),
            Map.entry("end", "end (1)"),
            Map.entry("nether", "nether (2)"),
            Map.entry("nether (1)", "nether (2)"),
            Map.entry("singlebiome", "singlebiome (1)"),
            Map.entry("test", "test (1)"),
            Map.entry("void", "void (2)"),
            Map.entry("void (1)", "void (2)"),
            Map.entry("world", "world (1)"),
            Map.entry("world_nether", "world_nether (1)")
    ));

    @ParameterizedTest
    @MethodSource("usedNames")
    public void testFreeName(final String name, final String expectedName) {
        final var freeName = PaperLevelView.findFreeName(usedNames.keySet(), name);
        Assertions.assertEquals(expectedName, freeName, "Unexpected name for '" + name + "'");
    }

    public static Stream<Arguments> usedNames() {
        return usedNames.entrySet().stream().map(entry -> Arguments.of(entry.getKey(), entry.getValue()));
    }
}
