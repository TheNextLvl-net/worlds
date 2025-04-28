package net.thenextlvl.worlds.view;

import net.thenextlvl.worlds.WorldsPlugin;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class FoliaLevelView extends PaperLevelView {
    public FoliaLevelView(WorldsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean unloadLevel(World world, boolean save) {
        return false; // todo implement
    }
}
