package worlds.util;

import core.api.placeholder.Placeholder;
import worlds.Worlds;

public class Placeholders {
    public static void init(Worlds plugin) {
        plugin.formatter().registry().register(Placeholder.of("prefix", Messages.PREFIX.message()));
    }
}
