package me.mmmjjkx.titlechanger.fabric.utils;

import me.mmmjjkx.titlechanger.Constants;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.util.FormattedCharSequence;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            File file = new File(FabricLoader.getInstance().getGameDir().toFile(), link);
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

    /*
    Copied from github.com/neoforged/NeoForge
    LICENSE: https://github.com/neoforged/NeoForge/blob/1.21.x/LICENSE.txt
     */

    static final Pattern URL_PATTERN = Pattern.compile(
            //         schema                          ipv4            OR        namespace                 port     path         ends
            //   |-----------------|        |-------------------------|  |-------------------------|    |---------| |--|   |---------------|
            "((?:[a-z0-9]{2,}:\\/\\/)?(?:(?:[0-9]{1,3}\\.){3}[0-9]{1,3}|(?:[-\\w_]{1,}\\.[a-z]{2,}?))(?::[0-9]{1,5})?.*?(?=[!\"\u00A7 \n]|$))",
            Pattern.CASE_INSENSITIVE);

    public static Component newChatWithLinks(String string, boolean allowMissingHeader) {
        // Includes ipv4 and domain pattern
        // Matches an ip (xx.xxx.xx.xxx) or a domain (something.com) with or
        // without a protocol or path.
        MutableComponent ichat = null;
        Matcher matcher = URL_PATTERN.matcher(string);
        int lastEnd = 0;

        // Find all urls
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            // Append the previous left overs.
            String part = string.substring(lastEnd, start);
            if (!part.isEmpty()) {
                if (ichat == null)
                    ichat = Component.literal(part);
                else
                    ichat.append(part);
            }
            lastEnd = end;
            String url = string.substring(start, end);
            MutableComponent link = Component.literal(url);

            try {
                URI uri = new URI(url);
                // Add schema so client doesn't crash.
                if (uri.getScheme() == null) {
                    if (!allowMissingHeader) {
                        if (ichat == null)
                            ichat = Component.literal(url);
                        else
                            ichat.append(url);
                        continue;
                    }
                    uri = new URI("http://" + url);
                }
                // Set the click event
                ClickEvent click = new ClickEvent(ClickEvent.Action.OPEN_URL, uri.toString());
                link.setStyle(link.getStyle().withClickEvent(click).withUnderlined(true).withColor(TextColor.fromLegacyFormat(ChatFormatting.BLUE)));
            } catch (URISyntaxException e) {
                // Bad syntax bail out!
                if (ichat == null)
                    ichat = Component.literal(url);
                else
                    ichat.append(url);
                continue;
            }

            // Append the link.
            if (ichat == null)
                ichat = Component.literal("");
            ichat.append(link);
        }

        // Append the rest of the message.
        String end = string.substring(lastEnd);
        if (ichat == null)
            ichat = Component.literal(end);
        else if (!end.isEmpty())
            ichat.append(Component.literal(string.substring(lastEnd)));
        return ichat;
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
