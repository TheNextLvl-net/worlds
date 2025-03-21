package net.thenextlvl.perworlds;

public interface GroupSettings {
    boolean absorption();

    boolean attributes();

    boolean chat();

    boolean enabled();

    boolean experience();

    boolean foodLevel();

    boolean gameMode();

    boolean gameRules();

    boolean health();

    boolean inventory();

    boolean potionEffects();

    boolean respawnLocation();

    boolean saturation();

    boolean score();

    boolean tabList();

    boolean time();

    boolean weather();

    void absorption(boolean absorption);

    void attributes(boolean attributes);

    void chat(boolean chat);

    void enabled(boolean enabled);

    void experience(boolean experience);

    void foodLevel(boolean foodLevel);

    void gameMode(boolean gameMode);

    void gameRules(boolean gameRules);

    void health(boolean health);

    void inventory(boolean inventory);

    void potionEffects(boolean potionEffects);

    void respawnLocation(boolean respawnLocation);

    void saturation(boolean saturation);

    void score(boolean score);

    void tabList(boolean tabList);

    void time(boolean time);

    void weather(boolean weather);
}
