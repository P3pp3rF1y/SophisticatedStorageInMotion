package net.p3pp3rf1y.sophisticatedstorageinmotion.compat.jei;

import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedstorage.compat.jei.subtypes.PropertyBasedSubtypeInterpreter;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.StorageMinecartItem;

import java.util.StringJoiner;

public class MovingStorageSubtypeInterpreter extends PropertyBasedSubtypeInterpreter {
	public MovingStorageSubtypeInterpreter() {
		addOptionalProperty(StorageMinecartItem::getStorageItemType);
		addOptionalProperty(StorageMinecartItem::getStorageItemWoodType);
		addOptionalProperty(StorageMinecartItem::getStorageItemMainColor);
		addOptionalProperty(StorageMinecartItem::getStorageItemAccentColor);
		addProperty(StorageMinecartItem::isStorageItemFlatTopBarrel);
	}

	@Override
	public String getLegacyStringSubtypeInfo(ItemStack itemStack, UidContext context) {
		StringJoiner result = new StringJoiner(",");
		StorageMinecartItem.getStorageItemType(itemStack).ifPresent(storageItemType -> result.add("storageItemType:" + storageItemType));
		StorageMinecartItem.getStorageItemWoodType(itemStack).ifPresent(woodName -> result.add("woodName:" + woodName));
		StorageMinecartItem.getStorageItemMainColor(itemStack).ifPresent(mainColor -> result.add("mainColor:" + mainColor));
		StorageMinecartItem.getStorageItemAccentColor(itemStack).ifPresent(accentColor -> result.add("accentColor:" + accentColor));
		result.add("flatTop:" + StorageMinecartItem.isStorageItemFlatTopBarrel(itemStack));
		return "{" + result + "}";
	}
}
