package com.winexp.maid.grape;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidFarmPlantTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.ysbbbbbb.kaleidoscopetavern.block.plant.GrapeCropBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import com.winexp.MaidTavernMod;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class TaskGrape implements IGrapeTask {
    public static final int MAX_GRAPE_HEIGHT = 3;
    private static final UUID FAKE_PLAYER_UUID = UUID.randomUUID();
    private static final ResourceLocation UID = MaidTavernMod.asResource("grape");
    private static final ItemStack ICON = ModItems.GRAPE.toStack();

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public ItemStack getIcon() {
        return ICON;
    }

    @Override
    public @Nullable BlockPos getGrapePos(Level level, BlockPos pos) {
        for (int i = 0; i < MAX_GRAPE_HEIGHT; i++) {
            BlockPos grapePos = pos.above(i);
            if (level.getBlockState(grapePos).getBlock() instanceof GrapeCropBlock) {
                return grapePos;
            }
        }
        return null;
    }

    @Override
    public boolean canHarvest(EntityMaid maid, BlockPos cropPos, BlockState cropState) {
        cropPos = getGrapePos(maid.level(), cropPos);
        if (cropPos == null) return false;
        cropState = maid.level().getBlockState(cropPos);
        boolean result = cropState.getBlock() instanceof GrapeCropBlock && cropState.getValue(GrapeCropBlock.AGE) == GrapeCropBlock.MAX_AGE;
        if (result) {
            maid.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(cropPos));
        }
        return result;
    }

    @Override
    public void harvest(EntityMaid maid, BlockPos cropPos, BlockState cropState) {
        cropPos = getGrapePos(maid.level(), cropPos);
        cropState = maid.level().getBlockState(cropPos);
        ItemStack shears = maid.getMainHandItem();
        if (shears.is(Tags.Items.TOOLS_SHEAR)) {
            ServerLevel level = (ServerLevel) maid.level();
            FakePlayer fakePlayer = new FakePlayer(level, new GameProfile(FAKE_PLAYER_UUID, "Arm"));
            fakePlayer.getInventory().setItem(0, shears);
            ((GrapeCropBlock) cropState.getBlock()).useItemOn(shears, cropState, level, cropPos, fakePlayer, InteractionHand.MAIN_HAND, null);
            level.playSound(null, cropPos, SoundEvents.BEEHIVE_SHEAR, SoundSource.BLOCKS, 1, 1);
        } else maid.destroyBlock(cropPos);
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid maid) {
        MaidGrapeMoveTask maidGrapeMoveTask = new MaidGrapeMoveTask(this, 0.6f);
        MaidFarmPlantTask maidFarmPlantTask = new MaidFarmPlantTask(this);
        return Lists.newArrayList(Pair.of(5, maidGrapeMoveTask), Pair.of(6, maidFarmPlantTask));
    }

    @Override
    public List<Pair<String, Predicate<EntityMaid>>> getConditionDescription(EntityMaid maid) {
        return Lists.newArrayList(
                Pair.of("has_shears", maid1 -> maid1.getMainHandItem().is(Tags.Items.TOOLS_SHEAR))
        );
    }
}
