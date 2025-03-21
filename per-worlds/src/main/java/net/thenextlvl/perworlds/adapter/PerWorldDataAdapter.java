package net.thenextlvl.perworlds.adapter;

import core.nbt.serialization.ParserException;
import core.nbt.serialization.TagAdapter;
import core.nbt.serialization.TagDeserializationContext;
import core.nbt.serialization.TagSerializationContext;
import core.nbt.tag.CompoundTag;
import core.nbt.tag.ListTag;
import core.nbt.tag.Tag;
import net.thenextlvl.perworlds.model.PerWorldData;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
public class PerWorldDataAdapter implements TagAdapter<PerWorldData> {
    @Override
    public PerWorldData deserialize(Tag tag, TagDeserializationContext context) throws ParserException {
        var root = tag.getAsCompound();
        return new PerWorldData(
                root.optional("enderChest").map(items -> context.deserialize(items, ItemStack[].class)).orElseGet(() -> new ItemStack[27]),
                root.optional("inventory").map(items -> context.deserialize(items, ItemStack[].class)).orElseGet(() -> new ItemStack[40]),
                root.optional("respawnLocation").map(location -> context.deserialize(location, Location.class)).orElse(null),
                root.optional("potionEffects").map(Tag::getAsList).map(list ->
                        list.stream().map(effect -> context.deserialize(effect, PotionEffect.class)).toList()
                ).orElseGet(List::of),
                root.optional("gameMode").map(mode -> context.deserialize(mode, GameMode.class)).orElse(GameMode.SURVIVAL),
                root.optional("absorption").map(Tag::getAsDouble).orElse(0d),
                root.optional("health").map(Tag::getAsDouble).orElse(20d),
                root.optional("exhaustion").map(Tag::getAsFloat).orElse(0f),
                root.optional("saturation").map(Tag::getAsFloat).orElse(5f),
                root.optional("experience").map(Tag::getAsFloat).orElse(0f),
                root.optional("foodLevel").map(Tag::getAsInt).orElse(20),
                root.optional("level").map(Tag::getAsInt).orElse(0),
                root.optional("score").map(Tag::getAsInt).orElse(0)
        );
    }

    @Override
    public CompoundTag serialize(PerWorldData data, TagSerializationContext context) throws ParserException {
        var tag = new CompoundTag();
        tag.add("enderChest", context.serialize(data.enderChestContents()));
        tag.add("inventory", context.serialize(data.inventoryContents()));
        if (data.respawnLocation() != null) tag.add("respawnLocation", context.serialize(data.respawnLocation()));
        tag.add("potionEffects", new ListTag<>(data.potionEffects().stream().map(context::serialize).toList(), CompoundTag.ID));
        tag.add("gameMode", context.serialize(data.gameMode()));
        tag.add("absorption", data.absorption());
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
