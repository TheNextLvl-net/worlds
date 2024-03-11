package net.thenextlvl.worlds.config;

import com.google.gson.annotations.SerializedName;
import core.annotation.FieldsAreNullableByDefault;
import lombok.Data;
import org.bukkit.Location;

@Data
@FieldsAreNullableByDefault
public class Config {
    private @SerializedName("first-join-location") Location firstJoinLocation;
    private @SerializedName("join-location") Location joinLocation;
}
