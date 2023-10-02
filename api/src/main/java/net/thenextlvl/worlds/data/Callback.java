package net.thenextlvl.worlds.data;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@AllArgsConstructor
@Accessors(fluent = true)
public class Callback {
    private @SerializedName("name") String name;
    private @SerializedName("type") String type;
}
