package com.eclipse.persistent.util;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Tuple;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        pack = pack.replace("file/", "");
        Matcher m = versionPattern.matcher(pack);
        String lastVersion = null;
        while (m.find()) {
            lastVersion = m.group();
        }

        if (lastVersion != null) {
            return new ResourcePackInfo(pack.replace(lastVersion, ""), lastVersion, pack);
        }
        return new ResourcePackInfo(pack, "0.0", pack);
    }
}
