package com.winexp.maidtavern.item;

import com.winexp.maidtavern.maid.brew.BrewingList;
import com.winexp.maidtavern.menu.BrewingListMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class BrewingListItem extends Item implements MenuProvider {
    public BrewingListItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        player.openMenu(this, buf -> {
            buf.writeEnum(usedHand);
            BrewingList brewingList = stack.get(MaidTavernItems.BREWING_LIST_DATA);
            if (brewingList == null) brewingList = new BrewingList();
            buf.writeJsonWithCodec(BrewingList.CODEC, brewingList);
        });
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.maidtavern.brewing_list");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        InteractionHand hand = player.getUsedItemHand();
        ItemStack stack = player.getItemInHand(hand);
        return new BrewingListMenu(containerId, inventory, hand, stack.getOrDefault(MaidTavernItems.BREWING_LIST_DATA.get(), new BrewingList()));
    }
}
