package net.thenextlvl.worlds;

import org.jspecify.annotations.Nullable;

import java.util.Optional;

final class SimpleActionResult<T> implements ActionResult<T> {
    private final @Nullable T result;
    private final Status status;

    public SimpleActionResult(@Nullable final T result, final Status status) {
        this.result = result;
        this.status = status;
    }

    @Override
    public Optional<T> result() {
        return Optional.ofNullable(result);
    }

    @Override
    public Status status() {
        return status;
    }
}
