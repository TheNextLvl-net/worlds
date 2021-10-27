package net.nonswag.tnl.world.api.errors;

import net.nonswag.tnl.core.api.errors.TNLException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WorldCloneException extends TNLException {

    public WorldCloneException() {
    }

    public WorldCloneException(@Nonnull String message) {
        super(message);
    }

    public WorldCloneException(@Nonnull String message, @Nonnull Throwable cause) {
        super(message, cause);
    }

    public WorldCloneException(@Nonnull Throwable cause) {
        super(cause);
    }

    public WorldCloneException(@Nullable String message, @Nullable Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
