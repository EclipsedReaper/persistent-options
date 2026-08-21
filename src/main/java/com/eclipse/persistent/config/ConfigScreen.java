package com.eclipse.persistent.config;

import com.eclipse.persistent.PersistentOptions;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfigScreen extends Screen {
    private final Screen parent;

    public ConfigScreen(Screen parent) {
        super(Component.literal("Persistent Options"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        ConfigManager manager = PersistentOptions.configManager;

        // 10x gap in middle, 2y gap between buttons, 100x wide buttons, 20x tall buttons

        this.addRenderableWidget(
            Button.builder(Component.literal("Display Toast on Startup: " + (manager.getDisplayStartupToast() ? "ON" : "OFF")),
                button -> {
                    manager.setDisplayStartupToast(!manager.getDisplayStartupToast());
                    button.setMessage(Component.literal("Display Toast on Startup: " + (manager.getDisplayStartupToast() ? "ON" : "OFF")));
                })
                .bounds(centerX - 205, centerY - 20, 200, 20)
                .build()
        );

        this.addRenderableWidget(
            Button.builder(Component.literal("Sync Resource Packs: " + (manager.getSyncResourcePacks() ? "ON" : "OFF")),
                button -> {
                    manager.setSyncResourcePacks(!manager.getSyncResourcePacks());
                    button.setMessage(Component.literal("Sync Resource Packs: " + (manager.getSyncResourcePacks() ? "ON" : "OFF")));
                })
                .bounds(centerX + 5, centerY - 20, 200, 20)
                .build()
        );

        this.addRenderableWidget(
                Button.builder(
                        Component.literal("Done"), button -> {
                            manager.saveConfig();
                            this.onClose();
                        })
                        .bounds(centerX - 100, centerY + 20, 200, 20)
                        .build()
        );
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        //? if <1.21.1 {
            this.renderBackground(guiGraphics);
        //?}
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
    }
}