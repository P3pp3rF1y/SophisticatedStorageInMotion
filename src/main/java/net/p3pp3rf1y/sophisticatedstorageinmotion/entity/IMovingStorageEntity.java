package net.p3pp3rf1y.sophisticatedstorageinmotion.entity;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public interface IMovingStorageEntity {
	Vec3 position();

	int getId();

	ItemStack getStorageItem();

	void setStorageItem(ItemStack storageItem);

	EntityStorageHolder getStorageHolder();
}
