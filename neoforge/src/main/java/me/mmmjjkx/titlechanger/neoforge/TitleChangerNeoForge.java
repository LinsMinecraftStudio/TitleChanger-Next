package me.mmmjjkx.titlechanger.neoforge;

import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import io.github.lijinhong11.titlechanger.api.TitleExtensionSource;
import it.unimi.dsi.fastutil.Pair;
import me.mmmjjkx.titlechanger.Constants;
import me.mmmjjkx.titlechanger.TitleProcessor;
import me.mmmjjkx.titlechanger.enums.UpdateCheckMode;
import me.mmmjjkx.titlechanger.neoforge.bulitin.TCPlaceholders;
import me.mmmjjkx.titlechanger.neoforge.config.TCConfig;
import me.mmmjjkx.titlechanger.neoforge.config.TCResourceSettings;
import me.mmmjjkx.titlechanger.neoforge.screens.LaunchScreen;
import me.mmmjjkx.titlechanger.neoforge.screens.UpdatableScreen;
import me.mmmjjkx.titlechanger.utils.FileUtils;
import me.mmmjjkx.titlechanger.utils.HttpUtils;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.AutoConfigClient;
import me.shedaniel.autoconfig.gui.ConfigScreenProvider;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
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

@Mod(TitleChangerNeoForge.MODID)
@EventBusSubscriber(modid = TitleChangerNeoForge.MODID)
@SuppressWarnings({"unsafe", "deprecation"})
public class TitleChangerNeoForge {
    public static final String MODID = "titlechanger";
    public static final TitleProcessor titleProcessor = new TitleProcessor();

    public static final String HITOKOTO;

    public static volatile String FINAL_TITLE = "";

    private static final Logger LOGGER = LoggerFactory.getLogger("TitleChanger");
    private static final File iconFolder = new File(FMLPaths.CONFIGDIR.get().toFile(), Constants.ICON_FOLDER);

    private static LocalDateTime start;

    static {
        TitleExtensionSource.registerExtension(new TCPlaceholders());

        AutoConfig.register(TCResourceSettings.class, JanksonConfigSerializer::new);

        AutoConfig.register(TCConfig.class, GsonConfigSerializer::new).registerSaveListener((_, c) -> {
            titleProcessor.restart();

            if (c.generalSettings.enabled) {
                if (c.generalSettings.randomTitle && !c.generalSettings.randomTitles.isEmpty()) {
                    FINAL_TITLE = c.generalSettings.randomTitles.get(new Random().nextInt(c.generalSettings.randomTitles.size()));
                } else {
                    FINAL_TITLE = c.generalSettings.title;
                }
                titleProcessor.refresh(FINAL_TITLE);
                titleProcessor.startProcessing(c.generalSettings.updateInterval, s -> Minecraft.getInstance().getWindow().setTitle(parseTPA(s)));
            }

            if (c.iconSettings.enabled) {
                Triple<ByteBuffer, IntBuffer, IntBuffer> icon = TitleChangerNeoForge.tryGetIcon();
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
        changeTitle();
    }

    private static void changeTitle() {
        if (getConfig().generalSettings.randomTitle && !getConfig().generalSettings.randomTitles.isEmpty()) {
            FINAL_TITLE = getConfig().generalSettings.randomTitles.get(new Random().nextInt(getConfig().generalSettings.randomTitles.size()));
        } else {
            FINAL_TITLE = getConfig().generalSettings.title;
        }
    }

    public TitleChangerNeoForge(ModContainer modContainer) {
        start = LocalDateTime.now();

        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ((_, parent) -> {
            ConfigScreenProvider<TCConfig> provider = (ConfigScreenProvider<TCConfig>) AutoConfigClient.getConfigScreen(TCConfig.class, parent);
            provider.setI13nFunction(_ -> "titlechanger");

            return provider.get();
        }));
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

    public static String parseWelcomeTitle(String title) {
        title = Strings.CS.replace(title, "%modpackName%", getResourceSettings().modpackName);
        title = Strings.CS.replace(title, "%modpackVersion%", getResourceSettings().modpackVersion);
        return title;
    }

    public static String parseTPA(String s) {
        if (ModList.get().isLoaded("placeholder_api_neoforge")) {
            ParserContext ctx = ParserContext.of();
            if (Minecraft.getInstance().player != null) {
                ctx = PlaceholderContext.of(Minecraft.getInstance().player).asParserContext();
            }

            return Placeholders.COMMON_PLACEHOLDER_PARSER.parseComponent(s, ctx).getString();
        } else {
            return s;
        }
    }

    @SubscribeEvent
    public static void onOpen(ScreenEvent.Opening e) {
        boolean checkUpdate = false;
        if (e.getNewScreen() instanceof TitleScreen) {
            if (getResourceSettings().enableWelcomeScreen) {
                e.setNewScreen(new LaunchScreen(new TitleScreen(), () -> {
                    Pair<String, List<String>> pair = FileUtils.readWelcomeText(FMLPaths.CONFIGDIR.get().toFile(), Minecraft.getInstance().getLanguageManager().getSelected());
                    String title = pair.left();
                    return Component.literal(parseWelcomeTitle(title));
                }, () -> {
                    Pair<String, List<String>> pair = FileUtils.readWelcomeText(FMLPaths.CONFIGDIR.get().toFile(), Minecraft.getInstance().getLanguageManager().getSelected());
                    return pair.right();
                }, () -> {
                    getResourceSettings().enableWelcomeScreen = false;
                    AutoConfig.getConfigHolder(TCResourceSettings.class).save();
                }));

                checkUpdate = true; //skip check update
            }

            if (getResourceSettings().checkUpdates && !checkUpdate) {
                String ver = HttpUtils.getLatestModrinthVersion("neoforge", getResourceSettings().modrinthProjectId, SharedConstants.getCurrentVersion().name());
                if (ver != null && !ver.equals(getResourceSettings().modpackVersion)) {
                    e.setNewScreen(new UpdatableScreen(m -> {
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
            }
        }
    }
}
