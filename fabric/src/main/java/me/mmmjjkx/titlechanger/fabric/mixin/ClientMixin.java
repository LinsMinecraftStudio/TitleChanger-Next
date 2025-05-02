package me.mmmjjkx.titlechanger.fabric.mixin;

import me.mmmjjkx.titlechanger.fabric.TitleChangerFabric;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Mixin(Minecraft.class)
public abstract class ClientMixin {
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
    private String modifyStartingWindowTitle(String title) {
        if (!TitleChangerFabric.getConfig().generalSettings.enabled) {
            return title;
        }

        CompletableFuture.runAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
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
