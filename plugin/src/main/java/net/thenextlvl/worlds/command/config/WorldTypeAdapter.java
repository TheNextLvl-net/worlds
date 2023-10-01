package net.thenextlvl.worlds.command.config;

import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.lang.reflect.Type;

public class WorldTypeAdapter implements JsonSerializer<World>, JsonDeserializer<World> {
    @Override
    public World deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        return Bukkit.getWorld(element.getAsString());
    }

    @Override
    public JsonElement serialize(World world, Type type, JsonSerializationContext context) {
        return new JsonPrimitive(world.getName());
    }
}
