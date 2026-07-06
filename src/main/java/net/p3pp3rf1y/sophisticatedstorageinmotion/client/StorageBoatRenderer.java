package net.p3pp3rf1y.sophisticatedstorageinmotion.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.AbstractBoatRenderer;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RaftRenderer;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.p3pp3rf1y.sophisticatedstorage.block.BarrelBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.ShulkerBoxBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.StorageBoat;
import org.joml.Quaternionf;

import java.util.Map;
import java.util.function.BiConsumer;

public class StorageBoatRenderer extends EntityRenderer<StorageBoat, BoatRenderState> implements IMovingStorageRenderer {
	private final Map<WoodType, AbstractBoatRenderer> baseBoatRenderers;
	public static final BiConsumer<StorageBoat, BoatRenderState> RENDER_STATE_MODIFIER = (storageBoat, renderState) -> {
		renderState.setRenderData(ContextKeys.BASE_BOAT_WOOD_TYPE, storageBoat.getWoodType());
		renderState.setRenderData(ContextKeys.RENDER_BLOCK_ENTITY, storageBoat.getStorageHolder().getRenderBlockEntity());
	};

	public StorageBoatRenderer(EntityRendererProvider.Context context) {
		super(context);

		baseBoatRenderers = Map.of(WoodType.ACACIA, new BoatRenderer(context, ModelLayers.ACACIA_BOAT), WoodType.BIRCH,
				new BoatRenderer(context, ModelLayers.BIRCH_BOAT), WoodType.CHERRY, new BoatRenderer(context, ModelLayers.CHERRY_BOAT), WoodType.DARK_OAK,
				new BoatRenderer(context, ModelLayers.DARK_OAK_BOAT), WoodType.JUNGLE, new BoatRenderer(context, ModelLayers.JUNGLE_BOAT), WoodType.MANGROVE,
				new BoatRenderer(context, ModelLayers.MANGROVE_BOAT), WoodType.OAK, new BoatRenderer(context, ModelLayers.OAK_BOAT), WoodType.PALE_OAK,
				new BoatRenderer(context, ModelLayers.PALE_OAK_BOAT), WoodType.SPRUCE, new BoatRenderer(context, ModelLayers.SPRUCE_BOAT), WoodType.BAMBOO,
				new RaftRenderer(context, ModelLayers.BAMBOO_RAFT));
	}

	@Override
	public BoatRenderState createRenderState() {
		return new BoatRenderState();
	}

	@Override
	public void extractRenderState(StorageBoat storageBoat, BoatRenderState boatRenderState, float partialTick) {
		super.extractRenderState(storageBoat, boatRenderState, partialTick);
		boatRenderState.yRot = storageBoat.getYRot(partialTick);
		boatRenderState.hurtTime = (float) storageBoat.getHurtTime() - partialTick;
		boatRenderState.hurtDir = storageBoat.getHurtDir();
		boatRenderState.damageTime = Math.max(storageBoat.getDamage() - partialTick, 0.0F);
		boatRenderState.bubbleAngle = storageBoat.getBubbleAngle(partialTick);
		boatRenderState.isUnderWater = storageBoat.isUnderWater();
		boatRenderState.rowingTimeLeft = storageBoat.getRowingTime(0, partialTick);
		boatRenderState.rowingTimeRight = storageBoat.getRowingTime(1, partialTick);
	}

	@Override
	public void submit(BoatRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
		super.submit(renderState, poseStack, submitNodeCollector, cameraRenderState);

		StorageBlockEntity renderBlockEntity = renderState.getRenderData(ContextKeys.RENDER_BLOCK_ENTITY);
		WoodType woodType = renderState.getRenderData(ContextKeys.BASE_BOAT_WOOD_TYPE);
		if (renderBlockEntity == null || woodType == null) {
			return;
		}

		poseStack.pushPose();
		poseStack.translate(0, woodType == WoodType.BAMBOO ? 8 / 16F : 3 / 16F, 0);
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - renderState.yRot));
		float interpolatedHurtTime = renderState.hurtTime;
		float interpolatedDamage = renderState.damageTime;
		if (interpolatedDamage < 0.0F) {
			interpolatedDamage = 0.0F;
		}

		if (interpolatedHurtTime > 0.0F) {
			poseStack.mulPose(
					Axis.XP.rotationDegrees(Mth.sin(interpolatedHurtTime) * interpolatedHurtTime * interpolatedDamage / 10.0F * (float) renderState.hurtDir));
		}

		float bubbleAngle = renderState.bubbleAngle;
		if (!Mth.equal(bubbleAngle, 0.0F)) {
			poseStack.mulPose((new Quaternionf()).setAngleAxis(renderState.bubbleAngle * 0.017453292F, 1.0F, 0.0F, 1.0F));
		}

		poseStack.scale(-1.0F, -1.0F, 1.0F);
		poseStack.mulPose(Axis.YP.rotationDegrees(180));
		poseStack.mulPose(Axis.XP.rotationDegrees(180));
		poseStack.scale(6 / 7F, 6 / 7F, 6 / 7F);
		poseStack.translate(-0.5F, 0,
				(renderBlockEntity instanceof BarrelBlockEntity || renderBlockEntity instanceof ShulkerBoxBlockEntity ? 0 : 1 / 16F) + 0.02F);
		StorageBlockRenderer.submitStorageBlock(renderState.partialTick, poseStack, submitNodeCollector, renderState.lightCoords, renderBlockEntity);
		poseStack.popPose();

		baseBoatRenderers.get(woodType).submit(renderState, poseStack, submitNodeCollector, cameraRenderState);
	}

	@Override
	public ModelPart rootModelPart() {
		return ((BoatRenderer) baseBoatRenderers.get(WoodType.OAK)).model.root();
	}
}
