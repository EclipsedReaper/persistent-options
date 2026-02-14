package com.eclipse.persistent.mixin;

import com.eclipse.persistent.PersistentOptions;
import com.eclipse.persistent.ui.SyncDialog;
import com.eclipse.persistent.util.OptionMerger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Mixin(Minecraft.class)
public class OptionsLoadMixin {

    @Redirect(method = "<init>", at = @At(value = "NEW", target = "net/minecraft/client/Options"))
    private Options redirectOptions(Minecraft minecraft, File gameDir) {
        Path globalFile = PersistentOptions.customOptionsFolder.resolve("options.txt");
        Path localFile = gameDir.toPath().resolve("options.txt");
        Path syncMarker = gameDir.toPath().resolve(".persistent_synced");

        try {
            Files.createDirectories(PersistentOptions.customOptionsFolder);
            boolean globalExists = Files.exists(globalFile);
            boolean localExists = Files.exists(localFile);

            if (globalExists && localExists) {
                if (Files.exists(syncMarker)) {
                    OptionMerger.merge(globalFile, localFile);
                    PersistentOptions.lastSyncResult = PersistentOptions.SyncResult.AUTO_SYNCED;
                } else {
                    if (isRealUserOptions(localFile)) {
                        System.setProperty("java.awt.headless", "false");
                        boolean keepLocal = SyncDialog.showConflictDialog();
                        if (keepLocal) {
                            OptionMerger.merge(localFile, globalFile);
                            PersistentOptions.lastSyncResult = PersistentOptions.SyncResult.IMPORTED_LOCAL;
                        } else {
                            OptionMerger.merge(globalFile, localFile);
                            PersistentOptions.lastSyncResult = PersistentOptions.SyncResult.APPLIED_GLOBAL;
                        }
                        createSyncMarker(syncMarker);
                    } else {
                        OptionMerger.merge(globalFile, localFile);
                        PersistentOptions.lastSyncResult = PersistentOptions.SyncResult.APPLIED_GLOBAL;
                        createSyncMarker(syncMarker);
                    }
                }
            } else if (globalExists) {
                Files.copy(globalFile, localFile);
                PersistentOptions.lastSyncResult = PersistentOptions.SyncResult.APPLIED_GLOBAL;
                createSyncMarker(syncMarker);
            } else {
                Files.createFile(globalFile);
                PersistentOptions.lastSyncResult = PersistentOptions.SyncResult.INITIALIZED;
                createSyncMarker(syncMarker);
            }
        } catch (Exception e) {
            PersistentOptions.LOGGER.error("Error handling options syncing: ", e);
            PersistentOptions.lastSyncResult = PersistentOptions.SyncResult.FAILED;
        }
        return new Options(minecraft, gameDir);
    }

    private void createSyncMarker(Path marker) {
        try {
            if (!Files.exists(marker)) {
                Files.createFile(marker);
            }
            try { Files.setAttribute(marker, "dos:hidden", true); } catch (Exception ignored) {}
        } catch (Exception e) {
            PersistentOptions.LOGGER.warn("Failed to create sync marker", e);
        }
    }

    private boolean isRealUserOptions(Path path) {
        try {
            if (Files.size(path) < 10) return false;
            List<String> lines = Files.readAllLines(path);
            return lines.size() >= 5;
        } catch (Exception e) {
            return false;
        }
    }
}