package net.thenextlvl.perworlds.listener;

import io.papermc.paper.event.world.WorldGameRuleChangeEvent;
import net.thenextlvl.perworlds.GroupData.Type;
import net.thenextlvl.perworlds.WorldGroup;
import net.thenextlvl.perworlds.group.PaperGroupProvider;
import org.bukkit.GameRule;
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
    private final Set<WorldGroup> processingGameRules = new HashSet<>();
    private final Set<WorldGroup> processingRain = new HashSet<>();
    private final Set<WorldGroup> processingThunder = new HashSet<>();
    private final Set<WorldGroup> processingTime = new HashSet<>();

    @SuppressWarnings("unchecked")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldGameRuleChange(WorldGameRuleChangeEvent event) {
        var group = provider.getGroup(event.getWorld())
                .orElse(provider.getUnownedWorldGroup());
        if (!processingGameRules.add(group)) return;
        var gameRule = (GameRule<Object>) event.getGameRule();
        var value = parseValue(gameRule, event.getValue());
        group.getGroupData().gameRule(gameRule, value);
        group.getWorlds().stream()
                .filter(world -> !world.equals(event.getWorld()))
                .forEach(world -> group.updateWorldData(world, Type.GAME_RULE));
        processingGameRules.remove(group);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTimeSkip(TimeSkipEvent event) {
        var group = provider.getGroup(event.getWorld())
                .orElse(provider.getUnownedWorldGroup());
        if (!processingTime.add(group)) return;
        group.getGroupData().time(event.getWorld().getFullTime() + event.getSkipAmount());
        group.getWorlds().stream()
                .filter(world -> !world.equals(event.getWorld()))
                .forEach(world -> group.updateWorldData(world, Type.TIME));
        processingTime.remove(group);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWeatherChange(WeatherChangeEvent event) {
        var group = provider.getGroup(event.getWorld())
                .orElse(provider.getUnownedWorldGroup());
        if (!processingRain.add(group)) return;
        group.getGroupData().raining(event.toWeatherState());
        group.getGroupData().rainDuration(event.getWorld().getWeatherDuration());
        group.getWorlds().stream()
                .filter(world -> !world.equals(event.getWorld()))
                .forEach(world -> group.updateWorldData(world, Type.WEATHER));
        processingRain.remove(group);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onThunderChange(ThunderChangeEvent event) {
        var group = provider.getGroup(event.getWorld())
                .orElse(provider.getUnownedWorldGroup());
        if (!processingThunder.add(group)) return;
        group.getGroupData().thundering(event.toThunderState());
        group.getGroupData().thunderDuration(event.getWorld().getThunderDuration());
        group.getWorlds().stream()
                .filter(world -> !world.equals(event.getWorld()))
                .forEach(world -> group.updateWorldData(world, Type.WEATHER));
        processingThunder.remove(group);
    }

    private Object parseValue(GameRule<?> rule, String value) {
        return rule.getType().equals(Integer.class) ? Integer.parseInt(value) : Boolean.parseBoolean(value);
    }
}
