package net.p3pp3rf1y.sophisticatedstorageinmotion.compat.trashslot;

import net.p3pp3rf1y.sophisticatedcore.compat.ICompat;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModEntities;

public class TrashSlotCompat implements ICompat {
	@Override
	public void setup() {
		net.p3pp3rf1y.sophisticatedcore.compat.trashslot.TrashSlotCompat.registerMenuType(ModEntities.MOVING_STORAGE_CONTAINER_TYPE.get());
	}
}
