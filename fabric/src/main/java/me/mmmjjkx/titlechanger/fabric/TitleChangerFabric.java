package me.mmmjjkx.titlechanger.fabric;

import io.github.lijinhong11.titlechanger.api.TitlePlaceholderExtension;
import me.mmmjjkx.titlechanger.fabric.config.TCConfig;
import me.mmmjjkx.titlechanger.fabric.utils.HttpUtils;
import me.mmmjjkx.titlechanger.fabric.utils.TitleProcessor;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

        HITOKOTO = HttpUtils.getHikotoko();
    }

    private static LocalDateTime start;

    public static TCConfig getConfig() {
        return AutoConfig.getConfigHolder(TCConfig.class).getConfig();
    }

    public static String getStartTime(String format) {
        return DateTimeFormatter.ofPattern(format).format(start);
    }

    public static LocalDateTime getStartTime() {
        return start;
    }

    @Override
    public void onInitializeClient() {
        placeholderUpdates();
        commandRegister();
    }

    public void commandRegister() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("titlechanger-ext")
                .executes(ctx -> {
                    List<TitlePlaceholderExtension> exts = FabricLoader.getInstance().getEntrypoints("titlechanger", TitlePlaceholderExtension.class);
                    MutableComponent send = Component.translatable("titlechanger.command.extensions");
                    MutableComponent appends = Component.empty();
                    for (var ext : exts) {
                        Component append = Component.literal(ext.getExtensionName()).withStyle(ChatFormatting.GREEN);
                        appends.append(append);

                        if (exts.get(exts.size() - 1) != ext) {
                            appends.append(Component.literal(", "));
                        }
                    }

                    send.append(appends);

                    ctx.getSource().sendFeedback(send);

                    return 1;
                })));
    }

    private void placeholderUpdates() {
        start = LocalDateTime.now();
    }
}
