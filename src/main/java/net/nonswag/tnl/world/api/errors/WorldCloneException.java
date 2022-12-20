package net.nonswag.tnl.world.api.errors;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class WorldCloneException extends Exception {

    public WorldCloneException() {
    }

    public WorldCloneException(String message) {
        super(message);
    }

    public WorldCloneException(String message, Throwable cause) {
        super(message, cause);
    }

    public WorldCloneException(Throwable cause) {
        super(cause);
    }

    public WorldCloneException(@Nullable String message, @Nullable Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
