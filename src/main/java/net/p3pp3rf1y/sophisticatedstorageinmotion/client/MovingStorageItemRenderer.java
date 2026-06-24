package net.p3pp3rf1y.sophisticatedstorageinmotion.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.IMovingStorageEntity;
import org.joml.Vector3f;

import javax.annotation.Nullable;

import java.util.Set;

public abstract class MovingStorageItemRenderer<T extends Entity & IMovingStorageEntity, D extends MovingStorageItemRenderer.RenderData>
		implements
			SpecialModelRenderer<D> {
	@Nullable
	private T movingStorage = null;

	@Override
	public void render(@Nullable D data, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
			boolean hasFoil) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || data == null) {
			return;
		}

		T movingStorage = getMovingStorage(mc);
		setMovingStoragePropertiesFromData(movingStorage, data);
		movingStorage.getStorageHolder().setStorageItem(data.storageItem());

		poseStack.pushPose();
		poseStack.translate(0.5, 0, 0.5);
		mc.getEntityRenderDispatcher().render(movingStorage, 0, 0, 0, 0, poseStack, buffer, packedLight);
		poseStack.popPose();
	}

	@Override
	public void getExtents(Set<Vector3f> set) {
		PoseStack posestack = new PoseStack();
		T entity = getMovingStorage(Minecraft.getInstance());
		EntityRenderer<? super T, ?> entityRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
		if (entityRenderer instanceof IMovingStorageRenderer movingStorageRenderer) {
			movingStorageRenderer.rootModelPart().getExtentsForGui(posestack, set);
		}
	}

	protected abstract void setMovingStoragePropertiesFromData(T movingStorage, D data);

	private T getMovingStorage(Minecraft mc) {
		if (movingStorage == null) {
			movingStorage = instantiateMovingStorage(mc);
		}

		return movingStorage;
	}

	protected abstract T instantiateMovingStorage(Minecraft mc);

	public static class RenderData {
		private final ItemStack storageItem;
		public RenderData(ItemStack storageItem) {
			this.storageItem = storageItem;
		}

		public ItemStack storageItem() {
			return storageItem;
		}
	}
}
