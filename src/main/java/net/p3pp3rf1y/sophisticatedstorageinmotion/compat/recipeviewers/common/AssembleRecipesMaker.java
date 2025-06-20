package net.p3pp3rf1y.sophisticatedstorageinmotion.compat.recipeviewers.common;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.common.ClientRecipeHelper;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.common.IRecipeDisplayGenerator;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.common.subtypes.PropertyBasedSubtypeInterpreter;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.crafting.MovingStorageFromStorageRecipe;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class AssembleRecipesMaker {
	private AssembleRecipesMaker() {
	}

	public static void addRecipes(IRecipeDisplayGenerator<?> generator, Function<ItemStack, Optional<PropertyBasedSubtypeInterpreter>> getSubtypeInterpreter) {
		ClientRecipeHelper.addVariantRecipes(generator, MovingStorageFromStorageRecipe.class, AssembleRecipesMaker::getStorageItems, getSubtypeInterpreter, SophisticatedStorageInMotion.MOD_ID, "assemble_moving_storage_");
	}
	
	private static List<ItemStack> getStorageItems(Recipe<?> recipe) {
		return ClientRecipeHelper.getIngredientCreativeTabVariants(recipe, StorageBlockItem.class);
	}
}
