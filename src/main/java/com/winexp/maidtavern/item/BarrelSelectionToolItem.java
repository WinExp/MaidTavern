package com.winexp.maidtavern.item;

import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarrelBlock;
import com.google.common.collect.ImmutableSet;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedList;
import java.util.List;

public class BarrelSelectionToolItem extends Item {
    public BarrelSelectionToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (!player.isShiftKeyDown()) return InteractionResult.PASS;
        ItemStack stack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        List<BlockPos> positions = new LinkedList<>(stack.get(MaidTavernItems.BARREL_POSITIONS_DATA));
        boolean isBarrel = BarrelBlock.getBarrelEntity(level, pos, state) != null;
        float soundPitch;
        if (positions.contains(pos)) {
            positions.remove(pos);
            soundPitch = 0.8f;
        } else if (isBarrel) {
            pos = BarrelBlock.getOriginPos(pos, state);
            if (positions.contains(pos)) {
                positions.remove(pos);
                soundPitch = 0.8f;
            } else {
                positions.add(pos);
                soundPitch = 0.9f;
            }
        } else return InteractionResult.FAIL;
        stack.set(MaidTavernItems.BARREL_POSITIONS_DATA, ImmutableSet.copyOf(positions));
        if (positions.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_MODEL_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(100));
        }
        if (level.isClientSide) {
            player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.8f, soundPitch);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        List<BlockPos> positions = List.copyOf(stack.get(MaidTavernItems.BARREL_POSITIONS_DATA));
        if (positions.isEmpty()) return;
        tooltipComponents.add(Component.translatable("item.maidtavern.barrel_selection_tool.tooltip.title").withStyle(ChatFormatting.GRAY));
        for (int i = 0; i < positions.size(); i++) {
            if (!tooltipFlag.hasShiftDown() && i >= 4) {
                Component component = Component.literal("  ")
                        .append(Component.translatable("maidtavern.target_selection.tooltip.remaining", positions.size() - i))
                        .withStyle(ChatFormatting.GRAY);
                tooltipComponents.add(component);
                break;
            }
            BlockPos pos = positions.get(i);
            Component component = Component.literal("  [")
                    .append(Component.literal(pos.toShortString()))
                    .append("]")
                    .withStyle(ChatFormatting.WHITE);
            tooltipComponents.add(component);
        }
    }
}
