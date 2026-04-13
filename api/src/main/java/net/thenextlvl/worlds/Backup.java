package net.thenextlvl.worlds;

import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.Contract;

import java.time.Instant;

public interface Backup extends Keyed {
    @Contract(pure = true)
    BackupProvider provider();
    
    @Contract(pure = true)
    String name();

    @Contract(pure = true)
    Instant createdAt();

    @Contract(pure = true)
    long size();
}
