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

package me.mmmjjkx.titlechanger.neoforge.screens;

import com.ibm.icu.impl.Pair;
import com.mojang.blaze3d.vertex.Tesselator;
import me.mmmjjkx.titlechanger.Constants;
import me.mmmjjkx.titlechanger.enums.formatting.Heading;
import me.mmmjjkx.titlechanger.neoforge.TitleChangerNeoForge;
import me.mmmjjkx.titlechanger.neoforge.utils.ComponentUtils;
import me.mmmjjkx.titlechanger.neoforge.utils.Reflects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.*;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.client.gui.widget.ScrollPanel;
import net.neoforged.neoforge.common.CommonHooks;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Matcher;

public class LaunchScreen extends Screen {
    private static final MethodHandle DRAW_STRING;
    private static final MethodHandle GET_POSE;

    static {
        DRAW_STRING = Arrays.stream(GuiGraphics.class.getMethods())
                .filter(m -> m.getName().equals("drawString"))
                .filter(m -> {
                    Class<?> ret = m.getReturnType();
                    return (ret == int.class || ret == void.class) && (m.getParameters().length == 5 && m.getParameters()[1].getType() == FormattedCharSequence.class);
                })
                .findFirst()
                .map(m -> {
                    try {
                        return MethodHandles.lookup().unreflect(m);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElse(null);

        GET_POSE = Arrays.stream(GuiGraphics.class.getMethods())
                .filter(m -> m.getName().equals("pose"))
                .filter(m -> m.getParameterCount() == 0)
                .findFirst()
                .map(m -> {
                    try {
                        return MethodHandles.lookup().unreflect(m);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElse(null);
    }

    private final Screen previousScreen;
    private final Minecraft mcInstance = Minecraft.getInstance();
    private final Supplier<List<String>> text;
    private final Supplier<Component> title;
    private final Runnable onDone;
    private ScrollableTextPanel scrollableTextPanel;

    private static Object getPoseFromGuiGraphics(GuiGraphics guiGraphics) {
        try {
            if (GET_POSE != null) {
                return GET_POSE.invoke(guiGraphics);
            } else {
                Method[] methods = GuiGraphics.class.getMethods();
                for (Method method : methods) {
                    if ((method.getName().equals("method_51448") || method.getName().equals("pose"))
                            && method.getParameterCount() == 0) {
                        return method.invoke(guiGraphics);
                    }
                }
                throw new RuntimeException("No pose method found in GuiGraphics");
            }
        } catch (Throwable e) {
            throw new RuntimeException("Failed to get pose from GuiGraphics: " + e.getMessage(), e);
        }
    }

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

        this.addRenderableWidget(this.scrollableTextPanel);
        this.addRenderableWidget(doneButton);
        this.addRenderableWidget(languageButton);
    }

    @Override
    public void render(final @NotNull GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTicks) {
        this.scrollableTextPanel.setText(this.text.get());
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        Object pose = getPoseFromGuiGraphics(guiGraphics);

        Reflects.pushPose(pose);
        Reflects.scale(pose, 1.5f, 1.5f, 1f);

        try {
            DRAW_STRING.invoke(guiGraphics, this.font,
                    this.title.get().getVisualOrderText(),
                    (int) ((this.width / 2f / 1.5f) - font.width(this.title.get()) / 2.0F),
                    5,
                    0xFFFFFFFF
            );
        } catch (Throwable e) {
            guiGraphics.drawString(this.font,
                    this.title.get().getVisualOrderText(),
                    (int) ((this.width / 2f / 1.5f) - font.width(this.title.get()) / 2.0F),
                    5,
                    0xFFFFFFFF
            );
        }

        Reflects.popPose(pose);
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

        protected void drawPanel(@NotNull GuiGraphics guiGraphics, int entryRight, int relativeY, Tesselator tess, int mouseX, int mouseY) {
            drawPanel(guiGraphics, entryRight, relativeY, mouseX, mouseY);
        }

        protected void drawPanel(@NotNull GuiGraphics guiGraphics, int entryRight, int relativeY, int mouseX, int mouseY) {
            guiGraphics.enableScissor(this.left, this.top, this.right, this.bottom);
            for (final Pair<Heading, ComponentUtils.LineStyles> line : lines) {
                if (line != null) {
                    if (line.first != Heading.NONE) {
                        Object poseStack = getPoseFromGuiGraphics(guiGraphics);
                        Reflects.pushPose(poseStack);
                        float scale = switch (line.first) {
                            case L1 -> 1.8F;
                            case L2 -> 1.6F;
                            case L3 -> 1.4F;
                            default -> 1.0F;
                        };
                        Reflects.scale(poseStack, scale, scale, 1.0F);
                        Reflects.translate(poseStack, 0.0F, scale, 0.0F);
                        try {
                            DRAW_STRING.invoke(guiGraphics, LaunchScreen.this.font, line.second.text(), (int) ((left + padding) / scale), (int) (relativeY / scale), 0xFFFFFFFF);
                        } catch (Throwable e) {
                            guiGraphics.drawString(LaunchScreen.this.font, line.second.text(), (int) ((left + padding) / scale), (int) (relativeY / scale), 0xFFFFFFFF);
                        }
                        Reflects.popPose(poseStack);
                    } else {
                        try {
                            DRAW_STRING.invoke(guiGraphics, LaunchScreen.this.font, line.second.text(), (left + padding), relativeY, 0xFFFFFFFF);
                        } catch (Throwable e) {
                            guiGraphics.drawString(LaunchScreen.this.font, line.second.text(), (left + padding), relativeY, 0xFFFFFFFF);
                        }
                    }
                }
                relativeY += font.lineHeight;
            }
            guiGraphics.disableScissor();
        }

        @Override
        public @NotNull NarrationPriority narrationPriority() {
            return NarrationPriority.FOCUSED;
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
                line = TitleChangerNeoForge.titleProcessor.firstParseNoCache(line); //allow parsing placeholders

                Heading heading = Heading.tryGetFromString(line);
                if (heading != Heading.NONE) {
                    line = StringUtils.replace(line, heading.getMark() + " ", "", 1);
                }

                var lineWithFormattedLinks = CommonHooks.newChatWithLinks(line, false);
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

            FormattedCharSequence line = lines.get(lineIdx);
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
