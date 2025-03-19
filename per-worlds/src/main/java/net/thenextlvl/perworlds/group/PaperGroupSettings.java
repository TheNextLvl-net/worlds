package net.thenextlvl.perworlds.group;

import net.thenextlvl.perworlds.GroupSettings;

public class PaperGroupSettings implements GroupSettings {
    private boolean chat = false;
    private boolean enabled = true;
    private boolean gameMode = true;
    private boolean gameRules = true;
    private boolean inventory = true;
    private boolean tabList = false;
    private boolean time = true;
    private boolean weather = true;

    @Override
    public boolean chat() {
        return chat;
    }

    @Override
    public boolean enabled() {
        return enabled;
    }

    @Override
    public boolean gameMode() {
        return gameMode;
    }

    @Override
    public boolean gameRules() {
        return gameRules;
    }

    @Override
    public boolean inventory() {
        return inventory;
    }

    @Override
    public boolean tabList() {
        return tabList;
    }

    @Override
    public boolean time() {
        return time;
    }

    @Override
    public boolean weather() {
        return weather;
    }

    @Override
    public void chat(boolean chat) {
        this.chat = chat;
    }

    @Override
    public void enabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void gameMode(boolean gameMode) {
        this.gameMode = gameMode;
    }

    @Override
    public void gameRules(boolean gameRules) {
        this.gameRules = gameRules;
    }

    @Override
    public void inventory(boolean inventory) {
        this.inventory = inventory;
    }

    @Override
    public void tabList(boolean tabList) {
        this.tabList = tabList;
    }

    @Override
    public void time(boolean time) {
        this.time = time;
    }

    @Override
    public void weather(boolean weather) {
        this.weather = weather;
    }
}
