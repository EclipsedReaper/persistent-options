//? if neoforge {
/*package com.eclipse.persistent.platforms.neoforge;

import com.eclipse.persistent.config.ConfigScreen;
import com.eclipse.persistent.PersistentOptions;
import com.eclipse.persistent.ModPlatform;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
//? if <1.21 {
import net.neoforged.neoforge.client.ConfigScreenHandler;
//?} else {
/^import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
^///?}
@Mod("persistent_options")
public class PersistentOptionsNeoForge {
	public PersistentOptionsNeoForge() {
		PersistentOptions.entrypoint(new NeoForgePlatform());
        ModLoadingContext.get().registerExtensionPoint(
                //? if <1.21 {
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        ((client, parent) -> new ConfigScreen(parent))
                )
                //?} else {
                /^IConfigScreenFactory.class,
                () -> (client, parent) -> new ConfigScreen(parent)
                ^///?}
        );
        ClientEventsNeoForge.init();
	}
    public static class NeoForgePlatform implements ModPlatform {
        @Override
        public String getModloader() {
            return "NeoForge";
        }

        @Override
        public boolean isModLoaded(String modId) {
            return ModList.get().isLoaded(modId);
        }
    }
}
*///?}