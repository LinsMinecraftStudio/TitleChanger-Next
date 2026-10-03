package me.mmmjjkx.titlechanger.utils;

import net.minecraft.client.Minecraft;
import org.lwjgl.sdl.SDLPixels;
import org.lwjgl.sdl.SDLSurface;
import org.lwjgl.sdl.SDLVideo;
import org.lwjgl.sdl.SDL_Surface;

import java.nio.ByteBuffer;

public class ImageUtils {
    public static void setWindowIcon(IconData data) {
        try (SDL_Surface sdlSurface = SDLSurface.SDL_CreateSurfaceFrom(
                data.width(),
                data.height(),
                SDLPixels.SDL_PIXELFORMAT_RGBA32,
                data.pixels(),
                data.width() * 4
        )) {
            if (sdlSurface != null) {
                SDLVideo.SDL_SetWindowIcon(Minecraft.getInstance().getWindow().handle(), sdlSurface);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public record IconData(ByteBuffer pixels, int width, int height) {
    }
}
