package net.p3pp3rf1y.sophisticatedstorageinmotion.init;

import net.minecraftforge.fml.ModList;
import net.p3pp3rf1y.sophisticatedcore.compat.CompatModIds;
import net.p3pp3rf1y.sophisticatedcore.compat.ICompat;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.compat.inventorytweaksrefoxed.InventoryTweaksCompat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

public class ModCompat {
	private ModCompat() {}

	private static final Map<String, Supplier<Callable<ICompat>>> compatFactories = new HashMap<>();
	private static final Map<String, ICompat> loadedCompats = new HashMap<>();

	static {
		compatFactories.put(CompatModIds.INVENTORY_TWEAKS, () -> InventoryTweaksCompat::new);
	}

	public static void compatsSetup() {
		loadedCompats.values().forEach(ICompat::setup);
	}

	public static void initCompats() {
		for (Map.Entry<String, Supplier<Callable<ICompat>>> entry : compatFactories.entrySet()) {
			if (ModList.get().isLoaded(entry.getKey())) {
				try {
					loadedCompats.put(entry.getKey(), entry.getValue().get().call());
				}
				catch (Exception e) {
					SophisticatedStorageInMotion.LOGGER.error("Error instantiating compatibility ", e);
				}
			}
		}

		loadedCompats.values().forEach(ICompat::init);
	}
}
