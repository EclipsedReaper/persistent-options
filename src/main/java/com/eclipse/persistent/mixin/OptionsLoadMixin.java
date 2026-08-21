package com.eclipse.persistent.mixin;

import com.eclipse.persistent.PersistentOptions;
import com.eclipse.persistent.options.OptionsManager;
import com.eclipse.persistent.ui.MergeDialog;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Mixin(Minecraft.class)
public class OptionsLoadMixin {
    @WrapOperation(
            method = "<init>",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/client/Minecraft;Ljava/io/File;)Lnet/minecraft/client/Options;"
            )
    )
    private Options wrapOptionsConstructor(Minecraft minecraft, File gameDir, Operation<Options> original) {
        Path localFile = gameDir.toPath().resolve("options.txt");
        Path syncMarker = gameDir.toPath().resolve(".persistent_synced");

        try {
            Files.createDirectories(PersistentOptions.configFolder);
            boolean globalExists = Files.exists(PersistentOptions.globalOptions);
            boolean localExists = Files.exists(localFile);

            if (globalExists && localExists) {
                if (Files.exists(syncMarker)) {
                    OptionsManager.mergeGlobalOverLocal(localFile);
                    PersistentOptions.lastSyncResult = PersistentOptions.SyncResult.AUTO_SYNCED;
                } else {
                    if (isRealUserOptions(localFile)) {
                        System.setProperty("java.awt.headless", "false");
                        boolean keepLocal = MergeDialog.showDialog();
                        if (keepLocal) {
                            OptionsManager.mergeLocalOverGlobal(localFile);
                            PersistentOptions.lastSyncResult = PersistentOptions.SyncResult.UPDATED_GLOBAL;
                        } else {
                            OptionsManager.mergeGlobalOverLocal(localFile);
                            PersistentOptions.lastSyncResult = PersistentOptions.SyncResult.APPLIED_GLOBAL;
                        }
                        createSyncMarker(syncMarker);
                    } else {
                        OptionsManager.mergeGlobalOverLocal(localFile);
                        PersistentOptions.lastSyncResult = PersistentOptions.SyncResult.APPLIED_GLOBAL;
                        createSyncMarker(syncMarker);
                    }
                }
            } else if (globalExists) {
                Files.createFile(localFile);
                OptionsManager.mergeGlobalOverLocal(localFile);
                PersistentOptions.lastSyncResult = PersistentOptions.SyncResult.APPLIED_GLOBAL;
                createSyncMarker(syncMarker);
            } else {
                Files.createFile(PersistentOptions.globalOptions);
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