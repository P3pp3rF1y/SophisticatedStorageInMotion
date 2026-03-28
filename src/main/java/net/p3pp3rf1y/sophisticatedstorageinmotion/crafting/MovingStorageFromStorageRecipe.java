package net.p3pp3rf1y.sophisticatedstorageinmotion.crafting;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedcore.crafting.CustomShapelessRecipe;
import net.p3pp3rf1y.sophisticatedcore.crafting.IWrapperRecipe;
import net.p3pp3rf1y.sophisticatedcore.crafting.RecipeWrapperSerializer;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockBase;
import net.p3pp3rf1y.sophisticatedstorage.item.WoodStorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;

import java.util.List;
import java.util.Optional;

public class MovingStorageFromStorageRecipe extends CustomShapelessRecipe implements IWrapperRecipe<ShapelessRecipe> {
	public static final RecipeSerializer<MovingStorageFromStorageRecipe> SERIALIZER = RecipeWrapperSerializer.create(MovingStorageFromStorageRecipe::new, ShapelessRecipe.SERIALIZER);
	private final ShapelessRecipe compose;

	public MovingStorageFromStorageRecipe(ShapelessRecipe compose) {
		super(compose.group(), compose.category(), compose.result, compose.ingredients);
		this.compose = compose;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		return super.matches(input, level) && getStorage(input).map(c -> !WoodStorageBlockItem.isPacked(c)).orElse(false);
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	private Optional<ItemStack> getStorage(CraftingInput inv) {
		for (int slot = 0; slot < inv.size(); slot++) {
			ItemStack slotStack = inv.getItem(slot);
			if (slotStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof StorageBlockBase) {
				return Optional.of(slotStack);
			}
		}
		return Optional.empty();
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		ItemStack movingStorageItem = super.assemble(input);
		getStorage(input).ifPresent(storage -> MovingStorageItem.setStorageItem(movingStorageItem, storage));
		return movingStorageItem;
	}

	@Override
	public ShapelessRecipe getCompose() {
		return compose;
	}

	@Override
	public RecipeSerializer<MovingStorageFromStorageRecipe> getSerializer() {
		return ModItems.MOVING_STORAGE_FROM_STORAGE_SERIALIZER.get();
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
		return compose.display();
	}

}
