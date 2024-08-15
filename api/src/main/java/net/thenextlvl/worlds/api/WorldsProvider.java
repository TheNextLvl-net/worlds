package net.thenextlvl.worlds.api;

import net.thenextlvl.worlds.api.link.LinkController;
import net.thenextlvl.worlds.api.view.GeneratorView;
import net.thenextlvl.worlds.api.view.LevelView;

public interface WorldsProvider {
    GeneratorView generatorView();

    LevelView levelView();

    LinkController linkController();
}
