package net.thenextlvl.worlds;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public sealed interface ActionResult<T> permits SimpleActionResult {
    @Contract(pure = true)
    Optional<T> result();

    @Contract(pure = true)
    Status status();

    @Contract(pure = true)
    default boolean isSuccess() {
        return status() == Status.SUCCESS || status() == Status.SCHEDULED;
    }

    static <T> ActionResult<T> result(@Nullable final T result, final Status status) {
        return new SimpleActionResult<T>(result, status);
    }

    enum Status {
        SUCCESS,
        SCHEDULED,
        REQUIRES_SCHEDULING,
        UNLOAD_FAILED,
        FAILED
    }
}
