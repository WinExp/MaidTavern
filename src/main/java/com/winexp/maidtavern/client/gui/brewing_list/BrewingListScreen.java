package com.winexp.maidtavern.client.gui.brewing_list;

import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.BarrelRecipe;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModRecipes;
import com.winexp.maidtavern.MaidTavern;
import com.winexp.maidtavern.client.gui.brewing_list.widget.BrewingListEntry;
import com.winexp.maidtavern.client.gui.brewing_list.widget.OrderPolicyButton;
import com.winexp.maidtavern.client.renderer.BarrelBindingTargetRenderer;
import com.winexp.maidtavern.item.MaidTavernItems;
import com.winexp.maidtavern.maid.brewing.BrewingList;
import com.winexp.maidtavern.menu.GhostSlot;
import com.winexp.maidtavern.network.serverbound.ServerboundSetBrewingListPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.*;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class BrewingListScreen extends AbstractContainerScreen<BrewingListScreen.BrewingListMenu> {
    public static final ResourceLocation BACKGROUND = MaidTavern.asResource("textures/gui/brewing_list/list.png");
    public static final int PICKER_ROWS = 8;
    public static final int PICKER_COLUMNS = 6;
    public static final int LEFT_PICKER_X = 19;
    public static final int LEFT_PICKER_Y = 16;
    public static final int RIGHT_PICKER_X = 161;
    public static final int RIGHT_PICKER_Y = 16;

    private final List<OrderPolicyButton> orderPolicyButtons = new ArrayList<>();

    public BrewingListScreen(LocalPlayer player, InteractionHand hand, BrewingList brewingList) {
        super(new BrewingListMenu(hand, player.registryAccess(), brewingList, player.level().getRecipeManager().getAllRecipesFor(ModRecipes.BARREL_RECIPE)),
                player.getInventory(), Component.empty());
        imageWidth = 286;
        imageHeight = 180;
    }

    @Override
    public void onClose() {
        minecraft.popGuiLayer();
    }

    @Override
    protected void init() {
        super.init();
        menu.entries.clear();
        orderPolicyButtons.clear();
        for (int i = 0; i < PICKER_ROWS; i++) {
            BrewingListEntry entry = new BrewingListEntry(i, leftPos + LEFT_PICKER_X, topPos + LEFT_PICKER_Y + i * 18,
                    18 * PICKER_COLUMNS, 16, this::onEntryButtonClicked, this::applySliderValue, Component.empty());
            entry.visible = false;
            addRenderableWidget(entry);
            menu.entries.add(entry);
        }
        for (int i = 0; i < BrewingList.OrderPolicy.values().length; i++) {
            BrewingList.OrderPolicy orderPolicy = BrewingList.OrderPolicy.values()[i];
            OrderPolicyButton button = new OrderPolicyButton(leftPos - 26, topPos + LEFT_PICKER_Y - 3 + i * 26, 22, 22, Component.empty(), button1 ->
                    onOrderPolicyButtonClicked((OrderPolicyButton) button1), orderPolicy);
            if (menu.builder.getOrderPolicy() == orderPolicy) {
                button.selected = true;
            }
            addRenderableWidget(button);
            orderPolicyButtons.add(button);
        }
        menu.update();
    }

    private void onOrderPolicyButtonClicked(OrderPolicyButton button) {
        for (OrderPolicyButton orderPolicyButton : orderPolicyButtons) {
            orderPolicyButton.selected = false;
        }
        menu.builder.orderPolicy(button.getOrderPolicy());
        button.selected = true;
    }

    private void onEntryButtonClicked(BrewingListEntry entry) {
        ResourceLocation recipeId = menu.selectedRecipes.get(menu.getScrolledSelectedIdx(entry.index));
        BrewingList.Config config = menu.builder.get(recipeId);
        if (hasShiftDown()) {
            if (config.barrelPos().isEmpty()) return;
            BarrelBindingTargetRenderer.overridePositions(config.barrelPos(), 80);
            onClose();
        } else {
            if (config.barrelPos().isEmpty()) {
                minecraft.pushGuiLayer(new InventorySelectionScreen(minecraft.player, stack -> stack.is(MaidTavernItems.BARREL_SELECTION_TOOL), stack -> {
                    Set<BlockPos> targetPositions = stack.get(MaidTavernItems.BARREL_POSITIONS_DATA);
                    applyNewBarrelPositions(entry, recipeId, targetPositions);
                }));
            } else {
                applyNewBarrelPositions(entry, recipeId, List.of());
            }
        }
    }

    private void applyNewBarrelPositions(BrewingListEntry entry, ResourceLocation recipeId, Collection<BlockPos> barrelPositions) {
        BrewingList.Config oldConfig = menu.builder.get(recipeId);
        BrewingList.Config.Builder builder = new BrewingList.Config.Builder(oldConfig);
        builder.removeAllBarrelPos();
        builder.addAllBarrelPos(barrelPositions);
        BrewingList.Config newConfig = builder.build();
        menu.builder.put(recipeId, newConfig);
        menu.updateEntry(entry);
    }

    private void applySliderValue(BrewingListEntry entry) {
        int value = entry.getSliderValue();
        ResourceLocation recipeId = menu.selectedRecipes.get(menu.getScrolledSelectedIdx(entry.index));
        BrewingList.Config config = menu.builder.get(recipeId);
        BrewingList.Config.Builder builder = new BrewingList.Config.Builder(config);
        builder.brewLevel(value);
        menu.builder.put(recipeId, builder.build());
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.getFocused() != null && this.isDragging() && button == 0) {
            if (this.getFocused().mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        Rect2i leftArea = new Rect2i(leftPos + LEFT_PICKER_X, topPos + LEFT_PICKER_Y, 18 * PICKER_COLUMNS, 18 * PICKER_ROWS);
        Rect2i rightArea = new Rect2i(leftPos + RIGHT_PICKER_X, topPos + RIGHT_PICKER_Y, 18 * PICKER_COLUMNS, 18 * PICKER_ROWS);
        if (leftArea.contains((int) mouseX, (int) mouseY) && menu.canSelectedScroll()) {
            int extraRows = menu.getSelectedRows() - PICKER_ROWS;
            menu.selectedScrollTo((int) Math.clamp(menu.selectedScrollRow - scrollY, 0, extraRows));
            return true;
        } else if (rightArea.contains((int) mouseX, (int) mouseY) && menu.canRecipeScroll()) {
            int extraRows = menu.getRecipeRows() - PICKER_ROWS;
            menu.recipeScrollTo((int) Math.clamp(menu.recipeScrollRow - scrollY, 0, extraRows));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
        if (type == ClickType.QUICK_MOVE) type = ClickType.PICKUP;
        menu.clicked(slotId, mouseButton, type, minecraft.player);
    }

    private void renderCustomSlotHighlight(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(leftPos, topPos, 0);
        for (Slot slot : menu.slots) {
            if (!slot.isActive() || !slot.isHighlightable()) continue;
            if (!(slot instanceof GhostSlot ghostSlot)) continue;
            if (ghostSlot.highlightPredicate != null && ghostSlot.highlightPredicate.shouldRenderHighlight(ghostSlot, mouseX, mouseY)) {
                renderSlotHighlight(guiGraphics, slot, mouseX, mouseY, partialTick);
            }
        }
        guiGraphics.pose().popPose();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderCustomSlotHighlight(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(BACKGROUND, (width - 384) / 2, (height - 384) / 2, 0, 0, 384, 384, 512, 512);
    }

    @OnlyIn(Dist.CLIENT)
    public static class BrewingListMenu extends AbstractContainerMenu {
        private final InteractionHand hand;
        private final HolderLookup.Provider registries;
        private final BrewingList oldList;
        private final BrewingList.Builder builder;
        private final List<ResourceLocation> selectedRecipes;
        private final List<RecipeHolder<BarrelRecipe>> allRecipes;
        private final Map<ResourceLocation, BarrelRecipe> recipeMap;
        private int selectedScrollRow;
        private int recipeScrollRow;

        private final List<BrewingListEntry> entries = new ArrayList<>();

        private final SimpleContainer selectedContainer = new SimpleContainer(PICKER_ROWS) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        };
        private final SimpleContainer recipeContainer = new SimpleContainer(PICKER_ROWS * PICKER_COLUMNS) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        };

        public BrewingListMenu(InteractionHand hand, HolderLookup.Provider registries, BrewingList brewingList, List<RecipeHolder<BarrelRecipe>> allRecipes) {
            super(null, 0);
            this.hand = hand;
            this.registries = registries;
            oldList = brewingList;
            builder = new BrewingList.Builder(brewingList);
            selectedRecipes = new LinkedList<>(brewingList.entries().keySet());
            this.allRecipes = allRecipes;
            recipeMap = allRecipes.stream().collect(Collectors.toMap(RecipeHolder::id, RecipeHolder::value));

            for (int i = 0; i < PICKER_ROWS; i++) {
                GhostSlot slot = new GhostSlot(selectedContainer, i, LEFT_PICKER_X, LEFT_PICKER_Y + 18 * i);
                slot.addListener(this::onSelectedSlotClicked);
                addSlot(slot);
            }

            for (int i = 0; i < PICKER_ROWS; i++) {
                for (int j = 0; j < PICKER_COLUMNS; j++) {
                    GhostSlot slot = new GhostSlot(recipeContainer, i * PICKER_COLUMNS + j, RIGHT_PICKER_X + 18 * j, RIGHT_PICKER_Y + 18 * i);
                    slot.addListener(this::onRecipeSlotClicked);
                    slot.highlightPredicate = (slot1, mouseX, mouseY) -> {
                        int idx = getScrolledRecipeIdx(slot1.getContainerSlot());
                        if (idx >= allRecipes.size()) return false;
                        RecipeHolder<BarrelRecipe> recipe = allRecipes.get(idx);
                        return selectedRecipes.contains(recipe.id());
                    };
                    addSlot(slot);
                }
            }
        }

        public boolean canSelectedScroll() {
            return getSelectedRows() > PICKER_ROWS;
        }

        public boolean canRecipeScroll() {
            return getRecipeRows() > PICKER_ROWS;
        }

        public int getScrolledSelectedIdx(int slot) {
            if (!canSelectedScroll()) return slot;
            int extraRows = getSelectedRows() - PICKER_ROWS;
            selectedScrollRow = Math.clamp(selectedScrollRow, 0, extraRows);
            return selectedScrollRow + slot;
        }

        public int getScrolledRecipeIdx(int slot) {
            if (!canRecipeScroll()) return slot;
            int extraRows = getRecipeRows() - PICKER_ROWS;
            recipeScrollRow = Math.clamp(recipeScrollRow, 0, extraRows);
            return recipeScrollRow * PICKER_COLUMNS + slot;
        }

        public void selectedScrollTo(int scroll) {
            selectedScrollRow = scroll;
            update();
        }

        public void recipeScrollTo(int scroll) {
            recipeScrollRow = scroll;
            update();
        }

        private void onSelectedSlotClicked(GhostSlot slot, Player player, ItemStack carriedStack, ClickAction action, SlotAccess carriedSlotAccess) {
            int idx = getScrolledSelectedIdx(slot.getContainerSlot());
            if (idx >= selectedRecipes.size()) return;
            ResourceLocation recipeId = selectedRecipes.get(idx);
            builder.remove(recipeId);
            selectedRecipes.remove(idx);
            update();
        }

        private void onRecipeSlotClicked(GhostSlot slot, Player player, ItemStack carriedStack, ClickAction action, SlotAccess carriedSlotAccess) {
            int idx = getScrolledRecipeIdx(slot.getContainerSlot());
            ResourceLocation recipeId = allRecipes.get(idx).id();
            if (selectedRecipes.contains(recipeId)) return;
            builder.put(recipeId, BrewingList.Config.DEFAULT);
            selectedRecipes.add(recipeId);
            update();
        }

        public void update() {
            selectedContainer.clearContent();
            for (Slot slot : slots) {
                GhostSlot ghostSlot = (GhostSlot) slot;
                ghostSlot.setActive(false);
            }
            for (BrewingListEntry entry : entries) {
                entry.visible = false;
            }
            int selectedBeginIdx = getScrolledSelectedIdx(0);
            for (int i = selectedBeginIdx; i < selectedRecipes.size(); i++) {
                int slotIdx = i - selectedBeginIdx;
                if (slotIdx >= PICKER_ROWS) break;
                ResourceLocation recipeId = selectedRecipes.get(i);
                BarrelRecipe recipe = recipeMap.get(recipeId);
                selectedContainer.setItem(slotIdx, recipe.getResultItem(registries));
                GhostSlot slot = (GhostSlot) getSlot(slotIdx);
                entries.get(slotIdx).visible = true;
                slot.setActive(true);
            }
            recipeContainer.clearContent();
            int recipeBeginIdx = getScrolledRecipeIdx(0);
            for (int i = recipeBeginIdx; i < allRecipes.size(); i++) {
                int slotIdx = i - recipeBeginIdx;
                if (slotIdx >= PICKER_ROWS * PICKER_COLUMNS) break;
                RecipeHolder<BarrelRecipe> recipe = allRecipes.get(i);
                recipeContainer.setItem(slotIdx, recipe.value().getResultItem(registries));
                GhostSlot slot = (GhostSlot) getSlot(slotIdx + PICKER_ROWS);
                slot.setActive(true);
            }

            for (BrewingListEntry entry : entries) {
                updateEntry(entry);
            }
        }

        public void updateEntry(BrewingListEntry entry) {
            if (!entry.visible) return;
            ResourceLocation recipeId = selectedRecipes.get(getScrolledSelectedIdx(entry.index));
            BrewingList.Config config = builder.get(recipeId);
            entry.setSliderValue(config.brewLevel());
            entry.setButtonText(config.barrelPos().isEmpty() ? Component.literal("+") : Component.literal("-"));
            entry.setButtonTooltip(getButtonTooltip(List.copyOf(config.barrelPos())));
        }

        private Tooltip getButtonTooltip(List<BlockPos> positions) {
            MutableComponent tooltip = Component.empty();
            for (int i = 0; i < positions.size(); i++) {
                BlockPos pos = positions.get(i);
                Component component = Component.literal("[")
                        .append(Component.literal(pos.toShortString()))
                        .append("]")
                        .withStyle(ChatFormatting.WHITE);
                tooltip.append(component);
                if (i < positions.size() - 1) {
                    tooltip.append("\n");
                }
            }
            return Tooltip.create(tooltip);
        }

        @Override
        public void removed(Player player) {
            super.removed(player);
            BrewingList newList = builder.build();
            if (oldList.equals(newList)) return;
            ServerboundSetBrewingListPayload payload = new ServerboundSetBrewingListPayload(hand, newList);
            Minecraft.getInstance().getConnection().send(payload);
        }

        @Override
        public ItemStack quickMoveStack(Player player, int i) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(Player player) {
            return player.getItemInHand(hand).is(MaidTavernItems.BREWING_LIST);
        }

        public int getSelectedRows() {
            return selectedRecipes.size();
        }

        public int getRecipeRows() {
            return Mth.positiveCeilDiv(allRecipes.size(), getRecipeColumns());
        }

        public int getRecipeColumns() {
            return PICKER_COLUMNS;
        }
    }
}
