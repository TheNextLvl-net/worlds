package net.thenextlvl.perworlds.model;

import core.nbt.serialization.ParserException;
import core.nbt.tag.Tag;
import net.kyori.adventure.key.Key;
import net.thenextlvl.perworlds.statistics.ItemTypeStat;
import org.bukkit.Registry;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemType;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PaperItemTypeStat extends PaperStat<ItemType> implements ItemTypeStat {
    @Override
    @SuppressWarnings("PatternValidation")
    public void deserialize(Tag tag) throws ParserException {
        tag.getAsCompound().forEach((type, value) -> {
            var entity = Registry.ITEM.getOrThrow(Key.key(type));
            values.put(entity, value.getAsInt());
        });

    }

    @Override
    @SuppressWarnings({"DataFlowIssue", "deprecation"})
    public void apply(Statistic statistic, Player player) {
        values.forEach((type, value) -> player.setStatistic(statistic, type.asMaterial(), value));
    }
}
