package net.p3pp3rf1y.sophisticatedstorageinmotion.data;

import net.neoforged.neoforge.data.event.GatherDataEvent;

public class DataGenerators {
	private DataGenerators() {}

	public static void gatherData(GatherDataEvent.Client evt) {
		evt.createProvider(StorageInMotionRecipeProvider.Runner::new);
		evt.createProvider(StorageInMotionModelProvider::new);
	}
}
