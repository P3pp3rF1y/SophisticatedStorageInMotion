package net.p3pp3rf1y.sophisticatedstorageinmotion.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.StorageMinecart;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModEntities;

import javax.annotation.Nullable;

public class StorageMinecartItemRenderer extends BlockEntityWithoutLevelRenderer {
	@Nullable
	private static StorageMinecart MINECART = null;
	public StorageMinecartItemRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher, EntityModelSet entityModelSet) {
		super(blockEntityRenderDispatcher, entityModelSet);
	}

	public static IClientItemExtensions getItemRenderProperties() {
		return new IClientItemExtensions() {
			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return new StorageMinecartItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
			}
		};
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
		super.renderByItem(stack, displayContext, poseStack, buffer, packedLight, packedOverlay);
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) {
			return;
		}

		StorageMinecart minecart = getStorageMinecart(mc);
		minecart.getStorageHolder().setStorageItemFrom(stack, false);

		poseStack.pushPose();
		poseStack.translate(0.5, 0, 0.5);
		mc.getEntityRenderDispatcher().render(minecart, 0, 0, 0, 0, 0, poseStack, buffer, packedLight);
		poseStack.popPose();
	}

	private static StorageMinecart getStorageMinecart(Minecraft mc) {
		if (MINECART == null) {
			MINECART = new StorageMinecart(ModEntities.STORAGE_MINECART.get(), mc.level);
		}

		return MINECART;
	}
}
