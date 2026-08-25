package com.winexp.maidtavern.maid.brewing.bottle;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.TapBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopetavern.util.ItemUtils;
import com.google.common.collect.ImmutableMap;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import com.winexp.maidtavern.maid.brewing.BrewingWork;
import com.winexp.maidtavern.maid.brewing.BrewingWorkTypes;
import com.winexp.maidtavern.maid.brewing.IBrewingTask;
import com.winexp.maidtavern.maid.brewing.MaidBrewingStateManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public class MaidBrewingTakeBottleTask extends Behavior<EntityMaid> {
    private final IBrewingTask task;

    public MaidBrewingTakeBottleTask(IBrewingTask task) {
        super(ImmutableMap.of(
                MaidTavernEntities.BREWING_WORK.get(), MemoryStatus.VALUE_PRESENT,
                MaidTavernEntities.BREWING_LIST.get(), MemoryStatus.VALUE_PRESENT,
                MaidTavernEntities.BREWING_SESSION.get(), MemoryStatus.VALUE_ABSENT
        ));
        this.task = task;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        if (!MaidBrewingStateManager.isSameWorkType(maid, BrewingWorkTypes.BOTTLE)) return false;
        BrewingWork work = MaidBrewingStateManager.getWork(maid);
        BlockPos pos = work.pos();
        if (!task.isBottleValid(maid, pos)) {
            BlockState tapState = level.getBlockState(pos.above());
            if (!tapState.is(ModBlocks.TAP) || !tapState.getValue(TapBlock.OPEN)) {
                MaidBrewingStateManager.stopWork(maid);
            }
            return false;
        }

        return work.isCloseEnough(maid);
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        BrewingWork work = MaidBrewingStateManager.getWork(maid);
        BlockPos pos = work.pos();
        BlockState state = level.getBlockState(pos);
        Block.getDrops(state, level, pos, level.getBlockEntity(pos))
                .forEach(stack -> ItemUtils.getItemToLivingEntity(maid, stack));
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_SUPPRESS_DROPS | Block.UPDATE_ALL);
        maid.swing(InteractionHand.MAIN_HAND);
        level.playSound(null, pos, SoundType.STONE.getPlaceSound(), maid.getSoundSource(), 1.0f, 1.0f);
        MaidBrewingStateManager.resetWorkExpiration(maid);
    }
}
