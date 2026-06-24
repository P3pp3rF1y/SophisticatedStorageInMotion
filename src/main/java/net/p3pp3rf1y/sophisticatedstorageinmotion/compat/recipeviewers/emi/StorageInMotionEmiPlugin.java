package net.p3pp3rf1y.sophisticatedstorageinmotion.compat.recipeviewers.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.widget.Bounds;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.Item;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.common.IRecipeViewerDisplayCatalog;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.common.IRecipeViewerDisplayContext;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.common.RecipeViewerDisplayCatalog;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.common.subtypes.PropertyBasedSubtypeInterpreter;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.emi.CraftingSpecEmiRecipe;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.emi.EmiGridMenuInfo;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.emi.EmiSettingsGhostDragDropHandler;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.emi.EmiStorageGhostDragDropHandler;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.emi.comparison.EmiSubtypeInterpreter;
import net.p3pp3rf1y.sophisticatedstorage.compat.recipeviewers.common.subtypes.SubtypeInterpreters;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.gui.MovingStorageScreen;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.gui.MovingStorageSettingsScreen;
import net.p3pp3rf1y.sophisticatedstorageinmotion.compat.recipeviewers.common.MovingStorageRecipeViewerDisplays;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModEntities;

import java.util.Map;
import java.util.Optional;

import static net.p3pp3rf1y.sophisticatedstorageinmotion.compat.recipeviewers.common.subtypes.SubtypeInterpreters.getSubtypeInterpreters;

@SuppressWarnings("unused")
@EmiEntrypoint
public class StorageInMotionEmiPlugin implements EmiPlugin {
	@Override
	public void register(EmiRegistry registry) {
		registerGuiHandlers(registry);
		registerRecipes(registry);
		registerDefaultComparisons(registry);
		registerRecipeHandlers(registry);
	}

	private void registerDefaultComparisons(EmiRegistry registry) {
		getSubtypeInterpreters().forEach((item, comparator) -> registry.setDefaultComparison(item, EmiSubtypeInterpreter.of(comparator)));
	}

	private void registerGuiHandlers(EmiRegistry registry) {
		registry.addExclusionArea(MovingStorageScreen.class, (screen, consumer) -> {
			// noinspection ConstantValue
			if (screen == null || screen.getUpgradeSettingsControl() == null) {
				return;
			}
			screen.getUpgradeSlotsRectangle().ifPresent(r -> consumer.accept(new Bounds(r.getX(), r.getY(), r.getWidth(), r.getHeight())));
			screen.getUpgradeSettingsControl().getTabRectangles().forEach(r -> consumer.accept(new Bounds(r.getX(), r.getY(), r.getWidth(), r.getHeight())));
			screen.getSortButtonsRectangle().ifPresent(r -> consumer.accept(new Bounds(r.getX(), r.getY(), r.getWidth(), r.getHeight())));
			Rect2i horseControl = screen.getHorseControlRectangle();
			consumer.accept(new Bounds(horseControl.getX(), horseControl.getY(), horseControl.getWidth(), horseControl.getHeight()));
		});
		registry.addExclusionArea(MovingStorageSettingsScreen.class, (screen, consumer) -> {
			if (screen == null) { // Due to how Emi collects the exclusion area this can be null
				return;
			}
			screen.getExtendedControlsRectangles().forEach(r -> consumer.accept(new Bounds(r.getX(), r.getY(), r.getWidth(), r.getHeight())));
		});

		registry.addDragDropHandler(MovingStorageScreen.class, new EmiStorageGhostDragDropHandler<>());
		registry.addDragDropHandler(MovingStorageSettingsScreen.class, new EmiSettingsGhostDragDropHandler<>());
	}

	public void registerRecipes(EmiRegistry registry) {
		Map<Item, PropertyBasedSubtypeInterpreter> subtypeInterpreters = getSubtypeInterpreters();
		// Add Storage subtype interpreters as well
		subtypeInterpreters.putAll(SubtypeInterpreters.getSubtypeInterpreters());

		IRecipeViewerDisplayCatalog catalog = createCatalog(subtypeInterpreters);
		registry.removeRecipes(recipe -> recipe.getBackingRecipe() != null && catalog.replacesCraftingRecipe(recipe.getBackingRecipe()));
		catalog.getCraftingSpecs().stream().flatMap(spec -> CraftingSpecEmiRecipe.ofGroupedUsageAndFocusedRecipes(spec).stream()).forEach(registry::addRecipe);
	}

	private static IRecipeViewerDisplayCatalog createCatalog(Map<Item, PropertyBasedSubtypeInterpreter> subtypeInterpreters) {
		IRecipeViewerDisplayCatalog catalog = new RecipeViewerDisplayCatalog();
		IRecipeViewerDisplayContext context = stack -> Optional.ofNullable(subtypeInterpreters.get(stack.getItem()));
		MovingStorageRecipeViewerDisplays.register(catalog, context);
		return catalog;
	}

	private void registerRecipeHandlers(EmiRegistry registry) {
		registry.addRecipeHandler(ModEntities.MOVING_STORAGE_CONTAINER_TYPE.get(), EmiGridMenuInfo.crafting());
	}
}
