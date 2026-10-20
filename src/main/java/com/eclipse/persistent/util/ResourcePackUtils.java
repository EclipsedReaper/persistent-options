package com.eclipse.persistent.util;

import com.eclipse.persistent.options.OptionsData;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ResourcePackUtils {
    public record ResourcePackInfo(String name, String version, String fullName) {}

    private static final Pattern versionPattern = Pattern.compile("\\d+(?:\\.\\d+)+");

    public static List<ResourcePackInfo> getResourcePacks() {
        Path resourcePackFolder = Minecraft.getInstance().gameDirectory.toPath().resolve("resourcepacks");
        List<ResourcePackInfo> resourcePackInfo = new ArrayList<>();
        resourcePackInfo.add(new ResourcePackInfo("high_contrast", "0.0", "high_contrast"));
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(resourcePackFolder)) {
            for (Path path : stream) {
                String fileName = path.getFileName().toString();
                resourcePackInfo.add(trimVersion(fileName));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resourcePackInfo;
    }

    public static ResourcePackInfo trimVersion(String pack) {
        pack = pack.trim();

        if (pack.startsWith("file/")) {
            pack = pack.substring("file/".length());
        }

        Matcher m = versionPattern.matcher(pack);
        int versionStart = -1;
        int versionEnd = -1;
        String lastVersion = null;

        while (m.find()) {
            versionStart = m.start();
            versionEnd = m.end();
            lastVersion = m.group();
        }

        if (lastVersion != null) {
            String name = (pack.substring(0, versionStart)
                    + pack.substring(versionEnd)).trim();
            return new ResourcePackInfo(name, lastVersion, pack);
        }

        return new ResourcePackInfo(pack, "0.0", pack);
    }


    // The provided set for local packs is actually the active set, as the packs stored in config are the enabled packs.
    // Conversely, the packs gathered from getResourcePacks() are the local packs.
    // The general logic is as follows:
    /*
    stored = packs saved to file
    local = packs in instance
    active = active packs in instance

    local -> global
    stored = stored - (local && !active) + active

    global -> local
    active = (local && stored) + (active && !stored)
     */

    public static void savePacksToGlobal(OptionsData.OptionsMenuData localData, OptionsData.OptionsMenuData globalData) {
        Set<ResourcePackInfo> stored =
                globalData.resourcePacks;
        Set<ResourcePackInfo> active =
                localData.resourcePacks;

        Set<String> localNames = getResourcePacks().stream()
                .map(ResourcePackInfo::name)
                .collect(Collectors.toSet());

        stored.removeIf(pack -> localNames.contains(pack.name()));
        stored.addAll(active);
    }

    public static void savePacksToLocal(
            OptionsData.OptionsMenuData globalData,
            OptionsData.OptionsMenuData localData) {

        Set<String> storedNames = globalData.resourcePacks.stream()
                .map(ResourcePackInfo::name)
                .collect(Collectors.toSet());

        Set<ResourcePackInfo> active = localData.resourcePacks;
        active.clear();

        for (ResourcePackInfo localPack : getResourcePacks()) {
            if (storedNames.contains(localPack.name())) {
                active.add(localPack);
            }
        }
    }
}
