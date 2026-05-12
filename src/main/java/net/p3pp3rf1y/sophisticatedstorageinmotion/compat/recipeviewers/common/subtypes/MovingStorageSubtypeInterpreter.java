package net.p3pp3rf1y.sophisticatedstorageinmotion.compat.recipeviewers.common.subtypes;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.common.subtypes.PropertyBasedSubtypeInterpreter;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.StorageBoatItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;

import java.util.Optional;

public class MovingStorageSubtypeInterpreter extends PropertyBasedSubtypeInterpreter {
	public MovingStorageSubtypeInterpreter() {
		addOptionalProperty(MovingStorageItem::getStorageItemType, "storageItemType", Item::toString);
		addOptionalProperty(MovingStorageItem::getStorageItemWoodType, "woodName", WoodType::name);
		addOptionalProperty(MovingStorageItem::getStorageItemMainColor, "mainColor", String::valueOf);
		addOptionalProperty(MovingStorageItem::getStorageItemAccentColor, "accentColor", String::valueOf);
		addProperty(MovingStorageItem::isStorageItemFlatTopBarrel, "flatTop", String::valueOf);
		addOptionalProperty(stack -> stack.getItem() instanceof StorageBoatItem ? Optional.of(StorageBoatItem.getWoodType(stack)) : Optional.empty(), "boatType", WoodType::name);
	}
}
