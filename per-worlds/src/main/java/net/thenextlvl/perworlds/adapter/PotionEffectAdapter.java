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
    public Tag serialize(PotionEffect effect, TagSerializationContext context) throws ParserException {
        var tag = new CompoundTag();
        tag.add("type", context.serialize(effect.getType()));
        tag.add("duration", effect.getDuration());
        tag.add("amplifier", effect.getAmplifier());
        tag.add("ambient", effect.isAmbient());
        tag.add("particles", effect.hasParticles());
        tag.add("icon", effect.hasIcon());
        return tag;
    }
}
