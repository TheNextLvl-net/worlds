package net.thenextlvl.worlds.api;

import net.thenextlvl.perworlds.GroupProvider;
import net.thenextlvl.worlds.api.link.LinkProvider;
import net.thenextlvl.worlds.api.model.LevelBuilder;
import net.thenextlvl.worlds.api.view.GeneratorView;
import net.thenextlvl.worlds.api.view.LevelView;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.File;

@NullMarked
public interface WorldsProvider extends Plugin {
    GeneratorView generatorView();

    LevelBuilder levelBuilder(File level);

    LevelView levelView();

    LinkProvider linkProvider();

    /**
     * Retrieves the {@link GroupProvider} instance responsible for managing and interacting with world groups.
     * A group provider facilitates the creation, retrieval, and modification of world groups.
     *
     * @return the {@link GroupProvider} instance, or {@code null} if unsupported (e.g., in environments like Folia)
     */
    @Nullable
    GroupProvider groupProvider();
}
