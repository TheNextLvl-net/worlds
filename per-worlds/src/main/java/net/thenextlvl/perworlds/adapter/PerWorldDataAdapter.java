package net.thenextlvl.perworlds.adapter;

import core.nbt.serialization.ParserException;
import core.nbt.serialization.TagAdapter;
import core.nbt.serialization.TagDeserializationContext;
import core.nbt.serialization.TagSerializationContext;
import core.nbt.tag.CompoundTag;
import core.nbt.tag.Tag;
import net.thenextlvl.perworlds.model.PerWorldData;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PerWorldDataAdapter implements TagAdapter<PerWorldData> {
    @Override
    public PerWorldData deserialize(Tag tag, TagDeserializationContext context) throws ParserException {
        var root = tag.getAsCompound();
        var enderChestContents = root.optional("enderChest").map(items -> context.deserialize(items, ItemStack[].class));
        var inventoryContents = root.optional("inventory").map(items -> context.deserialize(items, ItemStack[].class));
        var respawnLocation = root.optional("respawnLocation").map(location -> context.deserialize(location, Location.class));
        var gameMode = root.optional("gameMode").map(mode -> context.deserialize(mode, GameMode.class));
        var health = root.optional("health").map(Tag::getAsDouble);
        var exhaustion = root.optional("exhaustion").map(Tag::getAsFloat);
        var saturation = root.optional("saturation").map(Tag::getAsFloat);
        var experience = root.optional("experience").map(Tag::getAsFloat);
        var foodLevel = root.optional("foodLevel").map(Tag::getAsInt);
        var level = root.optional("level").map(Tag::getAsInt);
        var score = root.optional("score").map(Tag::getAsInt);
        return new PerWorldData(
                enderChestContents.orElse(new ItemStack[27]),
                inventoryContents.orElse(new ItemStack[40]),
                respawnLocation.orElse(null),
                gameMode.orElse(GameMode.SURVIVAL),
                health.orElse(20.0),
                exhaustion.orElse(0.0f),
                saturation.orElse(5.0f),
                experience.orElse(0f),
                foodLevel.orElse(20),
                level.orElse(0),
                score.orElse(0)
        );
    }

    @Override
    public CompoundTag serialize(PerWorldData data, TagSerializationContext context) throws ParserException {
        var tag = new CompoundTag();
        tag.add("enderChest", context.serialize(data.enderChestContents()));
        tag.add("inventory", context.serialize(data.inventoryContents()));
        if (data.respawnLocation() != null) tag.add("respawnLocation", context.serialize(data.respawnLocation()));
        tag.add("gameMode", context.serialize(data.gameMode()));
        tag.add("health", data.health());
        tag.add("exhaustion", data.exhaustion());
        tag.add("experience", data.experience());
        tag.add("saturation", data.saturation());
        tag.add("foodLevel", data.foodLevel());
        tag.add("level", data.level());
        tag.add("score", data.score());
        return tag;
    }
}
