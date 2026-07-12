package com.winexp.maidtavern.menu;

import com.winexp.maidtavern.item.MaidTavernItems;
import com.winexp.maidtavern.maid.brew.BrewingList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class BrewingListMenu extends AbstractContainerMenu {
    public final Player player;
    public final InteractionHand hand;
    public final BrewingList brewingList;

    public BrewingListMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory,
                buf.readEnum(InteractionHand.class),
                buf.readJsonWithCodec(BrewingList.CODEC)
        );
    }

    public BrewingListMenu(int containerId, Inventory playerInventory, InteractionHand hand, BrewingList brewingList) {
        super(MaidTavernMenuTypes.BREWING_LIST.get(), containerId);
        player = playerInventory.player;
        this.hand = hand;
        this.brewingList = brewingList;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getItemInHand(hand).is(MaidTavernItems.BREWING_LIST);
    }

    public int getRows() {
        return 2;
    }

    public int getColumns() {
        return 6;
    }
}
