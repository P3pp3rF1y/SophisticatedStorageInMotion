package net.p3pp3rf1y.sophisticatedstorageinmotion.compat.recipeviewers.common;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.common.ClientRecipeHelper;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.common.IRecipeDisplayGenerator;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.common.subtypes.PropertyBasedSubtypeInterpreter;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.crafting.MovingStorageIngredient;
import net.p3pp3rf1y.sophisticatedstorageinmotion.crafting.MovingStorageTierUpgradeShapedRecipe;
import net.p3pp3rf1y.sophisticatedstorageinmotion.crafting.MovingStorageTierUpgradeShapelessRecipe;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class MovingStorageTierUpgradeRecipesMaker {
	private MovingStorageTierUpgradeRecipesMaker() {
	}

	public static void addRecipes(IRecipeDisplayGenerator<?> generator, Function<ItemStack, Optional<PropertyBasedSubtypeInterpreter>> subtypeInterpreterGetter) {
		ClientRecipeHelper.addVariantRecipes(generator, MovingStorageTierUpgradeShapedRecipe.class, MovingStorageTierUpgradeRecipesMaker::getMovingStorages, subtypeInterpreterGetter, SophisticatedStorageInMotion.MOD_ID, "tier_upgrade_");
		ClientRecipeHelper.addVariantRecipes(generator, MovingStorageTierUpgradeShapelessRecipe.class, MovingStorageTierUpgradeRecipesMaker::getMovingStorages, subtypeInterpreterGetter, SophisticatedStorageInMotion.MOD_ID, "tier_upgrade_");
	}

	private static List<ItemStack> getMovingStorages(CraftingRecipe recipe) {
		return ClientRecipeHelper.getCustomIngredientVariants(recipe, MovingStorageIngredient.class);
	}
}
