package com.winexp.maidtavern.item;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import com.winexp.maidtavern.maid.brew.StorageBinding;
import com.winexp.maidtavern.maid.brew.TaskBrew;
import com.winexp.maidtavern.network.serverbound.ServerboundSetStorageBindingTypePayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

public class StorageBindingToolItem extends Item implements MaidInteractionItem, MouseScrollingItem {
    public StorageBindingToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        StorageBinding binding = stack.getOrDefault(MaidTavernItems.STORAGE_BINDING_DATA, StorageBinding.EMPTY);
        StorageBinding.Type selectedType = stack.getOrDefault(MaidTavernItems.STORAGE_BINDING_TYPE_DATA, StorageBinding.Type.INGREDIENTS);
        Component selectedTypeName = Component.translatable("item.maidtavern.storage_binding_tool.type." + selectedType.getSerializedName()).withStyle(ChatFormatting.WHITE);
        tooltipComponents.add(Component.translatable("item.maidtavern.storage_binding_tool.tip.binding_mode", selectedTypeName).withStyle(ChatFormatting.GRAY));
        boolean nextLine = false;
        for (StorageBinding.Type type : StorageBinding.Type.values()) {
            List<BlockPos> posList = binding.get(type);
            if (posList.isEmpty()) continue;
            if (!nextLine) {
                tooltipComponents.add(Component.empty());
                nextLine = true;
            }
            Component typeName = Component.translatable("item.maidtavern.storage_binding_tool.type." + type.getSerializedName()).withStyle(ChatFormatting.GRAY);
            tooltipComponents.add(typeName);
            for (int i = 0; i < posList.size(); i++) {
                if (!tooltipFlag.hasShiftDown() && i >= 4) {
                    tooltipComponents.add(Component.literal("  ")
                            .append(Component.translatable("item.maidtavern.storage_binding_tool.tooltip.remaining", posList.size() - i)));
                    break;
                }
                BlockPos pos = posList.get(i);
                Component posComponent = Component.literal("  [").append(Component.literal(pos.toShortString())).append("]");
                tooltipComponents.add(posComponent);
            }
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        if (!player.isShiftKeyDown()) return InteractionResult.PASS;
        ItemStack stack = context.getItemInHand();
        StorageBinding binding = stack.getOrDefault(MaidTavernItems.STORAGE_BINDING_DATA, StorageBinding.EMPTY);
        StorageBinding.Type type = stack.getOrDefault(MaidTavernItems.STORAGE_BINDING_TYPE_DATA, StorageBinding.Type.INGREDIENTS);
        if (!level.isClientSide) {
            if (binding.get(type).contains(pos)) binding = binding.remove(type, pos);
            else binding = binding.add(type, pos);
            stack.set(MaidTavernItems.STORAGE_BINDING_DATA, binding);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public boolean useOnMaid(Level level, Player player, EntityMaid maid, ItemStack stack) {
        if (!(maid.getTask() instanceof TaskBrew)) return false;
        if (!level.isClientSide) {
            if (player.isShiftKeyDown()) {
                StorageBinding binding = maid.getBrain().getMemory(MaidTavernEntities.STORAGE_BINDING.get()).orElse(StorageBinding.EMPTY);
                stack.set(MaidTavernItems.STORAGE_BINDING_DATA, binding);
                player.displayClientMessage(Component.translatable("item.maidtavern.storage_binding_tool.tip.load"), true);
            } else {
                StorageBinding binding = stack.getOrDefault(MaidTavernItems.STORAGE_BINDING_DATA, StorageBinding.EMPTY);
                maid.getBrain().setMemory(MaidTavernEntities.STORAGE_BINDING.get(), binding);
                player.displayClientMessage(Component.translatable("item.maidtavern.storage_binding_tool.tip.save"), true);
            }
        }
        return true;
    }

    @Override
    public boolean onMouseScroll(LocalPlayer player, ItemStack stack, double scrollX, double scrollY) {
        if (!player.isShiftKeyDown()) return false;
        StorageBinding.Type type = stack.getOrDefault(MaidTavernItems.STORAGE_BINDING_TYPE_DATA, StorageBinding.Type.INGREDIENTS);
        int direction = Mth.sign(scrollY);
        int types = StorageBinding.Type.values().length;
        if (direction != 0) {
            int idx = type.ordinal();
            idx -= direction;
            if (idx < 0) idx += types;
            else if (idx >= types) idx -= types;
            StorageBinding.Type newType = StorageBinding.Type.values()[idx];
            ServerboundSetStorageBindingTypePayload payload = new ServerboundSetStorageBindingTypePayload(newType);
            player.connection.send(payload);
            Component typeName = Component.translatable("item.maidtavern.storage_binding_tool.type." + newType.getSerializedName());
            player.displayClientMessage(Component.translatable("item.maidtavern.storage_binding_tool.tip.binding_mode", typeName), true);
            return true;
        }
        return false;
    }
}
