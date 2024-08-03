package net.thenextlvl.worlds.link;

import com.google.gson.annotations.SerializedName;
import org.bukkit.World;

public record Link(
        @SerializedName("source") World source,
        @SerializedName("destination") World destination
) {
    @Override
    public String toString() {
        return source.key().asMinimalString() + " -> " + destination.key().asMinimalString();
    }
}
