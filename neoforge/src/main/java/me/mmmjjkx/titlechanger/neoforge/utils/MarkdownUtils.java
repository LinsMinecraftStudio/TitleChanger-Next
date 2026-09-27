package me.mmmjjkx.titlechanger.neoforge.utils;

import me.mmmjjkx.titlechanger.enums.formatting.Alignment;
import me.mmmjjkx.titlechanger.enums.formatting.THeading;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.commonmark.Extension;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.ext.gfm.strikethrough.Strikethrough;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.gfm.tables.TableBlock;
import org.commonmark.ext.gfm.tables.TableCell;
import org.commonmark.ext.gfm.tables.TableRow;
import org.commonmark.ext.gfm.tables.TableHead;
import org.commonmark.ext.gfm.tables.TableBody;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public final class MarkdownUtils {
    private static final List<Extension> EXTENSIONS = List.of(StrikethroughExtension.create(), TablesExtension.create());
    private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();

    private MarkdownUtils() {
    }

    public static List<ComponentUtils.LineStyles> parse(List<String> source, File gameDirectory) {
        if (source.isEmpty()) {
            return List.of(ComponentUtils.getLine(Component.literal(" ").getVisualOrderText(), THeading.NONE, Alignment.LEFT));
        }
        List<String> normalized = TitleChangerFormattingExtension.normalizeAlignment(source);
        Node document = PARSER.parse(String.join("\n", normalized));
        List<ComponentUtils.LineStyles> result = new ArrayList<>();
        for (Node node = document.getFirstChild(); node != null; node = node.getNext()) {
            switch (node) {
                case Heading heading -> {
                    result.add(ComponentUtils.getComponent(renderChildren(heading, Style.EMPTY, gameDirectory), toHeading(heading.getLevel()), TitleChangerFormattingExtension.alignmentOf(nodeText(heading)), false));
                    result.add(ComponentUtils.getLine(Component.literal(" ").getVisualOrderText(), THeading.NONE, Alignment.LEFT));
                }
                case Image image -> result.add(ComponentUtils.getImageLine(image.getDestination()));
                case Paragraph paragraph -> renderParagraph(paragraph, result, gameDirectory);
                case BlockQuote quote -> renderQuote(quote, result, gameDirectory);
                case BulletList list -> renderList(list, "• ", 0, 0, result, gameDirectory);
                case TableBlock table -> renderTable(table, result, gameDirectory);
                case OrderedList list -> renderList(list, null, list.getMarkerStartNumber(), 0, result, gameDirectory);
                case ThematicBreak _ ->
                        result.add(ComponentUtils.getLine(Component.literal("--------------------").getVisualOrderText(), THeading.NONE, Alignment.LEFT));
                case FencedCodeBlock codeBlock ->
                        addCodeLines(codeBlock.getLiteral(), result);
                case IndentedCodeBlock codeBlock ->
                        addCodeLines(codeBlock.getLiteral(), result);
                default -> {
                }
            }
            result.add(ComponentUtils.getLine(Component.literal(" ").getVisualOrderText(), THeading.NONE, Alignment.LEFT));
        }
        if (!result.isEmpty()) {
            result.removeLast();
        }
        if (result.isEmpty()) {
            result.add(ComponentUtils.getLine(Component.literal(" ").getVisualOrderText(), THeading.NONE, Alignment.LEFT));
        }
        return result;
    }

    private static MutableComponent renderChildren(Node parent, Style style, File gameDirectory) {
        MutableComponent result = Component.empty();
        for (Node node = parent.getFirstChild(); node != null; node = node.getNext()) {
            result.append(render(node, style, gameDirectory));
        }
        return result;
    }

    private static void renderParagraph(Paragraph paragraph, List<ComponentUtils.LineStyles> result, File gameDirectory) {
        Alignment alignment = TitleChangerFormattingExtension.alignmentOf(nodeText(paragraph));
        MutableComponent text = Component.empty();
        for (Node node = paragraph.getFirstChild(); node != null; node = node.getNext()) {
            if (node instanceof Image image) {
                addParagraphText(text, alignment, result);
                result.add(ComponentUtils.getImageLine(image.getDestination()));
                text = Component.empty();
            } else {
                text.append(render(node, Style.EMPTY, gameDirectory));
            }
        }
        addParagraphText(text, alignment, result);
    }

    private static void addParagraphText(MutableComponent text, Alignment alignment,
                                         List<ComponentUtils.LineStyles> result) {
        if (!text.getString().isBlank()) {
            result.add(ComponentUtils.getComponent(text, THeading.NONE, alignment, false));
        }
    }

    private static void renderList(Node list, String bullet, int start, int depth, List<ComponentUtils.LineStyles> result, File gameDirectory) {
        int number = start;
        for (Node item = list.getFirstChild(); item != null; item = item.getNext()) {
            if (!(item instanceof ListItem)) continue;
            String prefix = "  ".repeat(depth) + (bullet != null ? bullet : number++ + ". ");
            for (Node child = item.getFirstChild(); child != null; child = child.getNext()) {
                switch (child) {
                    case Paragraph paragraph ->
                            result.add(ComponentUtils.getComponent(Component.literal(prefix).append(renderChildren(paragraph, Style.EMPTY, gameDirectory)), THeading.NONE, TitleChangerFormattingExtension.alignmentOf(nodeText(paragraph)), false));
                    case BulletList nested -> renderList(nested, "• ", 0, depth + 1, result, gameDirectory);
                    case OrderedList nested -> renderList(nested, null, 1, depth + 1, result, gameDirectory);
                    default -> {
                    }
                }
            }
        }
    }

    private static void renderTable(TableBlock table, List<ComponentUtils.LineStyles> result, File gameDirectory) {
        for (Node section = table.getFirstChild(); section != null; section = section.getNext()) {
            if (!(section instanceof TableHead) && !(section instanceof TableBody)) continue;
            for (Node row = section.getFirstChild(); row != null; row = row.getNext()) {
                if (!(row instanceof TableRow)) continue;
                renderTableRow((TableRow) row, result, gameDirectory);
            }
        }
    }

    private static void renderTableRow(TableRow row, List<ComponentUtils.LineStyles> result, File gameDirectory) {
            List<Component> cells = new ArrayList<>();
            for (Node cell = row.getFirstChild(); cell != null; cell = cell.getNext()) {
                if (cell instanceof TableCell tableCell) {
                    Component content = renderChildren(tableCell, tableCell.isHeader() ? Style.EMPTY.withBold(true) : Style.EMPTY, gameDirectory);
                    cells.add(content);
                }
            }
            result.add(ComponentUtils.getTableLine(cells));
    }

    private static void renderQuote(BlockQuote quote, List<ComponentUtils.LineStyles> result, File gameDirectory) {
        for (Node child = quote.getFirstChild(); child != null; child = child.getNext()) {
            switch (child) {
                case Paragraph paragraph -> result.add(ComponentUtils.getComponent(
                        Component.literal("│ ").append(renderChildren(paragraph, Style.EMPTY.withColor(ChatFormatting.GRAY), gameDirectory)),
                        THeading.NONE,
                        TitleChangerFormattingExtension.alignmentOf(nodeText(paragraph)),
                        false));
                case BlockQuote nested -> renderQuote(nested, result, gameDirectory);
                case BulletList list -> renderList(list, "│ • ", 0, 0, result, gameDirectory);
                case OrderedList list -> renderList(list, "│ ", list.getMarkerStartNumber(), 0, result, gameDirectory);
                default -> {
                }
            }
        }
    }

    private static void addCodeLines(String code, List<ComponentUtils.LineStyles> result) {
        for (String line : code.split("\\R", -1)) {
            result.add(ComponentUtils.getComponent(renderCode(line), THeading.NONE, Alignment.LEFT, true));
        }
    }

    private static MutableComponent render(Node node, Style style, File gameDirectory) {
        if (node instanceof Text text) {
            return TitleChangerFormattingExtension.renderText(text.getLiteral(), style);
        }
        if (node instanceof StrongEmphasis strong) return renderChildren(strong, style.withBold(true), gameDirectory);
        if (node instanceof Emphasis emphasis) return renderChildren(emphasis, style.withItalic(true), gameDirectory);
        if (node instanceof Strikethrough strike) return renderChildren(strike, style.withStrikethrough(true), gameDirectory);
        if (node instanceof Code code) return Component.literal(code.getLiteral()).setStyle(style.withColor(ChatFormatting.GRAY));
        if (node instanceof Image) return Component.empty();
        if (node instanceof Link link) {
            try {
                URI destination = new URI(link.getDestination());
                ClickEvent clickEvent = "file".equalsIgnoreCase(destination.getScheme())
                        ? new ClickEvent.OpenFile(resolveFile(destination, gameDirectory))
                        : new ClickEvent.OpenUrl(destination);
                return renderChildren(link, style.withClickEvent(clickEvent).withUnderlined(true).withColor(ChatFormatting.BLUE), gameDirectory);
            } catch (URISyntaxException e) {
                return renderChildren(link, style, gameDirectory);
            }
        }
        if (node instanceof SoftLineBreak || node instanceof HardLineBreak) return Component.literal("\n").setStyle(style);
        return renderChildren(node, style, gameDirectory);
    }

    private static File resolveFile(URI uri, File gameDirectory) {
        String path = uri.getPath();
        if (uri.getAuthority() != null && !uri.getAuthority().isBlank()) {
            path = uri.getAuthority() + path;
        }

        File file = new File(path);
        return file.isAbsolute() || gameDirectory == null ? file : new File(gameDirectory, path);
    }

    private static Component renderCode(String code) {
        return Component.literal(code).withStyle(ChatFormatting.GRAY);
    }

    private static THeading toHeading(int level) {
        return switch (Math.min(level, 3)) {
            case 1 -> THeading.L1;
            case 2 -> THeading.L2;
            case 3 -> THeading.L3;
            default -> THeading.NONE;
        };
    }

    private static String nodeText(Node node) {
        StringBuilder text = new StringBuilder();
        node.accept(new AbstractVisitor() {
            @Override
            public void visit(Text textNode) {
                text.append(textNode.getLiteral());
            }
        });
        return text.toString();
    }

}
