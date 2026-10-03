/*
MIT License

Copyright (c) 2020 Zlepper

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

/*
Copied and edited from https://github.com/zlepper/itlt
Respect to the original license.
 */

package me.mmmjjkx.titlechanger.fabric.screens;

import me.mmmjjkx.titlechanger.enums.formatting.Alignment;
import me.mmmjjkx.titlechanger.enums.formatting.THeading;
import me.mmmjjkx.titlechanger.fabric.TitleChangerFabric;
import me.mmmjjkx.titlechanger.fabric.screens.widget.ScrollPanel;
import me.mmmjjkx.titlechanger.fabric.utils.ComponentUtils;
import me.mmmjjkx.titlechanger.fabric.utils.ImageRenderer;
import me.mmmjjkx.titlechanger.fabric.utils.MarkdownUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class LaunchScreen extends Screen {
    private final Screen previousScreen;
    private final Minecraft mcInstance = Minecraft.getInstance();
    private final Supplier<List<String>> text;
    private final Supplier<Component> title;
    private final Runnable onDone;
    private ScrollableTextPanel scrollableTextPanel;

    public LaunchScreen(final Screen previousScreen, Supplier<Component> title, Supplier<List<String>> text,
                        Runnable onDone) {
        super(title.get());

        this.title = title;
        this.onDone = onDone;
        this.previousScreen = previousScreen;
        this.text = text;
    }

    @Override
    public void onClose() {
        onDone.run();
        mcInstance.setScreenAndShow(this.previousScreen);
    }

    @Override
    public @NotNull Component getNarrationMessage() {
        return this.title.get();
    }

    protected void init() {
        final Button doneButton = Button.builder(CommonComponents.GUI_DONE, onPress -> this.onClose())
                .bounds(this.width / 2 + 10, this.height - 30, 150, 20)
                .build();

        final Button languageButton = Button.builder(Component.translatable("options.language"), onPress -> {
            LanguageSelectScreen languageSelect = new LanguageSelectScreen(this, mcInstance.options,
                    mcInstance.getLanguageManager());
            mcInstance.setScreenAndShow(languageSelect);
        }).bounds(this.width / 2 - 160, this.height - 30, 150, 20).build();

        this.scrollableTextPanel = new ScrollableTextPanel(this.width - 40, this.height - 40 - doneButton.getHeight(),
                25, 20);

        this.addRenderableWidget(doneButton);
        this.addRenderableWidget(languageButton);
        this.addRenderableWidget(this.scrollableTextPanel);
    }

    @Override
    public void extractRenderState(final @NotNull GuiGraphicsExtractor guiGraphics, final int mouseX, final int mouseY,
                                   final float partialTicks) {
        this.scrollableTextPanel.setText(this.text.get());
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);

        Matrix3x2fStack pose = guiGraphics.pose();

        pose.pushMatrix();
        pose.scale(1.5f, 1.5f);

        guiGraphics.text(this.font,
                this.title.get().getVisualOrderText(),
                (int) ((this.width / 2f / 1.5f) - font.width(this.title.get()) / 2.0F),
                5,
                0xFFFFFFFF,
                true);

        pose.popMatrix();
    }

    public class ScrollableTextPanel extends ScrollPanel {
        private static final int IMAGE_BOTTOM_GAP = 5;
        private static final int CODE_VERTICAL_PADDING = 4;
        public int padding = 6;
        private List<ComponentUtils.LineStyles> lines = Collections.emptyList();

        ScrollableTextPanel(final int width, final int height, final int top, final int left) {
            super(Minecraft.getInstance(), width, height, top, left);
        }

        public void setText(final List<String> lines) {
            this.lines = wordWrapAndFormat(lines);
        }

        @Override
        protected int getContentHeight() {
            int height = font.lineHeight;
            for (int index = 0; index < lines.size(); index++) {
                ComponentUtils.LineStyles line = lines.get(index);
                if (line.imageSource() != null) {
                    height += ImageRenderer.height(ImageRenderer.get(line.imageSource(), net.fabricmc.loader.api.FabricLoader.getInstance().getGameDir().toFile()),
                            getImageMaxWidth()) + IMAGE_BOTTOM_GAP;
                    continue;
                }
                if (line.codeBlock() && (index == 0 || !lines.get(index - 1).codeBlock())) {
                    height += CODE_VERTICAL_PADDING;
                }
                height += line.heading() == THeading.NONE
                        ? font.lineHeight
                        : (int) Math.ceil(font.lineHeight * scaleFor(line.heading()));
                if (line.heading() != THeading.NONE) {
                    height += 4;
                }
                if (line.codeBlock() && (index == lines.size() - 1 || !lines.get(index + 1).codeBlock())) {
                    height += CODE_VERTICAL_PADDING;
                }
            }
            return height;
        }

        @Override
        protected void drawPanel(@NotNull GuiGraphicsExtractor guiGraphics, int entryRight, int relativeY, int mouseX, int mouseY) {
            int[] tableColumnWidths = tableColumnWidths();
            int tableWidth = java.util.Arrays.stream(tableColumnWidths).sum();
            for (int index = 0; index < lines.size(); index++) {
                final ComponentUtils.LineStyles line = lines.get(index);
                if (line != null) {
                    if (line.imageSource() != null) {
                        ImageRenderer.Image image = ImageRenderer.get(line.imageSource(), net.fabricmc.loader.api.FabricLoader.getInstance().getGameDir().toFile());
                        ImageRenderer.draw(guiGraphics, image, left + padding, relativeY,
                                getImageMaxWidth());
                        relativeY += ImageRenderer.height(image, getImageMaxWidth()) + IMAGE_BOTTOM_GAP;
                        continue;
                    }
                    if (line.tableRow() && (index == 0 || !lines.get(index - 1).tableRow())) {
                        int tableEnd = index;
                        while (tableEnd + 1 < lines.size() && lines.get(tableEnd + 1).tableRow()) tableEnd++;
                        guiGraphics.outline(left + padding, relativeY - 2, tableWidth, (tableEnd - index + 1) * font.lineHeight + 2, 0xFF808080);
                    }
                    if (line.tableRow()) {
                        drawTableRow(guiGraphics, line.tableCells(), relativeY, tableColumnWidths);
                        relativeY += font.lineHeight;
                        continue;
                    }
                    boolean firstCodeLine = line.codeBlock() && (index == 0 || !lines.get(index - 1).codeBlock());
                    if (firstCodeLine) {
                        relativeY += CODE_VERTICAL_PADDING;
                    }
                    int codeEnd = index;
                    while (codeEnd + 1 < lines.size() && lines.get(codeEnd + 1).codeBlock()) {
                        codeEnd++;
                    }
                    if (firstCodeLine) {
                        int blockTop = relativeY - CODE_VERTICAL_PADDING;
                        int blockBottom = relativeY + (codeEnd - index + 1) * font.lineHeight
                                + CODE_VERTICAL_PADDING;
                        int blockLeft = left + padding;
                        int blockRight = entryRight - 7;
                        guiGraphics.fill(blockLeft, blockTop, blockRight, blockBottom, 0xFF202020);
                        guiGraphics.fill(blockLeft, blockTop, blockRight, blockTop + 1, 0xFF808080);
                        guiGraphics.fill(blockLeft, blockBottom - 1, blockRight, blockBottom, 0xFF808080);
                        guiGraphics.fill(blockLeft, blockTop, blockLeft + 1, blockBottom, 0xFF808080);
                        guiGraphics.fill(blockRight - 1, blockTop, blockRight, blockBottom, 0xFF808080);
                    }
                    if (line.heading() != THeading.NONE) {
                        Matrix3x2fStack pose = guiGraphics.pose();
                        pose.pushMatrix();
                        float scale = switch (line.heading()) {
                            case L1 -> 1.8F;
                            case L2 -> 1.6F;
                            case L3 -> 1.4F;
                            default -> 1.0F;
                        };
                        pose.scale(scale, scale);
                        pose.translate(0.0F, scale);
                        guiGraphics.text(LaunchScreen.this.font, line.text(),
                                getTextX(line.text(), line.alignment(), entryRight, scale),
                                (int) (relativeY / scale), 0xFFFFFFFF, true);

                        pose.popMatrix();
                    } else {
                        guiGraphics.text(LaunchScreen.this.font, line.text(), line.codeBlock() ? left + padding + 4 : getTextX(line.text(), line.alignment(), entryRight, 1F), relativeY, 0xFFFFFFFF, true);
                    }
                    relativeY += line.heading() == THeading.NONE ? font.lineHeight : (int) Math.ceil(font.lineHeight * scaleFor(line.heading()));
                    if (line.heading() != THeading.NONE) {
                        relativeY += 4;
                    }
                    if (line.codeBlock() && (index == lines.size() - 1 || !lines.get(index + 1).codeBlock())) {
                        relativeY += CODE_VERTICAL_PADDING;
                    }
                }
            }
        }

        private float scaleFor(THeading heading) {
            return switch (heading) {
                case L1 -> 1.8F;
                case L2 -> 1.6F;
                case L3 -> 1.4F;
                default -> 1.0F;
            };
        }

        private int getImageMaxWidth() {
            int contentLeft = left + padding;
            int contentRight = right - 8;
            return Math.max(0, contentRight - contentLeft);
        }

        @Override
        public @NotNull NarratableEntry.NarrationPriority narrationPriority() {
            return NarrationPriority.FOCUSED;
        }

        @Override
        public void updateNarration(final @NotNull NarrationElementOutput narrationElementOutput) {
        }

        // This will fix the inconsistent gap issue of headings
        // compared to paragraphs without making the headings too big.
        public List<ComponentUtils.LineStyles> wordWrapAndFormat(final List<String> lines) {
            String parsed = TitleChangerFabric.titleProcessor.firstParseNoCache(String.join("\n", lines));
            List<ComponentUtils.LineStyles> markdown = MarkdownUtils.parse(java.util.Arrays.asList(parsed.split("\\n", -1)),
                    net.fabricmc.loader.api.FabricLoader.getInstance().getGameDir().toFile());
            final List<ComponentUtils.LineStyles> resized = new java.util.ArrayList<>();
            final int maxTextLength = this.width - padding * 2 - 17;
            if (maxTextLength < 1) {
                return resized;
            }
            for (ComponentUtils.LineStyles line : markdown) {
                if (line.imageSource() != null) {
                    resized.add(line);
                    continue;
                }
                if (line.tableRow()) {
                    resized.add(line);
                    continue;
                }
                Language.getInstance().getVisualOrder(font.getSplitter().splitLines(line.component() != null ? line.component() : Component.literal(""), maxTextLength, Style.EMPTY))
                        .forEach(sequence -> resized.add(line.codeBlock()
                                ? ComponentUtils.getCodeLine(sequence)
                                : line.tableRow()
                                ? ComponentUtils.getTableLine(sequence)
                                : ComponentUtils.getLine(sequence, line.heading(), line.alignment())));
            }
            return resized;
        }

        private int getTextX(FormattedCharSequence text, Alignment alignment, int entryRight, float scale) {
            int textWidth = font.width(text);
            int contentLeft = left + padding;
            int contentRight = entryRight - 8;
            float screenX = switch (alignment) {
                case CENTER -> (contentLeft + contentRight) / (2.0F * scale) - textWidth / 2.0F;
                case RIGHT -> contentRight / scale - textWidth;
                default -> contentLeft / scale;
            };
            return Math.round(screenX);
        }

        private int[] tableColumnWidths() {
            int columnCount = lines.stream().filter(ComponentUtils.LineStyles::tableRow)
                    .mapToInt(line -> line.tableCells().size()).max().orElse(0);
            int[] widths = new int[columnCount];
            for (ComponentUtils.LineStyles line : lines) {
                if (!line.tableRow()) {
                    continue;
                }
                for (int column = 0; column < line.tableCells().size(); column++) {
                    widths[column] = Math.max(widths[column], font.width(line.tableCells().get(column)) + 20);
                }
            }
            return widths;
        }

        private void drawTableRow(GuiGraphicsExtractor graphics, List<Component> cells, int y, int[] columnWidths) {
            int x = left + padding;
            for (int column = 0; column < cells.size(); column++) {
                Component cell = cells.get(column);
                int cellWidth = columnWidths[column];
                graphics.text(font, cell, x + Math.max(0, (cellWidth - font.width(cell)) / 2), y, 0xFFFFFFFF, true);
                x += cellWidth;
                if (column < columnWidths.length - 1) {
                    graphics.fill(x - 1, y - 2, x, y + font.lineHeight, 0xFF808080);
                }
            }
            int tableWidth = java.util.Arrays.stream(columnWidths).sum();
            graphics.fill(left + padding, y + font.lineHeight - 1,
                    left + padding + tableWidth, y + font.lineHeight, 0xFF808080);
        }

        @Override
        public boolean mouseClicked(final MouseButtonEvent e, boolean doubleClick) {
            double mouseX = e.x();
            double mouseY = e.y();
            final Style component = findTextLine((int) mouseX, (int) mouseY);
            if (component != null) {
                if (component.getClickEvent() != null) {
                    defaultHandleClickEvent(component.getClickEvent(), Minecraft.getInstance(), LaunchScreen.this);
                }
                return true;
            }
            return super.mouseClicked(e, doubleClick);
        }

        @Nullable
        private Style findTextLine(final int mouseX, final int mouseY) {
            if (!isMouseOver(mouseX, mouseY))
                return null;

            int currentY = top + border - (int) scrollDistance;
            for (int index = 0; index < lines.size(); index++) {
                ComponentUtils.LineStyles lineStyle = lines.get(index);
                if (lineStyle.imageSource() != null || lineStyle.tableRow()) {
                    currentY += lineStyle.imageSource() != null
                            ? ImageRenderer.height(ImageRenderer.get(lineStyle.imageSource(), net.fabricmc.loader.api.FabricLoader.getInstance().getGameDir().toFile()), getImageMaxWidth()) + IMAGE_BOTTOM_GAP
                            : font.lineHeight;
                    continue;
                }
                if (lineStyle.codeBlock() && (index == 0 || !lines.get(index - 1).codeBlock())) {
                    currentY += CODE_VERTICAL_PADDING;
                }
                float scale = scaleFor(lineStyle.heading());
                int rowHeight = (int) Math.ceil(font.lineHeight * scale);
                int textTop = currentY;
                if (mouseY >= textTop && mouseY < textTop + rowHeight) {
                    FormattedCharSequence line = lineStyle.text();
                    int textX = lineStyle.codeBlock() ? left + padding + 4
                            : getTextX(line, lineStyle.alignment(), right, scale);
                    if (line == null) return null;
                    int renderedTextX = (int) (textX * scale);
                    var styleFinder = new ActiveTextCollector.ClickableStyleFinder(
                            font,
                            Math.max(0, (int) ((mouseX - renderedTextX) / scale)),
                            Math.max(0, (int) ((mouseY - textTop) / scale)));
                    styleFinder.accept(TextAlignment.LEFT, 0, 0, line);
                    if (styleFinder.result() != null) {
                        return styleFinder.result();
                    }
                    return null;
                }
                currentY += rowHeight;
                if (lineStyle.heading() != THeading.NONE) {
                    currentY += 4;
                }
                if (lineStyle.codeBlock() && (index + 1 >= lines.size() || !lines.get(index + 1).codeBlock())) {
                    currentY += CODE_VERTICAL_PADDING;
                }
            }
            return null;
        }
    }
}
