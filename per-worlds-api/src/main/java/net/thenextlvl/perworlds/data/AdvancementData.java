package net.thenextlvl.perworlds.data;

import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.Date;
import java.util.Set;

/**
 * The individual data of an advancement.
 */
@NullMarked
public interface AdvancementData extends AdvancementProgress {
    /**
     * Retrieves the associated advancement.
     *
     * @return the {@link Advancement} associated with this data
     */
    Advancement getAdvancement();

    /**
     * Check if all criteria for this advancement have been met.
     *
     * @return {@code true} if this advancement is done, otherwise {@code false}
     */
    boolean isDone();

    /**
     * Mark the specified criteria as awarded at the current time.
     *
     * @param criteria the criteria to mark
     * @return {@code true} if awarded, {@code false} if criteria does not exist or already awarded.
     */
    boolean awardCriteria(String criteria);

    /**
     * Mark the specified criteria as uncompleted.
     *
     * @param criteria the criteria to mark
     * @return {@code true} if removed, {@code false} if criteria does not exist or not awarded
     */
    boolean revokeCriteria(String criteria);

    /**
     * Get the time the specified criteria was awarded.
     *
     * @param criteria the criteria to check
     * @return time awarded or {@code null} if unawarded or criteria does not exist
     */
    @Nullable
    Instant getTimeAwarded(String criteria);

    /**
     * Sets the date the specified criteria was awarded.
     *
     * @param criteria the criteria to set the date for
     * @param instant  the time to associate with the awarded criteria
     * @return {@code true} if the date was successfully set, {@code false} if the criteria does not exist or is already awarded
     */
    boolean setTimeAwarded(String criteria, Instant instant);

    /**
     * Get the date the specified criteria was awarded.
     *
     * @param criteria the criteria to check
     * @return date awarded or {@code null} if unawarded or criteria does not exist
     * @deprecated use {@link #getTimeAwarded(String)}
     */
    @Deprecated(forRemoval = true, since = "0.2.6")
    default @Nullable Date getDateAwarded(String criteria) {
        var timeAwarded = getTimeAwarded(criteria);
        return timeAwarded != null ? Date.from(timeAwarded) : null;
    }

    /**
     * Sets the date the specified criteria was awarded.
     *
     * @param criteria the criteria to set the date for
     * @param date     the date to associate with the awarded criteria
     * @return {@code true} if the date was successfully set, {@code false} if the criteria does not exist or is already awarded
     * @deprecated use {@link #setTimeAwarded(String, Instant)}
     */
    @Deprecated(forRemoval = true, since = "0.2.6")
    default boolean setDateAwarded(String criteria, Date date) {
        return setTimeAwarded(criteria, date.toInstant());
    }

    /**
     * Get the criteria which have not been awarded.
     *
     * @return an unmodifiable set of the remaining criteria
     */
    @Unmodifiable
    Set<String> getRemainingCriteria();

    /**
     * Gets the criteria which have been awarded.
     *
     * @return an unmodifiable set of the awarded criteria
     */
    @Unmodifiable
    Set<String> getAwardedCriteria();

    /**
     * Determines whether the advancement data should be serialized.
     *
     * @return {@code true} if the advancement data should be serialized, otherwise {@code false}
     */
    boolean shouldSerialize();
}
