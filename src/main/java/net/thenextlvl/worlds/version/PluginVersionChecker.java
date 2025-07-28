package net.thenextlvl.worlds.version;

import core.paper.version.PaperModrinthVersionChecker;
import core.version.SemanticVersion;
import net.thenextlvl.worlds.WorldsPlugin;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PluginVersionChecker extends PaperModrinthVersionChecker<SemanticVersion> {
    public PluginVersionChecker(Plugin plugin) {
        super(plugin, "gBIw3Gvy");
    }

    @Override
    public String getLoader() {
        return WorldsPlugin.RUNNING_FOLIA ? "folia" : "paper";
    }

    @Override
    public SemanticVersion parseVersion(String version) {
        return SemanticVersion.parse(version);
    }
}
