package net.thenextlvl.worlds.level;

import net.thenextlvl.worlds.WorldsPlugin;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
class PaperLevel extends LevelData {
    public PaperLevel(WorldsPlugin plugin, Builder builder) {
        super(plugin, builder);
    }

    protected @Nullable World createWorld(WorldCreator creator) {
        return creator.createWorld();
    }
}
