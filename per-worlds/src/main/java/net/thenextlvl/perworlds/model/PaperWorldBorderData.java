package net.thenextlvl.perworlds.model;

import io.papermc.paper.math.Position;
import net.thenextlvl.perworlds.data.WorldBorderData;

public class PaperWorldBorderData implements WorldBorderData {
    private double x;
    private double z;
    private double size;
    private double damageAmount;
    private double damageBuffer;
    private int warningDistance;
    private int warningTime;

    @Override
    public double centerX() {
        return x;
    }

    @Override
    public double centerZ() {
        return z;
    }

    public void centerX(double x) {
        this.x = x;
    }

    public void centerZ(double z) {
        this.z = z;
    }

    @Override
    public void center(double x, double z) {
        this.x = x;
        this.z = z;
    }

    @Override
    public void center(Position position) {
        center(position.x(), position.z());
    }

    @Override
    public double damageAmount() {
        return damageAmount;
    }

    @Override
    public void damageAmount(double damage) {
        this.damageAmount = damage;
    }

    @Override
    public double damageBuffer() {
        return damageBuffer;
    }

    @Override
    public void damageBuffer(double blocks) {
        this.damageBuffer = blocks;
    }

    @Override
    public double size() {
        return size;
    }

    @Override
    public void size(double size) {
        this.size = size;
    }

    @Override
    public int warningDistance() {
        return warningDistance;
    }

    @Override
    public void warningDistance(int blocks) {
        this.warningDistance = blocks;
    }

    @Override
    public int warningTime() {
        return warningTime;
    }

    @Override
    public void warningTime(int seconds) {
        this.warningTime = seconds;
    }
}
