package net.nonswag.tnl.world.listeners;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.nonswag.tnl.world.Worlds;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;

import java.util.Iterator;

public class WorldListener implements Listener {

    @EventHandler
    public void onInitializeWorld(WorldInitEvent event) {
        JsonArray worlds = Worlds.getInstance().getWorldArray();
        Iterator<JsonElement> iterator = worlds.iterator();
        while (iterator.hasNext()) {
            JsonElement jsonElement = iterator.next();
            if (jsonElement.getAsString().equalsIgnoreCase(event.getWorld().getName())) {
                iterator.remove();
            }
        }
        worlds.add(event.getWorld().getName());
        Worlds.getInstance().getConfiguration().getJsonElement().getAsJsonObject().add("worlds", worlds);
        if (!Worlds.getInstance().getConfiguration().getJsonElement().getAsJsonObject().has(event.getWorld().getName() + ".type")) {
            Worlds.getInstance().getConfiguration().getJsonElement().getAsJsonObject().addProperty(event.getWorld().getName() + ".type", event.getWorld().getWorldType().name());
        }
        if (!Worlds.getInstance().getConfiguration().getJsonElement().getAsJsonObject().has(event.getWorld().getName() + ".environment")) {
            Worlds.getInstance().getConfiguration().getJsonElement().getAsJsonObject().addProperty(event.getWorld().getName() + ".environment", event.getWorld().getEnvironment().name());
        }
        if (!Worlds.getInstance().getConfiguration().getJsonElement().getAsJsonObject().has(event.getWorld().getName() + ".generator")) {
            Worlds.getInstance().getConfiguration().getJsonElement().getAsJsonObject().addProperty(event.getWorld().getName() + ".generator", "plugins");
        }
        Worlds.getInstance().getConfiguration().save();
    }
}
