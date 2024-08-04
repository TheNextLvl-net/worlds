package net.thenextlvl.worlds.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public
enum WorldActionResult {
    DELETE_EXEMPTED("world.delete.disallowed"),
    DELETE_SCHEDULED("world.delete.scheduled"),
    DELETE_FAILED("world.delete.failed"),
    DELETE_SUCCESS("world.delete.success"),
    UNLOAD_EXEMPTED("world.unload.disallowed"),
    UNLOAD_SUCCESS("world.unload.success"),
    UNLOAD_FAILED("world.unload.failed");

    private final String message;
}
