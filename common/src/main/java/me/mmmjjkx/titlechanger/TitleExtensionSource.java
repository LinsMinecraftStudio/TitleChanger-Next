package me.mmmjjkx.titlechanger;

import io.github.lijinhong11.titlechanger.api.TitlePlaceholderExtension;

import java.util.ArrayList;
import java.util.List;

public class TitleExtensionSource {
    private static final List<TitlePlaceholderExtension> extensions;

    static {
        extensions = new ArrayList<>();
    }

    public static List<TitlePlaceholderExtension> getExtensions() {
        return extensions;
    }

    public static void registerExtension(TitlePlaceholderExtension extension) {
        extensions.add(extension);
    }

    public static void registerExtensions(List<TitlePlaceholderExtension> addingExtensions) {
        extensions.addAll(addingExtensions);
    }
}
