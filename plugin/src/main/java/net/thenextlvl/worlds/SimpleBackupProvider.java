package net.thenextlvl.worlds;

import net.kyori.adventure.key.Key;
import org.bukkit.Keyed;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@NullMarked
class SimpleBackupProvider implements BackupProvider {
    private static final BackupProvider INSTANCE = new SimpleBackupProvider();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
            .withZone(ZoneId.systemDefault());

    private final Path backupRoot = Path.of("backups");

    @Override
    public CompletableFuture<Backup> backup(final World world) {
        return CompletableFuture.supplyAsync(() -> createBackup(world.getWorldFolder().toPath(), world));
    }

    @Override
    public CompletableFuture<ActionResult<World>> restore(final World world, final Backup backup) {
        final var worldFolder = world.getWorldFolder().toPath();
        return WorldsAccess.access().unload(world, false).thenComposeAsync(success -> {
            if (!success) return CompletableFuture.completedFuture(
                    ActionResult.result(null, ActionResult.Status.FAILED)
            );
            final var status = restoreNow(worldFolder, backup);
            if (status != ActionResult.Status.SUCCESS) {
                return CompletableFuture.completedFuture(ActionResult.result(null, status));
            }
            return WorldsAccess.access().load(world.getWorldFolder().toPath());
        });
    }

    @Override
    public ActionResult.Status restoreNow(final Path path, final Backup backup) {
        try {
            final var backupPath = resolveBackupFolder(backup.key()).resolve(backup.name() + ".zip");
            restoreBackup(path, backupPath);
            return ActionResult.Status.SUCCESS;
        } catch (final IOException e) {
            WorldsAccess.access().getComponentLogger().warn("Failed to restore backup for world {}", backup.key().asString(), e);
            return ActionResult.Status.FAILED;
        }
    }

    @Override
    public CompletableFuture<List<Backup>> listBackups() {
        return CompletableFuture.supplyAsync(() -> WorldsAccess.access()
                .getServer().getWorlds().stream()
                .map(Keyed::key)
                .map(this::listBackupFiles)
                .flatMap(Collection::stream)
                .toList());
    }

    @Override
    public CompletableFuture<List<Backup>> listBackups(final Key world) {
        return CompletableFuture.supplyAsync(() -> listBackupFiles(world));
    }

    @Override
    public CompletableFuture<Boolean> delete(final Backup backup) {
        if (!(backup instanceof final FileBackup fileBackup)) return CompletableFuture.completedFuture(false);
        return CompletableFuture.supplyAsync(() -> {
            try {
                return Files.deleteIfExists(fileBackup.path());
            } catch (final IOException e) {
                return false;
            }
        });
    }

    private Path resolveBackupFolder(final Key key) {
        return backupRoot.resolve(key.namespace()).resolve(key.value());
    }

    private Backup createBackup(final Path worldDirectory, final World world) {
        final var folder = resolveBackupFolder(world.key());
        try {
            Files.createDirectories(folder);
        } catch (final IOException e) {
            throw new RuntimeException("Failed to create backup directory " + folder, e);
        }
        final var timestamp = FORMATTER.format(Instant.now());
        final var fileName = findAvailableName(folder, timestamp);
        final var backupPath = folder.resolve(fileName);
        try (final var output = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(
                backupPath, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE
        )))) {
            Files.walkFileTree(worldDirectory, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                    if (!file.endsWith("session.lock")) {
                        final var relative = worldDirectory.relativize(file).toString().replace('\\', '/');
                        output.putNextEntry(new ZipEntry(relative));
                        Files.copy(file, output);
                        output.closeEntry();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (final IOException e) {
            throw new RuntimeException("Failed to create backup for " + world.key().asString(), e);
        }
        try {
            final var attrs = Files.readAttributes(backupPath, BasicFileAttributes.class);
            final var backupName = backupPath.getFileName().toString();
            return new FileBackup(
                    backupName.substring(0, backupName.length() - 4),
                    attrs.creationTime().toInstant(),
                    attrs.size(),
                    backupPath,
                    world.key()
            );
        } catch (final IOException e) {
            throw new RuntimeException("Failed to read backup attributes for " + backupPath, e);
        }
    }

    private void restoreBackup(final Path worldDirectory, final Path backupFile) throws IOException {
        Path tempPath;
        do {
            tempPath = worldDirectory.resolveSibling("." + UUID.randomUUID());
        } while (Files.isDirectory(tempPath));
        try (final var input = new ZipInputStream(Files.newInputStream(backupFile, StandardOpenOption.READ))) {
            ZipEntry entry;
            final var root = tempPath.toAbsolutePath().normalize();
            while ((entry = input.getNextEntry()) != null) {
                final Path resolved;
                try {
                    resolved = resolveZipEntry(root, entry);
                } catch (final IOException e) {
                    continue;
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(resolved);
                } else {
                    final var parent = resolved.getParent();
                    if (parent != null) Files.createDirectories(parent);
                    Files.copy(input, resolved);
                }
            }
            deleteRecursively(worldDirectory);
            Files.move(tempPath, worldDirectory, StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException e) {
            try {
                if (Files.exists(tempPath)) deleteRecursively(tempPath);
            } catch (final IOException ignored) {
            }
            throw e;
        }
    }

    private List<Backup> listBackupFiles(final Key key) {
        final var folder = resolveBackupFolder(key);
        if (!Files.isDirectory(folder)) return List.of();
        try (final var files = Files.list(folder)) {
            return files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".zip"))
                    .map(path -> toBackup(path, key))
                    .sorted(Comparator.comparing(Backup::createdAt).reversed())
                    .toList();
        } catch (final IOException e) {
            return List.of();
        }
    }

    private static Backup toBackup(final Path path, final Key key) {
        try {
            final var attributes = Files.readAttributes(path, BasicFileAttributes.class);
            final var fileName = path.getFileName().toString();
            return new FileBackup(
                    fileName.substring(0, fileName.length() - 4),
                    attributes.lastModifiedTime().toInstant(),
                    attributes.size(), path, key
            );
        } catch (final IOException e) {
            final var fileName = path.getFileName().toString();
            return new FileBackup(
                    fileName.substring(0, fileName.length() - 4),
                    Instant.EPOCH, 0, path, key
            );
        }
    }

    private static String findAvailableName(final Path directory, final String baseName) {
        var candidate = baseName + ".zip";
        if (!Files.exists(directory.resolve(candidate))) return candidate;
        for (var i = 1; ; i++) {
            candidate = baseName + "-" + i + ".zip";
            if (!Files.exists(directory.resolve(candidate))) return candidate;
        }
    }

    private static Path resolveZipEntry(final Path root, final ZipEntry entry) throws IOException {
        final var target = root.resolve(entry.getName()).normalize();
        if (!target.startsWith(root)) {
            throw new IOException("Zip entry outside target dir: " + entry.getName());
        }
        return target;
    }

    private static void deleteRecursively(final Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(final Path dir, @Nullable final IOException exc) throws IOException {
                if (exc != null) throw exc;
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    record FileBackup(String name, Instant createdAt, long size, Path path, Key key) implements Backup {
        @Override
        public BackupProvider provider() {
            return SimpleBackupProvider.INSTANCE;
        }
    }
}
