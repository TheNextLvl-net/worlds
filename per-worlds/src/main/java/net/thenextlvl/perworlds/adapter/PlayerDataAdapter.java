package net.thenextlvl.perworlds.adapter;

import core.nbt.serialization.ParserException;
import core.nbt.serialization.TagAdapter;
import core.nbt.serialization.TagDeserializationContext;
import core.nbt.serialization.TagSerializationContext;
import core.nbt.tag.CompoundTag;
import core.nbt.tag.ListTag;
import core.nbt.tag.StringTag;
import core.nbt.tag.Tag;
import net.thenextlvl.perworlds.data.AttributeData;
import net.thenextlvl.perworlds.data.PlayerData;
import net.thenextlvl.perworlds.model.PaperPlayerData;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PlayerDataAdapter implements TagAdapter<PlayerData> {
    @Override
    public PlayerData deserialize(Tag tag, TagDeserializationContext context) throws ParserException {
        var data = new PaperPlayerData();
        var root = tag.getAsCompound();
        root.optional("attributes").map(Tag::getAsList).map(list ->
                list.stream().map(attribute -> context.deserialize(attribute, AttributeData.class)).toList()
        ).ifPresent(data::attributes);
        root.optional("enderChest").map(items -> context.deserialize(items, ItemStack[].class)).ifPresent(data::enderChestContents);
        root.optional("inventory").map(items -> context.deserialize(items, ItemStack[].class)).ifPresent(data::inventoryContents);
        root.optional("respawnLocation").map(location -> context.deserialize(location, Location.class)).ifPresent(data::respawnLocation);
        root.optional("recipes").map(Tag::getAsList).map(list ->
                list.stream().map(recipe -> context.deserialize(recipe, NamespacedKey.class)).toList()
        ).ifPresent(data::discoveredRecipes);
        root.optional("potionEffects").map(Tag::getAsList).map(list ->
                list.stream().map(effect -> context.deserialize(effect, PotionEffect.class)).toList()
        ).ifPresent(data::potionEffects);
        root.optional("gameMode").map(mode -> context.deserialize(mode, GameMode.class)).ifPresent(data::gameMode);
        root.optional("seenCredits").map(Tag::getAsBoolean).ifPresent(data::seenCredits);
        root.optional("absorption").map(Tag::getAsDouble).ifPresent(data::absorption);
        root.optional("health").map(Tag::getAsDouble).ifPresent(data::health);
        root.optional("exhaustion").map(Tag::getAsFloat).ifPresent(data::exhaustion);
        root.optional("experience").map(Tag::getAsFloat).ifPresent(data::experience);
        root.optional("fallDistance").map(Tag::getAsFloat).ifPresent(data::fallDistance);
        root.optional("saturation").map(Tag::getAsFloat).ifPresent(data::saturation);
        root.optional("fireTicks").map(Tag::getAsInt).ifPresent(data::fireTicks);
        root.optional("foodLevel").map(Tag::getAsInt).ifPresent(data::foodLevel);
        root.optional("freezeTicks").map(Tag::getAsInt).ifPresent(data::freezeTicks);
        root.optional("heldItemSlot").map(Tag::getAsInt).ifPresent(data::heldItemSlot);
        root.optional("level").map(Tag::getAsInt).ifPresent(data::level);
        root.optional("remainingAir").map(Tag::getAsInt).ifPresent(data::remainingAir);
        root.optional("score").map(Tag::getAsInt).ifPresent(data::score);
        return data;
    }

    @Override
    public CompoundTag serialize(PlayerData data, TagSerializationContext context) throws ParserException {
        var tag = new CompoundTag();
        tag.add("attributes", new ListTag<>(data.attributes().stream().map(context::serialize).toList(), CompoundTag.ID));
        tag.add("enderChest", context.serialize(data.enderChestContents()));
        tag.add("inventory", context.serialize(data.inventoryContents()));
        var location = data.respawnLocation();
        if (location != null) tag.add("respawnLocation", context.serialize(location));
        tag.add("recipes", new ListTag<>(data.discoveredRecipes().stream().map(context::serialize).toList(), StringTag.ID));
        tag.add("potionEffects", new ListTag<>(data.potionEffects().stream().map(context::serialize).toList(), CompoundTag.ID));
        tag.add("gameMode", context.serialize(data.gameMode()));
        tag.add("seenCredits", data.seenCredits());
        tag.add("absorption", data.absorption());
        tag.add("health", data.health());
        tag.add("exhaustion", data.exhaustion());
        tag.add("experience", data.experience());
        tag.add("fallDistance", data.fallDistance());
        tag.add("saturation", data.saturation());
        tag.add("fireTicks", data.fireTicks());
        tag.add("foodLevel", data.foodLevel());
        tag.add("freezeTicks", data.freezeTicks());
        tag.add("heldItemSlot", data.heldItemSlot());
        tag.add("level", data.level());
        tag.add("remainingAir", data.remainingAir());
        tag.add("score", data.score());
        return tag;
    }
}
