package net.p3pp3rf1y.sophisticatedstorageinmotion.crafting;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedcore.crafting.CustomShapelessRecipe;
import net.p3pp3rf1y.sophisticatedcore.crafting.IWrapperRecipe;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedstorage.entity.MovingStorageWrapper;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class MovingStorageTierUpgradeShapelessRecipe extends CustomShapelessRecipe implements IWrapperRecipe<ShapelessRecipe> {
	public static final RecipeSerializer<MovingStorageTierUpgradeShapelessRecipe> SERIALIZER = new RecipeSerializer<>(
			RecordCodecBuilder.mapCodec(instance -> instance.group(
					ShapelessRecipe.SERIALIZER.codec().forGetter(MovingStorageTierUpgradeShapelessRecipe::getCompose),
					ItemStackTemplate.CODEC.fieldOf("upgraded_storage_result").forGetter(MovingStorageTierUpgradeShapelessRecipe::getUpgradedStorageResult)
			).apply(instance, MovingStorageTierUpgradeShapelessRecipe::new)),
			StreamCodec.composite(
					ShapelessRecipe.SERIALIZER.streamCodec(), MovingStorageTierUpgradeShapelessRecipe::getCompose,
					ItemStackTemplate.STREAM_CODEC, MovingStorageTierUpgradeShapelessRecipe::getUpgradedStorageResult,
					MovingStorageTierUpgradeShapelessRecipe::new
			)
	);
	private final ShapelessRecipe compose;
	private final ItemStackTemplate upgradedStorageResult;

	public MovingStorageTierUpgradeShapelessRecipe(ShapelessRecipe compose, ItemStackTemplate upgradedStorageResult) {
		super(compose.group(), compose.category(), compose.result, compose.ingredients);
		this.compose = compose;
		this.upgradedStorageResult = upgradedStorageResult;
	}

	public static Function<ShapelessRecipe, MovingStorageTierUpgradeShapelessRecipe> wrapper(ItemStackTemplate upgradedStorageResult) {
		return compose -> new MovingStorageTierUpgradeShapelessRecipe(compose, upgradedStorageResult);
	}

	@Override
	public ShapelessRecipe getCompose() {
		return compose;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		return super.matches(input, level) && getOriginalMovingStorage(input).isPresent();
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		ItemStack upgradedMovingStorage = super.assemble(input);
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
	public RecipeSerializer<MovingStorageTierUpgradeShapelessRecipe> getSerializer() {
		return ModItems.MOVING_STORAGE_TIER_UPGRADE_SHAPELESS_RECIPE_SERIALIZER.get();
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
