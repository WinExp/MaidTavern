package com.winexp.maidtavern.client.gui.brewing_list.widget;

import com.google.common.collect.Maps;
import com.winexp.maidtavern.MaidTavern;
import com.winexp.maidtavern.maid.brewing.BrewingList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ExtendedButton;

import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class OrderPolicyButton extends ExtendedButton {
    public static final ResourceLocation DEFAULT = MaidTavern.asResource("textures/gui/brewing_list/order_policy/button.png");
    public static final ResourceLocation HOVERED = MaidTavern.asResource("textures/gui/brewing_list/order_policy/button_highlighted.png");
    public static final ResourceLocation SELECTED = MaidTavern.asResource("textures/gui/brewing_list/order_policy/button_selected.png");
    public static final Map<BrewingList.OrderPolicy, ResourceLocation> ORDER_POLICIES = Maps.toMap(List.of(BrewingList.OrderPolicy.values()), policy ->
            MaidTavern.asResource("textures/gui/brewing_list/order_policy/icon/%s.png".formatted(policy.getSerializedName())));

    private final BrewingList.OrderPolicy orderPolicy;
    public boolean selected;

    public OrderPolicyButton(int x, int y, int width, int height, Component text, OnPress handler, BrewingList.OrderPolicy orderPolicy) {
        super(x, y, width, height, text, handler);
        this.orderPolicy = orderPolicy;
    }

    public BrewingList.OrderPolicy getOrderPolicy() {
        return orderPolicy;
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        return !selected && super.clicked(mouseX, mouseY);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        ResourceLocation buttonTexture = DEFAULT;
        if (selected) {
            buttonTexture = SELECTED;
        } else if (isHovered()) {
            buttonTexture = HOVERED;
        }
        ResourceLocation policyTexture = ORDER_POLICIES.get(orderPolicy);
        int width = getWidth();
        int height = getHeight();
        guiGraphics.blit(buttonTexture, getX(), getY(), width, height, 0, 0, 44, 44, 44, 44);
        guiGraphics.blit(policyTexture, getX(), getY(), width, height, 0, 0, 44, 44, 44, 44);
        FormattedText buttonText = mc.font.ellipsize(this.getMessage(), this.width - 6);
        guiGraphics.drawCenteredString(mc.font, Language.getInstance().getVisualOrder(buttonText), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, this.getFGColor());
    }
}
