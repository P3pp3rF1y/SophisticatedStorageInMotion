package net.p3pp3rf1y.sophisticatedstorageinmotion.compat.recipeviewers.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.*;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.common.IRecipeViewerDisplayCatalog;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.common.IRecipeViewerDisplayContext;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.common.RecipeViewerDisplayCatalog;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.common.subtypes.PropertyBasedSubtypeInterpreter;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.jei.*;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.jei.subtypes.JeiSubtypeInterpreter;
import net.p3pp3rf1y.sophisticatedcore.util.RecipeHelper;
import net.p3pp3rf1y.sophisticatedstorage.compat.recipeviewers.common.subtypes.SubtypeInterpreters;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.gui.MovingStorageScreen;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.gui.MovingStorageSettingsScreen;
import net.p3pp3rf1y.sophisticatedstorageinmotion.common.gui.MovingStorageContainerMenu;
import net.p3pp3rf1y.sophisticatedstorageinmotion.compat.recipeviewers.common.MovingStorageRecipeViewerDisplays;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static net.p3pp3rf1y.sophisticatedstorageinmotion.compat.recipeviewers.common.subtypes.SubtypeInterpreters.getSubtypeInterpreters;

@SuppressWarnings("unused")
@JeiPlugin
public class StorageInMotionJeiPlugin implements IModPlugin {
	private IRecipeViewerDisplayCatalog catalog = null;

	public StorageInMotionJeiPlugin() {
		RecipeHelper.addRecipeChangeListener(() -> catalog = null);
	}

	@Override
	public ResourceLocation getPluginUid() {
		return ResourceLocation.fromNamespaceAndPath(SophisticatedStorageInMotion.MOD_ID, "default");
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistration registration) {
		getSubtypeInterpreters().forEach((item, subtypeInterpreter) -> registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, item, JeiSubtypeInterpreter.of(subtypeInterpreter)));
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

	private IRecipeViewerDisplayCatalog getCatalog() {
		if (catalog == null) {
			Map<Item, PropertyBasedSubtypeInterpreter> subtypeInterpreters = getSubtypeInterpreters();
			// Add Storage subtype interpreters as well
			subtypeInterpreters.putAll(SubtypeInterpreters.getSubtypeInterpreters());
			catalog = createCatalog(subtypeInterpreters);
		}
		return catalog;
	}

	private static IRecipeViewerDisplayCatalog createCatalog(Map<Item, PropertyBasedSubtypeInterpreter> subtypeInterpreters) {
		IRecipeViewerDisplayCatalog catalog = new RecipeViewerDisplayCatalog();
		IRecipeViewerDisplayContext context = stack -> Optional.ofNullable(subtypeInterpreters.get(stack.getItem()));
		MovingStorageRecipeViewerDisplays.register(catalog, context);
		return catalog;
	}

	@Override
	public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
		JeiCraftingSpecExtensionRegistrar.registerCraftingSpecExtensions(registration, this::getCatalog, stack -> true, List.of());
	}

	@Override
	public void registerAdvanced(IAdvancedRegistration registration) {
		registration.addRecipeManagerPlugin(new CraftingDisplayCatalogRecipeManagerPluginCompat(this::getCatalog,
				StorageInMotionJeiPlugin::canShowMovingStorageUsagesFor, StorageInMotionJeiPlugin::canShowMovingStorageRecipesFor));
	}

	private static boolean canShowMovingStorageUsagesFor(ItemStack stack) {
		return stack.getItem() instanceof StorageBlockItem || stack.getItem() instanceof MovingStorageItem;
	}

	private static boolean canShowMovingStorageRecipesFor(ItemStack stack) {
		return stack.getItem() instanceof MovingStorageItem;
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
