package com.eclipse.persistent;

import com.eclipse.persistent.config.ConfigManager;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class PersistentOptions {
	public static final String MODID = "persistent_options";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
	public static final Path configFolder = Path.of(System.getProperty("user.home")).resolve(".persistentoptions");
	public static final Path configPath = PersistentOptions.configFolder.resolve("persistent_options.json");
	public static final Path globalOptions = PersistentOptions.configFolder.resolve("options.json");
	public static final ConfigManager configManager = new ConfigManager();
	public static ModPlatform PLATFORM = null;

	public enum SyncResult {
		NONE(""),
		UPDATED_GLOBAL("persistent_options.sync.updated_global"),
		APPLIED_GLOBAL("persistent_options.sync.applied_global"),
		INITIALIZED("persistent_options.sync.initialized"),
		AUTO_SYNCED("persistent_options.sync.auto_synced"),
		FAILED("persistent_options.sync.failed");

		private final String key;
		SyncResult(String key) { this.key = key; }
		public String getKey() { return this.key; }

		public Component getComponent() {
			if (this == NONE) {
				return Component.empty();
			}
			return Component.translatable(this.key);
		}
	}
	public static SyncResult lastSyncResult = SyncResult.NONE;

	private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();
	private static final AtomicReference<Runnable> pendingTask = new AtomicReference<>();
	private static final AtomicReference<ScheduledFuture<?>> pendingFuture = new AtomicReference<>();


	public static void entrypoint(ModPlatform platform) {
		PersistentOptions.PLATFORM = platform;
		LOGGER.info("Loaded Persistent Options in %s modloader".formatted(PersistentOptions.PLATFORM.getModloader()));

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