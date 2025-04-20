package net.p3pp3rf1y.sophisticatedstorageinmotion.entity;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;

public interface IMovingStorageEntity {
	ItemStack getStorageItem();

	void setStorageItem(ItemStack storageItem);

	EntityStorageHolder<?> getStorageHolder();

	ItemStack getDropStack(ItemStack storageItem);

	default List<Slot> instantiateExtraSlots() {
		return Collections.emptyList();
	}
}
