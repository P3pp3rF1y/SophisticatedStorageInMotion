package net.p3pp3rf1y.sophisticatedstorageinmotion.data;

import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.recipes.RecipeProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public class DataGenerators {
	private DataGenerators() {
	}

	public static void gatherData(GatherDataEvent.Client evt) {
		evt.createReloadableRegistryObjects(new RegistrySetBuilder().add(RecipeProvider.asBootstrap(StorageInMotionRecipeProvider::new)));
		evt.createProvider(StorageInMotionModelProvider::new);
	}
}
