package me.mmmjjkx.titlechanger.fabric.mixin;

import me.mmmjjkx.titlechanger.fabric.TitleChangerFabric;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class ClientMixin {
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

    @Inject(method = "onGameLoadFinished", at = @At("RETURN"))
    public void init(CallbackInfo ci) {
        if (!TitleChangerFabric.getConfig().generalSettings.enabled) {
            return;
        }

        TitleChangerFabric.titleProcessor.startProcessing(
                TitleChangerFabric.getConfig().generalSettings.title,
                TitleChangerFabric.getConfig().generalSettings.updateInterval,
                t -> Minecraft.getInstance().getWindow().setTitle(t)
        );
    }
}
