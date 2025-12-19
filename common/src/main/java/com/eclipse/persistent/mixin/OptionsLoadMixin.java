package com.eclipse.persistent.mixin;

import com.eclipse.persistent.PersistentOptions;
import com.eclipse.persistent.ui.SyncDialog;
import com.eclipse.persistent.util.OptionMerger;
import dev.architectury.platform.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Mixin(Minecraft.class)
public class OptionsLoadMixin {

    @Redirect(method = "<init>", at = @At(value = "NEW", target = "net/minecraft/client/Options"))
    private Options redirectOptions(Minecraft minecraft, File gameDir) {

        String currentVersion = Platform.getMinecraftVersion();
        Path versionedFolder = PersistentOptions.customOptionsFolder.resolve(currentVersion);
        Path globalOptions = versionedFolder.resolve("options.txt");
        Path localOptions = gameDir.toPath().resolve("options.txt");

        try {
            Files.createDirectories(versionedFolder);
            boolean localIsReal = isRealUserOptions(localOptions);

            if (localIsReal) {
                if (Files.exists(globalOptions)) {
                    System.setProperty("java.awt.headless", "false");

                    if (SyncDialog.showConflictDialog()) {
                        importSettings(localOptions, globalOptions);
                    } else {
                        markLocalAsProcessed(localOptions);
                    }
                }
                else {
                    PersistentOptions.LOGGER.info("No global options for " + currentVersion + ". Importing local instance options.");
                    importSettings(localOptions, globalOptions);
                }
            }
            else {
                if (Files.exists(globalOptions)) {
                    checkForNewerConfigAndSync(versionedFolder, globalOptions);
                }
                else {
                    attemptUpgradeFromOlderVersion(versionedFolder, globalOptions);
                }
            }

        } catch (Exception e) {
            PersistentOptions.LOGGER.error("Error handling options synchronization: ", e);
        }
        return new Options(minecraft, versionedFolder.toFile());
    }

    private void importSettings(Path source, Path target) throws IOException {
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        markLocalAsProcessed(source);
    }

    private void markLocalAsProcessed(Path source) {
        try {
            Files.move(source, source.resolveSibling("options.txt.imported"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            PersistentOptions.LOGGER.warn("Failed to rename local options file.", e);
        }
    }

    private void checkForNewerConfigAndSync(Path currentFolder, Path currentFile) {
        try (Stream<Path> stream = Files.list(PersistentOptions.customOptionsFolder)) {

            Optional<Path> newestSibling = stream
                    .filter(Files::isDirectory)
                    .filter(path -> !path.equals(currentFolder))
                    .map(path -> path.resolve("options.txt"))
                    .filter(Files::exists)
                    .max(Comparator.comparing(this::getLastModified));

            if (newestSibling.isPresent()) {
                Path source = newestSibling.get();
                FileTime sourceTime = Files.getLastModifiedTime(source);
                FileTime currentTime = Files.getLastModifiedTime(currentFile);

                if (sourceTime.compareTo(currentTime) > 0) {
                    PersistentOptions.LOGGER.info("Found newer config in " + source.getParent().getFileName() + ". Merging shared settings...");

                    if (OptionMerger.mergeIntersection(source, currentFile)) {
                        Files.setLastModifiedTime(currentFile, FileTime.fromMillis(System.currentTimeMillis()));
                    }
                }
            }
        } catch (IOException e) {
            PersistentOptions.LOGGER.warn("Smart Sync failed", e);
        }
    }

    private void attemptUpgradeFromOlderVersion(Path currentVersionFolder, Path targetFile) {
        try (Stream<Path> stream = Files.list(PersistentOptions.customOptionsFolder)) {
            Optional<Path> bestMatch = stream
                    .filter(Files::isDirectory)
                    .filter(path -> !path.equals(currentVersionFolder))
                    .filter(path -> Files.exists(path.resolve("options.txt")))
                    .max(Comparator.comparing(Path::getFileName));

            if (bestMatch.isPresent()) {
                Path oldOptions = bestMatch.get().resolve("options.txt");
                Files.copy(oldOptions, targetFile);
                PersistentOptions.LOGGER.info("Upgraded settings from " + bestMatch.get().getFileName());
            }
        } catch (IOException e) {
            PersistentOptions.LOGGER.warn("Failed to auto-upgrade settings.", e);
        }
    }

    private FileTime getLastModified(Path p) {
        try { return Files.getLastModifiedTime(p); }
        catch (IOException e) { return FileTime.fromMillis(0); }
    }

    private boolean isRealUserOptions(Path path) {
        if (!Files.exists(path)) return false;
        try {
            if (Files.size(path) < 100) return false;
            List<String> lines = Files.readAllLines(path);
            if (lines.size() < 20) return false;

            boolean hasFov = false;
            boolean hasSensitivity = false;
            int limit = Math.min(lines.size(), 50);
            for (int i = 0; i < limit; i++) {
                String line = lines.get(i);
                if (line.startsWith("fov:")) hasFov = true;
                if (line.startsWith("mouseSensitivity:")) hasSensitivity = true;
            }
            return hasFov && hasSensitivity;
        } catch (Exception e) {
            return false;
        }
    }
}