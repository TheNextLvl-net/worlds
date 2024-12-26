package net.thenextlvl.worlds.api.preset.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.thenextlvl.worlds.api.preset.Biome;
import org.jspecify.annotations.NullMarked;

import java.lang.reflect.Type;

@NullMarked
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
