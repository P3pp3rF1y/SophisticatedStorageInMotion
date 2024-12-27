package net.p3pp3rf1y.sophisticatedstorageinmotion.compat.jei;

import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedstorage.compat.jei.subtypes.PropertyBasedSubtypeInterpreter;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;

import java.util.StringJoiner;

public class MovingStorageSubtypeInterpreter extends PropertyBasedSubtypeInterpreter {
	public MovingStorageSubtypeInterpreter() {
		addOptionalProperty(MovingStorageItem::getStorageItemType);
		addOptionalProperty(MovingStorageItem::getStorageItemWoodType);
		addOptionalProperty(MovingStorageItem::getStorageItemMainColor);
		addOptionalProperty(MovingStorageItem::getStorageItemAccentColor);
		addProperty(MovingStorageItem::isStorageItemFlatTopBarrel);
	}

	@Override
	public String getLegacyStringSubtypeInfo(ItemStack itemStack, UidContext context) {
		StringJoiner result = new StringJoiner(",");
		MovingStorageItem.getStorageItemType(itemStack).ifPresent(storageItemType -> result.add("storageItemType:" + storageItemType));
		MovingStorageItem.getStorageItemWoodType(itemStack).ifPresent(woodName -> result.add("woodName:" + woodName));
		MovingStorageItem.getStorageItemMainColor(itemStack).ifPresent(mainColor -> result.add("mainColor:" + mainColor));
		MovingStorageItem.getStorageItemAccentColor(itemStack).ifPresent(accentColor -> result.add("accentColor:" + accentColor));
		result.add("flatTop:" + MovingStorageItem.isStorageItemFlatTopBarrel(itemStack));
		return "{" + result + "}";
	}
}
