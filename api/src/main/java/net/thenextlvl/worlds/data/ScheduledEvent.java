package net.thenextlvl.worlds.data;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.security.Key;

@Getter
@Setter
@AllArgsConstructor
@Accessors(fluent = true)
public class ScheduledEvent {
    private @SerializedName("Callback") Callback callback;
    private @SerializedName("Name") Key name;
    private @SerializedName("TriggerTime") long triggerTime;
}
