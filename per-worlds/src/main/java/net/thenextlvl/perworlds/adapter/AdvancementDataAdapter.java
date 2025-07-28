package net.thenextlvl.perworlds.adapter;

import core.nbt.serialization.ParserException;
import core.nbt.serialization.TagAdapter;
import core.nbt.serialization.TagDeserializationContext;
import core.nbt.serialization.TagSerializationContext;
import core.nbt.tag.CompoundTag;
import core.nbt.tag.ListTag;
import core.nbt.tag.StringTag;
import core.nbt.tag.Tag;
import net.thenextlvl.perworlds.model.PaperAdvancementData;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.jspecify.annotations.NullMarked;

import java.time.Instant;
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
        var root = tag.getAsCompound();
        var key = context.deserialize(root.get("advancement"), NamespacedKey.class);
        var advancement = server.getAdvancement(key);
        if (advancement == null) throw new ParserException("Encountered unknown advancement: " + key);
        var awarded = new HashMap<String, Instant>();
        root.getAsCompound("awarded").forEach((criteria, instant) -> awarded.put(criteria, context.deserialize(instant, Instant.class)));
        var remaining = root.getAsList("remaining").stream().map(Tag::getAsString).collect(Collectors.toSet());
        return new PaperAdvancementData(advancement, awarded, remaining);
    }

    @Override
    public Tag serialize(PaperAdvancementData data, TagSerializationContext context) throws ParserException {
        var tag = new CompoundTag();
        var awarded = new CompoundTag();
        data.awardedCriteria().forEach((criteria, date) -> {
            if (date != null) awarded.add(criteria, context.serialize(date));
        });
        tag.add("advancement", context.serialize(data.getAdvancement().key()));
        tag.add("awarded", awarded);
        tag.add("remaining", new ListTag<>(data.getRemainingCriteria().stream().map(StringTag::new).toList(), StringTag.ID));
        return tag;
    }
}
