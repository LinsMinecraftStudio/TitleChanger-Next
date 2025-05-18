package me.mmmjjkx.titlechanger.fabric.screens;

import me.mmmjjkx.titlechanger.enums.UpdateCheckMode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class UpdatableScreen extends Screen {
    private final Component message;

    protected final Consumer<UpdateCheckMode> callback;

    private MultiLineLabel multilineMessage = MultiLineLabel.EMPTY;

    protected Component yesButton;
    protected Component noButton;

    public UpdatableScreen(Consumer<UpdateCheckMode> callback, String modpackName) {
        this(callback, Component.translatable("titlechanger.update-checker.available", modpackName), Component.translatable("titlechanger.update-checker.available.desc"), CommonComponents.GUI_YES, CommonComponents.GUI_NO);
    }

    public UpdatableScreen(Consumer<UpdateCheckMode> callback, Component title, Component message, Component yesButton, Component noButton) {
        super(title);

        this.callback = callback;
        this.message = message;
        this.yesButton = yesButton;
        this.noButton = noButton;
    }

    @Override
    public @NotNull Component getNarrationMessage() {
        return CommonComponents.joinForNarration(super.getNarrationMessage(), this.message);
    }

    @Override
    protected void init() {
        super.init();
        this.multilineMessage = MultiLineLabel.create(this.font, this.message, this.width - 50);
        int i = Mth.clamp(this.messageTop() + this.messageHeight() + 20, this.height / 6 + 96, this.height - 24);
        this.addButtons(i);
    }

    protected void addButtons(int y) {
        this.addRenderableWidget(
                Button.builder(this.yesButton, p_169249_ -> this.callback.accept(UpdateCheckMode.ALLOW)).bounds(this.width / 2 - 60 - 125, y, 120, 20).build()
        );
        this.addRenderableWidget(
                Button.builder(this.noButton, p_169245_ -> this.callback.accept(UpdateCheckMode.ALLOW_BUT_CANCEL)).bounds(this.width / 2 - 60 + 125, y, 120, 20).build()
        );
        this.addRenderableWidget(
                Button.builder(Component.translatable("titlechanger.update-checker.never"), p_169247_ -> this.callback.accept(UpdateCheckMode.NEVER)).bounds(this.width / 2 - 60, y, 120, 20).build()
        );
    }

    /**
     * Renders the graphical user interface (GUI) element.
     *
     * @param guiGraphics the GuiGraphics object used for rendering.
     * @param mouseX      the x-coordinate of the mouse cursor.
     * @param mouseY      the y-coordinate of the mouse cursor.
     * @param partialTick the partial tick time.
     */
    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.titleTop(), 16777215);
        this.multilineMessage.renderCentered(guiGraphics, this.width / 2, this.messageTop());
    }

    private int titleTop() {
        int i = (this.height - this.messageHeight()) / 2;
        return Mth.clamp(i - 20 - 9, 10, 80);
    }

    private int messageTop() {
        return this.titleTop() + 20;
    }

    private int messageHeight() {
        return this.multilineMessage.getLineCount() * 9;
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.callback.accept(UpdateCheckMode.ALLOW_BUT_CANCEL);
            return true;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }
}
