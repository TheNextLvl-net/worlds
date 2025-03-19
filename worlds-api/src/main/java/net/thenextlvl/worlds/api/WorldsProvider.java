package net.thenextlvl.worlds.api;

import net.thenextlvl.worlds.api.link.LinkController;
import net.thenextlvl.worlds.api.model.LevelBuilder;
import net.thenextlvl.worlds.api.view.GeneratorView;
import net.thenextlvl.worlds.api.view.LevelView;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NullMarked;

import java.io.File;

@NullMarked
public interface WorldsProvider extends Plugin {
    GeneratorView generatorView();

    LevelBuilder levelBuilder(File level);

    LevelView levelView();

    LinkController linkController();
}
