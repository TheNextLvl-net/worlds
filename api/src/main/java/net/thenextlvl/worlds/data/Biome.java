package net.thenextlvl.worlds.data;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.kyori.adventure.key.Key;

@Getter
@Setter
@NoArgsConstructor
@Accessors(fluent = true)
public class Biome {
    private @SerializedName("biome") Key name;
    private @SerializedName("parameters") BiomeParameters parameters;
}
