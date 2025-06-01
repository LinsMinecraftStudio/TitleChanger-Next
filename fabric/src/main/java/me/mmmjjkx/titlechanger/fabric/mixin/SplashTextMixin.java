package me.mmmjjkx.titlechanger.fabric.mixin;

import me.mmmjjkx.titlechanger.FileUtils;
import me.mmmjjkx.titlechanger.enums.SplashTextMode;
import me.mmmjjkx.titlechanger.fabric.TitleChangerFabric;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.User;
import net.minecraft.client.resources.SplashManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(SplashManager.class)
public class SplashTextMixin {
    @Shadow @Final private List<String> splashes;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void pushSplashText(User user, CallbackInfo ci) {
        if (TitleChangerFabric.getConfig().splashTextSettings.enabled) {
            SplashTextMode mode = TitleChangerFabric.getConfig().splashTextSettings.mode;
            List<String> splash = FileUtils.readSplashText(FabricLoader.getInstance().getConfigDir().toFile());
            switch (mode) {
                case ADD_TO_LIST -> splashes.addAll(splash);
                case REPLACE -> {
                    splashes.clear();
                    splashes.addAll(splash);
                }
            }
        }
    }

    @Inject(method = "apply(Ljava/util/List;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("HEAD"), cancellable = true)
    private void antiApplyChange(List<String> list, ResourceManager resourceManager, ProfilerFiller profilerFiller, CallbackInfo ci) {
        if (TitleChangerFabric.getConfig().splashTextSettings.enabled) {
            ci.cancel();
        }
    }

    @Inject(method = "prepare(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Ljava/util/List;", at = @At("HEAD"), cancellable = true)
    private void antiChange(ResourceManager resourceManager, ProfilerFiller profilerFiller, CallbackInfoReturnable<List<String>> cir) {
        if (TitleChangerFabric.getConfig().splashTextSettings.enabled) {
            cir.setReturnValue(List.of());
            cir.cancel();
        }
    }
}
