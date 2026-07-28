package com.winexp.maidtavern.item;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import com.winexp.maidtavern.maid.brew.BrewingList;
import com.winexp.maidtavern.maid.brew.IBrewTask;
import com.winexp.maidtavern.menu.BrewingListMenu;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class BrewingListItem extends Item implements MenuProvider, MaidInteractionItem {
    public BrewingListItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (player.isShiftKeyDown()) return InteractionResultHolder.pass(stack);
        if (!level.isClientSide) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            NetworkHooks.openScreen(serverPlayer, this, buf -> buf.writeEnum(usedHand));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public boolean useOnMaid(Level level, Player player, EntityMaid maid, ItemStack stack) {
        if (!(maid.getTask() instanceof IBrewTask)) return false;
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                BrewingList brewingList = maid.getBrain().getMemory(MaidTavernEntities.BREWING_LIST.get()).orElse(new BrewingList());
                stack.getOrCreateTag().put("BrewingList", BrewingList.CODEC.encodeStart(NbtOps.INSTANCE, brewingList).getOrThrow(false, message -> {}));
                player.displayClientMessage(Component.translatable("item.maidtavern.brewing_list.load"), true);
            }
            return true;
        } else {
            if (stack.getTagElement("BrewingList") != null) {
                if (!level.isClientSide) {
                    BrewingList brewingList = BrewingList.CODEC.parse(NbtOps.INSTANCE, stack.getTagElement("BrewingList")).getOrThrow(false, message -> {});
                    maid.getBrain().setMemory(MaidTavernEntities.BREWING_LIST.get(), brewingList);
                    player.displayClientMessage(Component.translatable("item.maidtavern.brewing_list.save"), true);
                }
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
        BrewingList brewingList = BrewingList.CODEC.parse(NbtOps.INSTANCE, stack.getTagElement("BrewingList")).result().orElse(new BrewingList());
        brewingList = new BrewingList(brewingList);
        return new BrewingListMenu(containerId, inventory, hand, brewingList);
    }
}
