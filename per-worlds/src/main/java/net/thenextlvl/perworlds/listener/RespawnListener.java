package net.thenextlvl.perworlds.listener;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import net.thenextlvl.perworlds.group.PaperGroupProvider;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class RespawnListener implements Listener {
    private final PaperGroupProvider provider;

    public RespawnListener(PaperGroupProvider provider) {
        this.provider = provider;
    }

    // fixme: new bug, no idea why
    //  just respawn and exp, health, and levels are not reset

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
            var attribute = event.getPlayer().getAttribute(Attribute.MAX_HEALTH);
            playerData.health(attribute != null ? attribute.getValue() : 20);
        });

        if (event.isBedSpawn() || event.isAnchorSpawn()) return;
        group.getGroupData().getSpawnLocation().ifPresentOrElse(event::setRespawnLocation,
                () -> group.getSpawnLocation().ifPresent(event::setRespawnLocation));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerPostRespawn(PlayerPostRespawnEvent event) {
        provider.getGroup(event.getPlayer().getWorld())
                .orElse(provider.getUnownedWorldGroup())
                .persistPlayerData(event.getPlayer());
    }
}
