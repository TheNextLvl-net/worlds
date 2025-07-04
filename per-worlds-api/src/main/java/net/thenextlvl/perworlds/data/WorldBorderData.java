package net.thenextlvl.perworlds.data;

import io.papermc.paper.math.Position;
import org.bukkit.WorldBorder;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.TimeUnit;

@NullMarked
public interface WorldBorderData {
    /**
     * Retrieves the center X-coordinate of the world's border.
     *
     * @return the X-coordinate of the center
     * @see WorldBorder#getCenter()
     */
    double centerX();

    /**
     * Retrieves the center Z-coordinate of the world's border.
     *
     * @return the Z-coordinate of the center
     * @see WorldBorder#getCenter()
     */
    double centerZ();

    /**
     * Retrieves the current transition duration of the world's border.
     *
     * @return the duration in milliseconds
     * @see WorldBorder#setSize(double, TimeUnit, long)
     */
    long duration();

    /**
     * Sets the transition duration in milliseconds.
     *
     * @param duration the duration to be set, in milliseconds
     * @return the current WorldBorderData instance for chaining
     * @throws IllegalArgumentException if the duration is less than 0
     * @see WorldBorder#setSize(double, TimeUnit, long)
     */
    WorldBorderData duration(long duration);

    /**
     * Sets the new border center x-coordinate.
     *
     * @param x The new X-coordinate of the border center.
     * @return the current WorldBorderData instance for chaining
     * @throws IllegalArgumentException if the absolute value of {@code x}
     *                                  is higher than {@link #getMaxCenterCoordinate()}
     */
    WorldBorderData centerX(double x);

    /**
     * Sets the new border center z-coordinate.
     *
     * @param z The new Z-coordinate of the border center.
     * @return the current WorldBorderData instance for chaining
     * @throws IllegalArgumentException if the absolute value of {@code z}
     *                                  is higher than {@link #getMaxCenterCoordinate()}
     */
    WorldBorderData centerZ(double z);

    /**
     * Sets the new border center.
     *
     * @param x The new X-coordinate of the border center.
     * @param z The new Z-coordinate of the border center.
     * @return the current WorldBorderData instance for chaining
     * @throws IllegalArgumentException if the absolute value of {@code x} or {@code z}
     *                                  is higher than {@link #getMaxCenterCoordinate()}
     */
    WorldBorderData center(double x, double z);

    /**
     * Sets the new border center.
     *
     * @param position The new position of the border center.
     * @return the current WorldBorderData instance for chaining
     * @throws IllegalArgumentException if the absolute value of {@link Position#x()} or {@link Position#z()}
     *                                  is higher than {@link #getMaxCenterCoordinate()}
     */
    WorldBorderData center(Position position);

    /**
     * Gets the current border damage amount.
     *
     * @return The current border damage amount.
     * @see WorldBorder#getDamageAmount()
     */
    double damageAmount();

    /**
     * Sets the amount of damage a player takes when outside the border plus the border buffer.
     *
     * @param damage The amount of damage.
     * @return the current WorldBorderData instance for chaining
     * @see WorldBorder#setDamageAmount(double)
     */
    WorldBorderData damageAmount(double damage);

    /**
     * Gets the current border damage buffer.
     *
     * @return The current border damage buffer.
     * @see WorldBorder#getDamageBuffer()
     */
    double damageBuffer();

    /**
     * Sets the number of blocks a player may safely be outside the border before taking damage.
     *
     * @param blocks The number of blocks.
     * @return the current WorldBorderData instance for chaining
     * @see WorldBorder#setDamageBuffer(double)
     */
    WorldBorderData damageBuffer(double blocks);

    /**
     * Gets the current side length of the border.
     *
     * @return The current side length of the border.
     * @see WorldBorder#getSize()
     */
    double size();

    /**
     * Sets the border to a square region with the specified side length in blocks.
     *
     * @param size The new size of the border.
     * @return the current WorldBorderData instance for chaining
     * @throws IllegalArgumentException if {@code size} is less than {@link #getMinSize()} or greater than {@link #getMaxSize()}
     * @see WorldBorder#setSize(double)
     */
    WorldBorderData size(double size);

    /**
     * Gets the current border warning distance.
     *
     * @return The current border warning distance.
     * @see WorldBorder#getWarningDistance()
     */
    int warningDistance();

    /**
     * Sets the warning distance that causes the screen to be tinted red when the player is within the specified number of blocks from the border.
     *
     * @param blocks The distance in blocks.
     * @return the current WorldBorderData instance for chaining
     * @see WorldBorder#setWarningDistance(int)
     */
    WorldBorderData warningDistance(int blocks);

    /**
     * Gets the current border warning time in seconds.
     *
     * @return The current border warning time in seconds.
     * @see WorldBorder#getWarningTime()
     */
    int warningTime();

    /**
     * Sets the warning time that causes the screen to be tinted red when a contracting border will reach the player within the specified time.
     *
     * @param seconds The amount of time in seconds.
     * @return the current WorldBorderData instance for chaining
     * @see WorldBorder#setWarningTime(int)
     */
    WorldBorderData warningTime(int seconds);
    
    /**
     * Resets the current WorldBorderData instance to its default state.
     *
     * @return the current WorldBorderData instance for chaining
     */
    WorldBorderData reset();

    /**
     * Retrieves the maximum allowed size of the border.
     *
     * @return the maximum size of the border
     * @see WorldBorder#getMaxSize()
     */
    double getMaxSize();

    /**
     * Retrieves the minimum allowed size of the border.
     *
     * @return the minimum size of the border
     */
    double getMinSize();

    /**
     * Retrieves the maximum allowed absolute value for the center coordinates of the border.
     *
     * @return the maximum center coordinate value that can be set for the border
     * @see WorldBorder#getMaxCenterCoordinate()
     */
    double getMaxCenterCoordinate();
}
