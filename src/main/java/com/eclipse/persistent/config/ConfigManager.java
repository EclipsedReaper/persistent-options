package com.eclipse.persistent.config;

import com.eclipse.persistent.PersistentOptions;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public class ConfigManager {
    private static final File configFile = new File(PersistentOptions.configPath.toUri());
    private final Gson gson;
    private final ConfigData data;

    private class ConfigData {
        public boolean displayStartupToast = true;
        public boolean syncResourcePacks = false;
    }

    public ConfigManager() {
        this.gson = new Gson();
        this.data = this.loadConfig();
    }

    private ConfigData loadConfig() {
        if (!configFile.exists()) {
            return new ConfigData();
        }

        try (FileReader reader = new FileReader(configFile)) {
            return gson.fromJson(reader, ConfigData.class);
        } catch (IOException e) {
            PersistentOptions.LOGGER.error("Failed to load config!");
            e.printStackTrace();
            return new ConfigData();
        }
    }

    public void saveConfig() {
        try (FileWriter writer = new FileWriter(configFile)) {
            Files.createDirectories(PersistentOptions.configFolder);
            gson.toJson(this.data, writer);
        } catch (IOException e) {
            PersistentOptions.LOGGER.error("Failed to save config!");
            e.printStackTrace();
        }
    }

    public void setSyncResourcePacks(boolean val) {
        this.data.syncResourcePacks = val;
    }

    public boolean getSyncResourcePacks() {
        return this.data.syncResourcePacks;
    }

    public void setDisplayStartupToast(boolean val) {
        this.data.displayStartupToast = val;
    }

    public boolean getDisplayStartupToast() {
        return this.data.displayStartupToast;
    }
}
