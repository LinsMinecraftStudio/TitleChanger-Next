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
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import me.mmmjjkx.titlechanger.Constants;
import me.mmmjjkx.titlechanger.enums.Heading;
import me.mmmjjkx.titlechanger.fabric.TitleChangerFabric;
import me.mmmjjkx.titlechanger.fabric.screens.widget.ScrollPanel;
import me.mmmjjkx.titlechanger.fabric.utils.ComponentUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.LanguageSelectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    public LaunchScreen(final Screen previousScreen, Supplier<Component> title, Supplier<List<String>> text, Runnable onDone) {
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
            LanguageSelectScreen languageSelect = new LanguageSelectScreen(this, mcInstance.options, mcInstance.getLanguageManager());
            mcInstance.setScreen(languageSelect);
        }).bounds(this.width / 2 - 160, this.height - 30, 150, 20).build();

        this.scrollableTextPanel = new ScrollableTextPanel(mcInstance, this.width - 40, this.height - 40 - doneButton.getHeight(), 25, 20);

        this.addRenderableWidget(doneButton);
        this.addRenderableWidget(languageButton);
        this.addRenderableWidget(this.scrollableTextPanel);
    }

    @Override
    public void render(final @NotNull GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTicks) {
        this.renderDirtBackground(guiGraphics);

        this.scrollableTextPanel.setText(this.text.get());
        this.scrollableTextPanel.render(guiGraphics, mouseX, mouseY, partialTicks);

        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.scale(1.5f, 1.5f, 1f);
        guiGraphics.drawString(this.font,
                Language.getInstance().getVisualOrder(this.title.get()),
                (int) ((this.width / 3f) - font.width(this.title.get()) / 2f),
                5,
                0xFFFFFF,
                true
        );
        pose.popPose();

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    public class ScrollableTextPanel extends ScrollPanel {
        private List<Pair<Heading, ComponentUtils.LineStyles>> lines = Collections.emptyList();
        public int padding = 6;

        ScrollableTextPanel(final Minecraft mcInstance, final int width, final int height, final int top, final int left) {
            super(mcInstance, width, height, top, left);
        }

        public void setText(final List<String> lines) {
            this.lines = wordWrapAndFormat(lines);
        }

        @Override
        protected int getContentHeight() {
            return (lines.size() * font.lineHeight) + font.lineHeight;
        }

        @Override
        protected void drawPanel(@NotNull GuiGraphics guiGraphics, int entryRight, int relativeY, @NotNull Tesselator tesselator, int mouseX, int mouseY) {
            for (final Pair<Heading, ComponentUtils.LineStyles> line : lines) {
                if (line != null) {
                    PoseStack poseStack = guiGraphics.pose();
                    RenderSystem.enableBlend();
                    if (line.first != Heading.NONE) {
                        poseStack.pushPose();
                        float scale = switch (line.first) {
                            case L1 -> 1.8F;
                            case L2 -> 1.6F;
                            case L3 -> 1.4F;
                            default -> 1.0F;
                        };
                        poseStack.scale(scale, scale, 1.0F);
                        poseStack.translate(0.0F, scale, 0.0F);
                        guiGraphics.drawString(LaunchScreen.this.font, line.second.text(), (int) ((left + padding) / scale), (int) (relativeY / scale), 0xFFFFFF, true);
                        poseStack.popPose();
                    } else {
                        guiGraphics.drawString(LaunchScreen.this.font, line.second.text(), left + padding, relativeY, 0xFFFFFF);
                    }
                    RenderSystem.disableBlend();
                }
                relativeY += font.lineHeight;
            }
        }

        // todo: check if this is the right priority and change it if necessary so narration works
        @Override
        public @NotNull NarratableEntry.NarrationPriority narrationPriority() {
            return NarratableEntry.NarrationPriority.NONE;
        }

        @Override
        public void updateNarration(final @NotNull NarrationElementOutput narrationElementOutput) {}

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
                line = TitleChangerFabric.titleProcessor.firstParse(line); //allow parsing placeholders

                Heading heading = Heading.tryGetFromString(line);
                if (heading != Heading.NONE) {
                    line = StringUtils.replace(line, heading.getMark() + " ", "", 1);
                }

                var lineWithFormattedLinks = ComponentUtils.newChatWithLinks(line, false);
                Matcher matcher = Constants.LINK_PATTERN.matcher(line);
                if (matcher.find()) {
                    lineWithFormattedLinks = ComponentUtils.parseLinks(line); //why someone needs write 2 styles of links
                }

                final int maxTextLength = this.width - padding * 2;
                if (maxTextLength >= 0) {
                    Language.getInstance().getVisualOrder(font.getSplitter().splitLines(lineWithFormattedLinks, maxTextLength, Style.EMPTY)).forEach(
                            formattedCharSequence -> resized.add(Pair.of(heading, ComponentUtils.getLine(formattedCharSequence)))
                    );
                }

                lineCounter += resized.size() - lineCounter;

                // add a blank line after headings to avoid overlapping with any text that may be directly below it.
                if (heading != Heading.NONE) {
                    resized.add(Pair.of(Heading.NONE, ComponentUtils.getLine(Component.literal(" ").getVisualOrderText())));
                }

                lineCounter++;
            }

            // if the last line isn't a heading, add a single line at the end of the panel for
            // aesthetical (looks nicer) and functional reasons (hard to click links on last line otherwise)
            if (resized.get(resized.size() - 1).first == Heading.NONE) {
                resized.add(Pair.of(Heading.NONE, ComponentUtils.getLine(Component.literal(" ").getVisualOrderText())));
            }

            return resized;
        }

        @Override
        public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
            final Style component = findTextLine((int) mouseX, (int) mouseY);
            if (component != null) {
                LaunchScreen.this.handleComponentClicked(component);
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Nullable
        private Style findTextLine(final int mouseX, final int mouseY) {
            if (!isMouseOver(mouseX, mouseY)) {
                return null;
            }

            final double offset = (mouseY - top) + border + scrollDistance + 1;
            if (offset <= 0) return null;

            final int lineIdx = (int) (offset / font.lineHeight);

            if (lineIdx >= lines.size() || lineIdx < 1) {
                return null;
            }

            final FormattedCharSequence line = lines.get(lineIdx - 1).second.text();

            if (line != null) {
                return font.getSplitter().componentStyleAtWidth(line, mouseX - left - border);
            }

            return null;
        }
    }
}
