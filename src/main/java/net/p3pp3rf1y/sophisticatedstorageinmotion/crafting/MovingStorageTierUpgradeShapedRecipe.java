package net.p3pp3rf1y.sophisticatedstorageinmotion.crafting;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedcore.crafting.IWrapperRecipe;
import net.p3pp3rf1y.sophisticatedcore.crafting.RecipeWrapperSerializer;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageStackData;
import net.p3pp3rf1y.sophisticatedcore.util.NBTHelper;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageWrapper;
import net.p3pp3rf1y.sophisticatedstorage.entity.MovingStorageWrapper;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public class MovingStorageTierUpgradeShapedRecipe extends ShapedRecipe implements IWrapperRecipe<ShapedRecipe> {
	public static final Set<ResourceLocation> REGISTERED_RECIPES = new LinkedHashSet<>();
	private final ShapedRecipe compose;

	public MovingStorageTierUpgradeShapedRecipe(ShapedRecipe compose) {
		super(compose.getId(), compose.getGroup(), compose.category(), compose.getWidth(), compose.getHeight(), compose.getIngredients(), compose.result);
		this.compose = compose;
		REGISTERED_RECIPES.add(compose.getId());
	}

	@Override
	public ShapedRecipe getCompose() {
		return compose;
	}

	@Override
	public boolean matches(CraftingContainer input, Level level) {
		return super.matches(input, level) && getOriginalMovingStorage(input).map(MovingStorageTierUpgradeShapedRecipe::isPrimaryOrUnlinked).orElse(false);
	}

	@Override
	public ItemStack assemble(CraftingContainer input, RegistryAccess registries) {
		if (getOriginalMovingStorage(input).filter(MovingStorageTierUpgradeShapedRecipe::isPrimaryOrUnlinked).isEmpty()) {
			return ItemStack.EMPTY;
		}
		ItemStack upgradedMovingStorage = super.assemble(input, registries);
		getOriginalMovingStorage(input).ifPresent(originalMovingStorage -> {
			ItemStack originalStorageItem = MovingStorageItem.getStorageItem(originalMovingStorage);
			ItemStack upgradedStorageItem = MovingStorageItem.getStorageItem(upgradedMovingStorage);
			upgradedStorageItem.setTag(originalStorageItem.getTag());
			NBTHelper.setInteger(upgradedStorageItem, StorageWrapper.NUMBER_OF_INVENTORY_SLOTS_TAG,
					MovingStorageWrapper.getDefaultNumberOfInventorySlots(upgradedStorageItem));
			NBTHelper.setInteger(upgradedStorageItem, StorageWrapper.NUMBER_OF_UPGRADE_SLOTS_TAG,
					MovingStorageWrapper.getDefaultNumberOfUpgradeSlots(upgradedStorageItem));
			upgradedMovingStorage.setTag(originalMovingStorage.getTag());
			MovingStorageItem.setStorageItem(upgradedMovingStorage, upgradedStorageItem);
		});
		return upgradedMovingStorage;
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	private Optional<ItemStack> getOriginalMovingStorage(CraftingContainer input) {
		for (int slot = 0; slot < input.getContainerSize(); slot++) {
			ItemStack slotStack = input.getItem(slot);
			if (slotStack.getItem() instanceof MovingStorageItem) {
				return Optional.of(slotStack);
			}
		}

		return Optional.empty();
	}

	static boolean isPrimaryOrUnlinked(ItemStack moving) {
		ItemStack storage = MovingStorageItem.getStorageItem(moving);
		return LinkedStorageStackData.getEndpoint(storage) == null || LinkedStorageStackData.isPrimaryEndpoint(storage);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModItems.MOVING_STORAGE_TIER_UPGRADE_SHAPED_RECIPE_SERIALIZER.get();
	}

	public static class Serializer extends RecipeWrapperSerializer<ShapedRecipe, MovingStorageTierUpgradeShapedRecipe> {
		public Serializer() {
			super(MovingStorageTierUpgradeShapedRecipe::new, RecipeSerializer.SHAPED_RECIPE);
		}
	}
}
