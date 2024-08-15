package net.thenextlvl.worlds.api.preset.adapter;

import com.google.gson.*;
import net.thenextlvl.worlds.api.preset.Structure;

import java.lang.reflect.Type;

public class StructureTypeAdapter implements JsonSerializer<Structure>, JsonDeserializer<Structure> {
    @Override
    public Structure deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        return new Structure(element.getAsString());
    }

    @Override
    public JsonElement serialize(Structure structure, Type type, JsonSerializationContext context) {
        return new JsonPrimitive(structure.toString());
    }
}
