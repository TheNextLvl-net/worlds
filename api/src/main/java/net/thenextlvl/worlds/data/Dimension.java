package net.thenextlvl.worlds.data;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.kyori.adventure.key.Key;

@Getter
@Setter
@AllArgsConstructor
@Accessors(fluent = true)
public class Dimension {
    private @SerializedName("generator") Generator generator;
    private @SerializedName("type") Key type;
}
