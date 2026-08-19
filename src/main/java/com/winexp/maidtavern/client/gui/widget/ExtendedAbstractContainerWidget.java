package com.winexp.maidtavern.client.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ExtendedAbstractContainerWidget extends AbstractContainerWidget {
    private final List<Renderable> renderables = new ArrayList<>();
    private final List<GuiEventListener> listeners = new ArrayList<>();

    public ExtendedAbstractContainerWidget(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    protected void addRenderable(Renderable renderable) {
        renderables.add(renderable);
    }

    protected void addGuiListener(GuiEventListener guiEventListener) {
        listeners.add(guiEventListener);
    }

    protected void addWidget(AbstractWidget widget) {
        addRenderable(widget);
        addGuiListener(widget);
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
        for (Renderable renderable : renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return listeners;
    }
}
