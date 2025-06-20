package net.p3pp3rf1y.sophisticatedstorageinmotion.compat.recipeviewers.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.common.subtypes.PropertyBasedSubtypeInterpreter;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.jei.JeiCraftingContainerRecipeTransferHandlerBase;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.jei.JeiRecipeDisplayGenerator;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.jei.JeiSettingsGhostIngredientHandler;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.jei.JeiStorageGhostIngredientHandler;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.jei.subtypes.JeiSubtypeInterpreter;
import net.p3pp3rf1y.sophisticatedstorage.compat.recipeviewers.common.subtypes.SubtypeInterpreters;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.gui.MovingStorageScreen;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.gui.MovingStorageSettingsScreen;
import net.p3pp3rf1y.sophisticatedstorageinmotion.common.gui.MovingStorageContainerMenu;
import net.p3pp3rf1y.sophisticatedstorageinmotion.compat.recipeviewers.common.AssembleRecipesMaker;
import net.p3pp3rf1y.sophisticatedstorageinmotion.compat.recipeviewers.common.MovingStorageTierUpgradeRecipesMaker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.p3pp3rf1y.sophisticatedstorageinmotion.compat.recipeviewers.common.subtypes.SubtypeInterpreters.getSubtypeInterpreter;
import static net.p3pp3rf1y.sophisticatedstorageinmotion.compat.recipeviewers.common.subtypes.SubtypeInterpreters.getSubtypeInterpreters;

@SuppressWarnings("unused")
@JeiPlugin
public class StorageInMotionJeiPlugin implements IModPlugin {

	@Override
	public ResourceLocation getPluginUid() {
		return ResourceLocation.fromNamespaceAndPath(SophisticatedStorageInMotion.MOD_ID, "default");
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistration registration) {
		getSubtypeInterpreters().forEach((item, subtypeInterpreter) -> registration.registerSubtypeInterpreter(item, JeiSubtypeInterpreter.of(subtypeInterpreter)));
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		registration.addGuiContainerHandler(MovingStorageScreen.class, new IGuiContainerHandler<>() {
			@Override
			public List<Rect2i> getGuiExtraAreas(MovingStorageScreen gui) {
				List<Rect2i> ret = new ArrayList<>();
				gui.getUpgradeSlotsRectangle().ifPresent(ret::add);
				ret.addAll(gui.getUpgradeSettingsControl().getTabRectangles());
				gui.getSortButtonsRectangle().ifPresent(ret::add);
				ret.add(gui.getHorseControlRectangle());
				return ret;
			}
		});

		registration.addGuiContainerHandler(MovingStorageSettingsScreen.class, new IGuiContainerHandler<>() {
			@Override
			public List<Rect2i> getGuiExtraAreas(MovingStorageSettingsScreen gui) {
				return new ArrayList<>(gui.getExtendedControlsRectangles());
			}
		});

		registration.addGhostIngredientHandler(MovingStorageScreen.class, new JeiStorageGhostIngredientHandler<>());
		registration.addGhostIngredientHandler(MovingStorageSettingsScreen.class, new JeiSettingsGhostIngredientHandler<>());
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		Map<Item, PropertyBasedSubtypeInterpreter> subtypeInterpreters = getSubtypeInterpreters();
		// Add Storage subtype interpreters as well
		subtypeInterpreters.putAll(SubtypeInterpreters.getSubtypeInterpreters());

		JeiRecipeDisplayGenerator generator = new JeiRecipeDisplayGenerator();
		AssembleRecipesMaker.addRecipes(generator, stack -> getSubtypeInterpreter(subtypeInterpreters, stack));
		MovingStorageTierUpgradeRecipesMaker.addRecipes(generator, stack -> getSubtypeInterpreter(subtypeInterpreters, stack));

		registration.addRecipes(RecipeTypes.CRAFTING, generator.getCraftingRecipes());
	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
		IRecipeTransferHandlerHelper handlerHelper = registration.getTransferHelper();
		IStackHelper stackHelper = registration.getJeiHelpers().getStackHelper();
		registration.addRecipeTransferHandler(new JeiCraftingContainerRecipeTransferHandlerBase<MovingStorageContainerMenu<?>, RecipeHolder<CraftingRecipe>>(handlerHelper, stackHelper) {
			@Override
			public Class<MovingStorageContainerMenu<?>> getContainerClass() {
				//noinspection unchecked
				return (Class<MovingStorageContainerMenu<?>>) (Class<?>) MovingStorageContainerMenu.class;
			}

			@Override
			public IRecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
				return RecipeTypes.CRAFTING;
			}
		}, RecipeTypes.CRAFTING);
	}
}
