package net.thenextlvl.perworlds.model;

import com.google.common.base.Preconditions;
import io.papermc.paper.math.Position;
import net.thenextlvl.perworlds.data.WorldBorderData;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PaperWorldBorderData implements WorldBorderData {
    /**
     * @see net.minecraft.world.level.border.WorldBorder#MAX_SIZE
     */
    @SuppressWarnings("JavadocReference")
    private static final double MAX_SIZE = 5.999997E7F;
    /**
     * @see net.minecraft.world.level.border.WorldBorder#MAX_CENTER_COORDINATE
     */
    @SuppressWarnings("JavadocReference")
    private static final double MAX_CENTER_COORDINATE = 2.9999984E7;

    private double x = 0D;
    private double z = 0D;
    private double size = 5.9999968E7;
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
    public void duration(long duration) {
        Preconditions.checkArgument(duration >= 0, "time cannot be lower than 0");
        this.duration = duration;
    }

    public void centerX(double x) {
        this.x = x;
    }

    public void centerZ(double z) {
        this.z = z;
    }

    @Override
    public void center(double x, double z) {
        Preconditions.checkArgument(Math.abs(x) <= MAX_CENTER_COORDINATE, "x coordinate cannot be outside +- %s", MAX_CENTER_COORDINATE);
        Preconditions.checkArgument(Math.abs(z) <= MAX_CENTER_COORDINATE, "z coordinate cannot be outside +- %s", MAX_CENTER_COORDINATE);
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
        Preconditions.checkArgument(size >= 1.0D && size <= MAX_SIZE, "size must be between 1.0D and %s", MAX_SIZE);
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
