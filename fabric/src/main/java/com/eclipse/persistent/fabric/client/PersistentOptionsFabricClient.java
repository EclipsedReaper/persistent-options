package com.eclipse.persistent.fabric.client;

import com.eclipse.persistent.PersistentOptions;
import dev.architectury.event.events.client.ClientGuiEvent;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Method;

public class PersistentOptionsFabricClient implements ClientModInitializer {

    private static boolean hasShownToast = false;

    @Override
    public void onInitializeClient() {
        ClientGuiEvent.INIT_POST.register((screen, access) -> {
            if (!hasShownToast && screen instanceof TitleScreen) {

                PersistentOptions.SyncResult result = PersistentOptions.lastSyncResult;
                if (result != PersistentOptions.SyncResult.NONE) {
                    showUniversalToast(result.getMessage());
                }
                hasShownToast = true;
            }
        });
    }

    private static void showUniversalToast(String message) {
        try {
            Minecraft mc = Minecraft.getInstance();

            Class<?> managerClass = null;
            String[] mgrNames = {
                    "net.minecraft.client.gui.components.toasts.ToastManager",
                    "net.minecraft.client.toast.ToastManager",
                    "net.minecraft.class_374"
            };
            for (String n : mgrNames) {
                try { managerClass = Class.forName(n); break; } catch (Exception ignored) {}
            }
            if (managerClass == null) return;

            Object manager = null;
            for (Method m : mc.getClass().getMethods()) {
                if (m.getParameterCount() == 0 && m.getReturnType().equals(managerClass)) {
                    try { manager = m.invoke(mc); if (manager != null) break; } catch (Exception ignored) {}
                }
            }
            if (manager == null) return;

            Class<?> sysToast = null;
            String[] sysNames = {
                    "net.minecraft.client.gui.components.toasts.SystemToast",
                    "net.minecraft.client.toast.SystemToast",
                    "net.minecraft.class_370"
            };
            for (String n : sysNames) {
                try { sysToast = Class.forName(n); break; } catch (Exception ignored) {}
            }
            if (sysToast == null) return;

            Method addMethod = null;
            Class<?> typeClass = null;
            for (Method m : sysToast.getDeclaredMethods()) {
                if (java.lang.reflect.Modifier.isStatic(m.getModifiers()) && m.getParameterCount() == 4) {
                    if (m.getParameterTypes()[0].isAssignableFrom(managerClass)) {
                        addMethod = m;
                        typeClass = m.getParameterTypes()[1];
                        break;
                    }
                }
            }
            if (addMethod == null || typeClass == null) return;

            Object typeObj = null;
            for (java.lang.reflect.Field f : typeClass.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(f.getModifiers()) && f.getType().equals(typeClass)) {
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
            PersistentOptions.LOGGER.warn("Failed to show toast: ", e);
        }
    }
}