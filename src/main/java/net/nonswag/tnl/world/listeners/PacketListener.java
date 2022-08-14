package net.nonswag.tnl.world.listeners;

import net.minecraft.server.v1_16_R3.DimensionManager;
import net.minecraft.server.v1_16_R3.PacketPlayOutRespawn;
import net.nonswag.tnl.core.api.reflection.Reflection;
import net.nonswag.tnl.listener.events.PlayerPacketEvent;
import net.nonswag.tnl.world.api.world.TNLWorld;
import net.nonswag.tnl.world.api.world.WorldType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import javax.annotation.Nonnull;

public class PacketListener implements Listener {

    @EventHandler
    public void onPacket(@Nonnull PlayerPacketEvent event) {
        if (!event.getDirection().isOutgoing()) return;
        if (!(event.getPacket() instanceof PacketPlayOutRespawn packet)) return;
        TNLWorld world = TNLWorld.cast(event.getPlayer().worldManager().getWorld());
        DimensionManager manager = event.<DimensionManager>getPacketField("a").nonnull();
        Reflection.setField(manager, "ambientLight", world.fullBright() ? 1 : 0);
        event.setPacketField("g", world.type().equals(WorldType.FLAT));
    }
}
