package com.winexp.maidtavern.menu;

import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.BarrelRecipe;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModRecipes;
import com.winexp.maidtavern.item.MaidTavernItems;
import com.winexp.maidtavern.maid.brew.BrewingList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public class BrewingListMenu extends AbstractContainerMenu {
    public final Player player;
    public final InteractionHand hand;
    public final SimpleContainer selectedItems = new SimpleContainer(getRows() * getColumns()) {
        @Override
        public int getMaxStackSize() {
            return 1;
        }
    };
    public final SimpleContainer recipeItems;
    public BrewingList brewingList;
    private final List<RecipeHolder<BarrelRecipe>> allRecipes;

    public BrewingListMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory,
                buf.readEnum(InteractionHand.class),
                BrewingList.STREAM_CODEC.decode(buf)
        );
    }

    public BrewingListMenu(int containerId, Inventory playerInventory, InteractionHand hand, BrewingList brewingList) {
        super(MaidTavernMenuTypes.BREWING_LIST.get(), containerId);
        player = playerInventory.player;
        this.hand = hand;
        this.brewingList = brewingList;
        for (int row = 0; row < getRows(); row++) {
            for (int col = 0; col < getColumns(); col++) {
                GhostSlot slot = new GhostSlot(selectedItems, col + row * getColumns(),
                        -36 + 18 * col, 9 + 18 * row) {
                    @Override
                    public boolean isActive() {
                        return !getItem().isEmpty();
                    }
                };
                slot.addListener(this::onSelectedSlotClicked);
                addSlot(slot);
            }
        }
        allRecipes = player.level().getRecipeManager().getAllRecipesFor(ModRecipes.BARREL_RECIPE);
        recipeItems = new SimpleContainer(allRecipes.size()) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        };
        for (int i = 0; i < allRecipes.size(); i++) {
            int row = i / getColumns();
            int col = i % getColumns();
            BarrelRecipe recipe = allRecipes.get(i).value();
            GhostSlot slot = new GhostSlot(recipeItems, col + row * getColumns(),
                    106 + 18 * col, 9 + 18 * row) {
                @Override
                public boolean isActive() {
                    return !brewingList.contains(allRecipes.get(getContainerSlot()).id());
                }
            };
            slot.addListener(this::onRecipeSlotClicked);
            slot.set(recipe.getResultItem(player.registryAccess()).copyWithCount(1));
            addSlot(slot);
        }
        updateSlots();
    }

    private void onSelectedSlotClicked(GhostSlot slot, Player player, ItemStack carriedStack, ItemStack slotStack, ClickAction action, SlotAccess carriedSlotAccess) {
        if (action == ClickAction.PRIMARY) {
            brewingList.remove(slot.getContainerSlot());
            updateSlots();
        }
    }

    private void onRecipeSlotClicked(GhostSlot slot, Player player, ItemStack carriedStack, ItemStack slotStack, ClickAction action, SlotAccess carriedSlotAccess) {
        brewingList.add(allRecipes.get(slot.getContainerSlot()).id());
        updateSlots();
    }

    public void updateSlots() {
        if (player instanceof ServerPlayer) {
            selectedItems.clearContent();
            for (ResourceLocation recipeId : brewingList.getRecipes()) {
                BarrelRecipe recipe = (BarrelRecipe) player.level().getRecipeManager().byKey(recipeId).map(RecipeHolder::value).get();
                ItemStack resultItem = recipe.getResultItem(player.registryAccess()).copyWithCount(1);
                selectedItems.addItem(resultItem);
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        GhostSlot slot = (GhostSlot) getSlot(i);
        if (i < getRows() * getColumns()) {
            onSelectedSlotClicked(slot, player, ItemStack.EMPTY, slot.getItem(), ClickAction.PRIMARY, SlotAccess.NULL);
        } else {
            onRecipeSlotClicked(slot, player, ItemStack.EMPTY, slot.getItem(), ClickAction.PRIMARY, SlotAccess.NULL);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getItemInHand(hand).is(MaidTavernItems.BREWING_LIST);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (player instanceof ServerPlayer) {
            ItemStack stack = player.getItemInHand(hand);
            if (!stack.is(MaidTavernItems.BREWING_LIST)) return;
            stack.set(MaidTavernItems.BREWING_LIST_DATA, brewingList);
        }
    }

    public int getRows() {
        return 8;
    }

    public int getColumns() {
        return 6;
    }
}
