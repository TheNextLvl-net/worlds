package net.thenextlvl.worlds.preset;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.Set;

/**
 * Represents a Superflat world preset, defining the layers, biome, structures,
 * and other generation settings.
 *
 * @see <a href="https://minecraft.wiki/w/Superflat">Superflat Wiki</a>
 * @since 4.0.0
 */
public sealed interface Preset permits SimplePreset {
    /**
     * Retrieves the name of the preset.
     *
     * @return an {@code Optional} containing the name of the preset, or empty if no name is set
     * @since 4.0.0
     */
    @Contract(pure = true)
    Optional<String> name();

    /**
     * Retrieves the biome associated with the preset.
     *
     * @return the biome as a {@code Biome} record
     * @since 4.0.0
     */
    @Contract(pure = true)
    Biome biome();

    /**
     * Determines whether lakes are enabled for the preset.
     *
     * @return true if lakes are enabled, false otherwise
     * @since 4.0.0
     */
    @Contract(pure = true)
    boolean lakes();

    /**
     * Determines whether features are enabled for the preset.
     *
     * @return true if features are enabled, false otherwise
     * @since 4.0.0
     */
    @Contract(pure = true)
    boolean features();

    /**
     * Determines whether decoration is enabled for the preset.
     *
     * @return true if decoration is enabled, false otherwise
     * @since 4.0.0
     */
    @Contract(pure = true)
    boolean decoration();

    /**
     * Retrieves the set of layers associated with the preset.
     *
     * @return a {@code Set} containing the layers of the preset
     * @since 4.0.0
     */
    @Unmodifiable
    @Contract(pure = true)
    Set<Layer> layers();

    /**
     * Retrieves the set of structures associated with the preset.
     *
     * @return a {@code Set} containing the structures of the preset
     * @since 4.0.0
     */
    @Unmodifiable
    @Contract(pure = true)
    Set<Structure> structures();

    /**
     * Converts the current {@code Preset} object into its corresponding preset code.
     * The generated code represents the layers of the preset and its biome.
     * Layers are serialized into a comma-separated string, followed by a semicolon
     * and the biome string representation.
     * <p>
     * This is a lossy conversion. If you want to save this preset, use {@link #serialize()}.
     *
     * @return a {@code String} containing the serialized layers and biome information of the preset
     * @since 4.0.0
     */
    @Contract(pure = true)
    String toPresetCode();

    /**
     * Serialize this preset into a JSON object.
     * <a href="https://minecraft.wiki/w/Superflat#Multiplayer">Wiki</a>
     *
     * @return the serialized preset as a JsonObject
     * @see #deserialize(JsonObject)
     * @since 4.0.0
     */
    @Contract(value = " -> new", pure = true)
    JsonObject serialize();

    /**
     * Creates a new {@link Builder} pre-populated with the values of this preset.
     *
     * @return a new builder initialized with this preset's values
     * @since 4.0.0
     */
    @Contract(value = " -> new", pure = true)
    Builder toBuilder();

    /**
     * Parses a Superflat preset code and generates a corresponding {@code Preset} object.
     * <p>
     * The preset code should follow the format specified in the Superflat preset code
     * <a href="https://minecraft.wiki/w/Superflat#Preset_code_format">documentation</a>.
     * It consists of layer definitions separated by commas and the biome definition separated by a semicolon.
     * Each layer may specify its material and optional height.
     * If the material is invalid or the format is incorrect,
     * an {@code IllegalArgumentException} is thrown.
     *
     * @param presetCode the preset code string to parse in the expected format
     * @return a {@code Preset} object configured with the layers and biome described in the preset code
     * @throws IllegalArgumentException if the preset code contains invalid materials or does not adhere to the required format
     * @since 4.0.0
     */
    @Contract(value = "_ -> new", pure = true)
    static Preset parse(final String presetCode) {
        return SimplePreset.parse(presetCode);
    }

    /**
     * Deserializes a JSON object into a preset.
     * <a href="https://minecraft.wiki/w/Superflat#Multiplayer">Wiki</a>
     *
     * @param json the JSON object to deserialize
     * @return the deserialized preset
     * @throws IllegalArgumentException if no layers are provided
     * @see #serialize()
     * @since 4.0.0
     */
    @Contract(value = "_ -> new", pure = true)
    static Preset deserialize(final JsonObject json) {
        return SimplePreset.deserialize(json);
    }

