package me.mmmjjkx.titlechanger.fabric.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "titlechanger")
public class TCConfig implements ConfigData {
    @ConfigEntry.Gui.TransitiveObject
    @ConfigEntry.Category("general")
    public General generalSettings = new General();

    @ConfigEntry.Gui.TransitiveObject
    @ConfigEntry.Category("placeholder")
    public Placeholder placeholderSettings = new Placeholder();

    public static class General implements ConfigData {
        @ConfigEntry.Gui.RequiresRestart
        public boolean enabled = true;

        @ConfigEntry.Gui.Tooltip(count = 2)
        public String title = "Minecraft* %mcver%";

        @ConfigEntry.Gui.Tooltip(count = 2)
        public long updateInterval = 1000;
    }

    public static class Placeholder implements ConfigData {
        public String defaultTimeFormat = "yyyy-MM-dd HH:mm:ss";

        @ConfigEntry.Gui.Tooltip(count = 4)
        public String playTimeFormat = "%h:%m:%s";
    }
}
