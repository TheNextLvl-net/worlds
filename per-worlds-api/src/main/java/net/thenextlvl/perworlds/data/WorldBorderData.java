package net.thenextlvl.perworlds.data;

import io.papermc.paper.math.Position;
import org.bukkit.WorldBorder;

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
     * Sets the new border center.
     *
     * @param x The new X-coordinate of the border center.
     * @param z The new Z-coordinate of the border center.
     *
     * @throws IllegalArgumentException if the absolute value of {@code x} or {@code z}
     * is higher than {@link WorldBorder#getMaxCenterCoordinate()}
     */
    void center(double x, double z);

    /**
     * Sets the new border center.
     *
     * @param position The new position of the border center.
     *
     * @throws IllegalArgumentException if the absolute value of {@link Position#x()} or {@link Position#z()}
     * is higher than {@link WorldBorder#getMaxCenterCoordinate()}
     */
    void center(Position position);

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
     * @see WorldBorder#setDamageAmount(double)
     */
    void damageAmount(double damage);

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
     * @see WorldBorder#setDamageBuffer(double)
     */
    void damageBuffer(double blocks);

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
     *
     * @throws IllegalArgumentException if {@code size} is less than 1.0D or greater than {@link WorldBorder#getMaxSize()}
     * @see WorldBorder#setSize(double)
     */
    void size(double size);

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
     * @see WorldBorder#setWarningDistance(int)
     */
    void warningDistance(int blocks);

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
     * @see WorldBorder#setWarningTime(int)
     */
    void warningTime(int seconds);
}
