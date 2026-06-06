package me.mmmjjkx.titlechanger.fabric.mixin;

import com.mojang.blaze3d.platform.Window;
import me.mmmjjkx.titlechanger.fabric.TitleChangerFabric;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import org.apache.commons.lang3.tuple.Triple;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.CompletableFuture;

@Mixin(Minecraft.class)
public abstract class ClientMixin {
    @Shadow
    @Final
    private Window window;

    @Inject(method = "updateTitle", at = @At("HEAD"), cancellable = true)
    public void updateTitleTC(CallbackInfo ci) {
        if (TitleChangerFabric.getConfig().generalSettings.enabled) {
            ci.cancel();
        }
    }

    // It makes the title shows when the game window shown. Yay!
    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;<init>(Lcom/mojang/blaze3d/platform/WindowEventHandler;Lcom/mojang/blaze3d/platform/DisplayData;Ljava/lang/String;Ljava/lang/String;Lcom/mojang/blaze3d/systems/GpuBackend;)V"), index = 3)
    private String startingSettings(String title) {
        if (!TitleChangerFabric.getConfig().generalSettings.enabled) {
            return title;
        }

        return TitleChangerFabric.parseTPA(TitleChangerFabric.titleProcessor.firstParse(TitleChangerFabric.FINAL_TITLE));
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;resizeGui()V"))
    private void setup(GameConfig gameConfig, CallbackInfo ci) {
        CompletableFuture.runAsync(() -> {
            if (TitleChangerFabric.getConfig().iconSettings.enabled) {
                Triple<ByteBuffer, IntBuffer, IntBuffer> icon = TitleChangerFabric.tryGetIcon();
                if (icon != null) {
                    IntBuffer w = icon.getMiddle();
                    IntBuffer h = icon.getRight();
                    try (GLFWImage.Buffer icons = GLFWImage.malloc(1)) {
                        GLFWImage iconImage = icons.get(0);
                        iconImage.set(w.get(0), h.get(0), icon.getLeft());

                        GLFW.glfwSetWindowIcon(window.handle(), icons);
                    }
                }
            }

            TitleChangerFabric.titleProcessor.startProcessing(TitleChangerFabric.getConfig().generalSettings.updateInterval, s -> {
                window.setTitle(TitleChangerFabric.parseTPA(s));
            });
        });
    }
}
