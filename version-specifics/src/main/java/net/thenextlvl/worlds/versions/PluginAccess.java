package net.thenextlvl.worlds.versions;

import dev.faststats.core.ErrorTracker;
import org.bukkit.plugin.Plugin;

public interface PluginAccess extends Plugin {
    ErrorTracker getErrorTracker();

    boolean isRunningFolia();
}
