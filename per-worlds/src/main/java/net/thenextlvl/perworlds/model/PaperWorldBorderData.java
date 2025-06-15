package net.thenextlvl.perworlds.model;

import com.google.common.base.Preconditions;
import io.papermc.paper.math.Position;
import net.thenextlvl.perworlds.data.WorldBorderData;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PaperWorldBorderData implements WorldBorderData {
    private double x = 0D;
    private double z = 0D;
    private double size = getMaxSize();
    private double damageAmount = 0.2D;
    private double damageBuffer = 5.0D;
    private int warningDistance = 5;
    private int warningTime = 15;
    private long duration = 0;

    @Override
    public double centerX() {
        return x;
    }

    @Override
    public double centerZ() {
        return z;
    }

    @Override
    public long duration() {
        return duration;
    }

    @Override
    public PaperWorldBorderData duration(long duration) {
        Preconditions.checkArgument(duration >= 0, "time cannot be lower than 0 but got %s", duration);
        this.duration = duration;
        return this;
    }

    public PaperWorldBorderData centerX(double x) {
        Preconditions.checkArgument(Math.abs(x) <= getMaxCenterCoordinate(), "x coordinate cannot be outside +- %s but got %s", getMaxCenterCoordinate(), x);
        this.x = x;
        return this;
    }

    public PaperWorldBorderData centerZ(double z) {
        Preconditions.checkArgument(Math.abs(z) <= getMaxCenterCoordinate(), "z coordinate cannot be outside +- %s but got %s", getMaxCenterCoordinate(), z);
        this.z = z;
        return this;
    }

    @Override
    public PaperWorldBorderData center(double x, double z) {
        centerX(x);
        centerZ(z);
        return this;
    }

    @Override
    public PaperWorldBorderData center(Position position) {
        return center(position.x(), position.z());
    }

    @Override
    public double damageAmount() {
        return damageAmount;
    }

    @Override
    public PaperWorldBorderData damageAmount(double damage) {
        this.damageAmount = damage;
        return this;
    }

    @Override
    public double damageBuffer() {
        return damageBuffer;
    }

    @Override
    public PaperWorldBorderData damageBuffer(double blocks) {
        this.damageBuffer = blocks;
        return this;
    }

    @Override
    public double size() {
        return size;
    }

    @Override
    public PaperWorldBorderData size(double size) {
        Preconditions.checkArgument(size >= getMinSize() && size <= getMaxSize(), "size must be between %s and %s but got %s", getMinSize(), getMaxSize(), size);
        this.size = size;
        return this;
    }

    @Override
    public int warningDistance() {
        return warningDistance;
    }

    @Override
    public PaperWorldBorderData warningDistance(int blocks) {
        this.warningDistance = blocks;
        return this;
    }

    @Override
    public int warningTime() {
        return warningTime;
    }

    @Override
    public PaperWorldBorderData warningTime(int seconds) {
        this.warningTime = seconds;
        return this;
    }

    @Override
    public WorldBorderData reset() {
        return center(0, 0)
                .size(getMaxSize())
                .damageAmount(0.2D)
                .damageBuffer(5.0D)
                .warningDistance(5)
                .warningTime(15)
                .duration(0);
    }

    /**
     * @see net.minecraft.world.level.border.WorldBorder#MAX_SIZE
     */
    @Override
    @SuppressWarnings("JavadocReference")
    public double getMaxSize() {
        return 5.999997E7F;
    }

    @Override
    public double getMinSize() {
        return 1;
    }

    /**
     * @see net.minecraft.world.level.border.WorldBorder#MAX_CENTER_COORDINATE
     */
    @Override
    @SuppressWarnings("JavadocReference")
    public double getMaxCenterCoordinate() {
        return 2.9999984E7;
    }
}
