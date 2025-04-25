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

import java.util.HashSet;
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
    // private final Set<WorldGroup> processingDifficulty = new HashSet<>();
    private final Set<WorldGroup> processingBorder = new HashSet<>();
    private final Set<WorldGroup> processingGameRules = new HashSet<>();
    private final Set<WorldGroup> processingRain = new HashSet<>();
    private final Set<WorldGroup> processingThunder = new HashSet<>();
    private final Set<WorldGroup> processingTime = new HashSet<>();

    @SuppressWarnings("unchecked")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldGameRuleChange(WorldGameRuleChangeEvent event) {
        processChangeUpdate(event.getWorld(), Type.GAME_RULE, processingGameRules, data -> {
            var gameRule = (GameRule<Object>) event.getGameRule();
            var value = parseValue(gameRule, event.getValue());
            data.gameRule(gameRule, value);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTimeSkip(TimeSkipEvent event) {
        processChangeUpdate(event.getWorld(), Type.TIME, processingTime, data ->
                data.time(event.getWorld().getFullTime() + event.getSkipAmount()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWeatherChange(WeatherChangeEvent event) {
        processChangeUpdate(event.getWorld(), Type.WEATHER, processingRain, data -> {
            data.raining(event.toWeatherState());
            data.rainDuration(event.getWorld().getWeatherDuration());
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onThunderChange(ThunderChangeEvent event) {
        processChangeUpdate(event.getWorld(), Type.WEATHER, processingThunder, data -> {
            data.thundering(event.toThunderState());
            data.thunderDuration(event.getWorld().getThunderDuration());
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldBorderChange(WorldBorderBoundsChangeEvent event) {
        processChangeUpdate(event.getWorld(), Type.WORLD_BORDER, processingBorder, data -> {
            data.worldBorder().size(event.getNewSize());
            data.worldBorder().duration(event.getDuration());
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldBorderChange(WorldBorderCenterChangeEvent event) {
        processChangeUpdate(event.getWorld(), Type.WORLD_BORDER, processingBorder, data ->
                data.worldBorder().center(event.getNewCenter()));
    }

    private void processChangeUpdate(World world, Type type, Set<WorldGroup> lock, Consumer<GroupData> process) {
        var group = provider.getGroup(world)
                .orElse(provider.getUnownedWorldGroup());
        if (!lock.add(group)) return;
        process.accept(group.getGroupData());
        group.getWorlds().stream()
                .filter(target -> !target.equals(world))
                .forEach(target -> group.updateWorldData(target, type));
        lock.remove(group);
    }

    private Object parseValue(GameRule<?> rule, String value) {
        return rule.getType().equals(Integer.class) ? Integer.parseInt(value) : Boolean.parseBoolean(value);
    }
}
