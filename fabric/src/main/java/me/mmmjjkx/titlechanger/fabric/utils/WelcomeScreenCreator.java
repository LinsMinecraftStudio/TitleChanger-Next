package me.mmmjjkx.titlechanger.fabric.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import me.mmmjjkx.titlechanger.TitleProcessor;
import me.mmmjjkx.titlechanger.fabric.TitleChangerFabric;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.apache.commons.lang3.StringUtils;

public class WelcomeScreenCreator {
    private WelcomeScreenCreator() {}

    public static Screen createWelcomeScreen(TitleProcessor processor, String title, String content) {
        return new WelcomeScreen(processor, title, content);
    }

    private static class WelcomeScreen extends Screen {
        private final TitleProcessor processor;
        private final Screen parent = new TitleScreen();
        private final String content;

        private WelcomeScreen(TitleProcessor processor, String title, String content) {
            super(Component.literal(title));

            this.processor = processor;
            this.content = content;
        }

        protected void init() {
            final Button doneButton = Button.builder(CommonComponents.GUI_DONE, onPress -> this.onClose())
                    .bounds(this.width / 2 - 100, this.height - 20, 200, 20)
                    .build();

            this.addRenderableWidget(doneButton);
        }

        @Override
        public void render(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTicks) {
            this.renderDirtBackground(guiGraphics);

            drawCenteredStringWithScale(guiGraphics, this.font, this.title, this.width / 2.0F, 8, 1.5F);

            super.render(guiGraphics, mouseX, mouseY, partialTicks);
        }

        private static void drawCenteredStringWithScale(final GuiGraphics guiGraphics, final Font font, final Component string, final float x, final float y, final float scale) {
            PoseStack stack = guiGraphics.pose();
            stack.pushPose();
            stack.scale(scale, scale, 1.0F);
            guiGraphics.drawString(font, (FormattedCharSequence) string, (int) ((x / scale) - font.width(string) / 2.0F), (int) ((int) y / scale), 16777215);
            stack.popPose();
        }

        private String parseContentPlaceholders() {
            String r = processor.firstParse(content);

            if (TitleChangerFabric.getResourceSettings().enableWelcomeScreen) {
                r = StringUtils.replace(r, "%modpackName%", TitleChangerFabric.getResourceSettings().modpackName);
                r = StringUtils.replace(r, "%modpackVersion%", TitleChangerFabric.getResourceSettings().modpackVersion);
            }

            return r;
        }

        @Override
        public void onClose() {
            Minecraft.getInstance().setScreen(parent);
        }
    }
}