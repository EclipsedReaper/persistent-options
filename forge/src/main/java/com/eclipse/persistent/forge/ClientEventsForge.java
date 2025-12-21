package com.eclipse.persistent.forge;

import com.eclipse.persistent.PersistentOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

@Mod.EventBusSubscriber(modid = PersistentOptions.MOD_ID, value = Dist.CLIENT)
public class ClientEventsForge {

    private static boolean hasShownToast = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getInstance();
            if (!hasShownToast && mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen) {

                PersistentOptions.SyncResult result = PersistentOptions.lastSyncResult;
                if (result != PersistentOptions.SyncResult.NONE) {
                    showUniversalToast(mc, result.getMessage());
                }

                hasShownToast = true;
            }
        }
    }

    private static void showUniversalToast(Minecraft mc, String message) {
        try {
            Method addMethod = null;
            Class<?> managerClass = null;
            Class<?> typeClass = null;

            for (Method m : SystemToast.class.getDeclaredMethods()) {
                if (Modifier.isStatic(m.getModifiers()) && m.getParameterCount() == 4) {
                    Class<?>[] params = m.getParameterTypes();
                    if (Component.class.isAssignableFrom(params[2]) && Component.class.isAssignableFrom(params[3])) {
                        addMethod = m;
                        managerClass = params[0];
                        typeClass = params[1];
                        break;
                    }
                }
            }

            if (addMethod == null) return;

            Object manager = null;
            for (Method m : Minecraft.class.getMethods()) {
                if (m.getParameterCount() == 0 && m.getReturnType().equals(managerClass)) {
                    try {
                        manager = m.invoke(mc);
                        if (manager != null) break;
                    } catch (Exception ignored) {}
                }
            }
            if (manager == null) return;

            Object typeObj = null;
            for (Field f : typeClass.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers()) && f.getType().equals(typeClass)) {
                    try {
                        f.setAccessible(true);
                        Object val = f.get(null);
                        if (val != null) {
                            if (f.getName().toUpperCase().contains("PERIODIC")) {
                                typeObj = val;
                                break;
                            }
                            if (typeObj == null) typeObj = val;
                        }
                    } catch (Exception ignored) {}
                }
            }
            if (typeObj == null) return;

            addMethod.setAccessible(true);
            addMethod.invoke(null, manager, typeObj,
                    Component.literal("Persistent Options"),
                    Component.literal(message));

        } catch (Exception e) {
            PersistentOptions.LOGGER.error("Failed to show toast", e);
        }
    }
}