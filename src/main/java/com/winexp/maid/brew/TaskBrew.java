package com.winexp.maid.brew;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.github.ysbbbbbb.kaleidoscopetavern.api.blockentity.IBarrel;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarrelBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.DrinkBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.TapBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.DrinkBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.BarrelRecipe;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModDataComponents;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.winexp.MaidTavernMod;
import com.winexp.entity.MaidTavernEntities;
import com.winexp.maid.brew.barrel.MaidBrewAddIngredientTask;
import com.winexp.maid.brew.barrel.MaidBrewMoveToBarrelTask;
import com.winexp.maid.brew.bottle.MaidBrewMoveToBottleTask;
import com.winexp.maid.brew.bottle.MaidBrewPlaceBottleTask;
import com.winexp.maid.brew.bottle.MaidBrewTakeBottleTask;
import com.winexp.maid.brew.common.MaidBrewPreCheckTask;
import com.winexp.maid.brew.storage.MaidBrewMoveToStorageTask;
import com.winexp.maid.brew.storage.MaidBrewTakeAndStoreTask;
import com.winexp.mixin.BarrelBlockEntityAccessor;
import com.winexp.util.ItemHandlerUtil;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

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
                Pair.of(Integer.MAX_VALUE, new MaidBrewPreCheckTask()),
                Pair.of(5, new MaidBrewMoveToStorageTask(this, 0.45f, 3)),
                Pair.of(5, new MaidBrewTakeAndStoreTask(this)),
                Pair.of(5, new MaidBrewMoveToBarrelTask(this, 0.45f, 3)),
                Pair.of(5, new MaidBrewAddIngredientTask(this, 20)),
                Pair.of(5, new MaidBrewMoveToBottleTask(this, 0.45f, 3)),
                Pair.of(5, new MaidBrewTakeBottleTask(this)),
                Pair.of(5, new MaidBrewPlaceBottleTask(this))
        );
    }

    @Override
    public boolean enableLookAndRandomWalk(EntityMaid maid) {
        return !maid.getBrain().hasMemoryValue(InitEntities.TARGET_POS.get());
    }

    @Override
    public boolean enableEating(EntityMaid maid) {
        return !maid.getBrain().hasMemoryValue(MaidTavernEntities.BREWING_SESSION.get());
    }

    @Override
    public double getCloseEnoughDist() {
        return 2;
    }

    @Override
    public @Nullable IBarrel getBarrel(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return BarrelBlock.getBarrelEntity(level, pos, state);
    }

    @Override
    public boolean isBarrelAvailable(EntityMaid maid, @Nullable IBarrel barrel) {
        Brain<EntityMaid> brain = maid.getBrain();
        if (barrel == null || barrel.isBrewing()) return false;
        var brewingSession = brain.getMemory(MaidTavernEntities.BREWING_SESSION.get());
        BarrelBlockEntityAccessor accessor = (BarrelBlockEntityAccessor) barrel;
        if (brewingSession.isEmpty() || brewingSession.get().fluidPlaced().isFalse()) {
            if (!accessor.getFluidTank().isEmpty()) return false;
        }
        if (brewingSession.isEmpty() || brewingSession.get().ingredientsPlaced().isFalse()) {
            if (!ItemHandlerUtil.isEmpty(accessor.getIngredients())) return false;
        }
        return true;
    }

    @Override
    public boolean hasRequiredMaterials(EntityMaid maid, ResourceLocation recipeId, @Nullable BrewingSession session) {
        BarrelRecipe recipe = (BarrelRecipe) maid.level().getRecipeManager().byKey(recipeId).map(RecipeHolder::value).orElse(null);
        if (recipe == null) return false;
        if (!ItemHandlerUtil.matchesCount(maid.getAvailableInv(true), stack ->
                stack.is(ModItems.EMPTY_BOTTLE), MinMaxBounds.Ints.atLeast(1))) return false;
        if (session == null || session.fluidPlaced().isFalse()) {
            if (!ItemHandlerUtil.matchesCount(maid.getAvailableInv(true), stack ->
                    recipe.fluid().getBucket() == stack.getItem(), MinMaxBounds.Ints.atLeast(4))) return false;
        }
        if (session == null || session.ingredientsPlaced().isFalse()) {
            for (Ingredient ingredient : recipe.getIngredients()) {
                if (ingredient.isEmpty()) continue;
                if (!ItemHandlerUtil.matchesCount(maid.getAvailableInv(true), ingredient, MinMaxBounds.Ints.atLeast(16))) return false;
            }
        }
        return true;
    }

    @Override
    public boolean hasRequiredMaterialsInStorage(EntityMaid maid, ResourceLocation recipeId, IItemHandler storage) {
        BarrelRecipe recipe = (BarrelRecipe) maid.level().getRecipeManager().byKey(recipeId).map(RecipeHolder::value).orElse(null);
        if (recipe == null) return false;
        IItemHandler maidInv = maid.getAvailableInv(true);
        Predicate<ItemStack> bottlePredicate = stack -> stack.is(ModItems.EMPTY_BOTTLE);
        int bottleRequired = 1 - ItemHandlerUtil.countItems(maidInv, bottlePredicate);
        if (bottleRequired > 0) {
            if (!ItemHandlerUtil.matchesCount(storage, bottlePredicate, MinMaxBounds.Ints.atLeast(bottleRequired))) return false;
        }
        Predicate<ItemStack> fluidPredicate = stack -> recipe.fluid().getBucket() == stack.getItem();
        int fluidRequired = 4 - ItemHandlerUtil.countItems(maidInv, fluidPredicate);
        if (fluidRequired > 0) {
            if (!ItemHandlerUtil.matchesCount(storage, fluidPredicate, MinMaxBounds.Ints.atLeast(fluidRequired))) return false;
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
            boolean takeFlag = false;
            for (ResourceLocation recipeId : brewingList.getRecipes()) {
                if (hasRequiredMaterialsInStorage(maid, recipeId, storage)) {
                    takeFlag = true;
                    break;
                }
            }
            boolean storeFlag = toStoreStack != null && ItemHandlerUtil.canInsert(storage, toStoreStack);
            return takeFlag || storeFlag;
        }
        return false;
    }

    @Override
    public boolean isBottleValid(EntityMaid maid, BlockPos pos) {
        if (pos == null) return false;
        BlockState state = maid.level().getBlockState(pos);
        BrewingList brewingList = maid.getBrain().getMemory(MaidTavernEntities.BREWING_LIST.get()).get();
        if (!state.is(ModBlocks.MOLOTOV)
                && !(state.getBlock() instanceof DrinkBlock)) return false;
        if (!maid.level().getBlockState(pos.above()).is(ModBlocks.TAP)) return false;
        if (state.is(ModBlocks.MOLOTOV)) {
            for (ResourceLocation recipeId : brewingList.getRecipes()) {
                BarrelRecipe recipe = (BarrelRecipe) maid.level().getRecipeManager().byKey(recipeId).map(RecipeHolder::value).get();
                ItemStack result = recipe.getResultItem(maid.level().registryAccess());
                if (result.is(ModItems.MOLOTOV)) return true;
            }
        } else if (state.getBlock() instanceof DrinkBlock drinkBlock
                && state.getValue(drinkBlock.getCountProperty()) == 1) {
            DrinkBlockEntity drink = (DrinkBlockEntity) maid.level().getBlockEntity(pos);
            if (drink.getItems().isEmpty()) return false;
            ItemStack stack = drink.getItems().getFirst();
            for (ResourceLocation recipeId : brewingList.getRecipes()) {
                BarrelRecipe recipe = (BarrelRecipe) maid.level().getRecipeManager().byKey(recipeId).map(RecipeHolder::value).get();
                ItemStack result = recipe.getResultItem(maid.level().registryAccess());
                if (ItemStack.isSameItem(stack, result)
                        && Objects.equals(stack.get(ModDataComponents.BREW_LEVEL), IBarrel.BREWING_FINISHED)) return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldPlaceBottle(EntityMaid maid, BlockPos pos) {
        BrewingList brewingList = maid.getBrain().getMemory(MaidTavernEntities.BREWING_LIST.get()).get();
        BlockState state = maid.level().getBlockState(pos);
        if (!ItemHandlerUtil.contains(maid.getAvailableInv(true), stack ->
                stack.is(ModItems.EMPTY_BOTTLE)) || !state.isAir()) return false;
        BlockState tapState = maid.level().getBlockState(pos.above());
        if (!tapState.is(ModBlocks.TAP)) return false;
        Direction tapFacing = tapState.getValue(TapBlock.FACING);
        IBarrel barrel = getBarrel((ServerLevel) maid.level(), pos.relative(tapFacing.getOpposite()));
        if (barrel == null || barrel.getRecipeId() == null
                || barrel.getBrewLevel() != IBarrel.BREWING_FINISHED) return false;
        for (ResourceLocation recipeId : brewingList.getRecipes()) {
            if (Objects.equals(barrel.getRecipeId(), recipeId)) return true;
        }
        return false;
    }
}
