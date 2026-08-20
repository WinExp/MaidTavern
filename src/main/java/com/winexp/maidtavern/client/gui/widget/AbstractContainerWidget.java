package com.winexp.maidtavern.client.gui.widget;

import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractContainerWidget extends AbstractWidget implements ContainerEventHandler {
    @Nullable
    private GuiEventListener focused;
    private boolean isDragging;

    public AbstractContainerWidget(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    public final boolean isDragging() {
        return this.isDragging;
    }

    public final void setDragging(boolean dragging) {
        this.isDragging = dragging;
    }

    @Nullable
    public GuiEventListener getFocused() {
        return this.focused;
    }

    public void setFocused(@Nullable GuiEventListener listener) {
        if (this.focused != null) {
            this.focused.setFocused(false);
        }

        if (listener != null) {
            listener.setFocused(true);
        }

        this.focused = listener;
    }

    @Nullable
    public ComponentPath nextFocusPath(FocusNavigationEvent event) {
        return ContainerEventHandler.super.nextFocusPath(event);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return ContainerEventHandler.super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return ContainerEventHandler.super.mouseReleased(mouseX, mouseY, button);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return ContainerEventHandler.super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    public boolean isFocused() {
        return ContainerEventHandler.super.isFocused();
    }

    public void setFocused(boolean focused) {
        ContainerEventHandler.super.setFocused(focused);
    }
}
