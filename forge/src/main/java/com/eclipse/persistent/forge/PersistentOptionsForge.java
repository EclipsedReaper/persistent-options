package com.eclipse.persistent.forge;

import com.eclipse.persistent.PersistentOptions;
import net.minecraftforge.fml.common.Mod;

@Mod(PersistentOptions.MOD_ID)
public class PersistentOptionsForge {
    public PersistentOptionsForge() {
        PersistentOptions.init();
    }
}