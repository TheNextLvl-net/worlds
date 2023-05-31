package net.thenextlvl.worlds.preset.adapter;

import com.google.gson.*;
import net.thenextlvl.worlds.preset.Structure;

import java.lang.reflect.Type;

public class StructureTypeAdapter implements JsonSerializer<Structure>, JsonDeserializer<Structure> {
    @Override
    public Structure deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        return Structure.literal(element.getAsString());
    }

    @Override
    public JsonElement serialize(Structure structure, Type type, JsonSerializationContext context) {
        return new JsonPrimitive(structure.toString());
    }
}
