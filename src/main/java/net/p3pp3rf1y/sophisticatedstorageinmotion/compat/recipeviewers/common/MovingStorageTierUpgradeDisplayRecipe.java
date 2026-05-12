package net.p3pp3rf1y.sophisticatedstorageinmotion.compat.recipeviewers.common;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.common.CraftingDisplaySpec;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.common.CraftingDisplayVariant;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.common.SourceResultFocusBehavior;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.common.subtypes.PropertyBasedSubtypeInterpreter;
import net.p3pp3rf1y.sophisticatedstorage.entity.MovingStorageWrapper;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.WoodStorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.StorageBoatItem;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public record MovingStorageTierUpgradeDisplayRecipe(ResourceLocation id, CraftingRecipe recipe, boolean shapeless, int width, int height,
												 NonNullList<Ingredient> ingredients, int movingStorageIngredientIndex, List<MovingStorageTierUpgradeVariantPair> variantPairs) {
	public Optional<MovingStorageTierUpgradeVariantPair> findBySource(ItemStack stack) {
		return variantPairs.stream().filter(pair -> ItemStack.isSameItemSameTags(pair.source(), stack)).findFirst();
	}

	public Optional<MovingStorageTierUpgradeVariantPair> findBySourceItem(ItemStack stack) {
		return variantPairs.stream().filter(pair -> ItemStack.isSameItem(pair.source(), stack)).findFirst();
	}

	public Optional<MovingStorageTierUpgradeVariantPair> findByResult(ItemStack stack) {
		return variantPairs.stream().filter(pair -> ItemStack.isSameItemSameTags(pair.result(), stack)).findFirst();
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
				.map(pair -> new MovingStorageTierUpgradeVariantPair(createFocusedTierSource(pair, stack, resultStorage), stack.copy()));
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
				.map(pair -> new MovingStorageTierUpgradeVariantPair(stack.copy(), createFocusedTierResult(pair, stack, sourceStorage)));
	}

	private static MovingStorageTierUpgradeVariantPair withAssemblySource(MovingStorageTierUpgradeVariantPair pair, ItemStack sourceStack) {
		ItemStack result = pair.result().copy();
		MovingStorageItem.setStorageItem(result, sourceStack.copy());
		return new MovingStorageTierUpgradeVariantPair(sourceStack.copy(), result);
	}

	private static ItemStack createFocusedTierSource(MovingStorageTierUpgradeVariantPair pair, ItemStack result, ItemStack resultStorage) {
		ItemStack sourceStorage = MovingStorageItem.getStorageItem(pair.source());
		copyStorageComponentsForTier(sourceStorage, resultStorage);

		ItemStack source = pair.source().copy();
		copyTag(result, source);
		MovingStorageItem.setStorageItem(source, sourceStorage);
		return source;
	}

	private static ItemStack createFocusedTierResult(MovingStorageTierUpgradeVariantPair pair, ItemStack source, ItemStack sourceStorage) {
		ItemStack resultStorage = MovingStorageItem.getStorageItem(pair.result());
		copyStorageComponentsForTier(resultStorage, sourceStorage);

		ItemStack result = pair.result().copy();
		copyTag(source, result);
		MovingStorageItem.setStorageItem(result, resultStorage);
		return result;
	}

	private static void copyStorageComponentsForTier(ItemStack targetStorage, ItemStack focusedStorage) {
		copyTag(focusedStorage, targetStorage);
		if (isTintedStorage(focusedStorage) && WoodStorageBlockItem.getWoodType(focusedStorage).isEmpty()) {
			targetStorage.getOrCreateTag().remove(WoodStorageBlockItem.WOOD_TYPE_TAG);
		}
		if (targetStorage.getTag() != null && targetStorage.getTag().contains("inventorySlots")) {
			targetStorage.getOrCreateTag().putInt("inventorySlots", MovingStorageWrapper.getDefaultNumberOfInventorySlots(targetStorage));
		}
		if (targetStorage.getTag() != null && targetStorage.getTag().contains("upgradeSlots")) {
			targetStorage.getOrCreateTag().putInt("upgradeSlots", MovingStorageWrapper.getDefaultNumberOfUpgradeSlots(targetStorage));
		}
	}

	private static void copyTag(ItemStack from, ItemStack to) {
		to.setTag(from.getTag() == null ? null : from.getTag().copy());
	}

	private static boolean matchesFocusedSource(ItemStack recipeSource, ItemStack queriedStack, Function<ItemStack, Optional<PropertyBasedSubtypeInterpreter>> getSubtypeInterpreter) {
		if (!ItemStack.isSameItem(recipeSource, queriedStack)) {
			return false;
		}
		if (recipeSource.getItem() instanceof StorageBoatItem && StorageBoatItem.getBoatType(recipeSource) != StorageBoatItem.getBoatType(queriedStack)) {
			return false;
		}

		Optional<PropertyBasedSubtypeInterpreter> subtypeInterpreter = getSubtypeInterpreter.apply(queriedStack);
		if (subtypeInterpreter.isPresent()) {
			return movingStorageSubtypesMatch(recipeSource, queriedStack);
		}

		return ItemStack.isSameItemSameTags(recipeSource, queriedStack);
	}

	private static boolean matchesFocusedResult(ItemStack recipeResult, ItemStack queriedStack, Function<ItemStack, Optional<PropertyBasedSubtypeInterpreter>> getSubtypeInterpreter) {
		if (!ItemStack.isSameItem(recipeResult, queriedStack)) {
			return false;
		}
		if (recipeResult.getItem() instanceof StorageBoatItem && StorageBoatItem.getBoatType(recipeResult) != StorageBoatItem.getBoatType(queriedStack)) {
			return false;
		}

		Optional<PropertyBasedSubtypeInterpreter> subtypeInterpreter = getSubtypeInterpreter.apply(queriedStack);
		if (subtypeInterpreter.isPresent()) {
			return movingStorageSubtypesMatch(recipeResult, queriedStack);
		}

		return ItemStack.isSameItemSameTags(recipeResult, queriedStack);
	}

	private static boolean movingStorageSubtypesMatch(ItemStack recipeStack, ItemStack queriedStack) {
		ItemStack recipeStorage = MovingStorageItem.getStorageItem(recipeStack);
		ItemStack queriedStorage = MovingStorageItem.getStorageItem(queriedStack);
		if (isTintedStorage(queriedStorage)) {
			if (isTintedStorage(recipeStorage)) {
				return ItemStack.isSameItem(recipeStorage, queriedStorage)
						&& Objects.equals(StorageBlockItem.getMainColorFromStack(recipeStorage), StorageBlockItem.getMainColorFromStack(queriedStorage))
						&& Objects.equals(StorageBlockItem.getAccentColorFromStack(recipeStorage), StorageBlockItem.getAccentColorFromStack(queriedStorage))
						&& MovingStorageItem.isStorageItemFlatTopBarrel(recipeStack) == MovingStorageItem.isStorageItemFlatTopBarrel(queriedStack)
						&& (!(recipeStack.getItem() instanceof StorageBoatItem) || StorageBoatItem.getBoatType(recipeStack) == StorageBoatItem.getBoatType(queriedStack));
			}
			return ItemStack.isSameItem(recipeStorage, queriedStorage)
					&& WoodStorageBlockItem.getWoodType(recipeStorage).isEmpty()
					&& MovingStorageItem.isStorageItemFlatTopBarrel(recipeStack) == MovingStorageItem.isStorageItemFlatTopBarrel(queriedStack)
					&& (!(recipeStack.getItem() instanceof StorageBoatItem) || StorageBoatItem.getBoatType(recipeStack) == StorageBoatItem.getBoatType(queriedStack));
		}
		return ItemStack.isSameItem(recipeStorage, queriedStorage)
				&& Objects.equals(MovingStorageItem.getStorageItemWoodType(recipeStack), MovingStorageItem.getStorageItemWoodType(queriedStack))
				&& MovingStorageItem.isStorageItemFlatTopBarrel(recipeStack) == MovingStorageItem.isStorageItemFlatTopBarrel(queriedStack)
				&& (!(recipeStack.getItem() instanceof StorageBoatItem) || StorageBoatItem.getBoatType(recipeStack) == StorageBoatItem.getBoatType(queriedStack));
	}

	private static boolean isTintedStorage(ItemStack storageStack) {
		return StorageBlockItem.getMainColorFromStack(storageStack).isPresent() || StorageBlockItem.getAccentColorFromStack(storageStack).isPresent();
	}

	public CraftingDisplaySpec toAssemblySpec() {
		return new CraftingDisplaySpec(id, shapeless, width, height, ingredients, variantPairs.stream().map(this::toVariant).toList(), getGlobalVariants(), Set.of(recipe.getId()),
				new SourceResultFocusBehavior(movingStorageIngredientIndex, this::focusAssemblySource, this::focusAssemblyResult));
	}

	public CraftingDisplaySpec toTierUpgradeSpec(Function<ItemStack, Optional<PropertyBasedSubtypeInterpreter>> getSubtypeInterpreter) {
		return new CraftingDisplaySpec(id, shapeless, width, height, ingredients, variantPairs.stream().map(this::toVariant).toList(), getGlobalVariants(), Set.of(recipe.getId()),
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
		return StorageBlockItem.getMainColorFromStack(storageStack).isEmpty() && StorageBlockItem.getAccentColorFromStack(storageStack).isEmpty();
	}

	private CraftingDisplayVariant toVariant(MovingStorageTierUpgradeVariantPair pair) {
		List<ItemStack> inputs = new ArrayList<>(ingredients.size());
		for (int i = 0; i < ingredients.size(); i++) {
			inputs.add(i == movingStorageIngredientIndex ? pair.source() : ItemStack.EMPTY);
		}
		return new CraftingDisplayVariant(inputs, List.of(pair.result()));
	}

	private Optional<CraftingDisplayVariant> focusAssemblySource(CraftingDisplayVariant variant, ItemStack focusedInput) {
		return findAssemblyUsageForSource(focusedInput)
				.filter(pair -> ItemStack.isSameItemSameTags(getSource(variant), pair.source()))
				.map(pair -> toVariant(pair));
	}

	private Optional<CraftingDisplayVariant> focusAssemblyResult(CraftingDisplayVariant variant, ItemStack focusedOutput) {
		MovingStorageTierUpgradeVariantPair pair = toPair(variant);
		if (ItemStack.isSameItemSameTags(pair.result(), focusedOutput)) {
			return Optional.of(toVariant(pair));
		}
		if (!(focusedOutput.getItem() instanceof MovingStorageItem) || !movingStorageItemAndBoatTypeMatch(pair.result(), focusedOutput)) {
			return Optional.empty();
		}

		ItemStack focusedStorage = MovingStorageItem.getStorageItem(focusedOutput);
		Optional<MovingStorageTierUpgradeVariantPair> templatePair = variantPairs.stream()
				.filter(candidate -> movingStorageItemAndBoatTypeMatch(candidate.result(), focusedOutput))
				.filter(candidate -> ItemStack.isSameItem(candidate.source(), focusedStorage))
				.findFirst();
		if (templatePair.isEmpty() || !ItemStack.isSameItemSameTags(pair.source(), templatePair.get().source()) || !ItemStack.isSameItemSameTags(pair.result(), templatePair.get().result())) {
			return Optional.empty();
		}
		return Optional.of(toVariant(new MovingStorageTierUpgradeVariantPair(focusedStorage.copy(), focusedOutput.copy())));
	}

	private static boolean movingStorageItemAndBoatTypeMatch(ItemStack recipeStack, ItemStack focusedStack) {
		return ItemStack.isSameItem(recipeStack, focusedStack)
				&& (!(recipeStack.getItem() instanceof StorageBoatItem) || StorageBoatItem.getBoatType(recipeStack) == StorageBoatItem.getBoatType(focusedStack));
	}

	private Optional<CraftingDisplayVariant> focusTierSource(CraftingDisplayVariant variant, ItemStack focusedInput, Function<ItemStack, Optional<PropertyBasedSubtypeInterpreter>> getSubtypeInterpreter) {
		MovingStorageTierUpgradeVariantPair pair = toPair(variant);
		Optional<MovingStorageTierUpgradeVariantPair> exactPair = variantPairs.stream().filter(candidate -> ItemStack.isSameItemSameTags(candidate.source(), focusedInput)).findFirst();
		if (exactPair.isPresent()) {
			return exactPair.filter(candidate -> ItemStack.isSameItemSameTags(pair.source(), candidate.source())).map(this::toVariant);
		}
		if (!matchesFocusedSource(pair.source(), focusedInput, getSubtypeInterpreter)) {
			return Optional.empty();
		}
		if (ItemStack.isSameItemSameTags(pair.source(), focusedInput)) {
			return Optional.of(toVariant(pair));
		}
		return Optional.of(toVariant(new MovingStorageTierUpgradeVariantPair(focusedInput.copy(), createFocusedTierResult(pair, focusedInput, MovingStorageItem.getStorageItem(focusedInput)))));
	}

	private Optional<CraftingDisplayVariant> focusTierResult(CraftingDisplayVariant variant, ItemStack focusedOutput, Function<ItemStack, Optional<PropertyBasedSubtypeInterpreter>> getSubtypeInterpreter) {
		MovingStorageTierUpgradeVariantPair pair = toPair(variant);
		Optional<MovingStorageTierUpgradeVariantPair> exactPair = variantPairs.stream().filter(candidate -> ItemStack.isSameItemSameTags(candidate.result(), focusedOutput)).findFirst();
		if (exactPair.isPresent()) {
			return exactPair.filter(candidate -> ItemStack.isSameItemSameTags(pair.result(), candidate.result())).map(this::toVariant);
		}
		if (!matchesFocusedResult(pair.result(), focusedOutput, getSubtypeInterpreter)) {
			return Optional.empty();
		}
		if (ItemStack.isSameItemSameTags(pair.result(), focusedOutput)) {
			return Optional.of(toVariant(pair));
		}
		return Optional.of(toVariant(new MovingStorageTierUpgradeVariantPair(createFocusedTierSource(pair, focusedOutput, MovingStorageItem.getStorageItem(focusedOutput)), focusedOutput.copy())));
	}

	private static ItemStack getSource(CraftingDisplayVariant variant) {
		return variant.inputs().stream().filter(stack -> !stack.isEmpty()).findFirst().orElse(ItemStack.EMPTY);
	}

	private static MovingStorageTierUpgradeVariantPair toPair(CraftingDisplayVariant variant) {
		return new MovingStorageTierUpgradeVariantPair(getSource(variant), variant.firstOutput());
	}
}
