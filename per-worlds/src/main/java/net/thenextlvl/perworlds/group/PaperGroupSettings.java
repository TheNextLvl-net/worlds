package net.thenextlvl.perworlds.group;

import net.thenextlvl.perworlds.GroupSettings;

public class PaperGroupSettings implements GroupSettings {
    private boolean attributes = false;
    private boolean chat = false;
    private boolean enabled = true;
    private boolean experience = true;
    private boolean foodLevel = true;
    private boolean gameMode = true;
    private boolean gameRules = true;
    private boolean health = true;
    private boolean inventory = true;
    private boolean potionEffects = true;
    private boolean respawnLocation = true;
    private boolean saturation = true;
    private boolean score = true;
    private boolean tabList = false;
    private boolean time = true;
    private boolean weather = true;

    @Override
    public boolean attributes() {
        return attributes;
    }

    @Override
    public boolean chat() {
        return chat;
    }

    @Override
    public boolean score() {
        return score;
    }

    @Override
    public boolean enabled() {
        return enabled;
    }

    @Override
    public boolean experience() {
        return experience;
    }

    @Override
    public boolean foodLevel() {
        return foodLevel;
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
    public boolean health() {
        return health;
    }

    @Override
    public boolean inventory() {
        return inventory;
    }

    @Override
    public boolean potionEffects() {
        return potionEffects;
    }

    @Override
    public boolean respawnLocation() {
        return respawnLocation;
    }

    @Override
    public boolean saturation() {
        return saturation;
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
    public void attributes(boolean attributes) {
        this.attributes = attributes;
    }

    @Override
    public void chat(boolean chat) {
        this.chat = chat;
    }

    @Override
    public void score(boolean score) {
        this.score = score;
    }

    @Override
    public void enabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void experience(boolean experience) {
        this.experience = experience;
    }

    @Override
    public void foodLevel(boolean foodLevel) {
        this.foodLevel = foodLevel;
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
    public void health(boolean health) {
        this.health = health;
    }

    @Override
    public void inventory(boolean inventory) {
        this.inventory = inventory;
    }

    @Override
    public void potionEffects(boolean potionEffects) {
        this.potionEffects = potionEffects;
    }

    @Override
    public void respawnLocation(boolean respawnLocation) {
        this.respawnLocation = respawnLocation;
    }

    @Override
    public void saturation(boolean saturation) {
        this.saturation = saturation;
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
