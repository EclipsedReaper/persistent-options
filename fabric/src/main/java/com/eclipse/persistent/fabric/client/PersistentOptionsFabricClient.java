package com.eclipse.persistent.fabric.client;

import com.eclipse.persistent.PersistentOptions;
import net.fabricmc.api.ClientModInitializer;
import dev.architectury.event.events.client.ClientGuiEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

public class PersistentOptionsFabricClient implements ClientModInitializer {

    private static boolean hasShownToast = false;
    @Override
    public void onInitializeClient() {
        ClientGuiEvent.INIT_POST.register((screen, access) -> {
            if (!hasShownToast && screen instanceof TitleScreen) {
                Minecraft client = Minecraft.getInstance();
                PersistentOptions.SyncResult result = PersistentOptions.lastSyncResult;
                if (result != PersistentOptions.SyncResult.NONE) {
                    client.getToasts().addToast(SystemToast.multiline(
                            client,
                            SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                            Component.literal("Persistent Options"),
                            Component.literal(result.getMessage())
                    ));
                }
                hasShownToast = true;
                PersistentOptions.LOGGER.info("Fabric Toast notification sent.");
            }
        });
    }
}