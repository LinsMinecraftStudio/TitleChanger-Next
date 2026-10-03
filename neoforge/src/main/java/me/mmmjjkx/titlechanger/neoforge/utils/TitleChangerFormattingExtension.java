package me.mmmjjkx.titlechanger.neoforge.utils;

import me.mmmjjkx.titlechanger.enums.formatting.Alignment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TitleChangerFormattingExtension {
    private static final String MARK_START = "\u0001";
    private static final String MARK_END = "\u0002";
    private static final String GRADIENT_END = "\u0003";
    private static final Pattern GRADIENT_TAG_PATTERN = Pattern.compile("<gradient=(#[0-9a-fA-F]{6}),\\s*(#[0-9a-fA-F]{6})>(.+?)</gradient>", Pattern.DOTALL);
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<gradient=(#[0-9a-fA-F]{6}),\\s*(#[0-9a-fA-F]{6})>(.+?)</gradient>", Pattern.DOTALL);

    private TitleChangerFormattingExtension() {
    }

    public static List<String> normalizeAlignment(List<String> source) {
        List<String> normalized = new ArrayList<>(source.size());
        Alignment current = Alignment.LEFT;
        for (String line : source) {
            String leading = line.substring(0, line.length() - line.stripLeading().length());
            line = line.stripLeading();
            Alignment lineAlignment = current;
            for (Alignment candidate : Alignment.values()) {
                if (line.startsWith(candidate.getMark())) {
                    current = candidate;
                    lineAlignment = candidate;
                    line = line.substring(candidate.getMark().length());
                    break;
                }
            }
            for (Alignment candidate : Alignment.values()) {
                String end = candidate.getEndMark();
                if (line.endsWith(end)) {
                    line = line.substring(0, line.length() - end.length());
                    lineAlignment = candidate;
                    current = Alignment.LEFT;
                    break;
                }
            }
            if (lineAlignment != Alignment.LEFT)
                line = MARK_START + "a" + lineAlignment.name().charAt(0) + MARK_END + line;
            normalized.add(leading + protectGradient(line));
        }
        return normalized;
    }

    private static String protectGradient(String text) {
        return GRADIENT_TAG_PATTERN.matcher(text).replaceAll(match ->
                MARK_START + "g" + match.group(1).substring(1) + "," + match.group(2).substring(1) + MARK_END + match.group(3) + GRADIENT_END);
    }

    public static Alignment alignmentOf(String text) {
        int start = text.indexOf(MARK_START + "a");
        if (start >= 0) {
            int valueStart = start + 2;
            if (valueStart < text.length()) return switch (text.charAt(valueStart)) {
                case 'L' -> Alignment.LEFT;
                case 'C' -> Alignment.CENTER;
                case 'R' -> Alignment.RIGHT;
                default -> Alignment.LEFT;
            };
        }
        return Alignment.LEFT;
    }

    public static MutableComponent renderText(String text, Style style) {
        text = removeAlignmentMarker(text);
        text = restoreGradientMarker(text);
        Matcher matcher = GRADIENT_PATTERN.matcher(text);
        MutableComponent result = Component.empty();
        int lastEnd = 0;
        while (matcher.find()) {
            if (matcher.start() > lastEnd) result.append(renderLegacy(text.substring(lastEnd, matcher.start()), style));
            int start = Integer.parseInt(matcher.group(1).substring(1), 16);
            int end = Integer.parseInt(matcher.group(2).substring(1), 16);
            String value = matcher.group(3);
            int count = value.codePointCount(0, value.length());
            int index = 0;
            for (int offset = 0; offset < value.length(); ) {
                int codePoint = value.codePointAt(offset);
                float progress = count <= 1 ? 0F : (float) index / (count - 1);
                result.append(Component.literal(new String(Character.toChars(codePoint))).setStyle(style.withColor(interpolate(start, end, progress))));
                offset += Character.charCount(codePoint);
                index++;
            }
            lastEnd = matcher.end();
        }
        if (lastEnd < text.length()) result.append(renderLegacy(text.substring(lastEnd), style));
        return result;
    }

    private static MutableComponent renderLegacy(String text, Style baseStyle) {
        MutableComponent result = Component.empty();
        Style current = baseStyle;
        StringBuilder plain = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            if ((character == '&' || character == '\u00a7') && i + 1 < text.length()) {
                ChatFormatting formatting = ChatFormatting.getByCode(text.charAt(i + 1));
                if (formatting != null) {
                    if (!plain.isEmpty()) {
                        result.append(Component.literal(plain.toString()).setStyle(current));
                        plain.setLength(0);
                    }
                    current = formatting == ChatFormatting.RESET ? baseStyle : applyFormatting(current, formatting);
                    i++;
                    continue;
                }
            }
            plain.append(character);
        }
        if (!plain.isEmpty()) result.append(Component.literal(plain.toString()).setStyle(current));
        return result;
    }

    private static Style applyFormatting(Style style, ChatFormatting formatting) {
        return switch (formatting) {
            case BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY,
                 DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW, WHITE -> style.withColor(formatting);
            case OBFUSCATED -> style.withObfuscated(true);
            case BOLD -> style.withBold(true);
            case STRIKETHROUGH -> style.withStrikethrough(true);
            case UNDERLINE -> style.withUnderlined(true);
            case ITALIC -> style.withItalic(true);
            default -> style;
        };
    }

    private static String restoreGradientMarker(String text) {
        return text.replaceAll("\u0001g([0-9a-fA-F]{6}),([0-9a-fA-F]{6})\u0002(.+?)\u0003", "<gradient=#$1,#$2>$3</gradient>");
    }

    private static String removeAlignmentMarker(String text) {
        int markerStart = text.indexOf(MARK_START + "a");
        if (markerStart < 0) {
            return text;
        }

        int markerEnd = text.indexOf(MARK_END, markerStart + 2);
        if (markerEnd < 0) {
            return text;
        }

        return text.substring(0, markerStart) + text.substring(markerEnd + MARK_END.length());
    }

    private static int interpolate(int start, int end, float progress) {
        int red = Math.round(((start >> 16) & 0xFF) + (((end >> 16) & 0xFF) - ((start >> 16) & 0xFF)) * progress);
        int green = Math.round(((start >> 8) & 0xFF) + (((end >> 8) & 0xFF) - ((start >> 8) & 0xFF)) * progress);
        int blue = Math.round((start & 0xFF) + ((end & 0xFF) - (start & 0xFF)) * progress);
        return (red << 16) | (green << 8) | blue;
    }
}
