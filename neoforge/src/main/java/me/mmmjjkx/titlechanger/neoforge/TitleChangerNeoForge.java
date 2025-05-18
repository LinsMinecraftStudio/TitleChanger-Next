package me.mmmjjkx.titlechanger.neoforge;

import com.google.common.base.Strings;
import com.mojang.logging.LogUtils;
import io.github.lijinhong11.titlechanger.api.TitleExtensionSource;
import it.unimi.dsi.fastutil.Pair;
import me.mmmjjkx.titlechanger.Constants;
import me.mmmjjkx.titlechanger.neoforge.screens.LaunchScreen;
import me.mmmjjkx.titlechanger.neoforge.bulitin.TCPlaceholders;
import me.mmmjjkx.titlechanger.neoforge.config.TCConfig;
import me.mmmjjkx.titlechanger.HttpUtils;
import me.mmmjjkx.titlechanger.TitleProcessor;
import me.mmmjjkx.titlechanger.neoforge.config.TCResourceSettings;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.gui.ConfigScreenProvider;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.ConfigScreenHandler;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;

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
@OnlyIn(Dist.CLIENT)
@SuppressWarnings({"unsafe", "deprecation"})
public class TitleChangerNeoForge {
    public static final String HITOKOTO;

    public static final String MODID = "titlechanger";

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final File iconFolder = new File(FMLPaths.CONFIGDIR.get().toFile(), Constants.ICON_FOLDER);

    private static LocalDateTime start;
    private boolean checkUpdate = false;

    public static TitleProcessor titleProcessor;

    static {
        TitleExtensionSource.registerExtension(new TCPlaceholders());

        titleProcessor = new TitleProcessor();

        AutoConfig.register(TCResourceSettings.class, JanksonConfigSerializer::new);

        AutoConfig.register(TCConfig.class, GsonConfigSerializer::new).registerSaveListener((hl, c) -> {
            titleProcessor.shutdown();

            if (c.generalSettings.enabled) {
                titleProcessor.startProcessing(c.generalSettings.title, 1000, t -> Minecraft.getInstance().getWindow().setTitle(t));
            }

            if (c.iconSettings.enabled) {
                Triple<ByteBuffer, IntBuffer, IntBuffer> icon = TitleChangerNeoForge.tryGetIcon();
                if (icon != null) {
                    IntBuffer w = icon.getMiddle();
                    IntBuffer h = icon.getRight();
                    try (GLFWImage.Buffer icons = GLFWImage.malloc(1)) {
                        GLFWImage iconImage = icons.get(0);
                        iconImage.set(w.get(0), h.get(0), icon.getLeft());

                        GLFW.glfwSetWindowIcon(Minecraft.getInstance().getWindow().getWindow(), icons);
                    }
                }
            }
            AutoConfig.getGuiRegistry(TCConfig.class);

            return InteractionResult.SUCCESS;
        });

        HITOKOTO = HttpUtils.getHikotoko(I18n.get("titlechanger.error.hitokoto"));
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

            if (Strings.isNullOrEmpty(file)) {
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

    public TitleChangerNeoForge(ModContainer modContainer) {
        start = LocalDateTime.now();

        NeoForge.EVENT_BUS.register(this);

        File welcome = new File(FMLPaths.CONFIGDIR.get().toFile(), "titlechanger/welcome_txt/welcome.txt");

        modContainer.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () ->
            new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> {
                ConfigScreenProvider<TCConfig> provider = (ConfigScreenProvider<TCConfig>) AutoConfig.getConfigScreen(TCConfig.class, parent);
                provider.setI13nFunction(a -> "titlechanger");

                return provider.get();
            }
        ));
    }

    @SubscribeEvent
    public void onOpen(ScreenEvent.Opening e) {
        if (e.getNewScreen() instanceof TitleScreen) {
            Pair<String, List<String>> pair = Constants.readWelcomeText(FMLPaths.CONFIGDIR.get().toFile(), Minecraft.getInstance().getLanguageManager().getSelected());
            String title = pair.left();
            List<String> lines = pair.right();
            e.setNewScreen(new LaunchScreen(new TitleScreen(), Component.literal(title), lines));

            /*
            if (TitleChangerNeoForge.getResourceSettings().checkUpdates && !checkUpdate) {
                String ver = HttpUtils.getLastestModrinthVersion("neoforge", TitleChangerNeoForge.getResourceSettings().modrinthProjectId, SharedConstants.getCurrentVersion().getName());
                if (ver != null && !ver.equals(TitleChangerNeoForge.getResourceSettings().modpackVersion)) {
                    e.setNewScreen(new UpdatableScreen(m -> {
                        if (m == UpdateCheckMode.ALLOW) {
                            Util.getPlatform().openUri("https://modrinth.com/project/" + TitleChangerNeoForge.getResourceSettings().modrinthProjectId);
                        }

                        if (m == UpdateCheckMode.NEVER) {
                            TitleChangerNeoForge.getResourceSettings().checkUpdates = false;
                            AutoConfig.getConfigHolder(TCResourceSettings.class).save();
                        }

                        Minecraft.getInstance().setScreen(new TitleScreen());
                    }, TitleChangerNeoForge.getResourceSettings().modpackName));
                }

                checkUpdate = true;
            }

             */
        }
    }
}
