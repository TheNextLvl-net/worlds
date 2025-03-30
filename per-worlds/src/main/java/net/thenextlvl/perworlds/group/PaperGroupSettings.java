package net.thenextlvl.perworlds.group;

import net.thenextlvl.perworlds.GroupSettings;

public class PaperGroupSettings implements GroupSettings {
    private boolean absorption = true;
    private boolean advancements = true;
    private boolean arrowsInBody = true;
    private boolean attributes = false;
    private boolean beeStingersInBody = true;
    private boolean chat = false;
    private boolean enabled = true;
    private boolean endCredits = true;
    private boolean enderChest = true;
    private boolean exhaustion = true;
    private boolean experience = true;
    private boolean fallDistance = true;
    private boolean fireTicks = true;
    private boolean flyState = true;
    private boolean foodLevel = true;
    private boolean freezeTicks = true;
    private boolean gameMode = true;
    private boolean gameRules = true;
    private boolean gliding = true;
    private boolean health = true;
    private boolean hotbarSlot = true;
    private boolean inventory = true;
    private boolean invulnerable = false;
    private boolean lastDeathLocation = true;
    private boolean lastLocation = true;
    private boolean portalCooldown = true;
    private boolean potionEffects = true;
    private boolean recipes = true;
    private boolean remainingAir = true;
    private boolean respawnLocation = true;
    private boolean saturation = true;
    private boolean score = true;
    private boolean statistics = true;
    private boolean tabList = false;
    private boolean time = true;
    private boolean velocity = true;
    private boolean wardenSpawnTracker = true;
    private boolean weather = true;

    @Override
    public boolean absorption() {
        return absorption;
    }

    @Override
    public boolean advancements() {
        return advancements;
    }

    @Override
    public boolean arrowsInBody() {
        return arrowsInBody;
    }

    @Override
    public boolean attributes() {
        return attributes;
    }

    @Override
    public boolean beeStingersInBody() {
        return beeStingersInBody;
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
    public boolean statistics() {
        return statistics;
    }

    @Override
    public boolean enabled() {
        return enabled;
    }

    @Override
    public boolean endCredits() {
        return endCredits;
    }

    @Override
    public boolean enderChest() {
        return enderChest;
    }

    @Override
    public boolean exhaustion() {
        return exhaustion;
    }

    @Override
    public boolean experience() {
        return experience;
    }

    @Override
    public boolean fallDistance() {
        return fallDistance;
    }

    @Override
    public boolean fireTicks() {
        return fireTicks;
    }

    @Override
    public boolean flyState() {
        return flyState;
    }

    @Override
    public boolean foodLevel() {
        return foodLevel;
    }

    @Override
    public boolean freezeTicks() {
        return freezeTicks;
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
    public boolean gliding() {
        return gliding;
    }

    @Override
    public boolean health() {
        return health;
    }

    @Override
    public boolean hotbarSlot() {
        return hotbarSlot;
    }

    @Override
    public boolean inventory() {
        return inventory;
    }

    @Override
    public boolean invulnerable() {
        return invulnerable;
    }

    @Override
    public boolean lastDeathLocation() {
        return lastDeathLocation;
    }

    @Override
    public boolean lastLocation() {
        return lastLocation;
    }

    @Override
    public boolean portalCooldown() {
        return portalCooldown;
    }

    @Override
    public boolean potionEffects() {
        return potionEffects;
    }

    @Override
    public boolean recipes() {
        return recipes;
    }

    @Override
    public boolean remainingAir() {
        return remainingAir;
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
    public boolean velocity() {
        return velocity;
    }

    @Override
    public boolean wardenSpawnTracker() {
        return wardenSpawnTracker;
    }

    @Override
    public boolean weather() {
        return weather;
    }

    @Override
    public void absorption(boolean enabled) {
        this.absorption = enabled;
    }

    @Override
    public void advancements(boolean enabled) {
        this.advancements = enabled;
    }

    @Override
    public void arrowsInBody(boolean enabled) {
        this.arrowsInBody = enabled;
    }

    @Override
    public void attributes(boolean enabled) {
        this.attributes = enabled;
    }

    @Override
    public void beeStingersInBody(boolean enabled) {
        this.beeStingersInBody = enabled;
    }

    @Override
    public void chat(boolean enabled) {
        this.chat = enabled;
    }

    @Override
    public void score(boolean enabled) {
        this.score = enabled;
    }

    @Override
    public void statistics(boolean enabled) {
        this.statistics = enabled;
    }

    @Override
    public void enabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void endCredits(boolean enabled) {
        this.endCredits = enabled;
    }

    @Override
    public void enderChest(boolean enabled) {
        this.enderChest = enabled;
    }

    @Override
    public void exhaustion(boolean enabled) {
        this.exhaustion = enabled;
    }

    @Override
    public void experience(boolean enabled) {
        this.experience = enabled;
    }

    @Override
    public void fallDistance(boolean enabled) {
        this.fallDistance = enabled;
    }

    @Override
    public void fireTicks(boolean enabled) {
        this.fireTicks = enabled;
    }

    @Override
    public void flyState(boolean enabled) {
        this.flyState = enabled;
    }

    @Override
    public void foodLevel(boolean enabled) {
        this.foodLevel = enabled;
    }

    @Override
    public void freezeTicks(boolean enabled) {
        this.freezeTicks = enabled;
    }

    @Override
    public void gameMode(boolean enabled) {
        this.gameMode = enabled;
    }

    @Override
    public void gameRules(boolean enabled) {
        this.gameRules = enabled;
    }

    @Override
    public void gliding(boolean enabled) {
        this.gliding = enabled;
    }

    @Override
    public void health(boolean enabled) {
        this.health = enabled;
    }

    @Override
    public void hotbarSlot(boolean enabled) {
        this.hotbarSlot = enabled;
    }

    @Override
    public void inventory(boolean enabled) {
        this.inventory = enabled;
    }

    @Override
    public void invulnerable(boolean enabled) {
        this.invulnerable = enabled;
    }

    @Override
    public void lastDeathLocation(boolean enabled) {
        this.lastDeathLocation = enabled;
    }

    @Override
    public void lastLocation(boolean enabled) {
        this.lastLocation = enabled;
    }

    @Override
    public void portalCooldown(boolean enabled) {
        this.portalCooldown = enabled;
    }

    @Override
    public void potionEffects(boolean enabled) {
        this.potionEffects = enabled;
    }

    @Override
    public void recipes(boolean enabled) {
        this.recipes = enabled;
    }

    @Override
    public void remainingAir(boolean enabled) {
        this.remainingAir = enabled;
    }

    @Override
    public void respawnLocation(boolean enabled) {
        this.respawnLocation = enabled;
    }

    @Override
    public void saturation(boolean enabled) {
        this.saturation = enabled;
    }

    @Override
    public void tabList(boolean enabled) {
        this.tabList = enabled;
    }

    @Override
    public void time(boolean enabled) {
        this.time = enabled;
    }

    @Override
    public void velocity(boolean enabled) {
        this.velocity = enabled;
    }

    @Override
    public void wardenSpawnTracker(boolean enabled) {
        this.wardenSpawnTracker = enabled;
    }

    @Override
    public void weather(boolean enabled) {
        this.weather = enabled;
    }
}
