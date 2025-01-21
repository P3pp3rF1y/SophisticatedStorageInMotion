package net.p3pp3rf1y.sophisticatedstorageinmotion.compat.jei;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.p3pp3rf1y.sophisticatedcore.compat.jei.ClientRecipeHelper;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.crafting.MovingStorageTierUpgradeShapedRecipe;
import net.p3pp3rf1y.sophisticatedstorageinmotion.crafting.MovingStorageTierUpgradeShapelessRecipe;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MovingStorageTierUpgradeRecipesMaker {
	private MovingStorageTierUpgradeRecipesMaker() {}

	public static List<RecipeHolder<CraftingRecipe>> getShapedCraftingRecipes() {
		RecipeConstructor<MovingStorageTierUpgradeShapedRecipe> constructRecipe = (originalRecipe, ingredients, result) -> {
			ShapedRecipePattern pattern = new ShapedRecipePattern(originalRecipe.getWidth(), originalRecipe.getHeight(), ingredients, Optional.empty());
			return new ShapedRecipe("", CraftingBookCategory.MISC, pattern, result);
		};
		return getCraftingRecipes(constructRecipe, MovingStorageTierUpgradeShapedRecipe.class);
	}

	public static List<RecipeHolder<CraftingRecipe>> getShapelessCraftingRecipes() {
		RecipeConstructor<MovingStorageTierUpgradeShapelessRecipe> constructRecipe = (originalRecipe, ingredients, result) -> new ShapelessRecipe("", CraftingBookCategory.MISC, result, ingredients);
		return getCraftingRecipes(constructRecipe, MovingStorageTierUpgradeShapelessRecipe.class);
	}

	@NotNull
	private static <T extends CraftingRecipe> List<RecipeHolder<CraftingRecipe>> getCraftingRecipes(RecipeConstructor<T> constructRecipe, Class<T> originalRecipeClass) {
		return ClientRecipeHelper.transformAllRecipesOfTypeIntoMultiple(RecipeType.CRAFTING, originalRecipeClass, recipe -> {
			List<RecipeHolder<CraftingRecipe>> itemGroupRecipes = new ArrayList<>();
			getStorageItems(recipe).forEach(storageItem -> {
				NonNullList<Ingredient> ingredients = recipe.getIngredients();
				CraftingContainer craftinginventory = new TransientCraftingContainer(new AbstractContainerMenu(null, -1) {
					@Override
					public ItemStack quickMoveStack(Player player, int index) {
						return ItemStack.EMPTY;
					}

					public boolean stillValid(Player playerIn) {
						return false;
					}
				}, 3, 3);
				NonNullList<Ingredient> ingredientsCopy = NonNullList.createWithCapacity(ingredients.size());
				List<ItemStack> baseMovingStorageItems = Collections.emptyList();
				int movingStorageIngredientIndex = -1;
				int i = 0;
				for (Ingredient ingredient : ingredients) {
					ItemStack[] ingredientItems = ingredient.getItems();
					if (ingredientItems.length > 0 && ingredientItems[0].getItem() instanceof MovingStorageItem movingStorageItem) {
						baseMovingStorageItems = movingStorageItem.getBaseMovingStorageItems();
						movingStorageIngredientIndex = i;
					} else {
						if (!ingredient.isEmpty()) {
							craftinginventory.setItem(i, ingredientItems[0]);
						}
					}
					ingredientsCopy.add(i, ingredient);
					i++;
				}

				for (ItemStack movingStorage : baseMovingStorageItems) {
					itemGroupRecipes.add(createMovingStorageTierUpgradeRecipe(constructRecipe, recipe, storageItem, movingStorage, ingredientsCopy, movingStorageIngredientIndex, craftinginventory));
				}


			});
			return itemGroupRecipes;
		});
	}

	private static <T extends CraftingRecipe> RecipeHolder<CraftingRecipe> createMovingStorageTierUpgradeRecipe(RecipeConstructor<T> constructRecipe, T recipe, ItemStack storageItem, ItemStack movingStorage, NonNullList<Ingredient> ingredients, int movingStorageIngredientIndex, CraftingContainer craftinginventory) {
		MovingStorageItem.setStorageItem(movingStorage, storageItem);
		NonNullList<Ingredient> ingredientsCopy = NonNullList.createWithCapacity(ingredients.size());
		ingredientsCopy.addAll(ingredients);
		ingredientsCopy.set(movingStorageIngredientIndex, Ingredient.of(movingStorage));
		craftinginventory.setItem(movingStorageIngredientIndex, movingStorage.copy());
		ItemStack result = ClientRecipeHelper.assemble(recipe, craftinginventory.asCraftInput());
		ResourceLocation id = ResourceLocation.fromNamespaceAndPath(SophisticatedStorageInMotion.MOD_ID, "tier_upgrade_" + BuiltInRegistries.ITEM.getKey(storageItem.getItem()).getPath() + result.getComponentsPatch().toString().toLowerCase(Locale.ROOT).replaceAll("[{\",}:>=@\\[\\]\\s]", "_"));
		return new RecipeHolder<>(id, constructRecipe.construct(recipe, ingredientsCopy, result));
	}

	private static List<ItemStack> getStorageItems(CraftingRecipe recipe) {
		NonNullList<ItemStack> storageItems = NonNullList.create();
		for (Ingredient ingredient : recipe.getIngredients()) {
			ItemStack[] ingredientItems = ingredient.getItems();
			for (ItemStack ingredientItem : ingredientItems) {
				Item item = ingredientItem.getItem();
				if (item instanceof MovingStorageItem && MovingStorageItem.getStorageItem(ingredientItem).getItem() instanceof StorageBlockItem) {
					storageItems.add(MovingStorageItem.getStorageItem(ingredientItem));
				}
			}
		}

		return storageItems;
	}

	private interface RecipeConstructor<T extends Recipe<?>> {
		CraftingRecipe construct(T originalRecipe, NonNullList<Ingredient> ingredients, ItemStack result);
	}
}
