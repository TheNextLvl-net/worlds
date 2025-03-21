package net.thenextlvl.perworlds.adapter;

import core.nbt.serialization.ParserException;
import core.nbt.serialization.TagAdapter;
import core.nbt.serialization.TagDeserializationContext;
import core.nbt.serialization.TagSerializationContext;
import core.nbt.tag.CompoundTag;
import core.nbt.tag.Tag;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PotionEffectAdapter implements TagAdapter<PotionEffect> {
    @Override
    public PotionEffect deserialize(Tag tag, TagDeserializationContext context) throws ParserException {
        var root = tag.getAsCompound();
        var type = context.deserialize(root.get("type"), PotionEffectType.class);
        var duration = root.get("duration").getAsInt();
        var amplifier = root.get("amplifier").getAsInt();
        var ambient = root.get("ambient").getAsBoolean();
        var particles = root.get("particles").getAsBoolean();
        var icon = root.get("icon").getAsBoolean();
        return new PotionEffect(type, duration, amplifier, ambient, particles, icon);
    }

    @Override
    public Tag serialize(PotionEffect object, TagSerializationContext context) throws ParserException {
        var tag = new CompoundTag();
        tag.add("type", context.serialize(object.getType()));
        tag.add("duration", object.getDuration());
        tag.add("amplifier", object.getAmplifier());
        tag.add("ambient", object.isAmbient());
        tag.add("particles", object.hasParticles());
        tag.add("icon", object.hasIcon());
        return tag;
    }
}
