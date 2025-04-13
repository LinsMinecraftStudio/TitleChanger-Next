package me.mmmjjkx.titlechanger.fabric;

import me.mmmjjkx.titlechanger.fabric.config.TCConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Environment(EnvType.CLIENT)
public class TitleChangerFabric implements ClientModInitializer {
    public static final TitleProcessor titleProcessor;
    public static final String HITOKOTO;

    static {
        titleProcessor = new TitleProcessor();

        AutoConfig.register(TCConfig.class, GsonConfigSerializer::new).registerSaveListener((h, c) -> {
            titleProcessor.shutdown();
            titleProcessor.startProcessing(c.generalSettings.title, 1000, t -> Minecraft.getInstance().getWindow().setTitle(t));
            return InteractionResult.SUCCESS;
        });

        HITOKOTO = HttpUtils.getHikotoko(getConfig().placeholderSettings.hitokotoInternational);
    }

    private static LocalDateTime start;

    public static TCConfig getConfig() {
        return AutoConfig.getConfigHolder(TCConfig.class).getConfig();
    }

    public static String getStartTime(String format) {
        return DateTimeFormatter.ofPattern(format).format(start);
    }

    @Override
    public void onInitializeClient() {
        placeholderUpdates();
    }

    private void placeholderUpdates() {
        start = LocalDateTime.now();
    }
}
