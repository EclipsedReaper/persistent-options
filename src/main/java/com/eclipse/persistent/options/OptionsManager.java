package com.eclipse.persistent.options;

import com.eclipse.persistent.PersistentOptions;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class OptionsManager {
    private static final Gson GSON = new Gson();

    public static OptionsData loadGlobalOptions() {
        if (!Files.exists(PersistentOptions.globalOptions)) return new OptionsData();

        OptionsData data = new OptionsData();
        data.loadFromGlobal();
        return data;

        /*
        Map<String, String> map = new LinkedHashMap<>();
        if (!Files.exists(path)) return map;

        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (String line : lines) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    map.put(parts[0], line);
                }
            }
        } catch (IOException e) {
            PersistentOptions.LOGGER.error("Failed to load settings: ", e);
        }
        return map;
        */
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
            if (Files.exists(localOptions)) {
                backup = PersistentOptions.globalOptions.resolveSibling(PersistentOptions.globalOptions.getFileName() + ".bak");
                Files.copy(PersistentOptions.globalOptions, backup, StandardCopyOption.REPLACE_EXISTING);
            }

            OptionsData localData = loadLocalOptions(localOptions, Path.of(""));
            OptionsData globalData = loadGlobalOptions();
            if (!localData.getOptionsMenuData().settings.containsKey("fullscreenResolution")) {
                globalData.getOptionsMenuData().settings.remove("fullscreenResolution");
            }
            globalData.getOptionsMenuData().settings.putAll(localData.getOptionsMenuData().settings);
            globalData.getOptionsMenuData().resourcePacks.addAll(localData.getOptionsMenuData().resourcePacks);

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
            if (Files.exists(localOptions)) {
                backup = PersistentOptions.globalOptions.resolveSibling(PersistentOptions.globalOptions.getFileName() + ".bak");
                Files.copy(PersistentOptions.globalOptions, backup, StandardCopyOption.REPLACE_EXISTING);
            }

            OptionsData globalData = loadGlobalOptions();
            OptionsData localData = loadLocalOptions(localOptions, Path.of(""));
            if (!globalData.getOptionsMenuData().settings.containsKey("fullscreenResolution")) {
                localData.getOptionsMenuData().settings.remove("fullscreenResolution");
            }
            localData.getOptionsMenuData().settings.putAll(globalData.getOptionsMenuData().settings);
            localData.getOptionsMenuData().resourcePacks.addAll(globalData.getOptionsMenuData().resourcePacks);

            localData.getOptionsMenuData().saveToLocal(localOptions);

            if (backup != null) {
                Files.deleteIfExists(backup);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}