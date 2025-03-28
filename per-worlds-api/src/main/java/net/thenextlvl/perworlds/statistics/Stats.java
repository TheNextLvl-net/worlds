package net.thenextlvl.perworlds.statistics;

import org.bukkit.Statistic;
import org.bukkit.block.BlockType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

import java.util.Map;

@NullMarked
public interface Stats {
    @Unmodifiable
    Map<Statistic, Stat<?>> getStatistics();

    int getStatistic(Statistic statistic);

    int getStatistic(Statistic statistic, BlockType type);

    int getStatistic(Statistic statistic, EntityType type);

    int getStatistic(Statistic statistic, ItemType type);

    void setStatistic(Statistic statistic, BlockType type, int value);

    void setStatistic(Statistic statistic, EntityType type, int value);

    void setStatistic(Statistic statistic, ItemType type, int value);

    void setStatistic(Statistic statistic, int value);

    void apply(Player player);
}
