package me.mmmjjkx.titlechanger.neoforge.utils;

import me.mmmjjkx.titlechanger.enums.formatting.Alignment;
import me.mmmjjkx.titlechanger.enums.formatting.THeading;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public final class ComponentUtils {
    private ComponentUtils() {
    }

    public static LineStyles getLine(FormattedCharSequence line, Alignment alignment) {
        return new LineStyles(null, line, THeading.NONE, 1F, alignment, false, false, null, List.of());
    }

    public static LineStyles getCodeLine(FormattedCharSequence line) {
        return new LineStyles(null, line, THeading.NONE, 1F, Alignment.LEFT, true, false, null, List.of());
    }

    public static LineStyles getLine(FormattedCharSequence line, THeading heading, Alignment alignment) {
        return new LineStyles(null, line, heading, 1F, alignment, false, false, null, List.of());
    }

    public static LineStyles getComponent(Component component, THeading heading, Alignment alignment, boolean codeBlock) {
        return new LineStyles(component, null, heading, 1F, alignment, codeBlock, false, null, List.of());
    }

    public static LineStyles getImageLine(String source) {
        return new LineStyles(null, null, THeading.NONE, 1F, Alignment.LEFT, false, false, source, List.of());
    }

    public static LineStyles getTableLine(Component component) {
        return new LineStyles(component, null, THeading.NONE, 1F, Alignment.LEFT, false, true, null, List.of());
    }

    public static LineStyles getTableLine(FormattedCharSequence line) {
        return new LineStyles(null, line, THeading.NONE, 1F, Alignment.LEFT, false, true, null, List.of());
    }

    public static LineStyles getTableLine(List<Component> cells) {
        return new LineStyles(null, null, THeading.NONE, 1F, Alignment.LEFT, false, true, null, cells);
    }

    public record LineStyles(Component component, FormattedCharSequence text, THeading heading, float scale,
                             Alignment alignment, boolean codeBlock, boolean tableRow, String imageSource,
                             List<Component> tableCells) {
    }
}
