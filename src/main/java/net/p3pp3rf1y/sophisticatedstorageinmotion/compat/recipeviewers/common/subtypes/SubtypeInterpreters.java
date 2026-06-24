package net.p3pp3rf1y.sophisticatedstorageinmotion.compat.recipeviewers.common.subtypes;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.common.subtypes.PropertyBasedSubtypeInterpreter;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SubtypeInterpreters {
	public static Map<Item, PropertyBasedSubtypeInterpreter> getSubtypeInterpreters() {
		return new HashMap<>() {
			{
				put(ModItems.STORAGE_MINECART.get(), new MovingStorageSubtypeInterpreter());
				put(ModItems.STORAGE_BOAT.get(), new StorageBoatSubtypeInterpreter());
			}
		};
	}

	public static Optional<PropertyBasedSubtypeInterpreter> getSubtypeInterpreter(Map<Item, PropertyBasedSubtypeInterpreter> subtypeInterpreters,
			ItemStack stack) {
		return Optional.ofNullable(subtypeInterpreters.get(stack.getItem()));
	}
}
