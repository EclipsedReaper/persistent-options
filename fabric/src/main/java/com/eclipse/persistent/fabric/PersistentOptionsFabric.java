package com.eclipse.persistent.fabric;

import net.fabricmc.api.ModInitializer;

import com.eclipse.persistent.PersistentOptions;

public final class PersistentOptionsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        PersistentOptions.init();
    }
}
