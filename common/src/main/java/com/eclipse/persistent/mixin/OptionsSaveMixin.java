package com.eclipse.persistent.mixin;

import com.eclipse.persistent.PersistentOptions;
import com.eclipse.persistent.util.OptionMerger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Files;
import java.nio.file.Path;

@Mixin(Options.class)
public class OptionsSaveMixin {

    @Inject(method = "save", at = @At("TAIL"))
    private void onSave(CallbackInfo ci) {
        Path globalFile = PersistentOptions.customOptionsFolder.resolve("options.txt");
        Path localFile = Minecraft.getInstance().gameDirectory.toPath().resolve("options.txt");

        PersistentOptions.scheduleSync(() -> {
            if (Files.exists(localFile)) {
                OptionMerger.smartMerge(localFile, globalFile);
            }
        });
    }
}