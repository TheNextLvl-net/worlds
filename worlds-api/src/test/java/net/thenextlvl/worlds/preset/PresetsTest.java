package net.thenextlvl.worlds.preset;

import net.thenextlvl.worlds.api.preset.Preset;
import net.thenextlvl.worlds.api.preset.Presets;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PresetsTest {
    @ParameterizedTest
    @MethodSource("unparsedPresets")
    public void testPreset(Preset preset, String unparsed) {
        var parsed = Preset.parse(unparsed);
        assertEquals(preset.toPresetCode(), unparsed, "Constant does not match unparsed preset");
        assertEquals(parsed.toPresetCode(), unparsed, "Parsed preset does not match unparsed preset");
    }

    public static Stream<Arguments> unparsedPresets() {
        return Stream.of(
                Arguments.argumentSet("classic flat", Presets.CLASSIC_FLAT, "minecraft:bedrock,2*minecraft:dirt,minecraft:grass_block;minecraft:plains"),
                Arguments.argumentSet("tunnelers dream", Presets.TUNNELERS_DREAM, "minecraft:bedrock,230*minecraft:stone,5*minecraft:dirt,minecraft:grass_block;minecraft:windswept_hills"),
                Arguments.argumentSet("water world", Presets.WATER_WORLD, "minecraft:bedrock,64*minecraft:deepslate,5*minecraft:stone,5*minecraft:dirt,5*minecraft:gravel,90*minecraft:water;minecraft:deep_ocean"),
                Arguments.argumentSet("overworld", Presets.OVERWORLD, "minecraft:bedrock,59*minecraft:stone,3*minecraft:dirt,minecraft:grass_block;minecraft:plains"),
                Arguments.argumentSet("snowy kingdom", Presets.SNOWY_KINGDOM, "minecraft:bedrock,59*minecraft:stone,3*minecraft:dirt,minecraft:grass_block,minecraft:snow;minecraft:snowy_plains"),
                Arguments.argumentSet("bottomless pit", Presets.BOTTOMLESS_PIT, "2*minecraft:cobblestone,3*minecraft:dirt,minecraft:grass_block;minecraft:plains"),
                Arguments.argumentSet("desert", Presets.DESERT, "minecraft:bedrock,3*minecraft:stone,52*minecraft:sandstone,8*minecraft:sand;minecraft:desert"),
                Arguments.argumentSet("redstone ready", Presets.REDSTONE_READY, "minecraft:bedrock,3*minecraft:stone,116*minecraft:sandstone;minecraft:desert"),
                Arguments.argumentSet("the void", Presets.THE_VOID, "minecraft:air;minecraft:the_void")
        );
    }
}
