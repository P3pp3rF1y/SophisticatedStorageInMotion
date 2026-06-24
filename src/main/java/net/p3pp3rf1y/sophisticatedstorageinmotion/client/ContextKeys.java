package net.p3pp3rf1y.sophisticatedstorageinmotion.client;

import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.animal.equine.AbstractChestedHorse;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;

public class ContextKeys {
	public static final ContextKey<Class<? extends AbstractChestedHorse>> HORSE_CLASS = new ContextKey<>(
			Identifier.fromNamespaceAndPath(SophisticatedStorageInMotion.MOD_ID, "horse_class"));
	public static final ContextKey<StorageBlockEntity> RENDER_BLOCK_ENTITY = new ContextKey<>(
			Identifier.fromNamespaceAndPath(SophisticatedStorageInMotion.MOD_ID, "render_block_entity"));
	public static final ContextKey<WoodType> BASE_BOAT_WOOD_TYPE = new ContextKey<>(
			Identifier.fromNamespaceAndPath(SophisticatedStorageInMotion.MOD_ID, "base_boat_wood_type"));
}
