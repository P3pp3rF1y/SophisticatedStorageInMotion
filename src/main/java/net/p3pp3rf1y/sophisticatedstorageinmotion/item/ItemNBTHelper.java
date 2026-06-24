package net.p3pp3rf1y.sophisticatedstorageinmotion.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedstorage.item.BarrelBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.WoodStorageBlockItem;

import java.util.Set;

public class ItemNBTHelper {
	private static final Set<String> DROP_TAGS = Set.of(WoodStorageBlockItem.WOOD_TYPE_TAG, StorageBlockItem.MAIN_COLOR_TAG, StorageBlockItem.ACCENT_COLOR_TAG,
			BarrelBlockItem.MATERIALS_TAG, BarrelBlockItem.FLAT_TOP_TAG);

	public static ItemStack cleanUpStack(ItemStack stack) {
		ItemStack cleanedUpStack = stack.copy();
		CompoundTag tag = stack.getTag();
		if (tag == null) {
			return cleanedUpStack;
		}

		for (String tagName : DROP_TAGS) {
			Tag subTag = tag.get(tagName);
			if (subTag != null) {
				cleanedUpStack.getOrCreateTag().put(tagName, subTag);
			}
		}
		return cleanedUpStack;
	}
}
