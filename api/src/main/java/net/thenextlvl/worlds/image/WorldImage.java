package net.thenextlvl.worlds.image;

import com.google.gson.JsonObject;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.jetbrains.annotations.Nullable;

public interface WorldImage {
    /**
     * Retrieves the name of the WorldImage.
     *
     * @return The name of the WorldImage.
     */
    String name();

    /**
     * Sets the name of the WorldImage.
     *
     * @param name The new name for the WorldImage.
     * @return The updated WorldImage instance.
     */
    WorldImage name(String name);

    /**
     * Retrieves the NamespacedKey associated with the WorldImage.
     *
     * @return The NamespacedKey associated with the WorldImage.
     */
    NamespacedKey key();

    /**
     * Sets the NamespacedKey associated with the WorldImage.
     *
     * @param key The NamespacedKey to set.
     * @return The updated WorldImage instance.
     */
    WorldImage key(NamespacedKey key);

    /**
     * Retrieves the settings associated with the WorldImage.
     *
     * @return The settings associated with the WorldImage, or null if no settings are specified.
     */
    @Nullable
    JsonObject settings();

    /**
     * Set the settings associated with the WorldImage.
     *
     * @param object The JsonObject representing the settings to set.
     * @return The updated WorldImage instance.
     */
    WorldImage settings(@Nullable JsonObject object);

    /**
     * Retrieves the Generator associated with the WorldImage.
     *
     * @return The Generator associated with the WorldImage, or null if no Generator is specified.
     */
    @Nullable
    Generator generator();

    /**
     * Sets the world generator for the WorldImage.
     *
     * @param generator The Generator to set as the world generator.
     * @return The updated WorldImage instance.
     */
    WorldImage generator(@Nullable Generator generator);

    /**
     * Retrieves the DeletionType associated with the WorldImage.
     *
     * @return The DeletionType associated with the WorldImage, or null if no DeletionType is specified.
     */
    @Nullable
    DeletionType deletionType();

    /**
     * Retrieves the DeletionType associated with the WorldImage.
     *
     * @param deletionType The DeletionType to associate with the WorldImage.
     * @return The updated WorldImage instance.
     */
    WorldImage deletionType(@Nullable DeletionType deletionType);


    /**
     * Retrieves the environment of the WorldImage.
     *
     * @return The environment of the WorldImage.
     */
    World.Environment environment();

    /**
     * Retrieves the environment of the WorldImage.
     *
     * @param environment The environment to set for the WorldImage.
     * @return The updated WorldImage instance.
     */
    WorldImage environment(World.Environment environment);

    /**
     * Retrieves the world type of the WorldImage.
     *
     * @return The world type of the WorldImage.
     */
    WorldType worldType();

    /**
     * Sets the world type of the WorldImage.
     *
     * @param worldType The world type to set for the WorldImage.
     * @return The updated WorldImage instance.
     */
    WorldImage worldType(WorldType worldType);

    /**
     * Returns whether the WorldImage has auto save enabled or not.
     *
     * @return true if auto save is enabled, false otherwise.
     */
    boolean autoSave();

    /**
     * Enables or disables the auto save feature for the WorldImage.
     *
     * @param autoSave true to enable auto save, false to disable auto save.
     * @return The updated WorldImage instance.
     */
    WorldImage autoSave(boolean autoSave);

    /**
     * Checks if structures are generated in the world.
     *
     * @return true if structures are generated, false otherwise.
     */
    boolean generateStructures();

    /**
     * Generates structures in the world based on the given flag.
     *
     * @param generateStructures Specifies whether structures should be generated in the world.
     *                           Set to true to generate structures, false otherwise.
     * @return The updated WorldImage instance.
     */
    WorldImage generateStructures(boolean generateStructures);

    /**
     * Checks if the WorldImage is set to hardcore mode.
     *
     * @return true if the WorldImage is set to hardcore mode, false otherwise.
     */
    boolean hardcore();

    /**
     * Sets the hardcore mode for the WorldImage.
     *
     * @param hardcore true to enable hardcore mode, false to disable hardcore mode.
     * @return The updated WorldImage instance.
     */
    WorldImage hardcore(boolean hardcore);

    /**
     * Returns whether the WorldImage is set to load on start.
     *
     * @return true if the WorldImage is set to load on start, false otherwise.
     */
    boolean loadOnStart();

    /**
     * Sets whether the WorldImage should be loaded on start.
     *
     * @param loadOnStart true if the WorldImage should be loaded on start, false otherwise.
     * @return The updated WorldImage instance.
     */
    WorldImage loadOnStart(boolean loadOnStart);

    /**
     * Retrieves the seed of the WorldImage.
     *
     * @return The seed of the WorldImage.
     */
    long seed();

    /**
     * Sets the seed of the WorldImage.
     *
     * @param seed The seed to set for the WorldImage.
     * @return The updated WorldImage instance.
     */
    WorldImage seed(long seed);

    /**
     * Builds and returns a World object based on the configuration of the WorldImage.
     *
     * @return The built World object, or null if the build fails.
     */
    @Nullable
    World build();
}
