package net.thenextlvl.perworlds.listener;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import net.thenextlvl.perworlds.group.PaperGroupProvider;
import org.bukkit.GameRule;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
public class RespawnListener implements Listener {
    private final PaperGroupProvider provider;

    public RespawnListener(PaperGroupProvider provider) {
        this.provider = provider;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        var group = provider.getGroup(event.getPlayer().getWorld())
                .orElse(provider.getUnownedWorldGroup());
        group.persistPlayerData(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        var group = provider.getGroup(event.getPlayer().getWorld())
                .orElse(provider.getUnownedWorldGroup());

        group.persistPlayerData(event.getPlayer(), playerData -> {
            // The PlayerRespawnEvent is fired before the player data is reset,
            // so to prevent duplication and preserve data integrity, we have to reset everything manually
            if (Boolean.FALSE.equals(event.getPlayer().getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY))) {
                playerData.experience(0);
                playerData.level(0);
                playerData.score(0);
            }

            playerData.absorption(0);
            playerData.arrowsInBody(0);
            playerData.beeStingersInBody(0);
            playerData.exhaustion(0);
            playerData.fallDistance(0);
            playerData.fireTicks(0);
            playerData.foodLevel(20);
            playerData.potionEffects(List.of());
            playerData.remainingAir(event.getPlayer().getMaximumAir());
            playerData.saturation(20);
            playerData.velocity(new Vector());

            var attribute = event.getPlayer().getAttribute(Attribute.MAX_HEALTH);
            playerData.health(attribute != null ? attribute.getValue() : 20);
        });

        if (event.isBedSpawn() || event.isAnchorSpawn()) return;
        group.getGroupData().getSpawnLocation().ifPresentOrElse(event::setRespawnLocation,
                () -> group.getSpawnLocation().ifPresent(event::setRespawnLocation));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerPostRespawn(PlayerPostRespawnEvent event) {
        provider.getGroup(event.getRespawnedLocation().getWorld())
                .orElse(provider.getUnownedWorldGroup())
                .loadPlayerData(event.getPlayer(), false);
    }
}
