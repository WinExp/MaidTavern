package com.winexp.maidtavern.maid.brew;

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
import com.winexp.maidtavern.MaidTavern;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import com.winexp.maidtavern.maid.brew.barrel.MaidBrewAddIngredientTask;
import com.winexp.maidtavern.maid.brew.barrel.MaidBrewMoveToBarrelTask;
import com.winexp.maidtavern.maid.brew.bottle.MaidBrewMoveToBottleTask;
import com.winexp.maidtavern.maid.brew.bottle.MaidBrewPlaceBottleTask;
import com.winexp.maidtavern.maid.brew.bottle.MaidBrewTakeBottleTask;
import com.winexp.maidtavern.maid.brew.common.MaidBrewPreCheckTask;
import com.winexp.maidtavern.maid.brew.storage.MaidBrewMoveToStorageTask;
import com.winexp.maidtavern.maid.brew.storage.MaidBrewTakeAndStoreTask;
import com.winexp.maidtavern.maid.task.IMaidTaskExt;
import com.winexp.maidtavern.mixin.BarrelBlockEntityAccessor;
import com.winexp.maidtavern.util.ItemHandlerUtil;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class TaskBrew implements IBrewTask, IMaidTaskExt {
    private static final ResourceLocation UID = MaidTavern.asResource("brewing");
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
                Pair.of(5, new MaidBrewMoveToStorageTask(this, 0.45f, 4)),
                Pair.of(5, new MaidBrewTakeAndStoreTask(this, 3)),
                Pair.of(5, new MaidBrewMoveToBarrelTask(this, 0.45f, 4)),
                Pair.of(5, new MaidBrewAddIngredientTask(this, 2.5, 20)),
                Pair.of(5, new MaidBrewMoveToBottleTask(this, 0.45f, 4)),
                Pair.of(5, new MaidBrewTakeBottleTask(this, 2)),
                Pair.of(5, new MaidBrewPlaceBottleTask(this, 2))
        );
    }

    @Override
    public boolean enableLookAndRandomWalk(EntityMaid maid) {
        return !maid.getBrain().hasMemoryValue(InitEntities.TARGET_POS.get());
    }

    @Override
    public boolean enableStealEdible(EntityMaid maid) {
        return !maid.getBrain().hasMemoryValue(InitEntities.TARGET_POS.get());
    }

    @Override
    public boolean enableEating(EntityMaid maid) {
        return !maid.getBrain().hasMemoryValue(MaidTavernEntities.BREWING_SESSION.get());
    }

    @Override
    public @Nullable IBarrel getBarrel(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return BarrelBlock.getBarrelEntity(level, pos, state);
    }

    @Override
    public boolean isBarrelValid(EntityMaid maid, @Nullable IBarrel barrel) {
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
    public boolean hasIngredients(EntityMaid maid, ResourceLocation recipeId) {
        BrewingSession session = maid.getBrain().getMemory(MaidTavernEntities.BREWING_SESSION.get()).orElse(null);
        BarrelRecipe recipe = (BarrelRecipe) maid.level().getRecipeManager().byKey(recipeId).map(RecipeHolder::value).orElse(null);
        IItemHandler maidInv = maid.getAvailableInv(true);
        if (recipe == null) return false;
        if (session == null || session.fluidPlaced().isFalse()) {
            if (!ItemHandlerUtil.matchesCount(maidInv, stack ->
                    stack.is(recipe.fluid().getBucket()), MinMaxBounds.Ints.atLeast(4))) return false;
        }
        if (session == null || session.ingredientsPlaced().isFalse()) {
            ingredient:
            for (Ingredient ingredient : recipe.getIngredients()) {
                if (ingredient.isEmpty()) continue;
                for (ItemStack ingredientStack : ingredient.getItems()) {
                    if (ingredientStack.isEmpty()) continue;
                    if (ItemHandlerUtil.matchesCount(maidInv, stack ->
                            ItemStack.isSameItemSameComponents(stack, ingredientStack), MinMaxBounds.Ints.atLeast(16))) {
                        continue ingredient;
                    }
                }
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean shouldTake(EntityMaid maid) {
        BrewingList brewingList = maid.getBrain().getMemory(MaidTavernEntities.BREWING_LIST.get()).orElse(null);
        if (brewingList == null) return false;
        ResourceLocation recipeId = brewingList.get();
        return !ItemHandlerUtil.contains(maid.getAvailableInv(true), stack ->
                stack.is(ModItems.EMPTY_BOTTLE)) || !hasIngredients(maid, recipeId);
    }

    @Override
    public @Nullable List<Pair<ItemStack, Integer>> getNeedToTakeStacks(EntityMaid maid, IItemHandler storage) {
        IItemHandler maidInv = maid.getAvailableInv(true);
        Brain<EntityMaid> brain = maid.getBrain();
        BrewingList brewingList = brain.getMemory(MaidTavernEntities.BREWING_LIST.get()).orElse(null);
        if (brewingList == null) return List.of();
        BrewingSession session = brain.getMemory(MaidTavernEntities.BREWING_SESSION.get()).orElse(null);
        ResourceLocation recipeId = brewingList.get();
        BarrelRecipe recipe = (BarrelRecipe) maid.level().getRecipeManager().byKey(recipeId).map(RecipeHolder::value).orElse(null);
        List<Pair<ItemStack, Integer>> baseResult = new ArrayList<>();
        Predicate<ItemStack> bottlePredicate = stack -> stack.is(ModItems.EMPTY_BOTTLE);
        int bottleRequired = 16 - ItemHandlerUtil.countItems(maidInv, bottlePredicate);
        if (bottleRequired > 0) {
            List<ItemStack> bottleStacks = ItemHandlerUtil.findStacks(storage, bottlePredicate);
            for (ItemStack stack : bottleStacks) {
                int count = Math.min(stack.getCount(), bottleRequired);
                baseResult.add(new Pair<>(stack, count));
                bottleRequired -= count;
                if (bottleRequired <= 0) break;
            }
        }
        List<Pair<ItemStack, Integer>> result = new ArrayList<>(baseResult);
        Predicate<ItemStack> fluidPredicate = stack -> stack.is(recipe.fluid().getBucket());
        int fluidRequired = (session == null || session.fluidPlaced().isFalse())
                ? (4 - ItemHandlerUtil.countItems(maidInv, fluidPredicate)) : 0;
        if (fluidRequired > 0) {
            List<ItemStack> fluidStacks = ItemHandlerUtil.findStacks(storage, fluidPredicate);
            for (ItemStack stack : fluidStacks) {
                int count = Math.min(stack.getCount(), fluidRequired);
                result.add(new Pair<>(stack, count));
                fluidRequired -= count;
                if (fluidRequired <= 0) break;
            }
            if (fluidRequired > 0) return baseResult;
        }
        ingredient:
        for (Ingredient ingredient : recipe.getIngredients()) {
            if (ingredient.isEmpty()) continue;
            List<Pair<ItemStack, Integer>> ingredientResults = new ArrayList<>();
            for (ItemStack ingredientStack : ingredient.getItems()) {
                if (ingredientStack.isEmpty()) continue;
                Predicate<ItemStack> ingredientPredicate = stack ->
                        ItemStack.isSameItemSameComponents(stack, ingredientStack);
                int ingredientRequired = (session == null || session.ingredientsPlaced().isFalse())
                        ? (16 - ItemHandlerUtil.countItems(maidInv, ingredientPredicate)) : 0;
                if (ingredientRequired > 0) {
                    ingredientResults.clear();
                    List<ItemStack> ingredientStacksInStorage = ItemHandlerUtil.findStacks(storage, ingredientPredicate);
                    for (ItemStack stack : ingredientStacksInStorage) {
                        int count = Math.min(stack.getCount(), ingredientRequired);
                        ingredientResults.add(new Pair<>(stack, count));
                        ingredientRequired -= count;
                        if (ingredientRequired <= 0) {
                            result.addAll(ingredientResults);
                            continue ingredient;
                        }
                    }
                }
            }
            return baseResult;
        }

        return result;
    }

    @Override
    public List<ItemStack> getNeedToStoreStacks(EntityMaid maid) {
        Brain<EntityMaid> brain = maid.getBrain();
        BrewingList brewingList = brain.getMemory(MaidTavernEntities.BREWING_LIST.get()).orElse(null);
        if (brewingList == null) return List.of();
        for (ResourceLocation recipeId : brewingList.getRecipes()) {
            BarrelRecipe recipe = (BarrelRecipe) maid.level().getRecipeManager().byKey(recipeId).map(RecipeHolder::value).get();
            ItemStack resultItem = recipe.getResultItem(maid.level().registryAccess());
            List<ItemStack> foundStacks = ItemHandlerUtil.findStacks(maid.getAvailableInv(false), stack ->
                    ItemStack.isSameItem(stack, resultItem) || stack.is(Items.BUCKET));
            if (!foundStacks.isEmpty()) {
                return foundStacks;
            }
        }
        return List.of();
    }

    @Override
    public boolean isStorageValid(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (level.getBlockEntity(pos) instanceof Container) {
            if (!state.is(Blocks.BARREL)
            && !state.is(Blocks.CHEST)) return false;
            return !state.is(Blocks.CHEST) || !ChestBlock.isChestBlockedAt(level, pos);
        }
        return false;
    }

    @Override
    public boolean isBottleValid(EntityMaid maid, BlockPos pos) {
        if (pos == null) return false;
        BlockState state = maid.level().getBlockState(pos);
        BrewingList brewingList = maid.getBrain().getMemory(MaidTavernEntities.BREWING_LIST.get()).orElse(null);
        if (brewingList == null) return false;
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
        IBarrel barrel = getBarrel(maid.level(), pos.relative(tapFacing.getOpposite()));
        if (barrel == null || barrel.getRecipeId() == null
                || barrel.getBrewLevel() != IBarrel.BREWING_FINISHED) return false;
        for (ResourceLocation recipeId : brewingList.getRecipes()) {
            if (Objects.equals(barrel.getRecipeId(), recipeId)) return true;
        }
        return false;
    }
}
