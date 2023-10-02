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
public class Version {
    public @SerializedName("Id") int id;
    public @SerializedName("Name") String name;
    public @SerializedName("Series") String series;
    public @SerializedName("Snapshot") boolean snapshot;
}
