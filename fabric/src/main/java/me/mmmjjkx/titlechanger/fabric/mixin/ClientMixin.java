package me.mmmjjkx.titlechanger.fabric.mixin;

import com.mojang.blaze3d.platform.Window;
import me.mmmjjkx.titlechanger.fabric.TitleChangerFabric;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.tuple.Triple;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Mixin(Minecraft.class)
public abstract class ClientMixin {
    @Shadow public abstract Window getWindow();

    @Redirect(method = "setScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;updateTitle()V"))
    public void updateTitleTC(Minecraft instance) {
        if (!TitleChangerFabric.getConfig().generalSettings.enabled) {
            instance.updateTitle();
        }
    }

    @Redirect(method = "updateLevelInEngines", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;updateTitle()V"))
    public void updateTitleTC2(Minecraft instance) {
        if (!TitleChangerFabric.getConfig().generalSettings.enabled) {
            instance.updateTitle();
        }
    }

    //It makes the title show when the game window shown. Yay!
    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/VirtualScreen;newWindow(Lcom/mojang/blaze3d/platform/DisplayData;Ljava/lang/String;Ljava/lang/String;)Lcom/mojang/blaze3d/platform/Window;"), index = 2)
    private String modifyStartingWindow(String title) {
        if (!TitleChangerFabric.getConfig().generalSettings.enabled) {
            return title;
        }

        CompletableFuture.runAsync(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (TitleChangerFabric.getConfig().iconSettings.enabled) {
                Triple<ByteBuffer, IntBuffer, IntBuffer> icon = TitleChangerFabric.tryGetIcon();
                if (icon != null) {
                    IntBuffer w = icon.getMiddle();
                    IntBuffer h = icon.getRight();
                    try (GLFWImage.Buffer icons = GLFWImage.malloc(1)) {
                        GLFWImage iconImage = icons.get(0);
                        iconImage.set(w.get(0), h.get(0), icon.getLeft());

                        GLFW.glfwSetWindowIcon(getWindow().getWindow(), icons);
                    }
                }
            }

            TitleChangerFabric.titleProcessor.startProcessing(
                    TitleChangerFabric.getConfig().generalSettings.title,
                    TitleChangerFabric.getConfig().generalSettings.updateInterval,
                    Minecraft.getInstance().getWindow()::setTitle
            );
        });

        return TitleChangerFabric.titleProcessor.firstParse(TitleChangerFabric.getConfig().generalSettings.title);
    }
}
