package me.mmmjjkx.titlechanger.fabric.config;

import me.mmmjjkx.titlechanger.Constants;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = Constants.RESOURCE_SETTINGS_FILE)
public class TCResourceSettings implements ConfigData {
    public String modpackName = "Example Modpack";
    public String modpackVersion = "1.0.0";
    @Comment("Only usable in modrinth")
    public boolean checkUpdates = false;
    @Comment("The project id of the modpack in modrinth")
    public String modrinthProjectId = "";

    public boolean enableWelcomeScreen = false;
    public boolean alwaysLaunchWelcomeScreen = false;
    public boolean welcomeScreenOpened = false;
}
