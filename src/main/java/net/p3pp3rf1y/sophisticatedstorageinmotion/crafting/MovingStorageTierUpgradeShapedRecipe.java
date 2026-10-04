package net.p3pp3rf1y.sophisticatedstorageinmotion.crafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedcore.crafting.IWrapperRecipe;
import net.p3pp3rf1y.sophisticatedcore.crafting.RecipeWrapperSerializer;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedstorage.entity.MovingStorageWrapper;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;

import java.util.Optional;

public class MovingStorageTierUpgradeShapedRecipe extends ShapedRecipe implements IWrapperRecipe<ShapedRecipe> {
	private final ShapedRecipe compose;

	public MovingStorageTierUpgradeShapedRecipe(ShapedRecipe compose) {
		super(compose.getGroup(), compose.category(), compose.pattern, compose.result);
		this.compose = compose;
	}

	@Override
	public ShapedRecipe getCompose() {
		return compose;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		return super.matches(input, level) && getOriginalMovingStorage(input).map(MovingStorageTierUpgradeShapedRecipe::isPrimaryOrUnlinked).orElse(false);
	}

	@Override
	public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
		if (getOriginalMovingStorage(input).filter(MovingStorageTierUpgradeShapedRecipe::isPrimaryOrUnlinked).isEmpty()) {
			return ItemStack.EMPTY;
		}
		ItemStack upgradedMovingStorage = super.assemble(input, registries);
		getOriginalMovingStorage(input).ifPresent(originalMovingStorage -> {
			ItemStack originalStorageItem = MovingStorageItem.getStorageItem(originalMovingStorage);
			ItemStack upgradedStorageItem = MovingStorageItem.getStorageItem(upgradedMovingStorage);
			upgradedStorageItem.applyComponents(originalStorageItem.getComponents());
			upgradedStorageItem.set(ModCoreDataComponents.NUMBER_OF_INVENTORY_SLOTS,
					MovingStorageWrapper.getDefaultNumberOfInventorySlots(upgradedStorageItem));
			upgradedStorageItem.set(ModCoreDataComponents.NUMBER_OF_UPGRADE_SLOTS, MovingStorageWrapper.getDefaultNumberOfUpgradeSlots(upgradedStorageItem));
			upgradedMovingStorage.applyComponents(originalMovingStorage.getComponents());
			MovingStorageItem.setStorageItem(upgradedMovingStorage, upgradedStorageItem);
		});
		return upgradedMovingStorage;
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	private Optional<ItemStack> getOriginalMovingStorage(CraftingInput input) {
		for (int slot = 0; slot < input.size(); slot++) {
			ItemStack slotStack = input.getItem(slot);
			if (slotStack.getItem() instanceof MovingStorageItem) {
				return Optional.of(slotStack);
			}
		}

		return Optional.empty();
	}

	static boolean isPrimaryOrUnlinked(ItemStack moving) {
		ItemStack storage = MovingStorageItem.getStorageItem(moving);
		return !storage.has(ModCoreDataComponents.LINKED_STORAGE_ENDPOINT)
				|| Boolean.TRUE.equals(storage.get(ModCoreDataComponents.LINKED_STORAGE_PRIMARY_ENDPOINT));
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModItems.MOVING_STORAGE_TIER_UPGRADE_SHAPED_RECIPE_SERIALIZER.get();
	}

	public static class Serializer extends RecipeWrapperSerializer<ShapedRecipe, MovingStorageTierUpgradeShapedRecipe> {
		public Serializer() {
			super(MovingStorageTierUpgradeShapedRecipe::new, SHAPED_RECIPE);
		}
	}
}
