package net.thenextlvl.worlds.data;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@Accessors(fluent = true)
public class DataPacks {
    private @SerializedName("Disabled") Set<String> disabled;
    private @SerializedName("Enabled") Set<String> enabled;
}
