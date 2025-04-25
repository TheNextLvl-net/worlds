package net.thenextlvl.perworlds.adapter;

import core.nbt.serialization.ParserException;
import core.nbt.serialization.TagAdapter;
import core.nbt.serialization.TagDeserializationContext;
import core.nbt.serialization.TagSerializationContext;
import core.nbt.tag.CompoundTag;
import core.nbt.tag.Tag;
import net.thenextlvl.perworlds.GroupData;
import net.thenextlvl.perworlds.data.WorldBorderData;
import net.thenextlvl.perworlds.group.PaperGroupData;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Server;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class GroupDataAdapter implements TagAdapter<GroupData> {
    private final Server server;

    public GroupDataAdapter(Server server) {
        this.server = server;
    }

    @Override
    @SuppressWarnings("unchecked")
    public GroupData deserialize(Tag tag, TagDeserializationContext context) throws ParserException {
        var data = new PaperGroupData(server);
        var root = tag.getAsCompound();
        root.optional("defaultGameMode").map(tag1 -> context.deserialize(tag1, GameMode.class)).ifPresent(data::defaultGameMode);
        root.optional("difficulty").map(tag1 -> context.deserialize(tag1, Difficulty.class)).ifPresent(data::difficulty);
        root.optional("spawnLocation").map(tag1 -> context.deserialize(tag1, Location.class)).ifPresent(data::spawnLocation);
        root.optional("worldBorder").map(tag1 -> context.deserialize(tag1, WorldBorderData.class)).ifPresent(data::worldBorder);
        root.optional("hardcore").map(Tag::getAsBoolean).ifPresent(data::hardcore);
        root.optional("raining").map(Tag::getAsBoolean).ifPresent(data::raining);
        root.optional("thundering").map(Tag::getAsBoolean).ifPresent(data::thundering);
        root.optional("thunderDuration").map(Tag::getAsInt).ifPresent(data::thunderDuration);
        root.optional("clearWeatherDuration").map(Tag::getAsInt).ifPresent(data::clearWeatherDuration);
        root.optional("rainDuration").map(Tag::getAsInt).ifPresent(data::rainDuration);
        root.optional("time").map(Tag::getAsLong).ifPresent(data::time);
        root.optional("gameRules").map(Tag::getAsCompound).ifPresent(rules -> rules.entrySet().forEach(entry -> {
            var rule = (GameRule<Object>) GameRule.getByName(entry.getKey());
            if (rule != null) data.gameRule(rule, context.deserialize(entry.getValue(), rule.getType()));
        }));
        return data;
    }

    @Override
    public Tag serialize(GroupData data, TagSerializationContext context) throws ParserException {
        var tag = new CompoundTag();
        var rules = new CompoundTag();
        data.forEachGameRule((rule, value) -> rules.add(rule.getName(), context.serialize(value)));
        var spawnLocation = data.spawnLocation();
        if (spawnLocation != null) tag.add("spawnLocation", context.serialize(spawnLocation));
        tag.add("defaultGameMode", context.serialize(data.defaultGameMode()));
        tag.add("difficulty", context.serialize(data.difficulty()));
        tag.add("worldBorder", context.serialize(data.worldBorder()));
        tag.add("gameRules", rules);
        tag.add("hardcore", data.hardcore());
        tag.add("raining", data.raining());
        tag.add("thundering", data.thundering());
        tag.add("thunderDuration", data.thunderDuration());
        tag.add("clearWeatherDuration", data.clearWeatherDuration());
        tag.add("rainDuration", data.rainDuration());
        tag.add("time", data.time());
        return tag;
    }
}
