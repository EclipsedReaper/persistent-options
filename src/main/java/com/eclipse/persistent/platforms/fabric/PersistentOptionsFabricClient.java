//? if fabric {
package com.eclipse.persistent.platforms.fabric;

import com.eclipse.persistent.PersistentOptions;
import dev.architectury.event.events.client.ClientGuiEvent;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.TitleScreen;

public class PersistentOptionsFabricClient implements ClientModInitializer {
    private static boolean hasShownToast = false;

    @Override
    public void onInitializeClient() {
        ClientGuiEvent.INIT_POST.register((screen, access) -> {
            if (!hasShownToast && screen instanceof TitleScreen) {
                PersistentOptions.SyncResult result = PersistentOptions.lastSyncResult;
                if (result != PersistentOptions.SyncResult.NONE
                        && (PersistentOptions.configManager.getDisplayStartupToast() || result == PersistentOptions.SyncResult.FAILED)) {
                    com.eclipse.persistent.util.UniversalToast.showToast(result.getComponent());
                }
                hasShownToast = true;
            }
        });
    }
}
//?}