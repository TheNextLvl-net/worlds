package net.thenextlvl.perworlds.model;

import core.nbt.serialization.ParserException;
import core.nbt.tag.Tag;
import net.kyori.adventure.key.Key;
import net.thenextlvl.perworlds.statistics.EntityTypeStat;
import org.bukkit.Registry;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PaperEntityTypeStat extends PaperStat<EntityType> implements EntityTypeStat {
    @Override
    @SuppressWarnings("PatternValidation")
    public void deserialize(Tag tag) throws ParserException {
        tag.getAsCompound().forEach((type, value) -> {
            var entity = Registry.ENTITY_TYPE.getOrThrow(Key.key(type));
            values.put(entity, value.getAsInt());
        });
    }

    @Override
    public void apply(Statistic statistic, Player player) {
        values.forEach((type, value) -> player.setStatistic(statistic, type, value));
    }
}
