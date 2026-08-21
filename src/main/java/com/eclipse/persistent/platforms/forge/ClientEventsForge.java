//? if forge {
/*package com.eclipse.persistent.platforms.forge;

import com.eclipse.persistent.PersistentOptions;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PersistentOptions.MODID, value = Dist.CLIENT)
public class ClientEventsForge {

    private static boolean hasShownToast = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getInstance();
            if (!hasShownToast && mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen) {

                PersistentOptions.SyncResult result = PersistentOptions.lastSyncResult;
                if (result != PersistentOptions.SyncResult.NONE
                        && (PersistentOptions.configManager.getDisplayStartupToast() || result == PersistentOptions.SyncResult.FAILED)) {
                    com.eclipse.persistent.util.UniversalToast.showToast(result.getComponent());
                }

                hasShownToast = true;
            }
        }
    }
}
*///?}