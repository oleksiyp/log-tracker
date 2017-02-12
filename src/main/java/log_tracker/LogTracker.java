package log_tracker;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.*;

public class LogTracker {
    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private final Map<Path, TrackedLogFile> trackedLogFileMap;
    final ScheduledExecutorService executor;
    private final Set<String> grepFilter;

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

    private void registerDir(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        keys.put(key, dir);
    }

    private TrackedLogFile registerFile(Path file) {
        System.out.println(Util.now() + " opening " + file);
        try {
            return new TrackedLogFile(file,
                    grepFilter,
                    System.out::println,
                    (path, replacement) -> {
                        if (replacement == null) {
                            trackedLogFileMap.remove(path);
                        } else {
                            trackedLogFileMap.put(path, replacement);
                        }
                    });
        } catch (Exception ex) {
            System.out.println(Util.now() + " failed to open '" + file + "': " + ex);
        }
        return null;
    }

    private void walkDirAndRegister(Path start) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException
            {
                registerDir(dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                registerFile(file);
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
    }

    LogTracker(Path dir) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<>();
        this.trackedLogFileMap = new ConcurrentHashMap<>();
        executor = Executors.newScheduledThreadPool(1);
        grepFilter = Collections.singleton("CRITICAL");

        System.out.printf("%s scanning %s%n", Util.now() , dir);
        walkDirAndRegister(dir);
        System.out.printf("%s done%n", Util.now());

    }

    void processEvents() {
        while (!keys.isEmpty()) {
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();

                if (kind == OVERFLOW) {
                    continue;
                }

                WatchEvent<Path> ev = cast(event);
                Path path = dir.resolve(ev.context());

                if (Files.isDirectory(path, NOFOLLOW_LINKS)) {
                    directoryChanges(kind, path);
                } else if (Files.isRegularFile(path)) {
                    fileChanges(kind, path);
                }
            }

             if (!key.reset()) {
                keys.remove(key);
            }
        }
    }

    private void fileChanges(WatchEvent.Kind kind, Path path) {
        if (path.toString().endsWith(".gz")) {
            return;
        }

        if (kind == ENTRY_MODIFY || kind == ENTRY_CREATE) {
            TrackedLogFile logFile = trackedLogFileMap.get(path);
            if (logFile == null) {
                logFile = registerFile(path);
                if (logFile != null) {
                    trackedLogFileMap.put(path, logFile);
                }
            }

            TrackedLogFile finalLogFile = logFile;

            executor.execute(() -> {
                int toRead = finalLogFile.getPossibleToRead();
                try {
                    finalLogFile.readNext(toRead);
                } catch (IOException e) {
                    System.out.println(Util.now() + " error reading '" + path);
                    finalLogFile.stopTracking();
                    trackedLogFileMap.remove(path);
                }
            });
        } else if (kind == ENTRY_DELETE) {
            System.out.println(Util.now() + " stop tracking " + path);
            TrackedLogFile logFile = trackedLogFileMap.remove(path);
            if (logFile != null) {
                logFile.stopTracking();
                trackedLogFileMap.remove(path);
            }
        }
    }

    private void directoryChanges(WatchEvent.Kind kind, Path path) {
        if (kind == ENTRY_CREATE) {
            try {
                walkDirAndRegister(path);
            } catch (IOException x) {
                // ignore to keep sample readbale
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new LogTracker(Paths.get("/var/log")).processEvents();
    }
}