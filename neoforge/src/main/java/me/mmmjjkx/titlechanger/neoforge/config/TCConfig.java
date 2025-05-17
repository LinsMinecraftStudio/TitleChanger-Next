package me.mmmjjkx.titlechanger.neoforge.config;

import me.mmmjjkx.titlechanger.Constants;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = Constants.CONFIG_FILE)
public class TCConfig implements ConfigData {
    @ConfigEntry.Gui.TransitiveObject
    @ConfigEntry.Category("general")
    public General generalSettings = new General();

    @ConfigEntry.Gui.TransitiveObject
    @ConfigEntry.Category("placeholder")
    public Placeholder placeholderSettings = new Placeholder();

    @ConfigEntry.Gui.TransitiveObject
    @ConfigEntry.Category("icon")
    public Icon iconSettings = new Icon();

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

    public static class Icon implements ConfigData {
        @ConfigEntry.Gui.RequiresRestart
        public boolean enabled = false;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.RequiresRestart
        public String icon = "example.png";

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.RequiresRestart
        public boolean randomIcons = false;
    }
}
