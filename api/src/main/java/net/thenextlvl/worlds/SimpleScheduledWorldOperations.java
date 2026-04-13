package net.thenextlvl.worlds;

import net.kyori.adventure.key.Key;
import net.thenextlvl.worlds.event.WorldActionScheduledEvent;
import org.bukkit.World;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static net.thenextlvl.worlds.event.WorldActionScheduledEvent.ActionType;

final class SimpleScheduledWorldOperations implements ScheduledWorldOperations {
    private final Set<ScheduledWorldOperations.Operation> operations = new CopyOnWriteArraySet<>();

    @Override
    public Stream<ScheduledWorldOperations.Operation> operations() {
        return operations.stream();
    }

    @Override
    public Stream<ScheduledWorldOperations.Operation> operations(final Key world) {
        return operations().filter(operation -> world.equals(operation.key()));
    }

    @Override
    public Stream<ScheduledWorldOperations.Operation> operations(final World world) {
        return operations(world.key());
    }

    @Override
    public Stream<ScheduledWorldOperations.Operation> operations(final ActionType actionType) {
        return operations().filter(operation -> actionType.equals(operation.type()));
    }

    @Override
    public boolean scheduleDeletion(final World world) {
        return false;
    }

    @Override
    public boolean scheduleRegeneration(final World world) {
        return scheduleAction(world, ActionType.REGENERATE, path -> {
            // todo: move to plugin impl? version dependant? 26.1+ compat
            delete(path.resolve("DIM-1"));
            delete(path.resolve("DIM1"));
            delete(path.resolve("advancements"));
            delete(path.resolve("data"));
            delete(path.resolve("entities"));
            delete(path.resolve("playerdata"));
            delete(path.resolve("poi"));
            delete(path.resolve("region"));
            delete(path.resolve("stats"));
        });
    }

    private void delete(final Path path) {
        try {
            if (!Files.isDirectory(path)) Files.deleteIfExists(path);
            else try (final var files = Files.list(path)) {
                files.forEach(this::delete);
                Files.deleteIfExists(path);
            }
        } catch (final IOException e) {
            WorldsAccess.access().getComponentLogger().warn("Failed to delete {}", path, e);
        }
    }

    @Override
    public boolean scheduleBackupRestoration(final World world, final Backup backup) {
        return scheduleAction(world, ActionType.RESTORE_BACKUP, path -> backup.provider().restoreNow(path, backup));
    }

    private boolean scheduleAction(final World world, final ActionType type, final Consumer<Path> consumer) {
        if (operations(world).anyMatch(operation -> operation.type().equals(type))) return false;

        final var event = new WorldActionScheduledEvent(world, type);
        if (!event.callEvent()) return false;

        final var action = event.getAction() == null ? consumer : event.getAction().andThen(consumer);

        final var path = world.getWorldFolder().toPath();
        operations.add(new Operation(type, world.key(), () -> action.accept(path)));
        return true;
    }

    @Override
    public boolean cancel(final ScheduledWorldOperations.Operation operation) {
        return operations.remove(operation);
    }

    public record Operation(
            ActionType type, Key key, Runnable runnable
    ) implements ScheduledWorldOperations.Operation {
        @Override
        public void run() {
            runnable.run();
        }
    }
}
