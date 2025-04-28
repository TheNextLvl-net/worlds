package net.thenextlvl.perworlds.model;

import core.nbt.serialization.ParserException;
import core.nbt.tag.IntTag;
import core.nbt.tag.Tag;
import net.thenextlvl.perworlds.statistics.CustomStat;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

import java.util.Map;

@NullMarked
public class PaperCustomStat implements CustomStat {
    private int value;

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public @Unmodifiable Map<Void, Integer> getValues() {
        return Map.of();
    }

    @Override
    public boolean shouldSerialize() {
        return value != 0;
    }

    @Override
    public int getValue(Void type) {
        return getValue();
    }

    @Override
    public void setValue(Void type, int value) {
        setValue(value);
    }

    @Override
    public void apply(Statistic statistic, Player player) {
        player.setStatistic(statistic, getValue());
    }

    @Override
    public Tag serialize() throws ParserException {
        return new IntTag(getValue());
    }

    @Override
    public void deserialize(Tag tag) throws ParserException {
        setValue(tag.getAsInt());
    }
}
