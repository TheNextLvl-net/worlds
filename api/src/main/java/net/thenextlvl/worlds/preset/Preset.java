package net.thenextlvl.worlds.preset;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import core.file.format.GsonFile;
import core.io.IO;
import core.paper.adapters.inventory.MaterialAdapter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.thenextlvl.worlds.preset.adapter.BiomeTypeAdapter;
import net.thenextlvl.worlds.preset.adapter.StructureTypeAdapter;
import org.bukkit.Material;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Accessors(chain = true, fluent = true)
public class Preset {
    private Biome biome = Biome.minecraft("plains");
    private boolean lakes;
    private boolean features;
    private boolean decoration;

    private Set<Layer> layers = new HashSet<>();
    @SerializedName("structure_overrides")
    private Set<Structure> structures = new HashSet<>();

    /**
     * Add a layer to the preset
     *
     * @param layer the layer to add
     * @return the preset
     */
    public Preset addLayer(Layer layer) {
        layers().add(layer);
        return this;
    }

    /**
     * Add a structure to the preset
     *
     * @param structure the structure to add
     * @return the preset
     */
    public Preset addStructure(Structure structure) {
        structures().add(structure);
        return this;
    }

    /**
     * Save a preset to a file
     *
     * @param file  the file to save the preset to
     * @param force whether to override the file if it already exists
     * @return whether the file could be saved
     */
    public boolean saveToFile(File file, boolean force) {
        if (!force && file.exists()) return false;
        new GsonFile<>(IO.of(file), this, gson).save();
        return true;
    }

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Structure.class, new StructureTypeAdapter())
            .registerTypeAdapter(Material.class, MaterialAdapter.NotNull.INSTANCE)
            .registerTypeAdapter(Biome.class, new BiomeTypeAdapter())
            .setPrettyPrinting()
            .create();

    /**
     * Serialize a preset into a json object
     *
     * @param preset the preset to serialize
     * @return the serialized preset
     */
    public static JsonObject serialize(Preset preset) {
        return gson.toJsonTree(preset).getAsJsonObject();
    }

    /**
     * Deserialize a json object into a preset
     *
     * @param object the object to deserialize
     * @return the deserialized preset
     */
    public static Preset deserialize(JsonObject object) {
        return gson.fromJson(object, Preset.class);
    }
}
