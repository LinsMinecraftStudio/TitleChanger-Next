package me.mmmjjkx.titlechanger.neoforge.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.platform.Window;
import me.mmmjjkx.titlechanger.neoforge.TitleChangerNeoForge;
import me.mmmjjkx.titlechanger.utils.ImageUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.main.GameConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

@Mixin(Minecraft.class)
public abstract class ClientMixin {
    @Shadow
    @Final
    public Options options;
    @Shadow
    @Final
    private Window window;

    @WrapMethod(method = "updateTitle")
    public void updateTitleTC(Operation<Void> original) {
        if (!TitleChangerNeoForge.getConfig().generalSettings.enabled) {
            original.call();
        }
    }

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;<init>(Lcom/mojang/blaze3d/platform/WindowEventHandler;Lcom/mojang/blaze3d/platform/DisplayData;Ljava/lang/String;ZLjava/lang/String;Lcom/mojang/blaze3d/platform/MonitorManager;Lcom/mojang/renderpearl/api/device/GpuBackend;I)V"), index = 4)
    private String startingSettings(String title) {
        if (!TitleChangerNeoForge.getConfig().generalSettings.enabled) {
            return title;
        }

        return TitleChangerNeoForge.titleProcessor.firstParse(TitleChangerNeoForge.FINAL_TITLE);
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;resizeGui()V"))
    private void setup(GameConfig gameConfig, CallbackInfo ci) {
        CompletableFuture.runAsync(() -> {
            if (TitleChangerNeoForge.getConfig().iconSettings.enabled) {
                ImageUtils.IconData icon = TitleChangerNeoForge.tryGetIcon();
                if (icon != null) {
                    ImageUtils.setWindowIcon(icon);
                }
            }

            if (TitleChangerNeoForge.getConfig().generalSettings.enabled) {
                TitleChangerNeoForge.titleProcessor.startProcessing(TitleChangerNeoForge.getConfig().generalSettings.updateInterval, window::setTitle);
            }

            if (TitleChangerNeoForge.getResourceSettings().enableWelcomeScreen) {
                options.onboardingAccessibilityFinished();
            }
        });
    }
}
