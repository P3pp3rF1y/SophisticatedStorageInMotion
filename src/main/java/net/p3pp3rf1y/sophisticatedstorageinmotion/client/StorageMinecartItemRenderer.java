package net.p3pp3rf1y.sophisticatedstorageinmotion.client;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.StorageMinecart;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;
import org.jspecify.annotations.Nullable;

public class StorageMinecartItemRenderer extends MovingStorageItemRenderer<StorageMinecart, MovingStorageItemRenderer.RenderData> {
	@Nullable
	@Override
	public RenderData extractArgument(ItemStack itemStack) {
		return new RenderData(MovingStorageItem.getStorageItem(itemStack));
	}

	@Override
	protected void setMovingStoragePropertiesFromData(StorageMinecart movingStorage, RenderData data) {
		//noop
	}

	@Override
	protected StorageMinecart instantiateMovingStorage(Minecraft mc) {
		return new StorageMinecart(mc.level);
	}

	public static class Unbaked implements SpecialModelRenderer.Unbaked<RenderData> {
		public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

		@Nullable
		@Override
		public SpecialModelRenderer<RenderData> bake(BakingContext bakingContext) {
			return new StorageMinecartItemRenderer();
		}

		@Override
		public MapCodec<? extends SpecialModelRenderer.Unbaked<RenderData>> type() {
			return MAP_CODEC;
		}
	}
}
