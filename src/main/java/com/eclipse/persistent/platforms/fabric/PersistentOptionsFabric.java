//? if fabric {
package com.eclipse.persistent.platforms.fabric;

import com.eclipse.persistent.ModPlatform;
import net.fabricmc.api.ModInitializer;
import com.eclipse.persistent.PersistentOptions;
import net.fabricmc.loader.api.FabricLoader;

public class PersistentOptionsFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		PersistentOptions.entrypoint(new FabricPlatform());
	}
	public static class FabricPlatform implements ModPlatform{

		@Override
		public String getModloader() {
			return "Fabric";
		}

		@Override
		public boolean isModLoaded(String modloader) {
			return FabricLoader.getInstance().isModLoaded(modloader);
		}
	}
}
//?}