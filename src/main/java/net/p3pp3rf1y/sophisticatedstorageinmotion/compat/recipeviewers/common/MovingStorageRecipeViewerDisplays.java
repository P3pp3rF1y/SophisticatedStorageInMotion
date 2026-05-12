package net.p3pp3rf1y.sophisticatedstorageinmotion.compat.recipeviewers.common;

import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.common.IRecipeViewerDisplayCatalog;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.common.IRecipeViewerDisplayContext;

public class MovingStorageRecipeViewerDisplays {
	private MovingStorageRecipeViewerDisplays() {
	}

	public static void register(IRecipeViewerDisplayCatalog catalog, IRecipeViewerDisplayContext context) {
		AssembleRecipesMaker.getGroupedShapelessCraftingRecipes(context::getSubtypeInterpreter).stream()
				.map(MovingStorageTierUpgradeDisplayRecipe::toAssemblySpec)
				.forEach(catalog::addCraftingSpec);
		MovingStorageTierUpgradeRecipesMaker.getGroupedShapedCraftingRecipes(context::getSubtypeInterpreter).stream()
				.map(recipe -> recipe.toTierUpgradeSpec(context::getSubtypeInterpreter))
				.forEach(catalog::addCraftingSpec);
		MovingStorageTierUpgradeRecipesMaker.getGroupedShapelessCraftingRecipes(context::getSubtypeInterpreter).stream()
				.map(recipe -> recipe.toTierUpgradeSpec(context::getSubtypeInterpreter))
				.forEach(catalog::addCraftingSpec);
	}
}
