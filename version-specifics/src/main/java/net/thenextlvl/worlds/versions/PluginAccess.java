package net.thenextlvl.worlds.versions;

import dev.faststats.core.ErrorTracker;
import net.thenextlvl.worlds.api.link.LinkProvider;
import org.bukkit.plugin.Plugin;

public interface PluginAccess extends Plugin {
    ErrorTracker getErrorTracker();

    LinkProvider linkProvider();

    boolean isRunningFolia();
}
