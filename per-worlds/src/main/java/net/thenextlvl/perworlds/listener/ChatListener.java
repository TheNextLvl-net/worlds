package net.thenextlvl.perworlds.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.thenextlvl.perworlds.GroupProvider;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {
    private final GroupProvider provider;

    public ChatListener(GroupProvider provider) {
        this.provider = provider;
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        var group = provider.getGroup(event.getPlayer().getWorld())
                .orElse(provider.getUnownedWorldGroup());
        if (group.getSettings().chat()) event.viewers().removeIf(audience ->
                audience instanceof Player player
                && !player.equals(event.getPlayer())
                && !group.containsWorld(player.getWorld())
        );
    }
}
