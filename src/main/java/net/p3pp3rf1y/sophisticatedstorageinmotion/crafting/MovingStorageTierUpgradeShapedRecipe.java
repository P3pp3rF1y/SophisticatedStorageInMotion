package net.p3pp3rf1y.sophisticatedstorageinmotion.crafting;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedcore.crafting.IWrapperRecipe;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedstorage.entity.MovingStorageWrapper;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class MovingStorageTierUpgradeShapedRecipe implements CraftingRecipe, IWrapperRecipe<ShapedRecipe> {
	public static final RecipeSerializer<MovingStorageTierUpgradeShapedRecipe> SERIALIZER = new RecipeSerializer<>(
			RecordCodecBuilder.mapCodec(instance -> instance.group(
					ShapedRecipe.SERIALIZER.codec().forGetter(MovingStorageTierUpgradeShapedRecipe::getCompose),
					ItemStackTemplate.CODEC.fieldOf("upgraded_storage_result").forGetter(MovingStorageTierUpgradeShapedRecipe::getUpgradedStorageResult)
			).apply(instance, MovingStorageTierUpgradeShapedRecipe::new)),
			StreamCodec.composite(
					ShapedRecipe.SERIALIZER.streamCodec(), MovingStorageTierUpgradeShapedRecipe::getCompose,
					ItemStackTemplate.STREAM_CODEC, MovingStorageTierUpgradeShapedRecipe::getUpgradedStorageResult,
					MovingStorageTierUpgradeShapedRecipe::new
			)
	);
	private final ShapedRecipe compose;
	private final ItemStackTemplate upgradedStorageResult;

	public MovingStorageTierUpgradeShapedRecipe(ShapedRecipe compose, ItemStackTemplate upgradedStorageResult) {
		this.compose = compose;
		this.upgradedStorageResult = upgradedStorageResult;
	}

	public static Function<ShapedRecipe, MovingStorageTierUpgradeShapedRecipe> wrapper(ItemStackTemplate upgradedStorageResult) {
		return compose -> new MovingStorageTierUpgradeShapedRecipe(compose, upgradedStorageResult);
	}

	@Override
	public ShapedRecipe getCompose() {
		return compose;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		return compose.matches(input, level) && getOriginalMovingStorage(input).isPresent();
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		ItemStack upgradedMovingStorage = compose.assemble(input);
		getOriginalMovingStorage(input).ifPresent(originalMovingStorage -> {
			ItemStack originalStorageItem = MovingStorageItem.getStorageItem(originalMovingStorage);
			ItemStack upgradedStorageItem = upgradedStorageResult.create();
			upgradedStorageItem.applyComponents(originalStorageItem.getComponentsPatch());
			upgradedStorageItem.set(ModCoreDataComponents.NUMBER_OF_INVENTORY_SLOTS, MovingStorageWrapper.getDefaultNumberOfInventorySlots(upgradedStorageItem));
			upgradedStorageItem.set(ModCoreDataComponents.NUMBER_OF_UPGRADE_SLOTS, MovingStorageWrapper.getDefaultNumberOfUpgradeSlots(upgradedStorageItem));
			upgradedMovingStorage.applyComponents(originalMovingStorage.getComponentsPatch());
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

	@Override
	public RecipeSerializer<MovingStorageTierUpgradeShapedRecipe> getSerializer() {
		return ModItems.MOVING_STORAGE_TIER_UPGRADE_SHAPED_RECIPE_SERIALIZER.get();
	}

	public ItemStackTemplate getUpgradedStorageResult() {
		return upgradedStorageResult;
	}

	@Override
	public boolean showNotification() {
		return compose.showNotification();
	}

	@Override
	public String group() {
		return compose.group();
	}

	@Override
	public CraftingBookCategory category() {
		return compose.category();
	}

	@Override
	public PlacementInfo placementInfo() {
		return compose.placementInfo();
	}

	@Override
	public List<net.minecraft.world.item.crafting.display.RecipeDisplay> display() {
		return List.of();
	}

}
