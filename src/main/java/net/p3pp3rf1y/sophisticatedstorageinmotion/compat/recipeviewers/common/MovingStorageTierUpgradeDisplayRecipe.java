package net.p3pp3rf1y.sophisticatedstorageinmotion.compat.recipeviewers.common;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.common.CraftingDisplaySpec;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.common.CraftingDisplayVariant;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.common.SourceResultFocusBehavior;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.common.subtypes.PropertyBasedSubtypeInterpreter;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedstorage.entity.MovingStorageWrapper;
import net.p3pp3rf1y.sophisticatedstorage.init.ModDataComponents;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.WoodStorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.StorageBoatItem;

import java.util.*;
import java.util.function.Function;

public record MovingStorageTierUpgradeDisplayRecipe(ResourceLocation id, RecipeHolder<CraftingRecipe> recipeHolder, boolean shapeless, int width, int height,
													 NonNullList<Ingredient> ingredients, int movingStorageIngredientIndex, List<MovingStorageTierUpgradeVariantPair> variantPairs) {
	public Optional<MovingStorageTierUpgradeVariantPair> findBySource(ItemStack stack) {
		return variantPairs.stream().filter(pair -> ItemStack.isSameItemSameComponents(pair.source(), stack)).findFirst();
	}

	public Optional<MovingStorageTierUpgradeVariantPair> findBySourceItem(ItemStack stack) {
		return variantPairs.stream().filter(pair -> ItemStack.isSameItem(pair.source(), stack)).findFirst();
	}

	public Optional<MovingStorageTierUpgradeVariantPair> findByResult(ItemStack stack) {
		return variantPairs.stream().filter(pair -> ItemStack.isSameItemSameComponents(pair.result(), stack)).findFirst();
	}

	public Optional<MovingStorageTierUpgradeVariantPair> findByResultItem(ItemStack stack) {
		return variantPairs.stream().filter(pair -> ItemStack.isSameItem(pair.result(), stack)).findFirst();
	}

	public Optional<MovingStorageTierUpgradeVariantPair> findAssemblyRecipeForResult(ItemStack stack) {
		Optional<MovingStorageTierUpgradeVariantPair> exactPair = findByResult(stack);
		if (exactPair.isPresent()) {
			return exactPair;
		}
		if (!(stack.getItem() instanceof MovingStorageItem)) {
			return Optional.empty();
		}

		ItemStack storage = MovingStorageItem.getStorageItem(stack);
		return findByResultItem(stack)
				.filter(pair -> ItemStack.isSameItem(pair.source(), storage))
				.map(pair -> new MovingStorageTierUpgradeVariantPair(storage.copy(), stack.copy()));
	}

	public Optional<MovingStorageTierUpgradeVariantPair> findAssemblyUsageForSource(ItemStack stack) {
		if (!(stack.getItem() instanceof StorageBlockItem)) {
			return Optional.empty();
		}
		Optional<MovingStorageTierUpgradeVariantPair> exactPair = findBySource(stack);
		if (exactPair.isPresent()) {
			return exactPair;
		}

		return findBySourceItem(stack).map(pair -> withAssemblySource(pair, stack));
	}

	public Optional<MovingStorageTierUpgradeVariantPair> findTierUpgradeRecipeForResult(ItemStack stack, Function<ItemStack, Optional<PropertyBasedSubtypeInterpreter>> getSubtypeInterpreter) {
		Optional<MovingStorageTierUpgradeVariantPair> exactPair = variantPairs.stream().filter(pair -> matchesFocusedResult(pair.result(), stack, getSubtypeInterpreter)).findFirst();
		if (exactPair.isPresent()) {
			return exactPair;
		}
		if (getSubtypeInterpreter.apply(stack).isPresent()) {
			return Optional.empty();
		}
		if (!(stack.getItem() instanceof MovingStorageItem)) {
			return Optional.empty();
		}

		ItemStack resultStorage = MovingStorageItem.getStorageItem(stack);
		return variantPairs.stream()
				.filter(pair -> ItemStack.isSameItem(pair.result(), stack) && ItemStack.isSameItem(MovingStorageItem.getStorageItem(pair.result()), resultStorage))
				.findFirst()
				.map(pair -> new MovingStorageTierUpgradeVariantPair(createFocusedTierSource(pair.source(), stack, resultStorage), stack.copy()));
	}

	public Optional<MovingStorageTierUpgradeVariantPair> findTierUpgradeUsageForSource(ItemStack stack) {
		Optional<MovingStorageTierUpgradeVariantPair> exactPair = findBySource(stack);
		if (exactPair.isPresent()) {
			return exactPair;
		}
		if (!(stack.getItem() instanceof MovingStorageItem)) {
			return Optional.empty();
		}

		ItemStack sourceStorage = MovingStorageItem.getStorageItem(stack);
		return variantPairs.stream()
				.filter(pair -> ItemStack.isSameItem(pair.source(), stack) && ItemStack.isSameItem(MovingStorageItem.getStorageItem(pair.source()), sourceStorage))
				.findFirst()
				.map(pair -> new MovingStorageTierUpgradeVariantPair(stack.copy(), createFocusedTierResult(pair.result(), stack, sourceStorage)));
	}

	private static MovingStorageTierUpgradeVariantPair withAssemblySource(MovingStorageTierUpgradeVariantPair pair, ItemStack sourceStack) {
		ItemStack result = pair.result().copy();
		MovingStorageItem.setStorageItem(result, sourceStack.copy());
		return new MovingStorageTierUpgradeVariantPair(sourceStack.copy(), result);
	}

	private static ItemStack createFocusedTierSource(ItemStack recipeSource, ItemStack result, ItemStack resultStorage) {
		ItemStack sourceStorage = MovingStorageItem.getStorageItem(recipeSource);
		copyStorageComponentsForTier(sourceStorage, resultStorage);

		ItemStack source = recipeSource.copy();
		MovingStorageItem.setStorageItem(source, sourceStorage);
		return source;
	}

	private static ItemStack createFocusedTierResult(ItemStack recipeResult, ItemStack source, ItemStack sourceStorage) {
		ItemStack resultStorage = MovingStorageItem.getStorageItem(recipeResult);
		copyStorageComponentsForTier(resultStorage, sourceStorage);

		ItemStack result = recipeResult.copy();
		result.applyComponents(source.getComponents());
		MovingStorageItem.setStorageItem(result, resultStorage);
		return result;
	}

	private static void copyStorageComponentsForTier(ItemStack targetStorage, ItemStack focusedStorage) {
		targetStorage.applyComponents(focusedStorage.getComponents());
		if (isTintedStorage(focusedStorage) && !focusedStorage.has(ModDataComponents.WOOD_TYPE)) {
			targetStorage.remove(ModDataComponents.WOOD_TYPE);
		}
		if (focusedStorage.has(ModCoreDataComponents.NUMBER_OF_INVENTORY_SLOTS)) {
			targetStorage.set(ModCoreDataComponents.NUMBER_OF_INVENTORY_SLOTS, MovingStorageWrapper.getDefaultNumberOfInventorySlots(targetStorage));
		}
		if (focusedStorage.has(ModCoreDataComponents.NUMBER_OF_UPGRADE_SLOTS)) {
			targetStorage.set(ModCoreDataComponents.NUMBER_OF_UPGRADE_SLOTS, MovingStorageWrapper.getDefaultNumberOfUpgradeSlots(targetStorage));
		}
	}

	private static boolean matchesFocusedSource(ItemStack recipeSource, ItemStack queriedStack, Function<ItemStack, Optional<PropertyBasedSubtypeInterpreter>> getSubtypeInterpreter) {
		if (!ItemStack.isSameItem(recipeSource, queriedStack)) {
			return false;
		}
		if (recipeSource.getItem() instanceof StorageBoatItem && StorageBoatItem.getWoodType(recipeSource) != StorageBoatItem.getWoodType(queriedStack)) {
			return false;
		}

		Optional<PropertyBasedSubtypeInterpreter> subtypeInterpreter = getSubtypeInterpreter.apply(queriedStack);
		if (subtypeInterpreter.isPresent()) {
			return movingStorageSubtypesMatch(recipeSource, queriedStack);
		}

		return ItemStack.isSameItemSameComponents(recipeSource, queriedStack);
	}

	private static boolean matchesFocusedResult(ItemStack recipeResult, ItemStack queriedStack, Function<ItemStack, Optional<PropertyBasedSubtypeInterpreter>> getSubtypeInterpreter) {
		if (!ItemStack.isSameItem(recipeResult, queriedStack)) {
			return false;
		}
		if (recipeResult.getItem() instanceof StorageBoatItem && StorageBoatItem.getWoodType(recipeResult) != StorageBoatItem.getWoodType(queriedStack)) {
			return false;
		}

		Optional<PropertyBasedSubtypeInterpreter> subtypeInterpreter = getSubtypeInterpreter.apply(queriedStack);
		if (subtypeInterpreter.isPresent()) {
			return movingStorageSubtypesMatch(recipeResult, queriedStack);
		}

		return ItemStack.isSameItemSameComponents(recipeResult, queriedStack);
	}

	private static boolean movingStorageSubtypesMatch(ItemStack recipeStack, ItemStack queriedStack) {
		ItemStack recipeStorage = MovingStorageItem.getStorageItem(recipeStack);
		ItemStack queriedStorage = MovingStorageItem.getStorageItem(queriedStack);
		if (isTintedStorage(queriedStorage)) {
			if (isTintedStorage(recipeStorage)) {
				return ItemStack.isSameItem(recipeStorage, queriedStorage)
						&& Objects.equals(StorageBlockItem.getMainColorFromComponentHolder(recipeStorage), StorageBlockItem.getMainColorFromComponentHolder(queriedStorage))
						&& Objects.equals(StorageBlockItem.getAccentColorFromComponentHolder(recipeStorage), StorageBlockItem.getAccentColorFromComponentHolder(queriedStorage))
						&& MovingStorageItem.isStorageItemFlatTopBarrel(recipeStack) == MovingStorageItem.isStorageItemFlatTopBarrel(queriedStack)
						&& (!(recipeStack.getItem() instanceof StorageBoatItem) || StorageBoatItem.getWoodType(recipeStack) == StorageBoatItem.getWoodType(queriedStack));
			}
			return ItemStack.isSameItem(recipeStorage, queriedStorage)
					&& WoodStorageBlockItem.getWoodType(recipeStorage).isEmpty()
					&& MovingStorageItem.isStorageItemFlatTopBarrel(recipeStack) == MovingStorageItem.isStorageItemFlatTopBarrel(queriedStack)
					&& (!(recipeStack.getItem() instanceof StorageBoatItem) || StorageBoatItem.getWoodType(recipeStack) == StorageBoatItem.getWoodType(queriedStack));
		}
		return ItemStack.isSameItem(recipeStorage, queriedStorage)
				&& Objects.equals(MovingStorageItem.getStorageItemWoodType(recipeStack), MovingStorageItem.getStorageItemWoodType(queriedStack))
				&& MovingStorageItem.isStorageItemFlatTopBarrel(recipeStack) == MovingStorageItem.isStorageItemFlatTopBarrel(queriedStack)
				&& (!(recipeStack.getItem() instanceof StorageBoatItem) || StorageBoatItem.getWoodType(recipeStack) == StorageBoatItem.getWoodType(queriedStack));
	}

	private static boolean isTintedStorage(ItemStack storageStack) {
		return StorageBlockItem.getMainColorFromComponentHolder(storageStack).isPresent() || StorageBlockItem.getAccentColorFromComponentHolder(storageStack).isPresent();
	}

	public CraftingDisplaySpec toAssemblySpec() {
		return new CraftingDisplaySpec(id, shapeless, width, height, ingredients, variantPairs.stream().map(this::toVariant).toList(), getGlobalVariants(), Set.of(recipeHolder.id().location()),
				new SourceResultFocusBehavior(movingStorageIngredientIndex, this::focusAssemblySource, this::focusAssemblyResult));
	}

	public CraftingDisplaySpec toTierUpgradeSpec(Function<ItemStack, Optional<PropertyBasedSubtypeInterpreter>> getSubtypeInterpreter) {
		return new CraftingDisplaySpec(id, shapeless, width, height, ingredients, variantPairs.stream().map(this::toVariant).toList(), getGlobalVariants(), Set.of(recipeHolder.id().location()),
				new SourceResultFocusBehavior(movingStorageIngredientIndex, (variant, focusedInput) -> focusTierSource(variant, focusedInput, getSubtypeInterpreter), (variant, focusedOutput) -> focusTierResult(variant, focusedOutput, getSubtypeInterpreter)));
	}

	private List<CraftingDisplayVariant> getGlobalVariants() {
		return variantPairs.stream()
				.filter(pair -> isUntintedStorage(pair.source()) && isUntintedStorage(pair.result()))
				.map(this::toVariant)
				.toList();
	}

	private static boolean isUntintedStorage(ItemStack stack) {
		ItemStack storageStack = stack.getItem() instanceof MovingStorageItem ? MovingStorageItem.getStorageItem(stack) : stack;
		return StorageBlockItem.getMainColorFromComponentHolder(storageStack).isEmpty() && StorageBlockItem.getAccentColorFromComponentHolder(storageStack).isEmpty();
	}

	private CraftingDisplayVariant toVariant(MovingStorageTierUpgradeVariantPair pair) {
		List<ItemStack> inputs = new ArrayList<>(ingredients.size());
		for (int i = 0; i < ingredients.size(); i++) {
			inputs.add(i == movingStorageIngredientIndex ? pair.source() : ItemStack.EMPTY);
		}
		return new CraftingDisplayVariant(inputs, List.of(pair.result()));
	}

	private Optional<CraftingDisplayVariant> focusAssemblySource(CraftingDisplayVariant variant, ItemStack focusedInput) {
		if (!(focusedInput.getItem() instanceof StorageBlockItem)) {
			return Optional.empty();
		}

		ItemStack source = getSource(variant);
		if (ItemStack.isSameItemSameComponents(source, focusedInput)) {
			return Optional.of(variant);
		}
		if (!ItemStack.isSameItem(source, focusedInput) || findBySource(focusedInput).isPresent()) {
			return Optional.empty();
		}

		return findBySourceItem(focusedInput)
				.filter(templatePair -> ItemStack.isSameItemSameComponents(source, templatePair.source()))
				.map(templatePair -> toVariant(withAssemblySource(templatePair, focusedInput)));
	}

	private Optional<CraftingDisplayVariant> focusAssemblyResult(CraftingDisplayVariant variant, ItemStack focusedOutput) {
		ItemStack source = getSource(variant);
		ItemStack result = variant.firstOutput();
		if (ItemStack.isSameItemSameComponents(result, focusedOutput)) {
			return Optional.of(variant);
		}
		if (!(focusedOutput.getItem() instanceof MovingStorageItem) || !movingStorageItemAndBoatTypeMatch(result, focusedOutput)) {
			return Optional.empty();
		}

		ItemStack focusedStorage = MovingStorageItem.getStorageItem(focusedOutput);
		Optional<MovingStorageTierUpgradeVariantPair> templatePair = variantPairs.stream()
				.filter(candidate -> movingStorageItemAndBoatTypeMatch(candidate.result(), focusedOutput))
				.filter(candidate -> ItemStack.isSameItem(candidate.source(), focusedStorage))
				.findFirst();
		if (templatePair.isEmpty() || !ItemStack.isSameItemSameComponents(source, templatePair.get().source()) || !ItemStack.isSameItemSameComponents(result, templatePair.get().result())) {
			return Optional.empty();
		}
		return Optional.of(toVariant(new MovingStorageTierUpgradeVariantPair(focusedStorage.copy(), focusedOutput.copy())));
	}

	private static boolean movingStorageItemAndBoatTypeMatch(ItemStack recipeStack, ItemStack focusedStack) {
		return ItemStack.isSameItem(recipeStack, focusedStack)
				&& (!(recipeStack.getItem() instanceof StorageBoatItem) || StorageBoatItem.getWoodType(recipeStack) == StorageBoatItem.getWoodType(focusedStack));
	}

	private Optional<CraftingDisplayVariant> focusTierSource(CraftingDisplayVariant variant, ItemStack focusedInput, Function<ItemStack, Optional<PropertyBasedSubtypeInterpreter>> getSubtypeInterpreter) {
		ItemStack source = getSource(variant);
		if (!movingStorageItemAndBoatTypeMatch(source, focusedInput)) {
			return Optional.empty();
		}
		ItemStack result = variant.firstOutput();
		Optional<MovingStorageTierUpgradeVariantPair> exactPair = variantPairs.stream().filter(candidate -> ItemStack.isSameItemSameComponents(candidate.source(), focusedInput)).findFirst();
		if (exactPair.isPresent()) {
			return exactPair.filter(candidate -> ItemStack.isSameItemSameComponents(source, candidate.source())).map(this::toVariant);
		}
		if (!matchesFocusedSource(source, focusedInput, getSubtypeInterpreter)) {
			return Optional.empty();
		}
		if (ItemStack.isSameItemSameComponents(source, focusedInput)) {
			return Optional.of(variant);
		}
		return Optional.of(toVariant(new MovingStorageTierUpgradeVariantPair(focusedInput.copy(), createFocusedTierResult(result, focusedInput, MovingStorageItem.getStorageItem(focusedInput)))));
	}

	private Optional<CraftingDisplayVariant> focusTierResult(CraftingDisplayVariant variant, ItemStack focusedOutput, Function<ItemStack, Optional<PropertyBasedSubtypeInterpreter>> getSubtypeInterpreter) {
		ItemStack source = getSource(variant);
		ItemStack result = variant.firstOutput();
		Optional<MovingStorageTierUpgradeVariantPair> exactPair = variantPairs.stream().filter(candidate -> ItemStack.isSameItemSameComponents(candidate.result(), focusedOutput)).findFirst();
		if (exactPair.isPresent()) {
			return exactPair.filter(candidate -> ItemStack.isSameItemSameComponents(result, candidate.result())).map(this::toVariant);
		}
		if (!matchesFocusedResult(result, focusedOutput, getSubtypeInterpreter)) {
			return Optional.empty();
		}
		if (ItemStack.isSameItemSameComponents(result, focusedOutput)) {
			return Optional.of(variant);
		}
		return Optional.of(toVariant(new MovingStorageTierUpgradeVariantPair(createFocusedTierSource(source, focusedOutput, MovingStorageItem.getStorageItem(focusedOutput)), focusedOutput.copy())));
	}

	private ItemStack getSource(CraftingDisplayVariant variant) {
		return movingStorageIngredientIndex < variant.inputs().size() ? variant.inputs().get(movingStorageIngredientIndex) : ItemStack.EMPTY;
	}
}
