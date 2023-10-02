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
public class GeneratorSettings {
    private @SerializedName("bonus_chest") boolean bonusChest = false;
    private @SerializedName("dimensions") KeyMap<Dimension> dimensions = new KeyMap<Dimension>()
            .add(Key.key("minecraft", "overworld"),
                    new Dimension(new Generator(
                            new BiomeSource(
                                    null,
                                    Key.key("minecraft", "overworld"),
                                    Key.key("minecraft", "multi_noise")
                            ),
                            Key.key("minecraft", "overworld"),
                            Key.key("minecraft", "noise")
                    ), Key.key("minecraft", "overworld")))
            .add(Key.key("minecraft", "the_end"),
                    new Dimension(new Generator(
                            new BiomeSource(
                                    null,
                                    Key.key("minecraft", "the_end"),
                                    Key.key("minecraft", "multi_noise")
                            ),
                            Key.key("minecraft", "the_end"),
                            Key.key("minecraft", "noise")
                    ), Key.key("minecraft", "the_end")))
            .add(Key.key("minecraft", "the_nether"),
                    new Dimension(new Generator(
                            new BiomeSource(
                                    null,
                                    Key.key("minecraft", "nether"),
                                    Key.key("minecraft", "multi_noise")
                            ),
                            Key.key("minecraft", "nether"),
                            Key.key("minecraft", "noise")
                    ), Key.key("minecraft", "the_nether")));
    private @SerializedName("generate_features") boolean generateFeatures = true;
    private @SerializedName("seed") long seed = 0;
}
