package com.winexp.maidtavern.item;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import com.winexp.maidtavern.maid.brew.BrewingList;
import com.winexp.maidtavern.menu.BrewingListMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class BrewingListItem extends Item implements MenuProvider, MaidInteractionItem {
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
            BrewingList.STREAM_CODEC.encode(buf, brewingList);
        });
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public boolean useOnMaid(Level level, Player player, EntityMaid maid, ItemStack stack) {
        if (player.isShiftKeyDown()) {
            BrewingList brewingList = maid.getBrain().getMemory(MaidTavernEntities.BREWING_LIST.get()).orElse(new BrewingList());
            stack.set(MaidTavernItems.BREWING_LIST_DATA, brewingList);
            player.displayClientMessage(Component.translatable("item.maidtavern.brewing_list.load"), true);
            return true;
        } else {
            if (stack.has(MaidTavernItems.BREWING_LIST_DATA)) {
                BrewingList brewingList = stack.get(MaidTavernItems.BREWING_LIST_DATA);
                maid.getBrain().setMemory(MaidTavernEntities.BREWING_LIST.get(), brewingList);
                player.displayClientMessage(Component.translatable("item.maidtavern.brewing_list.save"), true);
                return true;
            }
        }
        return false;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.maidtavern.brewing_list");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        InteractionHand hand = player.getUsedItemHand();
        ItemStack stack = player.getItemInHand(hand);
        BrewingList brewingList = stack.getOrDefault(MaidTavernItems.BREWING_LIST_DATA.get(), new BrewingList());
        brewingList = new BrewingList(brewingList);
        return new BrewingListMenu(containerId, inventory, hand, brewingList);
    }
}