    /**
     * Creates a new {@link Builder} instance for constructing a {@code Preset}.
     *
     * @return a new preset builder
     * @since 4.0.0
     */
    @Contract(value = " -> new", pure = true)
    static Builder builder() {
        return new SimplePreset.Builder();
    }

    /**
     * Returns all built-in presets.
     *
     * @return an unmodifiable set of all built-in presets
     * @since 4.0.0
     */
    @Contract(pure = true)
    static @Unmodifiable Set<Preset> presets() {
        return SimplePreset.PRESETS;
    }

    Preset BOTTOMLESS_PIT = SimplePreset.BOTTOMLESS_PIT;
    Preset CLASSIC_FLAT = SimplePreset.CLASSIC_FLAT;
    Preset DESERT = SimplePreset.DESERT;
    Preset OVERWORLD = SimplePreset.OVERWORLD;
    Preset REDSTONE_READY = SimplePreset.REDSTONE_READY;
    Preset SNOWY_KINGDOM = SimplePreset.SNOWY_KINGDOM;
    Preset THE_VOID = SimplePreset.THE_VOID;
    Preset TUNNELERS_DREAM = SimplePreset.TUNNELERS_DREAM;
    Preset WATER_WORLD = SimplePreset.WATER_WORLD;

    /**
     * A builder for constructing {@link Preset} instances.
     *
     * @since 4.0.0
     */
    sealed interface Builder permits SimplePreset.Builder {
        /**
         * Sets the name of the preset.
         *
         * @param name the name of the preset
         * @return this builder
         * @since 4.0.0
         */
        @Contract(value = "_ -> this", mutates = "this")
        Builder name(@Nullable String name);

        /**
         * Sets the biome of the preset.
         *
         * @param biome the biome to use
         * @return this builder
         * @since 4.0.0
         */
        @Contract(value = "_ -> this", mutates = "this")
        Builder biome(Biome biome);

        /**
         * Sets whether decoration is enabled for the preset.
         *
         * @param decoration true to enable decoration, false otherwise
         * @return this builder
         * @since 4.0.0
         */
        @Contract(value = "_ -> this", mutates = "this")
        Builder decoration(boolean decoration);

        /**
         * Sets whether features are enabled for the preset.
         *
         * @param features true to enable features, false otherwise
         * @return this builder
         * @since 4.0.0
         */
        @Contract(value = "_ -> this", mutates = "this")
        Builder features(boolean features);

        /**
         * Sets whether lakes are enabled for the preset.
         *
         * @param lakes true to enable lakes, false otherwise
         * @return this builder
         * @since 4.0.0
         */
        @Contract(value = "_ -> this", mutates = "this")
        Builder lakes(boolean lakes);

        /**
         * Sets the layers of the preset, replacing any previously added layers.
         *
         * @param layers the set of layers
         * @return this builder
         * @since 4.0.0
         */
        @Contract(value = "_ -> this", mutates = "this")
        Builder layers(Set<Layer> layers);

        /**
         * Adds a layer to the preset.
         *
         * @param layer the layer to add
         * @return this builder
         * @since 4.0.0
         */
        @Contract(value = "_ -> this", mutates = "this")
        Builder addLayer(final Layer layer);

        /**
         * Sets the structures of the preset, replacing any previously added structures.
         *
         * @param structures the set of structures
         * @return this builder
         * @since 4.0.0
         */
        @Contract(value = "_ -> this", mutates = "this")
        Builder structures(Set<Structure> structures);

        /**
         * Adds a structure to the preset.
         *
         * @param structure the structure to add
         * @return this builder
         * @since 4.0.0
         */
        @Contract(value = "_ -> this", mutates = "this")
        Builder addStructure(final Structure structure);

        /**
         * Builds and returns the {@link Preset} instance.
         *
         * @return the constructed preset
         * @since 4.0.0
         */
        @Contract(value = " -> new", pure = true)
        Preset build();
    }
}
