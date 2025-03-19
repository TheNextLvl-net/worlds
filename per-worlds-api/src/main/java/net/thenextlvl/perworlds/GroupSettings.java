package net.thenextlvl.perworlds;

public interface GroupSettings {
    boolean chat();

    boolean enabled();

    boolean gameMode();

    boolean gameRules();

    boolean inventory();

    boolean tabList();

    boolean time();

    boolean weather();

    void chat(boolean chat);

    void enabled(boolean enabled);

    void gameMode(boolean gameMode);

    void gameRules(boolean gameRules);

    void inventory(boolean inventory);

    void tabList(boolean tabList);

    void time(boolean time);

    void weather(boolean weather);
}
