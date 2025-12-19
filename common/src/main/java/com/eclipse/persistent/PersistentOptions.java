package com.eclipse.persistent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class PersistentOptions {
    public static final String MOD_ID = "persistent_options";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Path customOptionsFolder = Path.of(System.getProperty("user.home")).resolve(".persistentoptions");

    public enum SyncResult {
        NONE(""), // No action was taken
        IMPORTED_LOCAL("Imported local settings"),
        APPLIED_GLOBAL("Applied global settings"),
        INITIALIZED("Initialized global settings"),
        AUTO_SYNCED("Auto-synced with global file"),
        FAILED("Sync failed! Check logs.");

        private final String message;
        SyncResult(String message) { this.message = message; }
        public String getMessage() { return this.message; }
    }
    public static SyncResult lastSyncResult = SyncResult.NONE;

    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();
    private static final AtomicReference<Runnable> pendingTask = new AtomicReference<>();
    private static final AtomicReference<ScheduledFuture<?>> pendingFuture = new AtomicReference<>();

    public static void init() {
        LOGGER.info("Loaded PersistentOptions");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Runnable task = pendingTask.getAndSet(null);
            if (task != null) {
                try {
                    task.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            SCHEDULER.shutdownNow();
        }));
    }

    public static void scheduleSync(Runnable syncTask) {
        ScheduledFuture<?> existing = pendingFuture.get();
        if (existing != null && !existing.isDone()) {
            existing.cancel(false);
        }
        pendingTask.set(syncTask);
        ScheduledFuture<?> future = SCHEDULER.schedule(() -> {
            Runnable t = pendingTask.getAndSet(null);
            if (t != null) {
                try {
                    t.run();
                } catch (Exception e) {
                    LOGGER.error("Failed to run background sync", e);
                }
            }
        }, 2, TimeUnit.SECONDS);
        pendingFuture.set(future);
    }
}