package net.thenextlvl.worlds.preset;

import net.thenextlvl.worlds.api.preset.Preset;
import net.thenextlvl.worlds.api.preset.Presets;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PresetsTest {
    @ParameterizedTest
    @MethodSource("unparsedPresets")
    public void testPreset(Preset preset, String unparsed) {
        var parsed = Preset.parse(unparsed);
        assertEquals(parsed.toPresetCode(), unparsed, "Parsed preset does not match unparsed preset");
    }
    
    @ParameterizedTest
    @MethodSource("presets")
    public void testPresetSerialization(Preset preset) {
        var serialize = preset.serialize();
        var deserialize = Preset.deserialize(serialize);
        assertEquals(preset, deserialize, "Deserialized preset does not match original");
    }

    public static Stream<Arguments> unparsedPresets() {
        return Stream.of(
                Map.entry(Presets.CLASSIC_FLAT, "minecraft:bedrock,2*minecraft:dirt,minecraft:grass_block;minecraft:plains"),
                Map.entry(Presets.TUNNELERS_DREAM, "minecraft:bedrock,230*minecraft:stone,5*minecraft:dirt,minecraft:grass_block;minecraft:windswept_hills"),
                Map.entry(Presets.WATER_WORLD, "minecraft:bedrock,64*minecraft:deepslate,5*minecraft:stone,5*minecraft:dirt,5*minecraft:gravel,90*minecraft:water;minecraft:deep_ocean"),
                Map.entry(Presets.OVERWORLD, "minecraft:bedrock,59*minecraft:stone,3*minecraft:dirt,minecraft:grass_block;minecraft:plains"),
                Map.entry(Presets.SNOWY_KINGDOM, "minecraft:bedrock,59*minecraft:stone,3*minecraft:dirt,minecraft:grass_block,minecraft:snow;minecraft:snowy_plains"),
                Map.entry(Presets.BOTTOMLESS_PIT, "2*minecraft:cobblestone,3*minecraft:dirt,minecraft:grass_block;minecraft:plains"),
                Map.entry(Presets.DESERT, "minecraft:bedrock,3*minecraft:stone,52*minecraft:sandstone,8*minecraft:sand;minecraft:desert"),
                Map.entry(Presets.REDSTONE_READY, "minecraft:bedrock,3*minecraft:stone,116*minecraft:sandstone;minecraft:desert"),
                Map.entry(Presets.THE_VOID, "minecraft:air;minecraft:the_void")
        ).map(entry -> Arguments.argumentSet(entry.getKey().name(), entry.getKey(), entry.getValue()));
    }

    public static Stream<Arguments> presets() {
        return Presets.presets().stream().map(preset -> Arguments.argumentSet(preset.name(), preset));
    }
}
