package net.p3pp3rf1y.sophisticatedstorageinmotion.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;

public class ContextKeys {
	public static final ContextKey<Class<? extends AbstractChestedHorse>> HORSE_CLASS = new ContextKey<>(
			ResourceLocation.fromNamespaceAndPath(SophisticatedStorageInMotion.MOD_ID, "horse_class"));
	public static final ContextKey<StorageBlockEntity> RENDER_BLOCK_ENTITY = new ContextKey<>(
			ResourceLocation.fromNamespaceAndPath(SophisticatedStorageInMotion.MOD_ID, "render_block_entity"));
	public static final ContextKey<WoodType> BASE_BOAT_WOOD_TYPE = new ContextKey<>(
			ResourceLocation.fromNamespaceAndPath(SophisticatedStorageInMotion.MOD_ID, "base_boat_wood_type"));
}
