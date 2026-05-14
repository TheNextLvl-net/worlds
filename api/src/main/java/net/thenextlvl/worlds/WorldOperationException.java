package net.thenextlvl.worlds;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.Translatable;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;

// todo: replace with a more robust solution
@ApiStatus.Internal
public final class WorldOperationException extends RuntimeException {
    private final Reason reason;
    private final @Nullable String key;
    private final @Nullable Path path;
    private final @Nullable String backup;

    public WorldOperationException(final Reason reason) {
        this(reason, null);
    }

    public WorldOperationException(final Reason reason, @Nullable final Throwable cause) {
        this(reason, cause, null, null, null);
    }

    private WorldOperationException(
            final Reason reason,
            @Nullable final Throwable cause,
            @Nullable final String key,
            @Nullable final Path path,
            @Nullable final String backup
    ) {
        super(cause);
        this.reason = reason;
        this.key = key;
        this.path = path;
        this.backup = backup;
    }

    public Reason reason() {
        return reason;
    }

    public @Nullable String key() {
        return key;
    }

    public @Nullable Path path() {
        return path;
    }

    public @Nullable String backup() {
        return backup;
    }

    public WorldOperationException key(final String key) {
        return new WorldOperationException(reason, getCause(), key, path, backup);
    }

    public WorldOperationException key(final Key key) {
        return key(key.asString());
    }

    public WorldOperationException path(final Path path) {
        return new WorldOperationException(reason, getCause(), key, path, backup);
    }

    public WorldOperationException backup(final String backup) {
        return new WorldOperationException(reason, getCause(), key, path, backup);
    }

    public enum Reason implements Translatable {
        WORLD_KEY_EXISTS("world.failure.key-exists"),
        WORLD_NAME_EXISTS("world.failure.name-exists"),
        WORLD_PATH_EXISTS("world.failure.path-exists"),
        TARGET_PATH_IS_FILE("world.failure.path-file"),
        WORLD_DIRECTORY_LOADED("world.failure.directory-loaded"),
        DUPLICATE_METADATA_UUID("world.failure.duplicate-uuid"),
        MISSING_LEVEL_STEM("world.failure.missing-level-stem"),
        LEGACY_MIGRATION_FAILED("world.failure.legacy-migration"),
        GENERATOR_PLUGIN_MISSING("world.failure.generator-missing"),
        GENERATOR_PLUGIN_DISABLED("world.failure.generator-disabled"),
        GENERATOR_PLUGIN_HAS_NO_GENERATOR("world.failure.generator-empty"),
        BACKUP_NAME_EXISTS("world.failure.backup-exists"),
        BACKUP_DIRECTORY_FAILED("world.failure.backup-directory"),
        BACKUP_WRITE_FAILED("world.failure.backup-write"),
        BACKUP_READ_FAILED("world.failure.backup-read"),
        BACKUP_RESTORE_FAILED("world.backup.restore.failed"),
        SAVE_FAILED("world.failure.save"),
        EVENT_CANCELLED("world.failure.cancelled"),
        WORLD_NOT_FOUND("world.failure.not-found"),
        DELETE_REQUIRES_SCHEDULING("world.delete.disallowed"),
        REGENERATE_REQUIRES_SCHEDULING("world.regenerate.disallowed"),
        BACKUP_RESTORE_REQUIRES_SCHEDULING("world.backup.restore.disallowed"),
        UNLOAD_FAILED("world.unload.failed"),
        INTERNAL_ERROR("world.failure.internal");

        private final String translationKey;

        Reason(final String key) {
            this.translationKey = key;
        }

        @Override
        public String translationKey() {
            return translationKey;
        }
    }
}
