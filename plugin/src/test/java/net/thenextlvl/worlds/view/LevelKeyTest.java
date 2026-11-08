package net.thenextlvl.worlds.view;

import net.kyori.adventure.key.Key;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;

public class LevelKeyTest {
    @ParameterizedTest
    @ValueSource(strings = {"worlds:world", "worlds:a/b", "minecraft:overworld", "worlds:my.world", "worlds:a-b_c"})
    public void validKeys(final String key) {
        Assertions.assertTrue(PaperLevelView.isValidLevelKey(Key.key(key)), key);
    }

    @ParameterizedTest
    @ValueSource(strings = {"worlds:/smallserverhub", "worlds:hub/", "worlds:a//b", "worlds:..", "worlds:a/../b",
            "worlds:.", "worlds:con", "worlds:world.", "..:world", "worlds:"})
    public void invalidKeys(final String key) {
        Assertions.assertFalse(PaperLevelView.isValidLevelKey(Key.key(key)), key);
    }

    @Test
    public void createKeySanitizesSlashes() {
        Assertions.assertEquals("smallserverhub", PaperLevelView.createKey("/smallserverhub"));
        Assertions.assertEquals("a/b", PaperLevelView.createKey("/A//b/"));
        Assertions.assertEquals("b", PaperLevelView.createKey("../b"));
        Assertions.assertEquals("my_world", PaperLevelView.createKey("My World"));
    }

    @Test
    public void parseKeyRejectsInvalidSegments() {
        Assertions.assertTrue(PaperLevelView.parseKey(Path.of("worlds", "con"), false).isEmpty());
        Assertions.assertEquals(Key.key("worlds:a/b"), PaperLevelView.parseKey(Path.of("worlds", "a", "b"), false).orElseThrow());
    }
}
