package net.p3pp3rf1y.sophisticatedstorageinmotion.compat.recipeviewers.rei;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.forge.REIPluginClient;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.Item;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.common.subtypes.PropertyBasedSubtypeInterpreter;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.rei.ReiCraftingContainerTransferHandler;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.rei.ReiSettingsGhostIngredientHandler;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.rei.ReiStorageGhostIngredientHandler;
import net.p3pp3rf1y.sophisticatedstorage.compat.recipeviewers.common.subtypes.SubtypeInterpreters;
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
@REIPluginClient
public class StorageInMotionReiClientPlugin implements REIClientPlugin {
	@Override
	public void registerExclusionZones(ExclusionZones zones) {
		zones.register(MovingStorageScreen.class, screen -> {
			List<Rect2i> ret = new ArrayList<>();
			screen.getUpgradeSlotsRectangle().ifPresent(ret::add);
			ret.addAll(screen.getUpgradeSettingsControl().getTabRectangles());
			screen.getSortButtonsRectangle().ifPresent(ret::add);
			ret.add(screen.getHorseControlRectangle());
			return ret.stream().map(r -> new Rectangle(r.getX(), r.getY(), r.getWidth(), r.getHeight())).toList();
		});

		zones.register(MovingStorageSettingsScreen.class, screen -> screen.getSettingsTabControl().getTabRectangles().stream().map(r -> new Rectangle(r.getX(), r.getY(), r.getWidth(), r.getHeight())).toList());
	}

	@Override
	public void registerScreens(ScreenRegistry registry) {
		registry.registerDraggableStackVisitor(new ReiStorageGhostIngredientHandler<>(MovingStorageScreen.class));
		registry.registerDraggableStackVisitor(new ReiSettingsGhostIngredientHandler<>(MovingStorageSettingsScreen.class));
	}

	@Override
	public void registerDisplays(DisplayRegistry registry) {
		Map<Item, PropertyBasedSubtypeInterpreter> subtypeInterpreters = getSubtypeInterpreters();
		// Add Storage subtype interpreters as well
		subtypeInterpreters.putAll(SubtypeInterpreters.getSubtypeInterpreters());

		AssembleRecipesMaker.getShapelessCraftingRecipes(stack -> getSubtypeInterpreter(subtypeInterpreters, stack)).forEach(registry::add);
		MovingStorageTierUpgradeRecipesMaker.getShapedCraftingRecipes(stack -> getSubtypeInterpreter(subtypeInterpreters, stack)).forEach(registry::add);
		MovingStorageTierUpgradeRecipesMaker.getShapelessCraftingRecipes(stack -> getSubtypeInterpreter(subtypeInterpreters, stack)).forEach(registry::add);
	}

	@Override
	public void registerTransferHandlers(TransferHandlerRegistry registry) {
		registry.register(ReiCraftingContainerTransferHandler.crafting(MovingStorageContainerMenu.class));
	}
}
