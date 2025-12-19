package com.eclipse.persistent.util;

import com.eclipse.persistent.PersistentOptions;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class OptionMerger {

    public static Map<String, String> loadOptions(Path path) {
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
    }

    public static void smartMerge(Path source, Path target) {
        Path backup = null;
        try {
            if (Files.exists(target)) {
                backup = target.resolveSibling(target.getFileName() + ".bak");
                Files.copy(target, backup, StandardCopyOption.REPLACE_EXISTING);
            }

            Map<String, String> sourceMap = loadOptions(source);
            Map<String, String> targetMap = loadOptions(target);
            targetMap.putAll(sourceMap);
            Files.write(target, targetMap.values(), StandardCharsets.UTF_8);

            if (backup != null) {
                Files.deleteIfExists(backup);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}