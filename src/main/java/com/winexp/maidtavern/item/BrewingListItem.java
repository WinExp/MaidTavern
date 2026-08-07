package com.winexp.maidtavern.item;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.winexp.maidtavern.client.gui.brewing_list.BrewingListScreen;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import com.winexp.maidtavern.maid.brew.BrewingList;
import com.winexp.maidtavern.maid.brew.IBrewTask;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BrewingListItem extends Item implements MaidInteractionItem {
    public BrewingListItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (player.isShiftKeyDown()) return InteractionResultHolder.pass(stack);
        if (level.isClientSide) {
            BrewingList brewingList = stack.getOrDefault(MaidTavernItems.BREWING_LIST_DATA, new BrewingList());
            Minecraft.getInstance().setScreen(new BrewingListScreen((LocalPlayer) player, usedHand, brewingList));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public boolean useOnMaid(Level level, Player player, EntityMaid maid, ItemStack stack) {
        if (!(maid.getTask() instanceof IBrewTask)) return false;
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                BrewingList brewingList = maid.getBrain().getMemory(MaidTavernEntities.BREWING_LIST.get()).orElse(new BrewingList());
                stack.set(MaidTavernItems.BREWING_LIST_DATA, brewingList);
                player.displayClientMessage(Component.translatable("item.maidtavern.brewing_list.load"), true);
            }
            return true;
        } else {
            if (stack.has(MaidTavernItems.BREWING_LIST_DATA)) {
                if (!level.isClientSide) {
                    BrewingList brewingList = stack.get(MaidTavernItems.BREWING_LIST_DATA);
                    maid.getBrain().setMemory(MaidTavernEntities.BREWING_LIST.get(), brewingList);
                    player.displayClientMessage(Component.translatable("item.maidtavern.brewing_list.save"), true);
                }
                return true;
            }
        }
        return false;
    }
}
