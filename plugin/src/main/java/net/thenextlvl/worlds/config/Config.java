package net.thenextlvl.worlds.config;

import com.google.gson.annotations.SerializedName;
import core.annotation.FieldsAreNullableByDefault;
import lombok.Data;
import org.bukkit.World;

@Data
@FieldsAreNullableByDefault
public class Config {
    private @SerializedName("first-join-world") World firstJoinWorld;
    private @SerializedName("join-world") World joinWorld;
}
