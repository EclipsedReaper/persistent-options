package com.eclipse.persistent.options;

import com.eclipse.persistent.PersistentOptions;
import com.eclipse.persistent.ui.OldVersionWarningDialog;
import com.eclipse.persistent.util.ResourcePackUtils;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

class OptionsData {
    public static class OptionsMenuData {
        private static final Gson GSON = new Gson();
        public Map<String, String> settings;
        public Set<ResourcePackUtils.ResourcePackInfo> resourcePacks;

        private OptionsMenuData() {
            settings = new LinkedHashMap<>();
            resourcePacks = new HashSet<>();
        }

        public void loadFromLocal(Path optionsPath) {
            if (!Files.exists(optionsPath)) return;

            try {
                List<String> lines = Files.readAllLines(optionsPath, StandardCharsets.UTF_8);
                for (String line : lines) {
                    String[] parts = line.split(":", 2);
                    if (parts.length == 2) {
                        if (parts[0].equals("resourcePacks")) {
                            if (!PersistentOptions.configManager.getSyncResourcePacks()) continue;
                            Type listType = new TypeToken<List<String>>() {}.getType();
                            List<String> rawPacks = GSON.fromJson(parts[1], listType);
                            for (String pack : rawPacks) {
                                ResourcePackUtils.ResourcePackInfo packInfo = ResourcePackUtils.trimVersion(pack);
                                resourcePacks.add(packInfo);
                            }
                        } else {
                            settings.put(parts[0], parts[1]);
                        }
                    }
                }
            } catch (IOException e) {
                PersistentOptions.LOGGER.error("Failed to load settings: ", e);
            }
        }

        public void loadFromGlobal() {
            if (!Files.exists(PersistentOptions.globalOptions)) return;

            try (BufferedReader reader = Files.newBufferedReader(PersistentOptions.globalOptions)) {
                OptionsMenuData loadedData = GSON.fromJson(reader, OptionsMenuData.class);

                if (loadedData != null) {
                    if (loadedData.settings != null) {
                        this.settings.putAll(loadedData.settings);
                    }

                    if (loadedData.resourcePacks != null && PersistentOptions.configManager.getSyncResourcePacks()) {
                        this.resourcePacks.addAll(loadedData.resourcePacks);
                    }
                }
            } catch (IOException e) {
                PersistentOptions.LOGGER.error("Failed to load global settings: ", e);
            }
        }

        public void saveToLocal(Path optionsPath) {
            try {
                List<String> lines = new ArrayList<>();
                for (String key : this.settings.keySet()) {
                    lines.add(key + ":" + this.settings.get(key));
                }
                if (PersistentOptions.configManager.getSyncResourcePacks()) {
                    List<String> loadedPackNames = new ArrayList<>();
                    loadedPackNames.add("vanilla");
                    List<ResourcePackUtils.ResourcePackInfo> instancePacks = ResourcePackUtils.getResourcePacks();
                    Set<String> packNames = resourcePacks.stream()
                            .map(ResourcePackUtils.ResourcePackInfo::name)
                            .collect(Collectors.toSet());
                    System.out.println(packNames);

                    for (ResourcePackUtils.ResourcePackInfo pack : instancePacks) {
                        System.out.println(pack.name());
                        if (packNames.contains(pack.name())) {
                            if (pack.fullName().equals("high_contrast")) {
                                loadedPackNames.add(pack.fullName());
                            } else {
                                loadedPackNames.add("file/" + pack.fullName());
                            }
                        }
                    }
                    System.out.println("resourcePacks:" + GSON.toJson(loadedPackNames));
                    lines.add("resourcePacks:" + GSON.toJson(loadedPackNames));
                }
                Files.write(optionsPath, lines, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void saveToGlobal() {
            if (Files.exists(PersistentOptions.globalOptions)) {
                try (BufferedReader reader = Files.newBufferedReader(PersistentOptions.globalOptions, StandardCharsets.UTF_8)) {
                    OptionsMenuData existingData = GSON.fromJson(reader, OptionsMenuData.class);
                    if (existingData != null) {
                        if (existingData.settings != null) {
                            existingData.settings.putAll(this.settings);
                            this.settings = existingData.settings;
                        }
                        if (existingData.resourcePacks != null && PersistentOptions.configManager.getSyncResourcePacks()) {
                            List<ResourcePackUtils.ResourcePackInfo> instancePacks = ResourcePackUtils.getResourcePacks();
                            Set<String> packNames = resourcePacks.stream()
                                    .map(ResourcePackUtils.ResourcePackInfo::name)
                                    .collect(Collectors.toSet());
                            Set<String> storedPackNames = existingData.resourcePacks.stream()
                                    .map(ResourcePackUtils.ResourcePackInfo::name)
                                    .collect(Collectors.toSet());
                            for (ResourcePackUtils.ResourcePackInfo info : instancePacks) {
                                if (packNames.contains(info.name()) && !storedPackNames.contains(info.name())) {
                                    existingData.resourcePacks.removeIf(existingPack -> packNames.contains(existingPack.name()));
                                }
                            }
                            existingData.resourcePacks.addAll(this.resourcePacks);
                            this.resourcePacks = existingData.resourcePacks;
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            try (BufferedWriter writer = Files.newBufferedWriter(PersistentOptions.globalOptions, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private final OptionsMenuData optionsMenuData;

    public OptionsData() {
        this.optionsMenuData = new OptionsMenuData();
    }

    public void loadFromLocal(Path optionsPath, Path modConfigPath) {
        this.optionsMenuData.loadFromLocal(optionsPath);
    }

    public void loadFromGlobal() {
        if (Files.exists(PersistentOptions.configFolder.resolve("options.txt"))) {
            upgradeOld(PersistentOptions.configFolder.resolve("options.txt"));
        } else {
            this.optionsMenuData.loadFromGlobal();
        }
    }

    public void upgradeOld(Path globalPath) {
        Path upgradeInfo = PersistentOptions.configFolder.resolve("upgradeInfo.json");
        if (!Files.exists(upgradeInfo)) {
            this.optionsMenuData.loadFromLocal(globalPath);
            this.optionsMenuData.saveToGlobal();
            try {
                List<String> list = new ArrayList<>();
                list.add(String.valueOf(System.currentTimeMillis()));
                Files.write(upgradeInfo, list, StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            this.optionsMenuData.loadFromGlobal();
            try {
                if (globalPath.toFile().lastModified() > Long.parseLong(Files.readString(upgradeInfo).strip())) {
                    System.setProperty("java.awt.headless", "false");
                    OldVersionWarningDialog.showDialog();
                }
                List<String> list = new ArrayList<>();
                list.add(String.valueOf(System.currentTimeMillis()));
                Files.write(upgradeInfo, list, StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public OptionsMenuData getOptionsMenuData() {
        return optionsMenuData;
    }
}