package net.p3pp3rf1y.sophisticatedstorageinmotion.compat.recipeviewers.common;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.common.ClientRecipeHelper;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.common.subtypes.PropertyBasedSubtypeInterpreter;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.crafting.MovingStorageFromStorageRecipe;

import java.util.*;
import java.util.function.Function;

public class AssembleRecipesMaker {
	private AssembleRecipesMaker() {
	}

	public static <T extends PropertyBasedSubtypeInterpreter> List<CraftingRecipe> getShapelessCraftingRecipes(Function<ItemStack, Optional<T>> getSubtypeInterpreter) {
		return getShapelessCraftingRecipes(getSubtypeInterpreter, r -> r);
	}

	public static <R, T extends PropertyBasedSubtypeInterpreter> List<R> getShapelessCraftingRecipes(Function<ItemStack, Optional<T>> getSubtypeInterpreter, Function<CraftingRecipe, R> transformRecipe) {
		RecipeConstructor<MovingStorageFromStorageRecipe> constructRecipe = (originalRecipe, ingredients, result) -> new ShapelessRecipe(originalRecipe.getId(), "", CraftingBookCategory.MISC, result, ingredients);
		return getCraftingRecipes(constructRecipe, MovingStorageFromStorageRecipe.class, getSubtypeInterpreter, transformRecipe);
	}

	private static <R, T extends CraftingRecipe, U extends PropertyBasedSubtypeInterpreter> List<R> getCraftingRecipes(RecipeConstructor<T> constructRecipe, Class<T> originalRecipeClass, Function<ItemStack, Optional<U>> getSubtypeInterpreter, Function<CraftingRecipe, R> transformRecipe) {
		return ClientRecipeHelper.transformAllRecipesOfTypeIntoMultiple(RecipeType.CRAFTING, originalRecipeClass, recipe -> {
			List<R> itemGroupRecipes = new ArrayList<>();

			int storageIngredientIndex = -1;

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

			NonNullList<Ingredient> ingredientsTemplate = NonNullList.createWithCapacity(ingredients.size());
			List<ItemStack> storageItems = new ArrayList<>();
			int i = 0;
			for (Ingredient ingredient : ingredients) {
				ItemStack[] ingredientItems = ingredient.getItems();
				if (ingredientItems.length > 0 && ingredientItems[0].getItem() instanceof StorageBlockItem) {
					storageItems = expandStorageItems(ingredient.getItems());
					storageIngredientIndex = i;
					ingredientsTemplate.add(i, Ingredient.EMPTY);
				} else {
					ingredientsTemplate.add(i, ingredient);
					if (!ingredient.isEmpty()) {
						craftinginventory.setItem(i, ingredientItems[0]);
					}
				}
				i++;
			}

			for (ItemStack storageItem : storageItems) {
				NonNullList<Ingredient> ingredientsCopy = NonNullList.createWithCapacity(ingredientsTemplate.size());
				ingredientsCopy.addAll(ingredientsTemplate);
				ingredientsCopy.set(storageIngredientIndex, Ingredient.of(storageItem));
				craftinginventory.setItem(storageIngredientIndex, storageItem.copy());

				ItemStack result = ClientRecipeHelper.assemble(recipe, craftinginventory);
				ResourceLocation id = new ResourceLocation(SophisticatedStorageInMotion.MOD_ID, "assemble_moving_storage_"
						+ getSubtypeInterpreter.apply(result).map(intepreter -> intepreter.getRegistrySanitizedItemString(result)).orElse("")
				);
				itemGroupRecipes.add(transformRecipe.apply(constructRecipe.construct(recipe, ingredientsCopy, result)));
			}

			return itemGroupRecipes;
		});
	}

	private static List<ItemStack> expandStorageItems(ItemStack[] items) {
		List<ItemStack> storageItems = new ArrayList<>();
		Set<Item> alreadyExpanded = new HashSet<>();

		for (ItemStack item : items) {
			if (!alreadyExpanded.add(item.getItem())) {
				continue;
			}

			if (item.getItem() instanceof StorageBlockItem storageBlockItem) {
				storageBlockItem.addCreativeTabItems(storageItems::add);
			}
		}

		return storageItems;
	}

	private interface RecipeConstructor<T extends Recipe<?>> {
		CraftingRecipe construct(T originalRecipe, NonNullList<Ingredient> ingredients, ItemStack result);
	}
}
