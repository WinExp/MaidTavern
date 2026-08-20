package com.winexp.maidtavern.maid.brewing.bottle;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.TapBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import com.google.common.collect.ImmutableMap;
import com.mojang.authlib.GameProfile;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import com.winexp.maidtavern.maid.brewing.BrewingWork;
import com.winexp.maidtavern.maid.brewing.BrewingWorkTypes;
import com.winexp.maidtavern.maid.brewing.IBrewingTask;
import com.winexp.maidtavern.maid.brewing.MaidBrewingStateManager;
import com.winexp.maidtavern.util.ItemHandlerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayer;

import java.util.UUID;

public class MaidBrewingPlaceBottleTask extends Behavior<EntityMaid> {
    private static final UUID FAKE_PLAYER_UUID = UUID.randomUUID();
    private final IBrewingTask task;

    public MaidBrewingPlaceBottleTask(IBrewingTask task) {
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
        if (!task.shouldPlaceBottle(maid, pos)) {
            return false;
        }

        return work.isCloseEnough(maid);
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        BrewingWork work = MaidBrewingStateManager.getWork(maid);
        BlockPos pos = work.pos();
        ItemStack bottleStack = ItemHandlerUtil.findStack(maid.getAvailableInv(true), stack ->
                stack.is(ModItems.EMPTY_BOTTLE.get()));
        level.setBlockAndUpdate(pos, ModBlocks.EMPTY_BOTTLE.get().defaultBlockState());
        level.playSound(null, pos, SoundType.STONE.getPlaceSound(), maid.getSoundSource(), 1.0f, 1.0f);
        bottleStack.shrink(1);
        BlockState tapState = level.getBlockState(pos.above());
        FakePlayer fakePlayer = new FakePlayer(level, new GameProfile(FAKE_PLAYER_UUID, "Arm"));
        ((TapBlock) ModBlocks.TAP.get()).use(tapState, level, pos.above(), fakePlayer, InteractionHand.MAIN_HAND, null);
        maid.swing(InteractionHand.MAIN_HAND);
    }
}
