package net.thenextlvl.worlds.preset.adapter;

import com.google.gson.*;
import org.bukkit.Material;

import java.lang.reflect.Type;

public class MaterialTypeAdapter implements JsonSerializer<Material>, JsonDeserializer<Material> {
    @Override
    public Material deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        var material = Material.matchMaterial(element.getAsString());
        if (material == null) throw new JsonParseException("Not a valid material: " + element.getAsString());
        return material;
    }

    @Override
    public JsonElement serialize(Material material, Type type, JsonSerializationContext context) {
        return new JsonPrimitive(material.getKey().toString());
    }
}
