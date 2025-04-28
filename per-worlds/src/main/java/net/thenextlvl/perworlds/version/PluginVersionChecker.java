package net.thenextlvl.perworlds.version;

import core.paper.version.PaperHangarVersionChecker;
import core.version.SemanticVersion;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PluginVersionChecker extends PaperHangarVersionChecker<SemanticVersion> {
    public PluginVersionChecker(Plugin plugin) {
        super(plugin, "TheNextLvl", "PerWorlds");
    }

    @Override
    public SemanticVersion parseVersion(String version) {
        return SemanticVersion.parse(version);
    }
}
