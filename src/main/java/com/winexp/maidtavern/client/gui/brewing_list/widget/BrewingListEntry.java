package com.winexp.maidtavern.client.gui.brewing_list.widget;

import com.github.ysbbbbbb.kaleidoscopetavern.api.blockentity.IBarrel;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import net.neoforged.neoforge.client.gui.widget.ExtendedSlider;

import java.util.List;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class BrewingListEntry extends AbstractContainerWidget {
    public final int index;
    private final ExtendedSlider slider;
    private final ExtendedButton button;

    public BrewingListEntry(int index, int x, int y, int width, int height, Consumer<BrewingListEntry> onButtonPress, Consumer<BrewingListEntry> onSliderApply, Component message) {
        super(x, y, width, height, message);
        this.index = index;
        slider = new ExtendedSlider(x + 18, y + 2, width - 36, 12, Component.empty(), Component.empty(),
                IBarrel.BREWING_STARTED, IBarrel.BREWING_FINISHED, IBarrel.BREWING_FINISHED, true) {
            @Override
            protected void updateMessage() {
                if (drawString) {
                    Component levelText = Component.translatable("message.kaleidoscope_tavern.barrel.brew_level." + getValueInt());
                    setMessage(levelText);
                } else {
                    setMessage(Component.empty());
                }
            }

            @Override
            protected void applyValue() {
                onSliderApply.accept(BrewingListEntry.this);
            }
        };
        button = new ExtendedButton(x + width - 16, y, 16, 16, Component.literal("+"), button1 -> onButtonPress.accept(this));
    }

    public int getSliderValue() {
        return slider.getValueInt();
    }

    public void setSliderValue(int value) {
        slider.setValue(value);
    }

    public void setButtonText(Component text) {
        button.setMessage(text);
    }

    public void setButtonTooltip(Tooltip tooltip) {
        button.setTooltip(tooltip);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!active || !visible) return false;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!active || !visible) return false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!active || !visible) return false;
        GuiEventListener listener = getFocused();
        if (listener == null) return false;
        else if (listener.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
        else return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        slider.render(guiGraphics, mouseX, mouseY, partialTick);
        button.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        slider.updateWidgetNarration(output);
        button.updateWidgetNarration(output);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return List.of(
                slider,
                button
        );
    }
}
