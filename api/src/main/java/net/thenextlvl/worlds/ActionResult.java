package net.thenextlvl.worlds;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

@ApiStatus.NonExtendable
public interface ActionResult<T> {
    @Nullable
    @Contract(pure = true)
    T result();

    @Contract(pure = true)
    Status status();

    @Contract(pure = true)
    default boolean isSuccess() {
        return status() == Status.SUCCESS || status() == Status.SCHEDULED;
    }

    enum Status {
        SUCCESS,
        SCHEDULED,
        REQUIRES_SCHEDULING,
        UNLOAD_FAILED,
        FAILED
    }
}
