package net.thenextlvl.worlds.preset.adapter;

import com.google.gson.*;
import net.thenextlvl.worlds.preset.Biome;

import java.lang.reflect.Type;

public class BiomeTypeAdapter implements JsonSerializer<Biome>, JsonDeserializer<Biome> {
    @Override
    public Biome deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        return Biome.literal(element.getAsString());
    }

    @Override
    public JsonElement serialize(Biome biome, Type type, JsonSerializationContext context) {
        return new JsonPrimitive(biome.toString());
    }
}
