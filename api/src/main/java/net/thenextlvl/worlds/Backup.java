package net.thenextlvl.worlds;

import org.jetbrains.annotations.Contract;

import java.time.Instant;

public interface Backup {
    @Contract(pure = true)
    String name();

    @Contract(pure = true)
    Instant createdAt();

    @Contract(pure = true)
    long size();
}
