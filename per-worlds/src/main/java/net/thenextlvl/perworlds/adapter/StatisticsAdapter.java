package net.thenextlvl.perworlds.adapter;

import core.nbt.serialization.ParserException;
import core.nbt.serialization.TagAdapter;
import core.nbt.serialization.TagDeserializationContext;
import core.nbt.serialization.TagSerializationContext;
import core.nbt.tag.CompoundTag;
import core.nbt.tag.Tag;
import net.kyori.adventure.key.Key;
import net.thenextlvl.perworlds.model.PaperStats;
import net.thenextlvl.perworlds.statistics.Stats;
import org.bukkit.Registry;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class StatisticsAdapter implements TagAdapter<Stats> {
    @Override
    @SuppressWarnings("PatternValidation")
    public Stats deserialize(Tag tag, TagDeserializationContext context) throws ParserException {
        var statistics = new PaperStats();
        tag.getAsCompound().forEach((key, value) -> {
            var statistic = Registry.STATISTIC.getOrThrow(Key.key(key));
            statistics.setStatistic(statistic, value);
        });
        return statistics;
    }

    @Override
    public Tag serialize(Stats stats, TagSerializationContext context) throws ParserException {
        var tag = new CompoundTag();
        stats.getStatistics().forEach((statistic, value) -> {
            if (value.shouldSerialize()) tag.add(statistic.key().asString(), value.serialize());
        });
        return tag;
    }
}
