package net.thenextlvl.perworlds;

public interface GroupSettings {
    boolean absorption();

    boolean attributes();

    boolean chat();

    boolean enabled();

    boolean exhaustion();

    boolean experience();

    boolean fireTicks();

    boolean foodLevel();

    boolean freezeTicks();

    boolean gameMode();

    boolean gameRules();

    boolean health();

    boolean inventory();

    boolean potionEffects();

    boolean remainingAir();

    boolean respawnLocation();

    boolean saturation();

    boolean score();

    boolean tabList();

    boolean time();

    boolean weather();

    void absorption(boolean enabled);

    void attributes(boolean enabled);

    void chat(boolean enabled);

    void enabled(boolean enabled);

    void exhaustion(boolean enabled);

    void experience(boolean enabled);

    void fireTicks(boolean enabled);

    void foodLevel(boolean enabled);

    void freezeTicks(boolean enabled);

    void gameMode(boolean enabled);

    void gameRules(boolean enabled);

    void health(boolean enabled);

    void inventory(boolean enabled);

    void potionEffects(boolean enabled);

    void remainingAir(boolean enabled);

    void respawnLocation(boolean enabled);

    void saturation(boolean enabled);

    void score(boolean enabled);

    void tabList(boolean enabled);

    void time(boolean enabled);

    void weather(boolean enabled);
}
