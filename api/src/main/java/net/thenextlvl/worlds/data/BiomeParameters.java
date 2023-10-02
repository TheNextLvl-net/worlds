package net.thenextlvl.worlds.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.HashMap;

@Getter
@Setter
@NoArgsConstructor
@Accessors(fluent = true)
public class BiomeParameters extends HashMap<String, float[]> {
    private float offset;

    public BiomeParameters(int initialCapacity) {
        super(initialCapacity);
    }

    public BiomeParameters continentalness(float[] continentalness) {
        put("continentalness", continentalness);
        return this;
    }

    public BiomeParameters temperature(float[] temperature) {
        put("temperature", temperature);
        return this;
    }

    public BiomeParameters weirdness(float[] weirdness) {
        put("weirdness", weirdness);
        return this;
    }

    public BiomeParameters humidity(float[] humidity) {
        put("humidity", humidity);
        return this;
    }

    public BiomeParameters erosion(float[] erosion) {
        put("erosion", erosion);
        return this;
    }

    public BiomeParameters depth(float[] depth) {
        put("depth", depth);
        return this;
    }
}
