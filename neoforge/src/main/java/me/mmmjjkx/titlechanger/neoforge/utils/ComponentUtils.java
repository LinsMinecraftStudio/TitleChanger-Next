package me.mmmjjkx.titlechanger.neoforge.utils;

import me.mmmjjkx.titlechanger.Constants;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.util.regex.Matcher;

public class ComponentUtils {
    private ComponentUtils() {
        throw new IllegalStateException("utility class");
    }

    public static Component parseLinks(String line) {
        Matcher matcher = Constants.LINK_PATTERN.matcher(line);
        if (!matcher.find()) {
            return Component.literal(line);
        }

        String result = line.substring(matcher.start() + 1, matcher.end() - 1);
        String[] stringParts = line.split("<" + result + ">", 2);
        String[] split = result.split(";");
        String text = split[0];
        String link = split[1];

        Component head = Component.literal(stringParts[0]);

        ClickEvent.Action action;

        if (link.startsWith("file://")) {
            link = link.replaceFirst("file://", "");
            action = ClickEvent.Action.OPEN_FILE;
            File file = new File(FMLPaths.GAMEDIR.get().toFile(), link);
            link = file.getAbsolutePath();
        } else {
            action = ClickEvent.Action.OPEN_URL;
        }

        ClickEvent clickEvent = new ClickEvent(action, link);
        MutableComponent component = Component.literal(text).withStyle(ChatFormatting.UNDERLINE);
        Style style = component.getStyle().withClickEvent(clickEvent).withColor(TextColor.fromLegacyFormat(ChatFormatting.BLUE));
        component.setStyle(style);

        MutableComponent container = Component.empty();
        container = container.append(head).append(component);

        if (stringParts.length == 2 && Constants.LINK_PATTERN.matcher(stringParts[1]).find()) {
            Component tail = parseLinks(stringParts[1]);
            container = container.append(tail);
        }

        return container;
    }

    public static LineStyles getLine(FormattedCharSequence line) {
        return new LineStyles(line, 1F);
    }

    public static LineStyles getLine(FormattedCharSequence line, float scale) {
        return new LineStyles(line, scale);
    }

    //We will use it to add more things in the future,
    //So just let it there.
    public record LineStyles(FormattedCharSequence text, float scale) { }
}
