package com.winexp.maidtavern.client.gui.brewing_list;

import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.BarrelRecipe;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModRecipes;
import com.winexp.maidtavern.MaidTavern;
import com.winexp.maidtavern.item.MaidTavernItems;
import com.winexp.maidtavern.maid.brew.BrewingList;
import com.winexp.maidtavern.menu.GhostSlot;
import com.winexp.maidtavern.network.ServerboundSetBrewingListPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Rect2i;
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
    public static final Rect2i LEFT_AREA = new Rect2i(LEFT_PICKER_X, LEFT_PICKER_Y, 18 * (PICKER_COLUMNS - 1), 18 * (PICKER_ROWS - 1));
    public static final Rect2i RIGHT_AREA = new Rect2i(RIGHT_PICKER_X, RIGHT_PICKER_Y, 18 * (PICKER_COLUMNS - 1), 18 * (PICKER_ROWS - 1));

    private float leftScrollOffs = 0;
    private float rightScrollOffs = 0;

    public BrewingListScreen(LocalPlayer player, InteractionHand hand, BrewingList brewingList) {
        super(new BrewingListMenu(hand, player.registryAccess(), new BrewingList(brewingList), player.level().getRecipeManager().getAllRecipesFor(ModRecipes.BARREL_RECIPE)),
                player.getInventory(), Component.empty());
        imageWidth = 286;
        imageHeight = 180;
    }

    public boolean canLeftScroll() {
        return menu.getSelectedRows() > PICKER_ROWS;
    }

    public boolean canRightScroll() {
        return menu.getRecipeRows() > PICKER_ROWS;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (LEFT_AREA.contains((int) mouseX, (int) mouseY) && canLeftScroll()) {
            int extraRows = menu.getSelectedRows() - PICKER_ROWS;
            leftScrollOffs = (float) Math.clamp(leftScrollOffs - scrollY / extraRows, 0, 1);
            return true;
        } else if (RIGHT_AREA.contains((int) mouseX, (int) mouseY) && canRightScroll()) {
            int extraRows = menu.getRecipeRows() - PICKER_ROWS;
            rightScrollOffs = (float) Math.clamp(rightScrollOffs - scrollY / extraRows, 0, 1);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
        if (type == ClickType.QUICK_MOVE) type = ClickType.PICKUP;
        menu.clicked(slotId, mouseButton, type, minecraft.player);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
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
        private final BrewingList brewingList;
        private final List<RecipeHolder<BarrelRecipe>> allRecipes;
        private final Map<ResourceLocation, BarrelRecipe> recipeMap;

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
            this.brewingList = brewingList;
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
                    GhostSlot slot = new GhostSlot(recipeContainer, i * PICKER_COLUMNS + j, RIGHT_PICKER_X + 18 * j, RIGHT_PICKER_Y + 18 * i) {
                        @Override
                        public boolean isActive() {
                            return !brewingList.contains(allRecipes.get(getContainerSlot()).id());
                        }
                    };
                    slot.addListener(this::onRecipeSlotClicked);
                    addSlot(slot);
                }
            }
            updateSlots();
        }

        private void onSelectedSlotClicked(GhostSlot slot, Player player, ItemStack carriedStack, ItemStack slotStack, ClickAction action, SlotAccess carriedSlotAccess) {
            if (action == ClickAction.PRIMARY) {
                int idx = slot.getContainerSlot();
                brewingList.remove(idx);
                updateSlots();
            }
        }

        private void onRecipeSlotClicked(GhostSlot slot, Player player, ItemStack carriedStack, ItemStack slotStack, ClickAction action, SlotAccess carriedSlotAccess) {
            int idx = slot.getContainerSlot();
            brewingList.add(allRecipes.get(idx).id());
            updateSlots();
        }

        public void scrollTo() {
            updateSlots();
        }

        public void updateSlots() {
            selectedContainer.clearContent();
            for (int i = 0; i < brewingList.size(); i++) {
                if (i >= PICKER_ROWS * PICKER_COLUMNS) break;
                ResourceLocation recipeId = brewingList.get(i);
                BarrelRecipe recipe = recipeMap.get(recipeId);
                selectedContainer.setItem(i, recipe.getResultItem(registries));
            }
            recipeContainer.clearContent();
            for (int i = 0; i < allRecipes.size(); i++) {
                if (i >= PICKER_ROWS * PICKER_COLUMNS) break;
                BarrelRecipe recipe = allRecipes.get(i).value();
                recipeContainer.setItem(i, recipe.getResultItem(registries));
            }
        }

        @Override
        public void removed(Player player) {
            super.removed(player);
            ServerboundSetBrewingListPayload payload = new ServerboundSetBrewingListPayload(hand, brewingList);
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
            return Mth.positiveCeilDiv(brewingList.size(), getSelectedColumns());
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
