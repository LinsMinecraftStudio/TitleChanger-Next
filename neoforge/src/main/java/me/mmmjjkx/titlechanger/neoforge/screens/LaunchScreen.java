package me.mmmjjkx.titlechanger.neoforge.screens;

import com.ibm.icu.impl.Pair;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import me.mmmjjkx.titlechanger.enums.Heading;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.*;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.client.gui.widget.ScrollPanel;
import net.neoforged.neoforge.common.CommonHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LaunchScreen extends Screen {
    private final Screen previousScreen;
    private final Minecraft mcInstance = Minecraft.getInstance();
    private final List<String> text;
    private ScrollableTextPanel scrollableTextPanel;

    public LaunchScreen(final Screen previousScreen, final Component title, final List<String> text) {
        super(title);
        this.previousScreen = previousScreen;
        this.text = text;
    }

    @Override
    public void onClose() {
        mcInstance.setScreen(this.previousScreen);
    }

    protected void init() {
        final Button doneButton = Button.builder(CommonComponents.GUI_DONE, onPress -> this.onClose())
                .bounds(this.width / 2 - 100, this.height - 15, 200, 20)
                .build();

        this.scrollableTextPanel = new ScrollableTextPanel(mcInstance, this.width - 40, this.height - 40 - doneButton.getHeight(), 25, 20);
        this.scrollableTextPanel.setText(this.text);

        this.addRenderableWidget(doneButton);
        this.addRenderableWidget(this.scrollableTextPanel);
    }

    @Override
    public void render(final @NotNull GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTicks) {
        this.renderDirtBackground(guiGraphics);

        drawCenteredStringWithScale(guiGraphics, this.font, this.title, this.width / 2.0F, 5, 16777215, 1.5F);

        this.scrollableTextPanel.render(guiGraphics, mouseX, mouseY, partialTicks);

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    private static void drawCenteredStringWithScale(final GuiGraphics guiGraphics, final Font font, final Component string, final float x, final float y, final int color, final float scale) {
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.scale(scale, scale, 1.0F);
        guiGraphics.drawString(font, string.getString(), ((x / scale) - font.width(string) / 2.0F), y / scale, color, true);
        poseStack.popPose();
    }

    public class ScrollableTextPanel extends ScrollPanel {
        private final Pattern linkPattern = Pattern.compile("\\[.+;(http|https)://\\S+]");

        private List<Pair<Heading, FormattedCharSequence>> lines = Collections.emptyList();
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
            for (final Pair<Heading, FormattedCharSequence> line : lines) {
                if (line != null) {
                    final PoseStack poseStack = guiGraphics.pose();
                    RenderSystem.enableBlend();
                    if (line.first != Heading.NONE) {
                        poseStack.pushPose();
                        float scale = 1F;
                        switch (line.first) {
                            case L1 -> {
                                poseStack.scale(2F, 2F, 1F);
                                poseStack.translate(0.0F, 2F, 0.0F);
                                scale = 2F;
                            }
                            case L2 -> {
                                poseStack.scale(1.5F, 1.5F, 1.0F);
                                poseStack.translate(0.0F, 1.5F, 0.0F);
                                scale = 1.5F;
                            }
                            case L3 -> {
                                poseStack.scale(1.25F, 1.25F, 1.0F);
                                poseStack.translate(0.0F, 1.25F, 0.0F);
                                scale = 1.25F;
                            }
                        }
                        guiGraphics.drawString(LaunchScreen.this.font, line.second, (left + padding) / scale, relativeY / scale, 0xFFFFFF, true);
                        poseStack.popPose();
                    } else {
                        guiGraphics.drawString(LaunchScreen.this.font, line.second, left + padding, relativeY - 0.2F, 0xFFFFFF, true);
                    }
                    RenderSystem.disableBlend();
                }
                relativeY += font.lineHeight;
            }
        }

        // todo: check if this is the right priority and change it if necessary so narration works
        @Override
        public NarratableEntry.@NotNull NarrationPriority narrationPriority() {
            return NarratableEntry.NarrationPriority.NONE;
        }

        @Override
        public void updateNarration(final @NotNull NarrationElementOutput narrationElementOutput) {}

        // todo: change line size to 0.5F, have every paragraph (normal text) print itself plus one blank line, change
        // h1 from 2.0F to 1.5F and print itself plus 2 blank lines. This'll fix the inconsistent gap issue of headings
        // compared to paragraphs without making the headings too big
        public List<Pair<Heading,FormattedCharSequence>> wordWrapAndFormat(final List<String> lines) {
            final List<Pair<Heading,FormattedCharSequence>> resized = new ArrayList<>(lines.size());
            int lineCounter = 0;
            for (String line : lines) {
                if (line == null) {
                    resized.add(null);
                    continue;
                }

                // allow blank lines to be rendered
                if (line.isEmpty()) line += " ";

                // apply formatting codes where appropriate
                line = line.replaceAll("(?i)&([a-f]|[0-9]|l|m|n|o|r|k|u)", "§$1");
                line = line.replace("\\§", "&"); // allow formatting escaping with backslash (e.g. "\&a")

                Heading heading = Heading.tryGetFromString(line);
                if (heading != Heading.NONE) {
                    line = line.replace(heading.getMark() + " ", "");
                }

                var lineWithFormattedLinks = CommonHooks.newChatWithLinks(line, false);
                Matcher matcher = linkPattern.matcher(line);
                if (matcher.find()) {
                    String result = line.substring(matcher.start(), matcher.end());
                    result = result.replaceFirst("\\[", "").replace("]", "");
                    String[] stringParts = line.split(result);
                    String[] split = result.split(";");
                    String text = split[0];
                    String link = split[1];

                    Component head = Component.literal(stringParts[0]);

                    ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.OPEN_URL, link);
                    MutableComponent component = Component.literal(text).withStyle(ChatFormatting.UNDERLINE);
                    Style style = component.getStyle().withClickEvent(clickEvent).withColor(TextColor.fromLegacyFormat(ChatFormatting.BLUE));
                    component.setStyle(style);

                    Component tail = Component.literal(stringParts[1]);

                    lineWithFormattedLinks = Component.empty().append(head).append(component).append(tail);
                }

                final int maxTextLength = this.width - padding * 2;
                if (maxTextLength >= 0) {
                    Language.getInstance().getVisualOrder(font.getSplitter().splitLines(lineWithFormattedLinks, maxTextLength, Style.EMPTY)).forEach(
                            formattedCharSequence -> resized.add(Pair.of(heading, formattedCharSequence))
                    );
                }

                lineCounter += resized.size() - lineCounter;

                // add a blank line after headings to avoid overlapping with any text that may be directly below it
                if (heading != Heading.NONE) {
                    resized.add(Pair.of(Heading.NONE, Component.literal(" ").getVisualOrderText()));
                }
                lineCounter++;
            }

            // if the last line isn't a heading, add a single line at the end of the panel for
            // aesthetical (looks nicer) and functional reasons (hard to click links on last line otherwise)
            if (resized.get(resized.size() - 1).first == Heading.NONE) {
                resized.add(Pair.of(Heading.NONE, Component.literal(" ").getVisualOrderText()));
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
            if (lineIdx >= lines.size() || lineIdx < 1)
                return null;

            final FormattedCharSequence line = lines.get(lineIdx - 1).second;
            if (line != null)
                return font.getSplitter().componentStyleAtWidth(line, mouseX - left - border);

            return null;
        }
    }
}
