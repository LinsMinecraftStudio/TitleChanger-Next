package me.mmmjjkx.titlechanger.neoforge.utils;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ImageRenderer {
    private static final Map<String, Image> IMAGES = new ConcurrentHashMap<>();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private ImageRenderer() {
    }

    public static Image get(String source, File gameDirectory) {
        String cacheKey = (gameDirectory == null ? "" : gameDirectory.getAbsolutePath()) + '\0' + source;
        Image image = IMAGES.computeIfAbsent(cacheKey, ignored -> new Image());
        image.startLoading(source, gameDirectory);
        return image;
    }

    private static void load(String source, File gameDirectory, Image image) {
        try {
            NativeImage nativeImage = NativeImage.read(readBytes(source, gameDirectory));
            Minecraft.getInstance().execute(() -> register(source, nativeImage, image));
        } catch (Exception exception) {
            image.fail();
        }
    }

    private static byte[] readBytes(String source, File gameDirectory) throws IOException, InterruptedException {
        if (isWindowsPath(source)) {
            return Files.readAllBytes(resolveLocalPath(source, gameDirectory));
        }

        URI uri;
        try {
            uri = URI.create(source);
        } catch (IllegalArgumentException exception) {
            return Files.readAllBytes(resolveLocalPath(source, gameDirectory));
        }

        String scheme = uri.getScheme();
        if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(30))
                    .header("User-Agent", "TitleChanger/2.3.0")
                    .GET()
                    .build();
            HttpResponse<byte[]> response = HTTP.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IOException("Image request returned HTTP " + response.statusCode());
            }
            return response.body();
        }
        if ("file".equalsIgnoreCase(scheme)) {
            return Files.readAllBytes(resolveFileUri(uri, gameDirectory));
        }
        if (scheme == null) {
            String path = uri.getPath() == null ? source : uri.getPath();
            return Files.readAllBytes(resolveLocalPath(path, gameDirectory));
        }
        throw new IOException("Unsupported image URI scheme: " + scheme);
    }

    private static Path resolveFileUri(URI uri, File gameDirectory) {
        String authority = uri.getAuthority();
        if (authority != null && !authority.isBlank() && !"localhost".equalsIgnoreCase(authority)) {
            String path = authority + (uri.getPath() == null ? "" : uri.getPath());
            return resolveLocalPath(path, gameDirectory);
        }

        try {
            return Path.of(uri).normalize();
        } catch (IllegalArgumentException exception) {
            return resolveLocalPath(uri.getSchemeSpecificPart(), gameDirectory);
        }
    }

    private static Path resolveLocalPath(String source, File gameDirectory) {
        Path path = Path.of(source);
        if (!path.isAbsolute() && gameDirectory != null) {
            path = gameDirectory.toPath().resolve(path);
        }
        return path.normalize();
    }

    private static boolean isWindowsPath(String source) {
        return source.length() >= 3 && Character.isLetter(source.charAt(0)) && source.charAt(1) == ':'
                && (source.charAt(2) == '\\' || source.charAt(2) == '/');
    }

    private static void register(String source, NativeImage nativeImage, Image image) {
        try {
            Identifier id = Identifier.fromNamespaceAndPath("titlechanger",
                    "welcome/" + UUID.randomUUID().toString().replace("-", ""));
            Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(() -> source, nativeImage));
            image.complete(id, nativeImage.getWidth(), nativeImage.getHeight());
        } catch (RuntimeException exception) {
            nativeImage.close();
            image.fail();
        }
    }

    public static void draw(GuiGraphicsExtractor graphics, Image image, int x, int y, int maxWidth) {
        Size size = image.size(maxWidth);
        if (size == null) {
            return;
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, image.id, x, y, 0F, 0F,
                size.width, size.height, image.width, image.height, image.width, image.height);
    }

    public static int height(Image image, int maxWidth) {
        Size size = image.size(maxWidth);
        return size == null ? 0 : size.height;
    }

    private enum State {
        NEW,
        LOADING,
        READY,
        FAILED
    }

    private record Size(int width, int height) {
    }

    public static final class Image {
        private volatile State state = State.NEW;
        private volatile Identifier id;
        private volatile int width;
        private volatile int height;

        private synchronized void startLoading(String source, File gameDirectory) {
            if (state != State.NEW) {
                return;
            }
            state = State.LOADING;
            Thread.startVirtualThread(() -> load(source, gameDirectory, this));
        }

        private void complete(Identifier id, int width, int height) {
            this.id = id;
            this.width = width;
            this.height = height;
            this.state = State.READY;
        }

        private void fail() {
            this.state = State.FAILED;
        }

        private Size size(int maxWidth) {
            if (state != State.READY || id == null || width <= 0 || height <= 0 || maxWidth <= 0) {
                return null;
            }
            float scale = Math.min(1F, (float) maxWidth / width);
            return new Size(Math.max(1, Math.round(width * scale)), Math.max(1, Math.round(height * scale)));
        }
    }
}
