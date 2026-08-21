//? if forge {
/*package com.eclipse.persistent.platforms.forge;

import com.eclipse.persistent.config.ConfigScreen;
import com.eclipse.persistent.PersistentOptions;
import com.eclipse.persistent.ModPlatform;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

@Mod("persistent_options")
public class PersistentOptionsForge {
	public PersistentOptionsForge() {
		PersistentOptions.entrypoint(new ForgePlatform());
		ModLoadingContext.get().registerExtensionPoint(
				ConfigScreenHandler.ConfigScreenFactory.class,
				() -> new ConfigScreenHandler.ConfigScreenFactory(
						(client, parent) -> new ConfigScreen(parent)
				)
		);
	}

	public static class ForgePlatform implements ModPlatform {
		@Override
		public String getModloader() {
			return "LexForge";
		}

		@Override
		public boolean isModLoaded(String modId) {
			return ModList.get().isLoaded(modId);
		}
	}
}
*///?}