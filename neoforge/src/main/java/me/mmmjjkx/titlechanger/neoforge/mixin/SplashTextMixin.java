package me.mmmjjkx.titlechanger.neoforge.mixin;

import me.mmmjjkx.titlechanger.enums.SplashTextMode;
import me.mmmjjkx.titlechanger.neoforge.TitleChangerNeoForge;
import me.mmmjjkx.titlechanger.utils.FileUtils;
import net.minecraft.client.User;
import net.minecraft.client.resources.SplashManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.fml.loading.FMLPaths;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(SplashManager.class)
public class SplashTextMixin {
    @Shadow
    @Final
    private static Style DEFAULT_STYLE;
    @Shadow
    private List<Component> splashes;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void pushSplashText(User user, CallbackInfo ci) {
        if (TitleChangerNeoForge.getConfig().splashTextSettings.enabled) {
            SplashTextMode mode = TitleChangerNeoForge.getConfig().splashTextSettings.mode;
            List<MutableComponent> splash = FileUtils.readSplashText(FMLPaths.CONFIGDIR.get().toFile()).stream().map(k ->
                    Component.literal(k).withStyle(DEFAULT_STYLE)
            ).toList();
            switch (mode) {
                case ADD_TO_LIST -> {
                    splashes = new ArrayList<>(splashes);
                    splashes.addAll(splash);
                }
                case REPLACE -> splashes = new ArrayList<>(splash);
            }
        }
    }

    @Inject(method = "apply(Ljava/util/List;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("HEAD"), cancellable = true)
    private void antiApplyChange(List<String> preparations, ResourceManager manager, ProfilerFiller profiler, CallbackInfo ci) {
        if (TitleChangerNeoForge.getConfig().splashTextSettings.enabled) {
            ci.cancel();
        }
    }

    @Inject(method = "prepare(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Ljava/util/List;", at = @At("HEAD"), cancellable = true)
    private void antiChange(ResourceManager manager, ProfilerFiller profiler, CallbackInfoReturnable<List<String>> cir) {
        if (TitleChangerNeoForge.getConfig().splashTextSettings.enabled) {
            cir.setReturnValue(List.of());
            cir.cancel();
        }
    }
}
