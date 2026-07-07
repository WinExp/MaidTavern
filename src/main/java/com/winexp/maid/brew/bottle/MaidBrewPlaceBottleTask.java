package com.winexp.maid.brew.bottle;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.TapBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import com.google.common.collect.ImmutableMap;
import com.mojang.authlib.GameProfile;
import com.winexp.entity.MaidTavernEntities;
import com.winexp.maid.brew.IBrewTask;
import com.winexp.util.ItemHandlerUtil;
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
import net.neoforged.neoforge.common.util.FakePlayer;

import java.util.Optional;
import java.util.UUID;

public class MaidBrewPlaceBottleTask extends Behavior<EntityMaid> {
    private static final UUID FAKE_PLAYER_UUID = UUID.randomUUID();
    private final IBrewTask task;

    public MaidBrewPlaceBottleTask(IBrewTask task) {
        super(ImmutableMap.of(
                InitEntities.TARGET_POS.get(), MemoryStatus.VALUE_PRESENT,
                MaidTavernEntities.BREWING_LIST.get(), MemoryStatus.VALUE_PRESENT
        ));
        this.task = task;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        Brain<EntityMaid> brain = maid.getBrain();
        if (brain.hasMemoryValue(MaidTavernEntities.BREWING_SESSION.get())) return false;
        PositionTracker targetPos = brain.getMemory(InitEntities.TARGET_POS.get()).get();
        BlockPos pos = targetPos.currentBlockPosition();
        if (!task.shouldPlaceBottle(maid, pos)) return false;

        Vec3 targetV3d = targetPos.currentPosition();
        if (maid.distanceToSqr(targetV3d) > Math.pow(task.getCloseEnoughDist(), 2)) {
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
                stack.is(ModItems.EMPTY_BOTTLE));
        level.setBlockAndUpdate(pos, ModBlocks.EMPTY_BOTTLE.get().defaultBlockState());
        level.playSound(null, pos, SoundType.STONE.getPlaceSound(), maid.getSoundSource(), 1.0f, 1.0f);
        bottleStack.shrink(1);
        BlockState tapState = level.getBlockState(pos.above());
        FakePlayer fakePlayer = new FakePlayer(level, new GameProfile(FAKE_PLAYER_UUID, "Arm"));
        ((TapBlock) ModBlocks.TAP.get()).useItemOn(ItemStack.EMPTY, tapState, level, pos.above(), fakePlayer, InteractionHand.MAIN_HAND, null);
        brain.eraseMemory(InitEntities.TARGET_POS.get());
    }
}
