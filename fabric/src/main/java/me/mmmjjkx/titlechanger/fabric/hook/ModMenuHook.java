package me.mmmjjkx.titlechanger.fabric.hook;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.mmmjjkx.titlechanger.fabric.config.TCConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.gui.ConfigScreenProvider;

@SuppressWarnings("deprecation")
public class ModMenuHook implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigScreenProvider<TCConfig> provider = (ConfigScreenProvider<TCConfig>) AutoConfig.getConfigScreen(TCConfig.class, parent);
            provider.setI13nFunction(a -> "titlechanger");

            return provider.get();
        };
    }
}