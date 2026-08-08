package com.winexp.maidtavern.client.gui.brewing_list;

import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.BarrelRecipe;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModRecipes;
import com.winexp.maidtavern.MaidTavern;
import com.winexp.maidtavern.item.MaidTavernItems;
import com.winexp.maidtavern.maid.brew.BrewingList;
import com.winexp.maidtavern.menu.GhostSlot;
import com.winexp.maidtavern.network.serverbound.ServerboundSetBrewingListPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class BrewingListScreen extends AbstractContainerScreen<BrewingListScreen.BrewingListMenu> {
    public static final ResourceLocation LIST_LOCATION = MaidTavern.asResource("textures/gui/brewing_list/list.png");
    public static final int PICKER_ROWS = 8;
    public static final int PICKER_COLUMNS = 6;
    public static final int LEFT_PICKER_X = 19;
    public static final int LEFT_PICKER_Y = 16;
    public static final int RIGHT_PICKER_X = 161;
    public static final int RIGHT_PICKER_Y = 16;

    public BrewingListScreen(LocalPlayer player, InteractionHand hand, BrewingList brewingList) {
        super(new BrewingListMenu(hand, player.registryAccess(), brewingList, player.level().getRecipeManager().getAllRecipesFor(ModRecipes.BARREL_RECIPE)),
                player.getInventory(), Component.empty());
        imageWidth = 286;
        imageHeight = 180;
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
            if (!slot.isActive()) continue;
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
        guiGraphics.blit(LIST_LOCATION, (width - 384) / 2, (height - 384) / 2, 0, 0, 384, 384, 512, 512);
    }

    @OnlyIn(Dist.CLIENT)
    public static class BrewingListMenu extends AbstractContainerMenu {
        private final InteractionHand hand;
        private final HolderLookup.Provider registries;
        private final BrewingList.Builder builder;
        private final List<ResourceLocation> selectedRecipes;
        private final List<RecipeHolder<BarrelRecipe>> allRecipes;
        private final Map<ResourceLocation, BarrelRecipe> recipeMap;
        private int selectedScrollRow;
        private int recipeScrollRow;

        private final SimpleContainer selectedContainer = new SimpleContainer(PICKER_ROWS * PICKER_COLUMNS) {
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
            builder = new BrewingList.Builder(brewingList);
            selectedRecipes = new LinkedList<>(brewingList.entries().keySet());
            this.allRecipes = allRecipes;
            recipeMap = allRecipes.stream().collect(Collectors.toMap(RecipeHolder::id, RecipeHolder::value));

            for (int i = 0; i < PICKER_ROWS; i++) {
                for (int j = 0; j < PICKER_COLUMNS; j++) {
                    GhostSlot slot = new GhostSlot(selectedContainer, i * PICKER_COLUMNS + j, LEFT_PICKER_X + 18 * j, LEFT_PICKER_Y + 18 * i);
                    slot.addListener(this::onSelectedSlotClicked);
                    addSlot(slot);
                }
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
            updateSlots();
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
            return selectedScrollRow * PICKER_COLUMNS + slot;
        }

        public int getScrolledRecipeIdx(int slot) {
            if (!canRecipeScroll()) return slot;
            int extraRows = getRecipeRows() - PICKER_ROWS;
            recipeScrollRow = Math.clamp(recipeScrollRow, 0, extraRows);
            return recipeScrollRow * PICKER_COLUMNS + slot;
        }

        public void selectedScrollTo(int scroll) {
            selectedScrollRow = scroll;
            updateSlots();
        }

        public void recipeScrollTo(int scroll) {
            recipeScrollRow = scroll;
            updateSlots();
        }

        private void onSelectedSlotClicked(GhostSlot slot, Player player, ItemStack carriedStack, ItemStack slotStack, ClickAction action, SlotAccess carriedSlotAccess) {
            if (action == ClickAction.PRIMARY) {
                int idx = getScrolledSelectedIdx(slot.getContainerSlot());
                if (idx >= selectedRecipes.size()) return;
                ResourceLocation recipeId = selectedRecipes.get(idx);
                builder.remove(recipeId);
                selectedRecipes.remove(idx);
                updateSlots();
            }
        }

        private void onRecipeSlotClicked(GhostSlot slot, Player player, ItemStack carriedStack, ItemStack slotStack, ClickAction action, SlotAccess carriedSlotAccess) {
            int idx = getScrolledRecipeIdx(slot.getContainerSlot());
            ResourceLocation recipeId = allRecipes.get(idx).id();
            if (selectedRecipes.contains(recipeId)) return;
            builder.put(recipeId, new BrewingList.Config(5, List.of(new BlockPos(-3, -60, 22))));
            selectedRecipes.add(recipeId);
            updateSlots();
        }

        public void updateSlots() {
            selectedContainer.clearContent();
            int selectedBeginIdx = getScrolledSelectedIdx(0);
            for (int i = selectedBeginIdx; i < selectedRecipes.size(); i++) {
                int slot = i - selectedBeginIdx;
                if (slot >= PICKER_ROWS * PICKER_COLUMNS) break;
                ResourceLocation recipeId = selectedRecipes.get(i);
                BarrelRecipe recipe = recipeMap.get(recipeId);
                selectedContainer.setItem(slot, recipe.getResultItem(registries));
            }
            recipeContainer.clearContent();
            int recipeBeginIdx = getScrolledRecipeIdx(0);
            for (int i = recipeBeginIdx; i < allRecipes.size(); i++) {
                int slot = i - recipeBeginIdx;
                if (slot >= PICKER_ROWS * PICKER_COLUMNS) break;
                RecipeHolder<BarrelRecipe> recipe = allRecipes.get(i);
                recipeContainer.setItem(slot, recipe.value().getResultItem(registries));
            }
        }

        @Override
        public void removed(Player player) {
            super.removed(player);
            ServerboundSetBrewingListPayload payload = new ServerboundSetBrewingListPayload(hand, builder.build());
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
            return Mth.positiveCeilDiv(selectedRecipes.size(), getSelectedColumns());
        }

        public int getSelectedColumns() {
            return PICKER_COLUMNS;
        }

        public int getRecipeRows() {
            return Mth.positiveCeilDiv(allRecipes.size(), getRecipeColumns());
        }

        public int getRecipeColumns() {
            return PICKER_COLUMNS;
        }
    }
}
