package com.winexp.maid;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.ysbbbbbb.kaleidoscopetavern.api.blockentity.IBarrel;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarrelBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.BarrelRecipe;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.winexp.MaidTavernMod;
import com.winexp.entity.MaidTavernEntities;
import com.winexp.maid.brew.BrewingList;
import com.winexp.maid.brew.barrel.MaidBrewAddIngredientTask;
import com.winexp.maid.brew.barrel.MaidBrewMoveToBarrelTask;
import com.winexp.maid.brew.storage.MaidBrewMoveToStorageTask;
import com.winexp.maid.brew.storage.MaidBrewTakeAndStoreTask;
import com.winexp.mixin.BarrelBlockEntityAccessor;
import com.winexp.util.ItemHandlerUtil;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TaskBrew implements IBrewTask {
    private static final ResourceLocation UID = MaidTavernMod.asResource("brewing");
    private static final ItemStack ICON = ModItems.BARREL.toStack();

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public ItemStack getIcon() {
        return ICON;
    }

    @Override
    public @Nullable SoundEvent getAmbientSound(EntityMaid maid) {
        return null;
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid maid) {
        return Lists.newArrayList(
                Pair.of(11, new MaidBrewMoveToStorageTask(this, 0.45f)),
                Pair.of(10, new MaidBrewTakeAndStoreTask(this)),
                Pair.of(5, new MaidBrewMoveToBarrelTask(this, 0.45f)),
                Pair.of(5, new MaidBrewAddIngredientTask(this, 20))
        );
    }

    @Override
    public boolean enableLookAndRandomWalk(EntityMaid maid) {
        return true;
    }

    @Override
    public boolean enableEating(EntityMaid maid) {
        return !maid.getBrain().hasMemoryValue(MaidTavernEntities.BREWING_SESSION.get());
    }

    @Override
    public double getCloseEnoughDist() {
        return 1;
    }

    @Override
    public @Nullable IBarrel getBarrel(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(ModBlocks.BARREL) || state.getValue(BarrelBlock.LAYER) != AttachFace.CEILING || state.getValue(BarrelBlock.INDEX) != 4) return null;
        return (IBarrel) level.getBlockEntity(pos.below(2));
    }

    @Override
    public boolean isBarrelAvailable(@Nullable IBarrel barrel) {
        return barrel != null && !barrel.isBrewing();
    }

    @Override
    public boolean isBarrelAvailable(EntityMaid maid, @Nullable IBarrel barrel) {
        Brain<EntityMaid> brain = maid.getBrain();
        if (!isBarrelAvailable(barrel)) return false;
        var brewingSession = brain.getMemory(MaidTavernEntities.BREWING_SESSION.get());
        BarrelBlockEntityAccessor accessor = (BarrelBlockEntityAccessor) barrel;
        if (brewingSession.isPresent()) {
            if (brewingSession.get().fluidPlaced().isFalse()) {
                return accessor.getFluidTank().isEmpty();
            } else if (brewingSession.get().ingredientsPlaced().isFalse()) {
                return ItemHandlerUtil.isEmpty(accessor.getIngredients());
            } else return true;
        }
        return accessor.getFluidTank().isEmpty() && ItemHandlerUtil.isEmpty(accessor.getIngredients());
    }

    @Override
    public boolean hasRequiredMaterials(EntityMaid maid, ResourceLocation recipeId) {
        BarrelRecipe recipe = (BarrelRecipe) maid.level().getRecipeManager().byKey(recipeId).map(RecipeHolder::value).orElse(null);
        if (recipe == null) return false;
        if (!ItemHandlerUtil.matchesCount(maid.getAvailableInv(true), stack ->
                recipe.fluid().getBucket() == stack.getItem(), MinMaxBounds.Ints.atLeast(4))) return false;
        for (Ingredient ingredient : recipe.getIngredients()) {
            if (ingredient.isEmpty()) continue;
            if (!ItemHandlerUtil.matchesCount(maid.getAvailableInv(true), ingredient, MinMaxBounds.Ints.atLeast(16))) return false;
        }
        return true;
    }

    @Override
    public boolean hasRequiredMaterialsInStorage(EntityMaid maid, ResourceLocation recipeId, IItemHandler storage) {
        BarrelRecipe recipe = (BarrelRecipe) maid.level().getRecipeManager().byKey(recipeId).map(RecipeHolder::value).orElse(null);
        if (recipe == null) return false;
        IItemHandler maidInv = maid.getAvailableInv(true);
        int fluidRequired = 4 - ItemHandlerUtil.countItems(maidInv, stack -> recipe.fluid().getBucket() == stack.getItem());
        if (fluidRequired > 0) {
            if (!ItemHandlerUtil.matchesCount(storage, stack ->
                    recipe.fluid().getBucket() == stack.getItem(), MinMaxBounds.Ints.atLeast(fluidRequired))) return false;
        }
        for (Ingredient ingredient : recipe.getIngredients()) {
            if (ingredient.isEmpty()) continue;
            int ingredientRequired = 16 - ItemHandlerUtil.countItems(maidInv, ingredient);
            if (ingredientRequired > 0) {
                if (!ItemHandlerUtil.matchesCount(storage, ingredient, MinMaxBounds.Ints.atLeast(ingredientRequired))) return false;
            }
        }
        return true;
    }

    @Override
    public @Nullable ItemStack getToStoreStack(EntityMaid maid) {
        Brain<EntityMaid> brain = maid.getBrain();
        if (!brain.hasMemoryValue(MaidTavernEntities.BREWING_LIST.get())) return null;
        BrewingList brewingList = brain.getMemory(MaidTavernEntities.BREWING_LIST.get()).get();
        for (ResourceLocation recipeId : brewingList.getRecipes()) {
            BarrelRecipe recipe = (BarrelRecipe) maid.level().getRecipeManager().byKey(recipeId).map(RecipeHolder::value).orElse(null);
            if (recipe == null) continue;
            Item resultItem = recipe.getResultItem(maid.level().registryAccess()).getItem();
            ItemStack foundStack = ItemHandlerUtil.findStack(maid.getAvailableInv(true), stack -> resultItem == stack.getItem());
            ItemStack bucketStack = ItemHandlerUtil.findStack(maid.getAvailableInv(true), stack -> stack.is(Items.BUCKET));
            if (foundStack != null) {
                return foundStack;
            } else if (bucketStack != null) {
                return bucketStack;
            }
        }
        return null;
    }

    @Override
    public boolean isStorageValid(EntityMaid maid, BlockPos pos) {
        Brain<EntityMaid> brain = maid.getBrain();
        BlockState state = maid.level().getBlockState(pos);
        if (maid.level().getBlockEntity(pos) instanceof BaseContainerBlockEntity container) {
            if (!state.is(Blocks.BARREL)
            && !state.is(Blocks.CHEST)) return false;
            if (state.is(Blocks.CHEST) && ChestBlock.isChestBlockedAt(maid.level(), pos)) {
                return false;
            }
            IItemHandler storage = new InvWrapper(container);
            ItemStack toStoreStack = getToStoreStack(maid);
            BrewingList brewingList = brain.getMemory(MaidTavernEntities.BREWING_LIST.get()).get();
            boolean takeFlag = hasRequiredMaterialsInStorage(maid, brewingList.get(), storage);
            boolean storeFlag = toStoreStack != null && !ItemHandlerUtil.canInsert(storage, toStoreStack);
            return takeFlag || storeFlag;
        }
        return false;
    }
}
