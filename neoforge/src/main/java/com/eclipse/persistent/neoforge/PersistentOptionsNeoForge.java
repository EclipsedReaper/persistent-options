package com.eclipse.persistent.neoforge;

import net.neoforged.fml.common.Mod;

import com.eclipse.persistent.PersistentOptions;

@Mod(PersistentOptions.MOD_ID)
public final class PersistentOptionsNeoForge {
    public PersistentOptionsNeoForge() {
        PersistentOptions.init();
        ClientEventsNeoForge.init();
    }
}
