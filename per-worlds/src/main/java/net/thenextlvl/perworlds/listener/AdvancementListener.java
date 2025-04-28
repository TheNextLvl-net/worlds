package net.thenextlvl.perworlds.listener;

import net.thenextlvl.perworlds.GroupProvider;
import net.thenextlvl.perworlds.WorldGroup;
import org.bukkit.GameRule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

import java.util.Collection;
import java.util.Optional;

public class AdvancementListener implements Listener {
    private final GroupProvider provider;

    public AdvancementListener(GroupProvider provider) {
        this.provider = provider;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        var message = event.message();
        if (message == null) return;
        var world = event.getPlayer().getWorld();
        var rule = Optional.ofNullable(world.getGameRuleValue(GameRule.ANNOUNCE_ADVANCEMENTS))
                .orElseGet(() -> world.getGameRuleDefault(GameRule.ANNOUNCE_ADVANCEMENTS));
        if (rule != null && !rule) return;
        var group = provider.getGroup(world).orElse(provider.getUnownedWorldGroup());
        var receivers = group.getSettings().advancements() ? group.getPlayers() : provider.getAllGroups().stream()
                .filter(target -> !target.getSettings().advancements())
                .map(WorldGroup::getPlayers)
                .flatMap(Collection::stream)
                .toList();
        receivers.forEach(player -> player.sendMessage(message));
        event.message(null);
    }
}
