package net.thenextlvl.perworlds.listener;

import io.papermc.paper.event.world.WorldGameRuleChangeEvent;
import io.papermc.paper.event.world.border.WorldBorderBoundsChangeEvent;
import io.papermc.paper.event.world.border.WorldBorderCenterChangeEvent;
import net.thenextlvl.perworlds.GroupData;
import net.thenextlvl.perworlds.GroupData.Type;
import net.thenextlvl.perworlds.WorldGroup;
import net.thenextlvl.perworlds.group.PaperGroupProvider;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.TimeSkipEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.jspecify.annotations.NullMarked;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@NullMarked
public class WorldListener implements Listener {
    private final PaperGroupProvider provider;

    public WorldListener(PaperGroupProvider provider) {
        this.provider = provider;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldInit(WorldInitEvent event) {
        provider.getGroup(event.getWorld())
                .orElse(provider.getUnownedWorldGroup())
                .updateWorldData(event.getWorld());
    }

    // todo: there is no difficulty change event????
    // https://github.com/PaperMC/Paper/pull/12471

    private final Map<Type, Set<WorldGroup>> lock = new HashMap<>();

    @SuppressWarnings("unchecked")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldGameRuleChange(WorldGameRuleChangeEvent event) {
        processWorldDataUpdate(event.getWorld(), Type.GAME_RULE, data -> {
            var gameRule = (GameRule<Object>) event.getGameRule();
            var value = parseValue(gameRule, event.getValue());
            data.setGameRule(gameRule, value);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTimeSkip(TimeSkipEvent event) {
        processWorldDataUpdate(event.getWorld(), Type.TIME, data ->
                data.time(event.getWorld().getFullTime() + event.getSkipAmount()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWeatherChange(WeatherChangeEvent event) {
        processWorldDataUpdate(event.getWorld(), Type.WEATHER, data -> {
            data.raining(event.toWeatherState());
            data.rainDuration(event.getWorld().getWeatherDuration());
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onThunderChange(ThunderChangeEvent event) {
        processWorldDataUpdate(event.getWorld(), Type.WEATHER, data -> {
            data.thundering(event.toThunderState());
            data.thunderDuration(event.getWorld().getThunderDuration());
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldBorderChange(WorldBorderBoundsChangeEvent event) {
        processWorldDataUpdate(event.getWorld(), Type.WORLD_BORDER, data -> {
            data.getWorldBorder().size(event.getNewSize());
            data.getWorldBorder().duration(event.getDuration());
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldBorderChange(WorldBorderCenterChangeEvent event) {
        processWorldDataUpdate(event.getWorld(), Type.WORLD_BORDER, data ->
                data.getWorldBorder().center(event.getNewCenter()));
    }

    private void processWorldDataUpdate(World world, Type type, Consumer<GroupData> process) {
        var group = provider.getGroup(world)
                .orElse(provider.getUnownedWorldGroup());
        if (!lock.computeIfAbsent(type, ignored -> new HashSet<>()).add(group)) return;
        process.accept(group.getGroupData());
        group.getWorlds()
                .filter(target -> !target.equals(world))
                .forEach(target -> group.updateWorldData(target, type));
        lock.computeIfPresent(type, (ignored, groups) -> {
            groups.remove(group);
            return groups.isEmpty() ? null : groups;
        });
    }

    private Object parseValue(GameRule<?> rule, String value) {
        return rule.getType().equals(Integer.class) ? Integer.parseInt(value) : Boolean.parseBoolean(value);
    }
}
