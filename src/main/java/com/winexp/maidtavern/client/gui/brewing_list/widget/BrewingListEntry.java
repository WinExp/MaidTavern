package com.winexp.maidtavern.client.gui.brewing_list.widget;

import com.github.ysbbbbbb.kaleidoscopetavern.api.blockentity.IBarrel;
import com.winexp.maidtavern.client.gui.widget.ExtendedAbstractContainerWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import net.neoforged.neoforge.client.gui.widget.ExtendedSlider;

import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class BrewingListEntry extends ExtendedAbstractContainerWidget {
    public final int index;
    private final ExtendedSlider slider;
    private final ExtendedButton button;

    public BrewingListEntry(int index, int x, int y, int width, int height, Consumer<BrewingListEntry> onButtonPress, Consumer<BrewingListEntry> onSliderApply, Component message) {
        super(x, y, width, height, message);
        this.index = index;
        slider = new ExtendedSlider(x + 20, y + 2, width - 40, 12, Component.empty(), Component.empty(),
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
        button = new ExtendedButton(x + width - 16, y, 16, 16, Component.literal("+"), button1 -> onButtonPress.accept(BrewingListEntry.this));
        addWidget(slider);
        addWidget(button);
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
}
