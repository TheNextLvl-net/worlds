package net.thenextlvl.worlds.link;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record Link(
        @SerializedName("portal")
        @NotNull PortalType portalType,
        @SerializedName("source")
        @Nullable String first,
        @SerializedName("destination")
        @Nullable String second
) {
}
