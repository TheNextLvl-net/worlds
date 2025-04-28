package net.thenextlvl.perworlds.adapter;

import core.nbt.serialization.ParserException;
import core.nbt.serialization.TagAdapter;
import core.nbt.serialization.TagDeserializationContext;
import core.nbt.serialization.TagSerializationContext;
import core.nbt.tag.CompoundTag;
import core.nbt.tag.Tag;
import net.thenextlvl.perworlds.data.WardenSpawnTracker;
import net.thenextlvl.perworlds.model.PaperWardenSpawnTracker;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class WardenSpawnTrackerAdapter implements TagAdapter<WardenSpawnTracker> {
    @Override
    public WardenSpawnTracker deserialize(Tag tag, TagDeserializationContext context) throws ParserException {
        var root = tag.getAsCompound();
        var cooldownTicks = root.get("cooldownTicks").getAsInt();
        var ticksSinceLastWarning = root.get("ticksSinceLastWarning").getAsInt();
        var warningLevel = root.get("warningLevel").getAsInt();
        return new PaperWardenSpawnTracker()
                .cooldownTicks(cooldownTicks)
                .ticksSinceLastWarning(ticksSinceLastWarning)
                .warningLevel(warningLevel);
    }

    @Override
    public Tag serialize(WardenSpawnTracker tracker, TagSerializationContext context) throws ParserException {
        var tag = new CompoundTag();
        tag.add("cooldownTicks", tracker.cooldownTicks());
        tag.add("ticksSinceLastWarning", tracker.ticksSinceLastWarning());
        tag.add("warningLevel", tracker.warningLevel());
        return tag;
    }
}
