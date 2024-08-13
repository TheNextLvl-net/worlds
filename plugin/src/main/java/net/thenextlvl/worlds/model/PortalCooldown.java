package net.thenextlvl.worlds.model;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.WeakHashMap;

public class PortalCooldown extends WeakHashMap<Entity, @Nullable ScheduledTask> {
    public boolean isActive(Entity entity) {
        return containsKey(entity);
    }

    public boolean start(Plugin plugin, Entity entity) {
        var wasActive = isActive(entity);
        cancel(entity);
        put(entity, entity.getScheduler().runDelayed(plugin, task -> remove(entity), () -> remove(entity), 10));
        return !wasActive;
    }

    private void cancel(Entity entity) {
        var task = get(entity);
        if (task != null) task.cancel();
        remove(entity);
    }
}
