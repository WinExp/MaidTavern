package com.winexp.maidtavern.maid.brew.bottle;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.TapBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import com.google.common.collect.ImmutableMap;
import com.mojang.authlib.GameProfile;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import com.winexp.maidtavern.maid.brew.IBrewTask;
import com.winexp.maidtavern.util.ItemHandlerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;

import java.util.Optional;
import java.util.UUID;

public class MaidBrewPlaceBottleTask extends Behavior<EntityMaid> {
    private static final UUID FAKE_PLAYER_UUID = UUID.randomUUID();
    private final IBrewTask task;
    private final double closeEnoughDist;

    public MaidBrewPlaceBottleTask(IBrewTask task, double closeEnoughDist) {
        super(ImmutableMap.of(
                InitEntities.TARGET_POS.get(), MemoryStatus.VALUE_PRESENT,
                MaidTavernEntities.BREWING_LIST.get(), MemoryStatus.VALUE_PRESENT,
                MaidTavernEntities.BREWING_SESSION.get(), MemoryStatus.VALUE_ABSENT
        ));
        this.task = task;
        this.closeEnoughDist = closeEnoughDist;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        Brain<EntityMaid> brain = maid.getBrain();
        PositionTracker targetPos = brain.getMemory(InitEntities.TARGET_POS.get()).get();

        BlockPos pos = targetPos.currentBlockPosition();
        if (!task.shouldPlaceBottle(maid, pos)) {
            BlockState tapState = level.getBlockState(pos.above());
            if (!tapState.is(ModBlocks.TAP.get()) || !tapState.getValue(TapBlock.OPEN)) {
                brain.eraseMemory(InitEntities.TARGET_POS.get());
            }
            return false;
        }

        Vec3 targetV3d = targetPos.currentPosition();
        if (maid.distanceToSqr(targetV3d) > Math.pow(closeEnoughDist, 2)) {
            Optional<WalkTarget> walkTarget = brain.getMemory(MemoryModuleType.WALK_TARGET);
            if (walkTarget.isEmpty()) {
                brain.eraseMemory(InitEntities.TARGET_POS.get());
            }
            return false;
        }
        return true;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        Brain<EntityMaid> brain = maid.getBrain();
        BlockPos pos = brain.getMemory(InitEntities.TARGET_POS.get()).get().currentBlockPosition();
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
