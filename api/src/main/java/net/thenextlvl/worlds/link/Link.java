package net.thenextlvl.worlds.link;

import com.google.gson.annotations.SerializedName;
import org.bukkit.PortalType;
import org.bukkit.World;

public record Link(
        @SerializedName("portal") PortalType portalType,
        @SerializedName("source") World source,
        @SerializedName("destination") World destination
) {
    @Override
    public String toString() {
        return portalType.name().toLowerCase() + ": " + source.getName() + " -> " + destination.getName();
    }
}
