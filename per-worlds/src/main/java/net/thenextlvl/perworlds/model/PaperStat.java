package net.thenextlvl.perworlds.model;

import core.nbt.serialization.ParserException;
import core.nbt.tag.CompoundTag;
import core.nbt.tag.Tag;
import net.kyori.adventure.key.Keyed;
import net.thenextlvl.perworlds.statistics.Stat;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

import java.util.HashMap;
import java.util.Map;

@NullMarked
public abstract class PaperStat<T extends Keyed> implements Stat<T> {
    protected final Map<T, Integer> values = new HashMap<>();

    @Override
    public @Unmodifiable Map<T, Integer> getValues() {
        return Map.copyOf(values);
    }

    @Override
    public int getValue(T type) {
        return values.getOrDefault(type, 0);
    }

    @Override
    public void setValue(T type, int value) {
        values.put(type, value);
    }

    @Override
    public boolean shouldSerialize() {
        return values.values().stream().anyMatch(integer -> integer != 0);
    }

    @Override
    public Tag serialize() throws ParserException {
        var tag = new CompoundTag();
        values.forEach((type, integer) -> {
            if (integer != 0) tag.add(type.key().asString(), integer);
        });
        return tag;
    }
}
