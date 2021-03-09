package net.nonswag.tnl.world.listeners;

import com.google.gson.JsonArray;
import net.nonswag.tnl.world.Worlds;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;

public class WorldListener implements Listener {

    /**
     * "worlds": ["x", "y", "z"],
     * "x": {
     *     "type": "type",
     *     "environment": "environment",
     *     "generator": "generator"
     * },
     * "y": {
     *     "type": "type",
     *     "environment": "environment",
     *     "generator": "generator"
     * },
     * "z": {
     *     "type": "type",
     *     "environment": "environment",
     *     "generator": "generator"
     * }
     */

    @EventHandler
    public void onInitializeWorld(WorldInitEvent event) {
        JsonArray worlds = Worlds.getInstance().getWorldArray();
        worlds.add(event.getWorld().getName());
        Worlds.getInstance().getConfiguration().getJsonElement().getAsJsonObject().add("worlds", worlds);
        Worlds.getInstance().getConfiguration().getJsonElement().getAsJsonObject().addProperty(event.getWorld().getName() + "-type", event.getWorld().getWorldType().name());
        Worlds.getInstance().getConfiguration().getJsonElement().getAsJsonObject().addProperty(event.getWorld().getName() + "-environment", event.getWorld().getEnvironment().name());
        Worlds.getInstance().getConfiguration().getJsonElement().getAsJsonObject().addProperty(event.getWorld().getName() + "-generator", "plugins");
        Worlds.getInstance().getConfiguration().save();
    }
}
