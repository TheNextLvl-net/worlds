package net.thenextlvl.perworlds.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.thenextlvl.perworlds.GroupData;
import net.thenextlvl.perworlds.group.PaperGroupData;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.jspecify.annotations.NullMarked;

import java.lang.reflect.Type;

@NullMarked
public class GroupDataAdapter implements JsonDeserializer<GroupData>, JsonSerializer<GroupData> {
    @Override
    public GroupData deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        var data = new PaperGroupData();
        var object = element.getAsJsonObject();
        if (object.has("defaultGameMode"))
            data.defaultGameMode(context.deserialize(object.get("defaultGameMode"), GameMode.class));
        if (object.has("difficulty"))
            data.difficulty(context.deserialize(object.get("difficulty"), Difficulty.class));
        if (object.has("spawnLocation"))
            data.spawnLocation(context.deserialize(object.get("spawnLocation"), Location.class));
        var rules = object.getAsJsonObject("gameRules");
        if (rules != null) rules.entrySet().forEach(entry -> {
            var rule = GameRule.getByName(entry.getKey());
            if (rule != null) data.gameRule(rule, context.deserialize(entry.getValue(), Object.class));
        });
        if (object.has("rain")) data.rain(object.get("rain").getAsBoolean());
        if (object.has("thunder")) data.thunder(object.get("thunder").getAsBoolean());
        if (object.has("time")) data.time(object.get("time").getAsInt());
        return data;
    }

    @Override
    public JsonObject serialize(GroupData data, Type type, JsonSerializationContext context) {
        var object = new JsonObject();
        var rules = new JsonObject();
        for (var rule : GameRule.values()) rules.add(rule.getName(), context.serialize(data.gameRule(rule)));
        object.add("defaultGameMode", context.serialize(data.defaultGameMode()));
        object.add("difficulty", context.serialize(data.difficulty()));
        object.add("spawnLocation", context.serialize(data.spawnLocation()));
        object.add("gameRules", rules);
        object.addProperty("rain", data.rain());
        object.addProperty("thunder", data.thunder());
        object.addProperty("time", data.time());
        return object;
    }
}
