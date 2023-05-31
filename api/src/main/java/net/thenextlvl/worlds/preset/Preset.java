package net.thenextlvl.worlds.preset;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.thenextlvl.worlds.preset.adapter.BiomeTypeAdapter;
import net.thenextlvl.worlds.preset.adapter.MaterialTypeAdapter;
import net.thenextlvl.worlds.preset.adapter.StructureTypeAdapter;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true, fluent = true)
public class Preset implements Cloneable {
    private Biome biome = Biome.minecraft("plains");
    private boolean lakes;
    private boolean features;
    private boolean decoration;

    private final List<Layer> layers = new ArrayList<>();
    @SerializedName("structure_overrides")
    private final List<Structure> structures = new ArrayList<>();

    public Preset addLayer(Layer layer) {
        layers().add(layer);
        return this;
    }

    public Preset addStructure(Structure structure) {
        structures().add(structure);
        return this;
    }

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Structure.class, new StructureTypeAdapter())
            .registerTypeAdapter(Material.class, new MaterialTypeAdapter())
            .registerTypeAdapter(Biome.class, new BiomeTypeAdapter())
            .setPrettyPrinting()
            .create();

    public static JsonObject serialize(Preset preset) {
        return gson.toJsonTree(preset).getAsJsonObject();
    }

    public static @Nullable Preset deserialize(JsonObject object) {
        return gson.fromJson(object, Preset.class);
    }

    @Override
    public Preset clone() {
        try {
            return (Preset) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
