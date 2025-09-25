package net.p3pp3rf1y.sophisticatedstorageinmotion.init;

import net.p3pp3rf1y.sophisticatedcore.compat.CompatInfo;
import net.p3pp3rf1y.sophisticatedcore.compat.CompatModIds;
import net.p3pp3rf1y.sophisticatedcore.compat.CompatRegistry;
import net.p3pp3rf1y.sophisticatedstorageinmotion.compat.inventorytweaksrefoxed.InventoryTweaksCompat;

public class ModCompat {
	private ModCompat() {
	}

	public static void register() {
		CompatRegistry.registerCompat(new CompatInfo(CompatModIds.INVENTORY_TWEAKS), () -> mobBus -> new InventoryTweaksCompat());
	}
}
