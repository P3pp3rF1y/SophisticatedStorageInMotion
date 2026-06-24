package net.p3pp3rf1y.sophisticatedstorageinmotion.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.p3pp3rf1y.sophisticatedstorage.block.BarrelBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.ShulkerBoxBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.StorageMinecart;

import java.util.function.BiConsumer;

public class StorageMinecartRenderer extends AbstractMinecartRenderer<StorageMinecart, MinecartRenderState> implements IMovingStorageRenderer {
	public static final BiConsumer<StorageMinecart, MinecartRenderState> RENDER_STATE_MODIFIER = (minecart, minecartRenderState) -> {
		minecartRenderState.setRenderData(ContextKeys.RENDER_BLOCK_ENTITY, minecart.getStorageHolder().getRenderBlockEntity());
	};

	public StorageMinecartRenderer(EntityRendererProvider.Context context) {
		super(context, ModelLayers.MINECART);
	}

	@Override
	public MinecartRenderState createRenderState() {
		return new MinecartRenderState();
	}

	@Override
	protected void submitMinecartContents(MinecartRenderState minecartRenderState, BlockModelRenderState blockModel, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, int packedLight) {
		StorageBlockEntity renderBlockEntity = minecartRenderState.getRenderData(ContextKeys.RENDER_BLOCK_ENTITY);
		if (renderBlockEntity == null) {
			return;
		}

		poseStack.pushPose();
		double yOffset = 0;
		if (renderBlockEntity instanceof BarrelBlockEntity || renderBlockEntity instanceof ShulkerBoxBlockEntity) {
			yOffset -= 2 / 16D;
		}
		poseStack.translate(0, yOffset, 0);

		StorageBlockRenderer.submitStorageBlock(minecartRenderState.partialTick, poseStack, submitNodeCollector, packedLight, renderBlockEntity);
		poseStack.popPose();
	}

	@Override
	public ModelPart rootModelPart() {
		return model.root();
	}
}
