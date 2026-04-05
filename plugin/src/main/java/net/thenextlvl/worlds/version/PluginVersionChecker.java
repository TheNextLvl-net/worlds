package net.thenextlvl.worlds.version;

import net.thenextlvl.version.SemanticVersion;
import net.thenextlvl.version.modrinth.paper.PaperModrinthVersionChecker;
import net.thenextlvl.worlds.WorldsPlugin;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class PluginVersionChecker extends PaperModrinthVersionChecker<SemanticVersion> {
    public PluginVersionChecker(final Plugin plugin) {
        super(plugin, "gBIw3Gvy");
    }

    @Override
    public String getLoader() {
        return WorldsPlugin.RUNNING_FOLIA ? "folia" : "paper";
    }

    @Override
    public SemanticVersion parseVersion(final String version) {
        return SemanticVersion.parse(version);
    }
}
