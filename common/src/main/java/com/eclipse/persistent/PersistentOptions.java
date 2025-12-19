package com.eclipse.persistent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public final class PersistentOptions {
    public static final String MOD_ID = "persistent_options";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Path customOptionsFolder = Path.of(System.getProperty("user.home")).resolve(".persistentoptions");

    public static void init() {
    }
}
