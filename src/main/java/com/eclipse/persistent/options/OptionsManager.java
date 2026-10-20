package com.eclipse.persistent.options;

import com.eclipse.persistent.PersistentOptions;

import com.eclipse.persistent.util.ResourcePackUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class OptionsManager {
    private static final Gson GSON = new Gson();

    public static OptionsData loadGlobalOptions() {
        if (!Files.exists(PersistentOptions.globalOptions)) return new OptionsData();

        OptionsData data = new OptionsData();
        data.loadFromGlobal();
        return data;
    }

    public static OptionsData loadLocalOptions(Path optionsPath, Path modConfigPath) {
        if (!Files.exists(optionsPath) || !Files.exists(modConfigPath)) return new OptionsData();

        OptionsData data = new OptionsData();
        data.loadFromLocal(optionsPath, modConfigPath);
        return data;
    }

    public static void mergeLocalOverGlobal(Path localOptions) {
        Path backup = null;
        try {
            if (Files.exists(PersistentOptions.globalOptions.toAbsolutePath())) {
                backup = PersistentOptions.globalOptions.resolveSibling(PersistentOptions.globalOptions.getFileName() + ".bak");
                Files.copy(PersistentOptions.globalOptions, backup, StandardCopyOption.REPLACE_EXISTING);
            }

            OptionsData localData = loadLocalOptions(localOptions, Path.of(""));
            OptionsData globalData = loadGlobalOptions();
            if (!localData.getOptionsMenuData().settings.containsKey("fullscreenResolution")) {
                globalData.getOptionsMenuData().settings.remove("fullscreenResolution");
            }
            globalData.getOptionsMenuData().settings.putAll(localData.getOptionsMenuData().settings);
            if (PersistentOptions.configManager.getSyncResourcePacks()) {
                ResourcePackUtils.savePacksToGlobal(localData.getOptionsMenuData(), globalData.getOptionsMenuData());
            }

            globalData.getOptionsMenuData().saveToGlobal();

            if (backup != null) {
                Files.deleteIfExists(backup);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void mergeGlobalOverLocal(Path localOptions) {
        Path backup = null;
        try {
            if (Files.exists(PersistentOptions.globalOptions.toAbsolutePath())) {
                backup = PersistentOptions.globalOptions.resolveSibling(PersistentOptions.globalOptions.getFileName() + ".bak");
                Files.copy(PersistentOptions.globalOptions, backup, StandardCopyOption.REPLACE_EXISTING);
            }

            OptionsData globalData = loadGlobalOptions();
            OptionsData localData = loadLocalOptions(localOptions, Path.of(""));
            if (!globalData.getOptionsMenuData().settings.containsKey("fullscreenResolution")) {
                localData.getOptionsMenuData().settings.remove("fullscreenResolution");
            }
            localData.getOptionsMenuData().settings.putAll(globalData.getOptionsMenuData().settings);
            if (PersistentOptions.configManager.getSyncResourcePacks()) {
                ResourcePackUtils.savePacksToLocal(globalData.getOptionsMenuData(), localData.getOptionsMenuData());
            }

            localData.getOptionsMenuData().saveToLocal(localOptions);

            if (backup != null) {
                Files.deleteIfExists(backup);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}