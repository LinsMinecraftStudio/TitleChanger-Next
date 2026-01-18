package me.mmmjjkx.titlechanger.fabric;

import io.github.lijinhong11.titlechanger.api.TitlePlaceholderExtension;
import it.unimi.dsi.fastutil.Pair;
import me.mmmjjkx.titlechanger.Constants;
import me.mmmjjkx.titlechanger.FileUtils;
import me.mmmjjkx.titlechanger.HttpUtils;
import io.github.lijinhong11.titlechanger.api.TitleExtensionSource;
import me.mmmjjkx.titlechanger.TitleProcessor;
import me.mmmjjkx.titlechanger.fabric.config.TCConfig;
import me.mmmjjkx.titlechanger.fabric.config.TCResourceSettings;
import me.mmmjjkx.titlechanger.fabric.screens.LaunchScreen;
import me.mmmjjkx.titlechanger.fabric.screens.UpdatableScreen;
import me.mmmjjkx.titlechanger.enums.UpdateCheckMode;
import me.mmmjjkx.titlechanger.fabric.utils.Reflects;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionResult;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Environment(EnvType.CLIENT)
public class TitleChangerFabric implements ClientModInitializer {
    public static TitleProcessor titleProcessor;
    public static final String HITOKOTO;

    public static final Logger LOGGER = LoggerFactory.getLogger("titlechanger");

    private static final File iconFolder = new File(FabricLoader.getInstance().getConfigDir().toFile(), Constants.ICON_FOLDER);

    private boolean checkUpdate = false;

    static {
        TitleExtensionSource.registerExtensions(FabricLoader.getInstance().getEntrypoints("titlechanger", TitlePlaceholderExtension.class));

        AutoConfig.register(TCResourceSettings.class, JanksonConfigSerializer::new);

        AutoConfig.register(TCConfig.class, GsonConfigSerializer::new).registerSaveListener((hl, c) -> {
            titleProcessor.restart();

            if (c.generalSettings.enabled) {
                titleProcessor.refresh(c.generalSettings.title);
                titleProcessor.startProcessing(c.generalSettings.updateInterval, Minecraft.getInstance().getWindow()::setTitle);
            }

            if (c.iconSettings.enabled) {
                Triple<ByteBuffer, IntBuffer, IntBuffer> icon = TitleChangerFabric.tryGetIcon();
                if (icon != null) {
                    IntBuffer w = icon.getMiddle();
                    IntBuffer h = icon.getRight();
                    try (GLFWImage.Buffer icons = GLFWImage.malloc(1)) {
                        GLFWImage iconImage = icons.get(0);
                        iconImage.set(w.get(0), h.get(0), icon.getLeft());

                        GLFW.glfwSetWindowIcon(Minecraft.getInstance().getWindow().handle(), icons);
                    }
                }
            }

            return InteractionResult.SUCCESS;
        });

        HITOKOTO = HttpUtils.getHikotoko(I18n.get("titlechanger.error.hitokoto"));
    }

    private static LocalDateTime start;

    @Nullable
    public static Triple<ByteBuffer, IntBuffer, IntBuffer> tryGetIcon() {
        if (getConfig().iconSettings.enabled) {
            boolean random = getConfig().iconSettings.randomIcons;
            String file;
            if (!random) {
                file = getConfig().iconSettings.icon;
            } else {
                String[] icons = iconFolder.list();

                if (icons == null) {
                    return null;
                }

                Random r = new Random();
                file = icons[r.nextInt(icons.length)];
            }

            if (file == null || file.isBlank()) {
                return null;
            }

            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer w = stack.mallocInt(1);
                IntBuffer h = stack.mallocInt(1);
                IntBuffer channels = stack.mallocInt(1);

                byte[] iconBytes = Files.readAllBytes(iconFolder.toPath().resolve(file));

                ByteBuffer buffer = ByteBuffer.allocateDirect(iconBytes.length).put(iconBytes).flip();
                ByteBuffer icon = STBImage.stbi_load_from_memory(buffer, w, h, channels, 4);

                if (icon == null) {
                    LOGGER.error("Failed to load image from path: {} - {}", file, STBImage.stbi_failure_reason());
                    return null;
                }

                return Triple.of(icon, w, h);
            } catch (IOException e) {
                return null;
            }
        }

        return null;
    }

    public static TCConfig getConfig() {
        return AutoConfig.getConfigHolder(TCConfig.class).getConfig();
    }

    public static TCResourceSettings getResourceSettings() {
        return AutoConfig.getConfigHolder(TCResourceSettings.class).getConfig();
    }

    public static String getStartTime(String format) {
        return DateTimeFormatter.ofPattern(format).format(start);
    }

    public static LocalDateTime getStartTime() {
        return start;
    }

    @Override
    public void onInitializeClient() {
        titleProcessor = new TitleProcessor();
        placeholderUpdates();

        ScreenEvents.BEFORE_INIT.register((client, screen, w, h) -> {
            if (screen instanceof TitleScreen) {
                if (getResourceSettings().enableWelcomeScreen) {
                    Minecraft.getInstance().setScreen(new LaunchScreen(new TitleScreen(), () -> {
                        Pair<String, List<String>> pair = FileUtils.readWelcomeText(FabricLoader.getInstance().getConfigDir().toFile(), Minecraft.getInstance().getLanguageManager().getSelected());
                        String title = pair.left();
                        return Component.literal(parseWelcomeTitle(title));
                    }, () -> {
                        Pair<String, List<String>> pair = FileUtils.readWelcomeText(FabricLoader.getInstance().getConfigDir().toFile(), Minecraft.getInstance().getLanguageManager().getSelected());
                        return pair.right();
                    }, () -> {
                        getResourceSettings().enableWelcomeScreen = false;
                        AutoConfig.getConfigHolder(TCResourceSettings.class).save();
                    }));

                    checkUpdate = true; //skip check update
                }

                if (getResourceSettings().checkUpdates && !checkUpdate) {
                    String ver = HttpUtils.getLastestModrinthVersion("fabric", getResourceSettings().modrinthProjectId, Reflects.getCurrentVersion());
                    if (ver != null && !ver.equals(getResourceSettings().modpackVersion)) {
                        client.setScreen(new UpdatableScreen(m -> {
                            if (m == UpdateCheckMode.ALLOW) {
                                Util.getPlatform().openUri("https://modrinth.com/project/" + getResourceSettings().modrinthProjectId);
                            }

                            if (m == UpdateCheckMode.NEVER) {
                                getResourceSettings().checkUpdates = false;
                                AutoConfig.getConfigHolder(TCResourceSettings.class).save();
                            }

                            Minecraft.getInstance().setScreen(new TitleScreen());
                        }, getResourceSettings().modpackName));
                    }

                    checkUpdate = true;
                }
            }
        });

        if (!iconFolder.exists()) {
            iconFolder.mkdirs();
        }
    }

    private void placeholderUpdates() {
        start = LocalDateTime.now();
    }

    public static String parseWelcomeTitle(String title) {
        title = Strings.CS.replace(title, "%modpackName%", getResourceSettings().modpackName);
        title = Strings.CS.replace(title, "%modpackVersion%", getResourceSettings().modpackVersion);
        return title;
    }
}
