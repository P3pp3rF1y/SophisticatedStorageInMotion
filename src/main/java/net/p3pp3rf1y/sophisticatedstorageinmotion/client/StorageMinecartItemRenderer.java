package net.p3pp3rf1y.sophisticatedstorageinmotion.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.StorageMinecart;

public class StorageMinecartItemRenderer extends MovingStorageItemRenderer<StorageMinecart> {
	public StorageMinecartItemRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher, EntityModelSet entityModelSet) {
		super(blockEntityRenderDispatcher, entityModelSet);
	}

	@Override
	protected void setMovingStoragePropertiesFromStack(StorageMinecart movingStorage, ItemStack stack) {
		//noop
	}

	@Override
	protected StorageMinecart instantiateMovingStorage(Minecraft mc) {
		return new StorageMinecart(mc.level);
	}
}
