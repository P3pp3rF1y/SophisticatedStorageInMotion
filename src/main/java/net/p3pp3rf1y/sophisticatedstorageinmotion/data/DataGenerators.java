package net.p3pp3rf1y.sophisticatedstorageinmotion.data;

import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.data.event.GatherDataRegistryEntriesEvent;

public class DataGenerators {
	private DataGenerators() {
	}

	public static void gatherRegistryEntries(GatherDataRegistryEntriesEvent evt) {
		evt.recipe(StorageInMotionRecipeProvider::new);
	}

	public static void gatherData(GatherDataEvent.Client evt) {
		evt.createProvider(StorageInMotionModelProvider::new);
	}
}
