package com.winexp.maidtavern.menu;

import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.BarrelRecipe;
import com.winexp.maidtavern.item.MaidTavernItems;
import com.winexp.maidtavern.maid.brew.BrewingList;
import com.winexp.maidtavern.network.ServerBoundSetBrewingListPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

public class BrewingListMenu extends AbstractContainerMenu {
    public final Player player;
    public final InteractionHand hand;
    public final SimpleContainer displayItems = new SimpleContainer(getRows() * getColumns()) {
        @Override
        public int getMaxStackSize() {
            return 1;
        }
    };
    public BrewingList brewingList;

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
                GhostSlot slot = new GhostSlot(displayItems, col + row * getColumns(),
                        -36 + 18 * col, 9 + 18 * row) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false;
                    }
                };
                slot.addListener(this::onSlotEmptied);
                addSlot(slot);
            }
        }
        updateSlots();
    }

    private void onSlotEmptied(GhostSlot slot, ItemStack prevStack) {
        brewingList.remove(slot.getContainerSlot());
        updateSlots();
    }

    public void sendList() {
        if (player instanceof LocalPlayer localPlayer) {
            var payload = new ServerBoundSetBrewingListPayload(containerId, brewingList);
            localPlayer.connection.send(payload);
        }
    }

    public void updateSlots() {
        if (player instanceof ServerPlayer) {
            displayItems.clearContent();
            for (ResourceLocation recipeId : brewingList.getRecipes()) {
                BarrelRecipe recipe = (BarrelRecipe) player.level().getRecipeManager().byKey(recipeId).map(RecipeHolder::value).get();
                ItemStack resultItem = recipe.getResultItem(player.registryAccess()).copyWithCount(1);
                displayItems.addItem(resultItem);
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
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
        return 2;
    }

    public int getColumns() {
        return 6;
    }
}
