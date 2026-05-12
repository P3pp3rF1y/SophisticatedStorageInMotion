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
import net.p3pp3rf1y.sophisticatedstorageinmotion.crafting.MovingStorageFromStorageRecipe;

import java.util.*;
import java.util.function.Function;

public class AssembleRecipesMaker {
	private AssembleRecipesMaker() {
	}

	public static <T extends PropertyBasedSubtypeInterpreter> List<MovingStorageTierUpgradeDisplayRecipe> getGroupedShapelessCraftingRecipes(Function<ItemStack, Optional<T>> getSubtypeInterpreter) {
		return ClientRecipeHelper.transformAllRecipeHoldersOfTypeIntoMultiple(RecipeType.CRAFTING, MovingStorageFromStorageRecipe.class, recipeHolder -> {
			MovingStorageTierUpgradeDisplayRecipe displayRecipe = createDisplayRecipe(recipeHolder, getSubtypeInterpreter);
			return List.of(displayRecipe);
		});
	}

	private static <T extends MovingStorageFromStorageRecipe, U extends PropertyBasedSubtypeInterpreter> MovingStorageTierUpgradeDisplayRecipe createDisplayRecipe(RecipeHolder<T> recipeHolder,
			Function<ItemStack, Optional<U>> getSubtypeInterpreter) {
		T recipe = recipeHolder.value();
		int storageIngredientIndex = -1;

		NonNullList<Ingredient> ingredients = recipe.getIngredients();
		CraftingContainer craftingInventory = createCraftingInventory();

		NonNullList<Ingredient> ingredientsTemplate = NonNullList.createWithCapacity(ingredients.size());
		List<ItemStack> storageItems = new ArrayList<>();
		int i = 0;
		for (Ingredient ingredient : ingredients) {
			if (ingredient.getValues().length > 0 && ingredient.getValues()[0] instanceof Ingredient.ItemValue itemValue && itemValue.item().getItem() instanceof StorageBlockItem) {
				storageItems = expandStorageItems(ingredient.getItems());
				storageIngredientIndex = i;
				ingredientsTemplate.add(i, Ingredient.EMPTY);
			} else {
				ingredientsTemplate.add(i, ingredient);
				if (!ingredient.isEmpty()) {
					ItemStack[] ingredientItems = ingredient.getItems();
					craftingInventory.setItem(i, ingredientItems[0]);
				}
			}
			i++;
		}

		Map<String, MovingStorageTierUpgradeVariantPair> variantPairs = new LinkedHashMap<>();
		for (ItemStack storageItem : storageItems) {
			NonNullList<Ingredient> ingredientsCopy = NonNullList.createWithCapacity(ingredientsTemplate.size());
			ingredientsCopy.addAll(ingredientsTemplate);
			ingredientsCopy.set(storageIngredientIndex, Ingredient.of(storageItem));
			craftingInventory.setItem(storageIngredientIndex, storageItem.copy());

			ItemStack result = ClientRecipeHelper.assemble(recipe, craftingInventory.asCraftInput());
			MovingStorageTierUpgradeVariantPair pair = new MovingStorageTierUpgradeVariantPair(storageItem.copy(), result.copy());
			variantPairs.putIfAbsent(getPairKey(pair, getSubtypeInterpreter), pair);
		}

		ResourceLocation id = recipeHolder.id().withPath(path -> "assemble_moving_storage_grouped/" + path);
		RecipeHolder<CraftingRecipe> displayRecipeHolder = new RecipeHolder<>(recipeHolder.id(), new ShapelessRecipe("", CraftingBookCategory.MISC, ClientRecipeHelper.getResultItem(recipe), ingredientsTemplate));
		return new MovingStorageTierUpgradeDisplayRecipe(id, displayRecipeHolder, true, 0, 0, ingredientsTemplate, storageIngredientIndex, List.copyOf(variantPairs.values()));
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

	private static <U extends PropertyBasedSubtypeInterpreter> String getPairKey(MovingStorageTierUpgradeVariantPair pair, Function<ItemStack, Optional<U>> getSubtypeInterpreter) {
		return getSubtypeInterpreter.apply(pair.result()).map(interpreter -> interpreter.getRegistrySanitizedItemString(pair.result())).orElse(pair.result().toString());
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
				addTintVariants(storageItems, storageBlockItem);
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
