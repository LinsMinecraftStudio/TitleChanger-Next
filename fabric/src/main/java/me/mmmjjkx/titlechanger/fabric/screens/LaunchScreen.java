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

import com.ibm.icu.impl.Pair;
import me.mmmjjkx.titlechanger.Constants;
import me.mmmjjkx.titlechanger.enums.formatting.Heading;
import me.mmmjjkx.titlechanger.fabric.TitleChangerFabric;
import me.mmmjjkx.titlechanger.fabric.screens.widget.ScrollPanel;
import me.mmmjjkx.titlechanger.utils.ComponentUtils;
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
import org.apache.commons.lang3.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Matcher;

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
        mcInstance.setScreen(this.previousScreen);
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
            mcInstance.setScreen(languageSelect);
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
        public int padding = 6;
        private List<Pair<Heading, ComponentUtils.LineStyles>> lines = Collections.emptyList();

        ScrollableTextPanel(final int width, final int height, final int top, final int left) {
            super(Minecraft.getInstance(), width, height, top, left);
        }

        public void setText(final List<String> lines) {
            this.lines = wordWrapAndFormat(lines);
        }

        @Override
        protected int getContentHeight() {
            return (lines.size() * font.lineHeight) + font.lineHeight;
        }

        @Override
        protected void drawPanel(@NotNull GuiGraphicsExtractor guiGraphics, int entryRight, int relativeY, int mouseX, int mouseY) {
            for (final Pair<Heading, ComponentUtils.LineStyles> line : lines) {
                if (line != null) {
                    if (line.first != Heading.NONE) {
                        Matrix3x2fStack pose = guiGraphics.pose();
                        pose.pushMatrix();
                        float scale = switch (line.first) {
                            case L1 -> 1.8F;
                            case L2 -> 1.6F;
                            case L3 -> 1.4F;
                            default -> 1.0F;
                        };
                        pose.scale(scale, scale);
                        pose.translate(0.0F, scale);
                        guiGraphics.text(LaunchScreen.this.font, line.second.text(),
                                (int) ((left + padding) / scale), (int) (relativeY / scale), 0xFFFFFFFF, true);

                        pose.popMatrix();
                    } else {
                        guiGraphics.text(LaunchScreen.this.font, line.second.text(), (left + padding), relativeY, 0xFFFFFFFF, true);
                    }
                }
                relativeY += font.lineHeight;
            }
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
        public List<Pair<Heading, ComponentUtils.LineStyles>> wordWrapAndFormat(final List<String> lines) {
            final List<Pair<Heading, ComponentUtils.LineStyles>> resized = new ArrayList<>(lines.size());
            int lineCounter = 0;
            for (String line : lines) {
                if (line == null) {
                    resized.add(null);
                    continue;
                }

                // allows blank lines to be rendered
                if (line.isEmpty()) line += " ";

                // apply formatting codes where appropriate
                line = line.replaceAll("(?i)&([a-f]|[0-9]|l|m|n|o|r|k)", "§$1");
                line = line.replace("\\§", "&"); // allow formatting escaping with a backslash (for example, “\&a”)
                line = TitleChangerFabric.titleProcessor.firstParseNoCache(line); // allow parsing placeholders

                Heading heading = Heading.tryGetFromString(line);
                if (heading != Heading.NONE) {
                    line = Strings.CS.replace(line, heading.getMark() + " ", "", 1);
                }

                var lineWithFormattedLinks = ComponentUtils.newChatWithLinks(line, false);
                Matcher matcher = Constants.LINK_PATTERN.matcher(line);
                if (matcher.find()) {
                    lineWithFormattedLinks = ComponentUtils.parseLinks(line); // why someone needs write 2 styles of
                    // links
                }

                final int maxTextLength = this.width - padding * 2;
                if (maxTextLength >= 0) {
                    Language.getInstance()
                            .getVisualOrder(
                                    font.getSplitter().splitLines(lineWithFormattedLinks, maxTextLength, Style.EMPTY))
                            .forEach(
                                    formattedCharSequence -> resized
                                            .add(Pair.of(heading, ComponentUtils.getLine(formattedCharSequence))));
                }

                lineCounter += resized.size() - lineCounter;

                // add a blank line after headings to avoid overlapping with any text that may
                // be directly below it.
                if (heading != Heading.NONE) {
                    resized.add(
                            Pair.of(Heading.NONE, ComponentUtils.getLine(Component.literal(" ").getVisualOrderText())));
                }

                lineCounter++;
            }

            // if the last line isn't a heading, add a single line at the end of the panel
            // for
            // aesthetical (looks nicer) and functional reasons (hard to click links on last
            // line otherwise)
            if (resized.getLast().first == Heading.NONE) {
                resized.add(Pair.of(Heading.NONE, ComponentUtils.getLine(Component.literal(" ").getVisualOrderText())));
            }

            return resized;
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

            double offset = (mouseY - top - padding - border) + scrollDistance;

            if (offset <= 0)
                return null;

            int lineIdx = (int) (offset / font.lineHeight);
            if (lineIdx >= lines.size() || lineIdx < 0)
                return null;

            FormattedCharSequence line = lines.get(lineIdx).second.text();
            if (line != null) {
                var styleFinder = new ActiveTextCollector.ClickableStyleFinder(
                        // TODO 1.21.11: The calculating of Y needs to be validated, it should be relative to the vertical line origin
                        font, mouseX - left - border - 1, (int) (offset - (lineIdx * font.lineHeight)));
                styleFinder.accept(TextAlignment.LEFT, 0, 0, line);
                return styleFinder.result();
            }
            return null;
        }
    }
}
