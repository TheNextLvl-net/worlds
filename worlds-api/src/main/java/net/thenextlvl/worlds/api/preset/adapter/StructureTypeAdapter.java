package net.thenextlvl.worlds.api.preset.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.thenextlvl.worlds.api.preset.Structure;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

import java.lang.reflect.Type;

@NullMarked
@ApiStatus.Internal
public class StructureTypeAdapter implements JsonSerializer<Structure>, JsonDeserializer<Structure> {
    @Override
    @SuppressWarnings("PatternValidation")
    public Structure deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        return new Structure(element.getAsString());
    }

    @Override
    public JsonElement serialize(Structure structure, Type type, JsonSerializationContext context) {
        return new JsonPrimitive(structure.toString());
    }
}
