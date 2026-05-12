package net.p3pp3rf1y.sophisticatedstorageinmotion.compat.recipeviewers.common;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.common.ClientRecipeHelper;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.common.subtypes.PropertyBasedSubtypeInterpreter;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.crafting.MovingStorageTierUpgradeShapedRecipe;
import net.p3pp3rf1y.sophisticatedstorageinmotion.crafting.MovingStorageTierUpgradeShapelessRecipe;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class MovingStorageTierUpgradeRecipesMaker {
	private MovingStorageTierUpgradeRecipesMaker() {
	}

	public static <T extends PropertyBasedSubtypeInterpreter> List<MovingStorageTierUpgradeDisplayRecipe> getGroupedShapedCraftingRecipes(Function<ItemStack, Optional<T>> getSubtypeInterpreter) {
		return getGroupedCraftingRecipes(MovingStorageTierUpgradeShapedRecipe.class, getSubtypeInterpreter, false);
	}

	public static <T extends PropertyBasedSubtypeInterpreter> List<MovingStorageTierUpgradeDisplayRecipe> getGroupedShapelessCraftingRecipes(Function<ItemStack, Optional<T>> getSubtypeInterpreter) {
		return getGroupedCraftingRecipes(MovingStorageTierUpgradeShapelessRecipe.class, getSubtypeInterpreter, true);
	}

	private static <T extends CraftingRecipe, U extends PropertyBasedSubtypeInterpreter> List<MovingStorageTierUpgradeDisplayRecipe> getGroupedCraftingRecipes(Class<T> originalRecipeClass,
			Function<ItemStack, Optional<U>> getSubtypeInterpreter,
			boolean shapeless) {
		return ClientRecipeHelper.transformAllRecipeHoldersOfTypeIntoMultiple(RecipeType.CRAFTING, originalRecipeClass, recipeHolder -> createDisplayRecipes(recipeHolder, getSubtypeInterpreter, shapeless).stream()
				.toList());
	}

	private static <T extends CraftingRecipe, U extends PropertyBasedSubtypeInterpreter> List<MovingStorageTierUpgradeDisplayRecipe> createDisplayRecipes(RecipeHolder<T> recipeHolder,
			Function<ItemStack, Optional<U>> getSubtypeInterpreter,
			boolean shapeless) {
		T recipe = recipeHolder.value();
		CraftingContainer craftingInventory = createCraftingInventory();
		int movingStorageIngredientIndex = findMovingStorageIngredientIndex(recipe.getIngredients());
		NonNullList<Ingredient> ingredientsCopy = copyIngredients(recipe.getIngredients());
		List<MovingStorageTierUpgradeDisplayRecipe> displayRecipes = new ArrayList<>();
		for (ItemStack baseMovingStorage : getBaseMovingStorageItems(recipe.getIngredients(), movingStorageIngredientIndex)) {
			Map<String, MovingStorageTierUpgradeVariantPair> variantPairs = new LinkedHashMap<>();
			for (ItemStack storageItem : getStorageItems(recipe)) {
				ItemStack sourceMovingStorage = baseMovingStorage.copy();
				MovingStorageItem.setStorageItem(sourceMovingStorage, storageItem);
				populateCraftingInventory(recipe.getIngredients(), craftingInventory, movingStorageIngredientIndex, sourceMovingStorage);
				ItemStack result = ClientRecipeHelper.assemble(recipe, craftingInventory.asCraftInput());
				MovingStorageTierUpgradeVariantPair pair = new MovingStorageTierUpgradeVariantPair(sourceMovingStorage.copy(), result.copy());
				variantPairs.putIfAbsent(getPairKey(pair, getSubtypeInterpreter), pair);
			}
			ResourceLocation id = recipeHolder.id().withPath(path -> "tier_upgrade_grouped/" + path + getMovingStorageGroupSuffix(baseMovingStorage, getSubtypeInterpreter));
			int width = recipe instanceof ShapedRecipe shapedRecipe ? shapedRecipe.getWidth() : 0;
			int height = recipe instanceof ShapedRecipe shapedRecipe ? shapedRecipe.getHeight() : 0;
			RecipeHolder<CraftingRecipe> displayRecipeHolder = new RecipeHolder<>(recipeHolder.id(), shapeless ? new ShapelessRecipe("", CraftingBookCategory.MISC, ClientRecipeHelper.getResultItem(recipe), ingredientsCopy)
					: new ShapedRecipe("", CraftingBookCategory.MISC, new ShapedRecipePattern(width, height, ingredientsCopy, Optional.empty()), ClientRecipeHelper.getResultItem(recipe)));
			displayRecipes.add(new MovingStorageTierUpgradeDisplayRecipe(id, displayRecipeHolder, shapeless, width, height, ingredientsCopy, movingStorageIngredientIndex, List.copyOf(variantPairs.values())));
		}
		return displayRecipes;
	}

	private static CraftingContainer createCraftingInventory() {
		return new TransientCraftingContainer(new AbstractContainerMenu(null, -1) {
			@Override
			public ItemStack quickMoveStack(Player player, int index) {
				return ItemStack.EMPTY;
			}

			public boolean stillValid(Player playerIn) {
				return false;
			}
		}, 3, 3);
	}

	private static NonNullList<Ingredient> copyIngredients(NonNullList<Ingredient> ingredients) {
		NonNullList<Ingredient> ingredientsCopy = NonNullList.createWithCapacity(ingredients.size());
		ingredientsCopy.addAll(ingredients);
		return ingredientsCopy;
	}

	private static int findMovingStorageIngredientIndex(NonNullList<Ingredient> ingredients) {
		for (int i = 0; i < ingredients.size(); i++) {
			ItemStack[] ingredientItems = ingredients.get(i).getItems();
			if (ingredientItems.length > 0 && ingredientItems[0].getItem() instanceof MovingStorageItem) {
				return i;
			}
		}
		throw new IllegalStateException("Moving storage tier upgrade recipe missing moving storage ingredient");
	}

	private static List<ItemStack> getBaseMovingStorageItems(NonNullList<Ingredient> ingredients, int movingStorageIngredientIndex) {
		ItemStack[] ingredientItems = ingredients.get(movingStorageIngredientIndex).getItems();
		if (ingredientItems.length == 0 || !(ingredientItems[0].getItem() instanceof MovingStorageItem movingStorageItem)) {
			return List.of();
		}
		return movingStorageItem.getBaseMovingStorageItems();
	}

	private static void populateCraftingInventory(NonNullList<Ingredient> ingredients, CraftingContainer craftingInventory, int movingStorageIngredientIndex, ItemStack movingStorage) {
		for (int i = 0; i < ingredients.size(); i++) {
			if (i == movingStorageIngredientIndex) {
				craftingInventory.setItem(i, movingStorage.copy());
				continue;
			}
			Ingredient ingredient = ingredients.get(i);
			ItemStack[] ingredientItems = ingredient.getItems();
			craftingInventory.setItem(i, ingredient.isEmpty() ? ItemStack.EMPTY : ingredientItems[0]);
		}
	}

	private static <U extends PropertyBasedSubtypeInterpreter> String getPairKey(MovingStorageTierUpgradeVariantPair pair, Function<ItemStack, Optional<U>> getSubtypeInterpreter) {
		return getSubtypeInterpreter.apply(pair.source()).map(interpreter -> interpreter.getRegistrySanitizedItemString(pair.source())).orElse(pair.source().toString())
				+ "->"
				+ getSubtypeInterpreter.apply(pair.result()).map(interpreter -> interpreter.getRegistrySanitizedItemString(pair.result())).orElse(pair.result().toString());
	}

	private static <U extends PropertyBasedSubtypeInterpreter> String getMovingStorageGroupSuffix(ItemStack baseMovingStorage, Function<ItemStack, Optional<U>> getSubtypeInterpreter) {
		return getSubtypeInterpreter.apply(baseMovingStorage)
				.map(interpreter -> "/" + interpreter.getRegistrySanitizedItemString(baseMovingStorage))
				.orElse("");
	}

	private static List<ItemStack> getStorageItems(CraftingRecipe recipe) {
		NonNullList<ItemStack> storageItems = NonNullList.create();
		Set<Item> alreadyExpanded = new HashSet<>();
		for (Ingredient ingredient : recipe.getIngredients()) {
			ItemStack[] ingredientItems = ingredient.getItems();
			for (ItemStack ingredientItem : ingredientItems) {
				Item item = ingredientItem.getItem();
				ItemStack storageItem = item instanceof MovingStorageItem ? MovingStorageItem.getStorageItem(ingredientItem) : ItemStack.EMPTY;
				if (storageItem.getItem() instanceof StorageBlockItem storageBlockItem && alreadyExpanded.add(storageItem.getItem())) {
					storageItems.add(new ItemStack(storageBlockItem));
					storageBlockItem.addCreativeTabItems(storageItems::add);
					addTintVariants(storageItems, storageBlockItem);
				}
			}
		}

		return storageItems;
	}

	private static void addTintVariants(List<ItemStack> storageItems, StorageBlockItem storageBlockItem) {
		for (DyeColor color : DyeColor.values()) {
			ItemStack storageStack = new ItemStack(storageBlockItem);
			storageBlockItem.setMainColor(storageStack, color.getTextureDiffuseColor());
			storageBlockItem.setAccentColor(storageStack, color.getTextureDiffuseColor());
			storageItems.add(storageStack);
		}
		ItemStack storageStack = new ItemStack(storageBlockItem);
		storageBlockItem.setMainColor(storageStack, DyeColor.YELLOW.getTextureDiffuseColor());
		storageBlockItem.setAccentColor(storageStack, DyeColor.LIME.getTextureDiffuseColor());
		storageItems.add(storageStack);
	}

}
