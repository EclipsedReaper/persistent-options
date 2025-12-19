package com.eclipse.persistent.neoforge;

import com.eclipse.persistent.PersistentOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.TickEvent;

@Mod.EventBusSubscriber(modid = PersistentOptions.MOD_ID, value = Dist.CLIENT)
public class ClientEventsNeoForge {

    private static boolean hasShownToast = false;
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft client = Minecraft.getInstance();
            if (!hasShownToast && client.screen instanceof TitleScreen) {
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
            }
        }
    }
}