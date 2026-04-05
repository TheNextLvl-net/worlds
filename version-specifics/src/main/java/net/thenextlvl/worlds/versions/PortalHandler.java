package net.thenextlvl.worlds.versions;

import org.bukkit.Location;
import org.bukkit.PortalType;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class PortalHandler<T> implements Listener {
    protected final Map<UUID, T> portalProcessors = new ConcurrentHashMap<>();
    protected final Map<UUID, TickCounter> tickCounters = new ConcurrentHashMap<>();
    protected final PluginAccess plugin;

    public PortalHandler(final PluginAccess plugin) {
        this.plugin = plugin;
    }

    protected abstract void handlePortal(Entity entity, Location location);

    /**
     * Count the number of ticks since the last event was processed.
     * <p>
     * This is required since the {@link EntityPortalEnterEvent} is fired for each portal block the entity is touching.
     */
    protected class TickCounter {
        private int lastTick;
        private int current;

        public TickCounter() {
            final var currentTick = plugin.getServer().getCurrentTick();
            this.lastTick = currentTick - 1;
            this.current = currentTick;
        }

        public TickCounter update() {
            final var currentTick = plugin.getServer().getCurrentTick();
            this.lastTick = this.current;
            this.current = currentTick;
            return this;
        }

        public boolean hasTickChanged() {
            return this.lastTick != this.current;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityRemove(final EntityRemoveEvent event) {
        portalProcessors.remove(event.getEntity().getUniqueId());
        tickCounters.remove(event.getEntity().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        portalProcessors.remove(event.getPlayer().getUniqueId());
        tickCounters.remove(event.getPlayer().getUniqueId());
    }

    /**
     * @see NetherPortalBlock#entityInside(BlockState, Level, BlockPos, Entity, InsideBlockEffectApplier)
     */
    @SuppressWarnings("JavadocReference")
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onNetherPortalEnter(final EntityPortalEnterEvent event) {
        if (!event.getPortalType().equals(PortalType.NETHER)) return;

        event.setCancelled(true);

        if (!tickCounters.compute(event.getEntity().getUniqueId(), (uuid, counter) -> {
            return counter != null ? counter.update() : new TickCounter();
        }).hasTickChanged()) return;

        handlePortal(event.getEntity(), event.getLocation());
    }
}
