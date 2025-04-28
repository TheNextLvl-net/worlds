package net.thenextlvl.perworlds.adapter;

import core.nbt.serialization.ParserException;
import core.nbt.serialization.TagAdapter;
import core.nbt.serialization.TagDeserializationContext;
import core.nbt.serialization.TagSerializationContext;
import core.nbt.tag.CompoundTag;
import core.nbt.tag.ListTag;
import core.nbt.tag.StringTag;
import core.nbt.tag.Tag;
import net.kyori.adventure.key.Key;
import net.thenextlvl.perworlds.model.PaperAdvancementData;
import org.bukkit.Server;
import org.bukkit.advancement.Advancement;
import org.jspecify.annotations.NullMarked;

import java.util.Date;
import java.util.HashMap;
import java.util.stream.Collectors;

@NullMarked
public class AdvancementDataAdapter implements TagAdapter<PaperAdvancementData> {
    private final Server server;

    public AdvancementDataAdapter(Server server) {
        this.server = server;
    }

    @Override
    public PaperAdvancementData deserialize(Tag tag, TagDeserializationContext context) throws ParserException {
        var lookup = new HashMap<Key, Advancement>();
        server.advancementIterator().forEachRemaining(advancement -> lookup.put(advancement.key(), advancement));

        var root = tag.getAsCompound();
        var advancement = lookup.get(context.deserialize(root.get("advancement"), Key.class));
        var awarded = new HashMap<String, Date>();
        root.getAsCompound("awarded").forEach((criteria, date) -> awarded.put(criteria, context.deserialize(date, Date.class)));
        var remaining = root.getAsList("remaining").stream().map(Tag::getAsString).collect(Collectors.toSet());
        return new PaperAdvancementData(advancement, awarded, remaining);
    }

    @Override
    public Tag serialize(PaperAdvancementData data, TagSerializationContext context) throws ParserException {
        var tag = new CompoundTag();
        var awarded = new CompoundTag();
        data.awardedCriteria().forEach((criteria, date) -> awarded.add(criteria, context.serialize(date)));
        tag.add("advancement", context.serialize(data.getAdvancement().key()));
        tag.add("awarded", awarded);
        tag.add("remaining", new ListTag<>(data.getRemainingCriteria().stream().map(StringTag::new).toList(), StringTag.ID));
        return tag;
    }
}
