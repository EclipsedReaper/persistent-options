//? if neoforge {
/*package com.eclipse.persistent.platforms.neoforge;

import com.eclipse.persistent.PersistentOptions;
import dev.architectury.event.events.client.ClientGuiEvent;
import net.minecraft.client.gui.screens.TitleScreen;

public class ClientEventsNeoForge {

    private static boolean hasShownToast = false;

    public static void init() {
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
*///?}