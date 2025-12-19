package com.eclipse.persistent.util;

import com.eclipse.persistent.PersistentOptions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class OptionMerger {

    public static boolean mergeIntersection(Path source, Path target) {
        try {
            if (!Files.exists(source) || !Files.exists(target)) return false;

            List<String> sourceLines = Files.readAllLines(source);
            List<String> targetLines = Files.readAllLines(target);

            Map<String, String> sourceMap = new HashMap<>();
            for (String line : sourceLines) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    sourceMap.put(parts[0], parts[1]);
                }
            }

            if (sourceMap.isEmpty()) return false;

            boolean changed = false;
            List<String> newTargetLines = new ArrayList<>();

            for (String line : targetLines) {
                String[] parts = line.split(":", 2);

                if (parts.length == 2) {
                    String key = parts[0];
                    if (sourceMap.containsKey(key)) {
                        String newValue = sourceMap.get(key);
                        String oldValue = parts[1];

                        if (!newValue.equals(oldValue)) {
                            newTargetLines.add(key + ":" + newValue);
                            changed = true;
                        } else {
                            newTargetLines.add(line);
                        }
                    } else {
                        newTargetLines.add(line);
                    }
                } else {
                    newTargetLines.add(line);
                }
            }

            if (changed) {
                Files.write(target, newTargetLines);
                PersistentOptions.LOGGER.info("Updated shared settings from newer configuration.");
                return true;
            }

        } catch (IOException e) {
            PersistentOptions.LOGGER.error("Failed to merge options", e);
        }
        return false;
    }
}