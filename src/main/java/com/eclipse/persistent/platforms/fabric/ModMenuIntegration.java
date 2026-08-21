//? if fabric {
package com.eclipse.persistent.platforms.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.eclipse.persistent.config.ConfigScreen;

public class ModMenuIntegration implements ModMenuApi {
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ConfigScreen::new;
    }
}
//?}
