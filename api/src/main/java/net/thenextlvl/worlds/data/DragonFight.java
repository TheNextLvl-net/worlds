package net.thenextlvl.worlds.data;

import com.google.gson.annotations.SerializedName;
import lombok.*;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DragonFight {
    private @SerializedName("DragonKilled") boolean dragonKilled = false;
    private @SerializedName("Gateways") Set<Integer> gateways = IntStream.range(0, 20).boxed()
            .collect(Collectors.toSet());
    private @SerializedName("NeedsStateScanning") boolean needsStateScanning = true;
    private @SerializedName("PreviouslyKilled") boolean previouslyKilled = false;
}
