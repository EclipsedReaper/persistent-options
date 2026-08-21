package com.eclipse.persistent.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static com.eclipse.persistent.PersistentOptions.LOGGER;

public class UniversalToast {
    public static void showToast(Component message) {
        //? if <1.20.4 {
        SystemToast.add(
                Minecraft.getInstance().getToasts(),
                SystemToast.SystemToastIds.PERIODIC_NOTIFICATION,
                Component.literal("Persistent Options"),
                message
        );
        //?} else if =1.20.4 {
        /*SystemToast.add(
                Minecraft.getInstance().getToasts(),
                SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                Component.literal("Persistent Options"),
                message
        );
        *///?} else if =1.21.1 {
        /*SystemToast.add(
                Minecraft.getInstance().getToasts(),
                SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                Component.literal("Persistent Options"),
                message
        );
        *///?} else if >1.21.2 {
        /*SystemToast.add(
                Minecraft.getInstance().getToastManager(),
                SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                Component.literal("Persistent Options"),
                message
        );
        *///?}
    }
}
