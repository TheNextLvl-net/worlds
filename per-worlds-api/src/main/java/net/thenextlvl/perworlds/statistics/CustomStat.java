package net.thenextlvl.perworlds.statistics;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface CustomStat extends Stat<Void> {
    int getValue();

    void setValue(int value);
}
