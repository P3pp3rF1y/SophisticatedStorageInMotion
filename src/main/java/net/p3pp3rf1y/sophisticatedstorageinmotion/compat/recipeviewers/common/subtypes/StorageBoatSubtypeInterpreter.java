package net.p3pp3rf1y.sophisticatedstorageinmotion.compat.recipeviewers.common.subtypes;

import net.p3pp3rf1y.sophisticatedstorageinmotion.item.StorageBoatItem;

import java.util.Locale;
import java.util.Optional;

public class StorageBoatSubtypeInterpreter extends MovingStorageSubtypeInterpreter {
	public StorageBoatSubtypeInterpreter() {
		super();
		addOptionalProperty(boatStack -> Optional.of(StorageBoatItem.getWoodType(boatStack)), "boatType", boatType -> boatType.name().toLowerCase(Locale.ROOT));
	}
}