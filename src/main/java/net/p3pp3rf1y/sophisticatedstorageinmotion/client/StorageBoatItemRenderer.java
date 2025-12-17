package net.p3pp3rf1y.sophisticatedstorageinmotion.client;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.StorageBoat;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.StorageBoatItem;
import org.jspecify.annotations.Nullable;

public class StorageBoatItemRenderer extends MovingStorageItemRenderer<StorageBoat, StorageBoatItemRenderer.BoatRenderData> {
	@Nullable
	@Override
	public BoatRenderData extractArgument(ItemStack itemStack) {
		ItemStack storageItem = MovingStorageItem.getStorageItem(itemStack);
		if (storageItem == ItemStack.EMPTY) {
			return new BoatRenderData(ItemStack.EMPTY, WoodType.ACACIA);
		}

		return new BoatRenderData(storageItem, StorageBoatItem.getWoodType(itemStack));
	}

	@Override
	protected void setMovingStoragePropertiesFromData(StorageBoat movingStorage, BoatRenderData data) {
		if (data != null) {
			movingStorage.setWoodType(data.woodType());
		}
	}

	@Override
	protected StorageBoat instantiateMovingStorage(Minecraft mc) {
		return new StorageBoat(mc.level);
	}

	public static class BoatRenderData extends MovingStorageItemRenderer.RenderData {
		private final WoodType woodType;
		public BoatRenderData(ItemStack storageItem, WoodType woodType) {
			super(storageItem);
			this.woodType = woodType;
		}

		public WoodType woodType() {
			return woodType;
		}
	}

	public static class Unbaked implements SpecialModelRenderer.Unbaked {
		public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

		@Nullable
		@Override
		public SpecialModelRenderer<?> bake(BakingContext bakingContext) {
			return new StorageBoatItemRenderer();
		}

		@Override
		public MapCodec<? extends Unbaked> type() {
			return MAP_CODEC;
		}
	}
}
