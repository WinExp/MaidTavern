package com.winexp.maidtavern.maid.brewing;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.ysbbbbbb.kaleidoscopetavern.api.blockentity.IBarrel;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarrelBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.DrinkBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.TapBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.DrinkBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.BarrelRecipe;
import com.github.ysbbbbbb.kaleidoscopetavern.game.tap.TapBehaviorManager;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.winexp.maidtavern.MaidTavern;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import com.winexp.maidtavern.maid.brewing.barrel.MaidBrewingAddIngredientsTask;
import com.winexp.maidtavern.maid.brewing.barrel.MaidBrewingMoveToBarrelTask;
import com.winexp.maidtavern.maid.brewing.bottle.MaidBrewingMoveToBottleTask;
import com.winexp.maidtavern.maid.brewing.bottle.MaidBrewingPlaceBottleTask;
import com.winexp.maidtavern.maid.brewing.bottle.MaidBrewingTakeBottleTask;
import com.winexp.maidtavern.maid.brewing.common.MaidBrewingPreTickTask;
import com.winexp.maidtavern.maid.brewing.storage.MaidBrewingMoveToStorageTask;
import com.winexp.maidtavern.maid.brewing.storage.MaidBrewingStorageOperationTask;
import com.winexp.maidtavern.maid.task.IMaidTaskExt;
import com.winexp.maidtavern.tag.MaidTavernItemTags;
import com.winexp.maidtavern.util.ItemHandlerUtil;
import com.winexp.maidtavern.util.RecipeHolder;
import com.winexp.maidtavern.util.Utils;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class TaskBrewing implements IBrewingTask, IMaidTaskExt {
    private static final ResourceLocation UID = MaidTavern.asResource("brewing");
    private static final ItemStack ICON = ModItems.BARREL.get().getDefaultInstance();

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
                Pair.of(Integer.MIN_VALUE, new MaidBrewingPreTickTask()),
                Pair.of(5, new MaidBrewingMoveToStorageTask(this, 0.45f, 4, 3, 20)),
                Pair.of(5, new MaidBrewingStorageOperationTask(this)),
                Pair.of(5, new MaidBrewingMoveToBarrelTask(this, 0.45f, 4, 2.5, 20)),
                Pair.of(5, new MaidBrewingAddIngredientsTask(this, 20)),
                Pair.of(5, new MaidBrewingMoveToBottleTask(this, 0.45f, 4, 2, 20)),
                Pair.of(5, new MaidBrewingPlaceBottleTask(this)),
                Pair.of(5, new MaidBrewingTakeBottleTask(this))
        );
    }

    @Override
    public boolean enableLookAndRandomWalk(EntityMaid maid) {
        return !MaidBrewingStateManager.isWorking(maid);
    }

    @Override
    public boolean enableEating(EntityMaid maid) {
        return !MaidBrewingStateManager.isWorking(maid);
    }

    @Override
    public boolean shouldPickupGaveItem(EntityMaid maid, ItemEntity itemEntity) {
        return true;
    }

    @Override
    public boolean enableDrinking(EntityMaid maid) {
        return true;
    }

    @Override
    public boolean isBarrelValid(EntityMaid maid, @Nullable IBarrel barrel) {
        Brain<EntityMaid> brain = maid.getBrain();
        if (barrel == null || barrel.isBrewing()) return false;
        BrewingSession session = brain.getMemory(MaidTavernEntities.BREWING_SESSION.get()).orElse(null);
        if (session == null || !session.stage().isFluidsPlaced()) {
            if (!barrel.getFluid().isEmpty()) return false;
        }
        if (session == null || !session.stage().isIngredientsPlaced()) {
            if (!ItemHandlerUtil.isEmpty(barrel.getIngredient())) return false;
        }
        return true;
    }

    @Override
    public boolean hasIngredients(EntityMaid maid, ResourceLocation recipeId) {
        BrewingSession session = maid.getBrain().getMemory(MaidTavernEntities.BREWING_SESSION.get()).orElse(null);
        BarrelRecipe recipe = (BarrelRecipe) Utils.byKey(maid.level().getRecipeManager(), recipeId).map(RecipeHolder::value).orElse(null);
        IItemHandler maidInv = maid.getAvailableInv(true);
        if (recipe == null) return false;
        if (session == null || !session.stage().isFluidsPlaced()) {
            if (!ItemHandlerUtil.matchesCount(maidInv, stack ->
                    stack.is(recipe.fluid().getBucket()), MinMaxBounds.Ints.atLeast(4))) return false;
        }
        if (session == null || !session.stage().isIngredientsPlaced()) {
            ingredient:
            for (Ingredient ingredient : recipe.getIngredients()) {
                if (ingredient.isEmpty()) continue;
                for (ItemStack ingredientStack : ingredient.getItems()) {
                    if (ingredientStack.isEmpty()) continue;
                    if (ItemHandlerUtil.matchesCount(maidInv, stack ->
                            ItemStack.isSameItemSameTags(stack, ingredientStack), MinMaxBounds.Ints.atLeast(16))) {
                        continue ingredient;
                    }
                }
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean shouldExtract(EntityMaid maid) {
        BrewingList brewingList = maid.getBrain().getMemory(MaidTavernEntities.BREWING_LIST.get()).orElse(null);
        if (brewingList == null) return false;
        if (!ItemHandlerUtil.contains(maid.getAvailableInv(true), stack ->
                stack.is(ModItems.EMPTY_BOTTLE.get()))) return true;
        for (BrewingList.Entry entry : brewingList.getEntries()) {
            ResourceLocation recipeId = entry.recipeId();
            if (hasIngredients(maid, recipeId)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<Pair<ItemStack, Integer>> getBottlesToExtract(IItemHandler inventory, IItemHandler storage) {
        List<Pair<ItemStack, Integer>> result = new ArrayList<>();
        Predicate<ItemStack> bottlePredicate = stack -> stack.is(ModItems.EMPTY_BOTTLE.get());
        int bottleRequired = ModItems.EMPTY_BOTTLE.get().getDefaultInstance().getMaxStackSize() - ItemHandlerUtil.countItems(inventory, bottlePredicate);
        if (bottleRequired > 0) {
            List<ItemStack> bottleStacks = ItemHandlerUtil.findStacks(storage, bottlePredicate);
            for (ItemStack stack : bottleStacks) {
                int count = Math.min(stack.getCount(), bottleRequired);
                result.add(new Pair<>(stack, count));
                bottleRequired -= count;
                if (bottleRequired <= 0) break;
            }
        }
        return result;
    }

    @Override
    public @Nullable List<Pair<ItemStack, Integer>> getIngredientsToExtract(IItemHandler inventory, IItemHandler storage, RecipeManager recipeManager, BrewingList.Entry entry) {
        BarrelRecipe recipe = entry.getRecipe(recipeManager);
        List<Pair<ItemStack, Integer>> result = new ArrayList<>();
        Predicate<ItemStack> fluidPredicate = stack -> stack.is(recipe.fluid().getBucket());
        int fluidRequired = 4 - ItemHandlerUtil.countItems(inventory, fluidPredicate);
        if (fluidRequired > 0) {
            List<ItemStack> fluidStacks = ItemHandlerUtil.findStacks(storage, fluidPredicate);
            for (ItemStack stack : fluidStacks) {
                int count = Math.min(stack.getCount(), fluidRequired);
                result.add(new Pair<>(stack, count));
                fluidRequired -= count;
                if (fluidRequired <= 0) break;
            }
            if (fluidRequired > 0) return List.of();
        }
        ingredient:
        for (Ingredient ingredient : recipe.getIngredients()) {
            if (ingredient.isEmpty()) continue;
            List<Pair<ItemStack, Integer>> ingredientResults = new ArrayList<>();
            for (ItemStack ingredientStack : ingredient.getItems()) {
                if (ingredientStack.isEmpty()) continue;
                Predicate<ItemStack> ingredientPredicate = stack ->
                        ItemStack.isSameItemSameTags(stack, ingredientStack);
                int ingredientRequired = 16 - ItemHandlerUtil.countItems(inventory, ingredientPredicate);
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
            return List.of();
        }
        return result;
    }

    @Override
    public List<ItemStack> getResultsToInsert(EntityMaid maid) {
        Brain<EntityMaid> brain = maid.getBrain();
        BrewingList brewingList = brain.getMemory(MaidTavernEntities.BREWING_LIST.get()).orElse(null);
        if (brewingList == null) return List.of();
        ReferenceSet<ItemStack> results = new ReferenceArraySet<>();
        for (BrewingList.Entry entry : brewingList.getEntries()) {
            BarrelRecipe recipe = entry.getRecipe(maid.level().getRecipeManager());
            ItemStack resultItem = recipe.getResultItem(maid.level().registryAccess());
            List<ItemStack> foundStacks = ItemHandlerUtil.findStacks(maid.getAvailableInv(false), stack ->
                    ItemStack.isSameItem(stack, resultItem));
            results.addAll(foundStacks);
        }
        return List.copyOf(results);
    }

    @Override
    public List<ItemStack> getByproductsToInsert(EntityMaid maid) {
        Brain<EntityMaid> brain = maid.getBrain();
        BrewingList brewingList = brain.getMemory(MaidTavernEntities.BREWING_LIST.get()).orElse(null);
        if (brewingList == null) return List.of();
        return ItemHandlerUtil.findStacks(maid.getAvailableInv(false), stack ->
                stack.is(MaidTavernItemTags.BREWING_BYPRODUCTS));
    }

    @Override
    public boolean isBottleValid(EntityMaid maid, BlockPos pos) {
        if (pos == null) return false;
        BlockState state = maid.level().getBlockState(pos);
        BrewingList brewingList = maid.getBrain().getMemory(MaidTavernEntities.BREWING_LIST.get()).orElse(null);
        if (brewingList == null) return false;
        if (!state.is(ModBlocks.MOLOTOV.get())
                && !(state.getBlock() instanceof DrinkBlock)) return false;
        BlockState tapState = maid.level().getBlockState(pos.above());
        if (!tapState.is(ModBlocks.TAP.get())) return false;
        Direction tapFacing = tapState.getValue(TapBlock.FACING);
        BlockPos sourcePos = pos.above().relative(tapFacing.getOpposite());
        BlockState sourceState = maid.level().getBlockState(sourcePos);
        if (!TapBehaviorManager.contains(sourceState)) return false;
        if (state.is(ModBlocks.MOLOTOV.get())) {
            for (BrewingList.Entry entry : brewingList.getEntries()) {
                BarrelRecipe recipe = entry.getRecipe(maid.level().getRecipeManager());
                ItemStack result = recipe.getResultItem(maid.level().registryAccess());
                if (result.is(ModItems.MOLOTOV.get())) return true;
            }
        } else if (state.getBlock() instanceof DrinkBlock drinkBlock
                && state.getValue(drinkBlock.getCountProperty()) == 1) {
            DrinkBlockEntity drink = (DrinkBlockEntity) maid.level().getBlockEntity(pos);
            if (drink.getItems().isEmpty()) return false;
            ItemStack stack = drink.getItems().get(0);
            for (BrewingList.Entry entry : brewingList.getEntries()) {
                BrewingList.Config config = entry.config();
                BarrelRecipe recipe = entry.getRecipe(maid.level().getRecipeManager());
                ItemStack result = recipe.getResultItem(maid.level().registryAccess());
                if (ItemStack.isSameItem(stack, result)
                        && Objects.equals(BottleBlockItem.getBrewLevel(stack), config.brewLevel())) return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldPlaceBottle(EntityMaid maid, BlockPos pos) {
        BrewingList brewingList = maid.getBrain().getMemory(MaidTavernEntities.BREWING_LIST.get()).get();
        BlockState state = maid.level().getBlockState(pos);
        if (!ItemHandlerUtil.contains(maid.getAvailableInv(true), stack ->
                stack.is(ModItems.EMPTY_BOTTLE.get())) || !state.isAir()) return false;
        BlockState tapState = maid.level().getBlockState(pos.above());
        if (!tapState.is(ModBlocks.TAP.get())) return false;
        Direction tapFacing = tapState.getValue(TapBlock.FACING);
        BlockPos sourcePos = pos.above().relative(tapFacing.getOpposite());
        BlockState sourceState = maid.level().getBlockState(sourcePos);
        if (!TapBehaviorManager.contains(sourceState)) return false;
        IBarrel barrel = BarrelBlock.getBarrelEntity(maid.level(), sourcePos, sourceState);
        if (barrel == null || barrel.getRecipeId() == null) return false;
        for (BrewingList.Entry entry : brewingList.getEntries()) {
            ResourceLocation recipeId = entry.recipeId();
            BrewingList.Config config = entry.config();
            if (Objects.equals(barrel.getRecipeId(), recipeId) && barrel.getBrewLevel() == config.brewLevel()) return true;
        }
        return false;
    }
}
