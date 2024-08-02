package net.thenextlvl.worlds.image;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public
enum DeleteResult {
    EXEMPTED("world.delete.disallowed"),
    SCHEDULED("world.delete.scheduled"),
    FAILED("world.delete.failed"),
    SUCCESS("world.delete.success"),
    UNLOAD_FAILED("world.unload.failed");

    private final String message;
}
