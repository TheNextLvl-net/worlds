package net.thenextlvl.perworlds.adapter;

import core.nbt.serialization.ParserException;
import core.nbt.serialization.TagAdapter;
import core.nbt.serialization.TagDeserializationContext;
import core.nbt.serialization.TagSerializationContext;
import core.nbt.tag.Tag;
import net.kyori.adventure.key.Key;
import org.bukkit.Registry;
import org.bukkit.potion.PotionEffectType;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PotionEffectTypeAdapter implements TagAdapter<PotionEffectType> {
    @Override
    public PotionEffectType deserialize(Tag tag, TagDeserializationContext context) throws ParserException {
        return Registry.POTION_EFFECT_TYPE.getOrThrow(context.deserialize(tag, Key.class));
    }

    @Override
    public Tag serialize(PotionEffectType effectType, TagSerializationContext context) throws ParserException {
        return context.serialize(effectType.key());
    }
}
