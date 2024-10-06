package net.thenextlvl.worlds.version;

import core.paper.version.PaperHangarVersionChecker;
import core.version.SemanticVersion;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

public class PluginVersionChecker extends PaperHangarVersionChecker<SemanticVersion> {
    public PluginVersionChecker(Plugin plugin) {
        super(plugin, "TheNextLvl", "Worlds");
    }

    @Override
    public @Nullable SemanticVersion parseVersion(String version) {
        return SemanticVersion.parse(version);
    }
}
