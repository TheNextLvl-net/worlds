package net.thenextlvl.worlds.api;

import net.thenextlvl.worlds.api.link.LinkController;
import net.thenextlvl.worlds.api.view.GeneratorView;
import net.thenextlvl.worlds.api.view.LevelView;
import org.bukkit.plugin.Plugin;

public interface WorldsProvider extends Plugin {
    GeneratorView generatorView();

    LevelView levelView();

    LinkController linkController();
}
